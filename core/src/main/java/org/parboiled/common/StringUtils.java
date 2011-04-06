/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.common;

import org.parboiled.support.Characters;
import org.parboiled.support.Chars;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

/**
 * General utility methods for string manipulation.
 */
public final class StringUtils {

    private StringUtils() {}

    /**
     * Replaces carriage returns, newlines, tabs, formfeeds and the special chars defined in {@link Characters}
     * with their respective escape sequences.
     *
     * @param string the string
     * @return the escaped string
     */
    public static String escape(String string) {
        if (isEmpty(string)) return "";
        StringBuilder sb = new StringBuilder();
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (i == chars.length - 1 || chars[i] != '\r' || chars[i + 1] != '\n') {
                sb.append(escape(chars[i]));
            }
        }
        return sb.toString();
    }

    /**
     * Replaces carriage returns, newlines, tabs, formfeeds and the special chars defined in {@link Characters}
     * with their respective escape sequences.
     *
     * @param c the character to escape
     * @return the escaped string
     */
    public static String escape(char c) {
        switch (c) {
            case '\r':
                return "\\r";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            case '\f':
                return "\\f";
            case Chars.DEL_ERROR:
                return "DEL_ERROR";
            case Chars.INS_ERROR:
                return "INS_ERROR";
            case Chars.RESYNC:
                return "RESYNC";
            case Chars.RESYNC_START:
                return "RESYNC_START";
            case Chars.RESYNC_END:
                return "RESYNC_END";
            case Chars.RESYNC_EOI:
                return "RESYNC_EOI";
            case Chars.INDENT:
                return "INDENT";
            case Chars.DEDENT:
                return "DEDENT";
            case Chars.EOI:
                return "EOI";
            default:
                return String.valueOf(c);
        }
    }

    /**
     * Creates a string consisting of n times the given character.
     *
     * @param c the char
     * @param n the number of times to repeat
     * @return the string
     */
    public static String repeat(char c, int n) {
        char[] array = new char[n];
        Arrays.fill(array, c);
        return String.valueOf(array);
    }

    //***********************************************************************************************
    //**                 THE FOLLOWING CODE IS A PARTIAL, VERBATIM COPY OF                         **
    //**                       org.apache.commons.lang.StringUtils                                 **
    //**                         which is licensed under ASF 2.0                                   **
    //***********************************************************************************************

    /**
     * <p>Joins the elements of the provided <code>Iterable</code> into
     * a single String containing the provided elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").</p>
     *
     * @param iterable the <code>Iterable</code> of values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     */
    public static String join(Iterable iterable, String separator) {
        return iterable == null ? null : join(iterable.iterator(), separator);
    }
    
    /**
     * <p>Joins the elements of the provided <code>Iterator</code> into
     * a single String containing the provided elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").</p>
     *
     * @param iterator  the <code>Iterator</code> of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     */
    public static String join(Iterator iterator, String separator) {
        // handle null, zero and one elements before building a buffer
        if (iterator == null) return null;
        if (!iterator.hasNext()) return "";
        Object first = iterator.next();
        if (!iterator.hasNext()) return Utils.toString(first);

        // two or more elements
        StringBuilder buf = new StringBuilder(256); // Java default is 16, probably too small
        if (first != null) buf.append(first);

        while (iterator.hasNext()) {
            if (separator != null) buf.append(separator);
            Object obj = iterator.next();
            if (obj != null) buf.append(obj);
        }
        return buf.toString();
    }

    /**
     * <p>Joins the elements of the provided array into a single String
     * containing the provided list of elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").
     * Null objects or empty strings within the array are represented by
     * empty strings.</p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array     the array of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null array input
     */
    public static String join(Object[] array, String separator) {
        return array == null ? null : join(array, separator, 0, array.length);
    }

    /**
     * <p>Joins the elements of the provided array into a single String
     * containing the provided list of elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").
     * Null objects or empty strings within the array are represented by
     * empty strings.</p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array      the array of values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @param startIndex the first index to start joining from.  It is
     *                   an error to pass in an end index past the end of the array
     * @param endIndex   the index to stop joining from (exclusive). It is
     *                   an error to pass in an end index past the end of the array
     * @return the joined String, <code>null</code> if null array input
     */
    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) return null;
        if (separator == null) separator = "";

        // lastIndex - firstIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
        //           (Assuming that all Strings are roughly equally long)
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) return "";
        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator.length());
        StringBuilder buf = new StringBuilder(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) buf.append(separator);
            if (array[i] != null) buf.append(array[i]);
        }
        return buf.toString();
    }

    // Empty checks
    //-----------------------------------------------------------------------

    /**
     * <p>Checks if a String is empty ("") or null.</p>
     * <p/>
     * <pre>
     * StringUtils.isEmpty(null)      = true
     * StringUtils.isEmpty("")        = true
     * StringUtils.isEmpty(" ")       = false
     * StringUtils.isEmpty("bob")     = false
     * StringUtils.isEmpty("  bob  ") = false
     * </pre>
     * <p/>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    /**
     * <p>Checks if a String is not empty ("") and not null.</p>
     * <p/>
     * <pre>
     * StringUtils.isNotEmpty(null)      = false
     * StringUtils.isNotEmpty("")        = false
     * StringUtils.isNotEmpty(" ")       = true
     * StringUtils.isNotEmpty("bob")     = true
     * StringUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Gets a String's length or <code>0</code> if the String is <code>null</code>.
     *
     * @param str a String or <code>null</code>
     * @return String length or <code>0</code> if the String is <code>null</code>.
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * <p>Compares two Strings, returning <code>true</code> if they are equal ignoring
     * the case.</p>
     * <p/>
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered equal. Comparison is case insensitive.</p>
     * <p/>
     * <pre>
     * StringUtils.equalsIgnoreCase(null, null)   = true
     * StringUtils.equalsIgnoreCase(null, "abc")  = false
     * StringUtils.equalsIgnoreCase("abc", null)  = false
     * StringUtils.equalsIgnoreCase("abc", "abc") = true
     * StringUtils.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @param str1 the first String, may be null
     * @param str2 the second String, may be null
     * @return <code>true</code> if the Strings are equal, case insensitive, or
     *         both <code>null</code>
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    /**
     * Test whether a string starts with a given prefix, handling null values without exceptions.
     * <p/>
     * StringUtils.startsWith(null, null)   = false
     * StringUtils.startsWith(null, "abc")  = false
     * StringUtils.startsWith("abc", null)  = true
     * StringUtils.startsWith("abc", "ab")  = true
     * StringUtils.startsWith("abc", "abc") = true
     *
     * @param string the string
     * @param prefix the prefix
     * @return true if string starts with prefix
     */
    public static boolean startsWith(String string, String prefix) {
        return string != null && (prefix == null || string.startsWith(prefix));
    }

    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     * <p/>
     * <p>A negative start position can be used to start <code>n</code>
     * characters from the end of the String.</p>
     * <p/>
     * <p>A <code>null</code> String will return <code>null</code>.
     * An empty ("") String will return "".</p>
     * <p/>
     * <pre>
     * StringUtils.substring(null, *)   = null
     * StringUtils.substring("", *)     = ""
     * StringUtils.substring("abc", 0)  = "abc"
     * StringUtils.substring("abc", 2)  = "c"
     * StringUtils.substring("abc", 4)  = ""
     * StringUtils.substring("abc", -2) = "bc"
     * StringUtils.substring("abc", -4) = "abc"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means
     *              count back from the end of the String by this many characters
     * @return substring from start position, <code>null</code> if null String input
     */
    public static String substring(String str, int start) {
        if (str == null) {
            return null;
        }

        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return "";
        }

        return str.substring(start);
    }

    /**
     * <p>Gets a substring from the specified String avoiding exceptions.</p>
     * <p/>
     * <p>A negative start position can be used to start/end <code>n</code>
     * characters from the end of the String.</p>
     * <p/>
     * <p>The returned substring starts with the character in the <code>start</code>
     * position and ends before the <code>end</code> position. All position counting is
     * zero-based -- i.e., to start at the beginning of the string use
     * <code>start = 0</code>. Negative start and end positions can be used to
     * specify offsets relative to the end of the String.</p>
     * <p/>
     * <p>If <code>start</code> is not strictly to the left of <code>end</code>, ""
     * is returned.</p>
     * <p/>
     * <pre>
     * StringUtils.substring(null, *, *)    = null
     * StringUtils.substring("", * ,  *)    = "";
     * StringUtils.substring("abc", 0, 2)   = "ab"
     * StringUtils.substring("abc", 2, 0)   = ""
     * StringUtils.substring("abc", 2, 4)   = "c"
     * StringUtils.substring("abc", 4, 6)   = ""
     * StringUtils.substring("abc", 2, 2)   = ""
     * StringUtils.substring("abc", -2, -1) = "b"
     * StringUtils.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means
     *              count back from the end of the String by this many characters
     * @param end   the position to end at (exclusive), negative means
     *              count back from the end of the String by this many characters
     * @return substring from start position to end positon,
     *         <code>null</code> if null String input
     */
    public static String substring(String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    // Left/Right/Mid
    //-----------------------------------------------------------------------

    /**
     * <p>Gets the leftmost <code>len</code> characters of a String.</p>
     * <p/>
     * <p>If <code>len</code> characters are not available, or the
     * String is <code>null</code>, the String will be returned without
     * an exception. An exception is thrown if len is negative.</p>
     * <p/>
     * <pre>
     * StringUtils.left(null, *)    = null
     * StringUtils.left(*, -ve)     = ""
     * StringUtils.left("", *)      = ""
     * StringUtils.left("abc", 0)   = ""
     * StringUtils.left("abc", 2)   = "ab"
     * StringUtils.left("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the leftmost characters from, may be null
     * @param len the length of the required String, must be zero or positive
     * @return the leftmost characters, <code>null</code> if null String input
     */
    public static String left(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return "";
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(0, len);
    }

    /**
     * <p>Gets the rightmost <code>len</code> characters of a String.</p>
     * <p/>
     * <p>If <code>len</code> characters are not available, or the String
     * is <code>null</code>, the String will be returned without an
     * an exception. An exception is thrown if len is negative.</p>
     * <p/>
     * <pre>
     * StringUtils.right(null, *)    = null
     * StringUtils.right(*, -ve)     = ""
     * StringUtils.right("", *)      = ""
     * StringUtils.right("abc", 0)   = ""
     * StringUtils.right("abc", 2)   = "bc"
     * StringUtils.right("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the rightmost characters from, may be null
     * @param len the length of the required String, must be zero or positive
     * @return the rightmost characters, <code>null</code> if null String input
     */
    public static String right(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0) {
            return "";
        }
        if (str.length() <= len) {
            return str;
        }
        return str.substring(str.length() - len);
    }

    /**
     * <p>Gets <code>len</code> characters from the middle of a String.</p>
     * <p/>
     * <p>If <code>len</code> characters are not available, the remainder
     * of the String will be returned without an exception. If the
     * String is <code>null</code>, <code>null</code> will be returned.
     * An exception is thrown if len is negative.</p>
     * <p/>
     * <pre>
     * StringUtils.mid(null, *, *)    = null
     * StringUtils.mid(*, *, -ve)     = ""
     * StringUtils.mid("", 0, *)      = ""
     * StringUtils.mid("abc", 0, 2)   = "ab"
     * StringUtils.mid("abc", 0, 4)   = "abc"
     * StringUtils.mid("abc", 2, 4)   = "c"
     * StringUtils.mid("abc", 4, 2)   = ""
     * StringUtils.mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param str the String to get the characters from, may be null
     * @param pos the position to start from, negative treated as zero
     * @param len the length of the required String, must be zero or positive
     * @return the middle characters, <code>null</code> if null String input
     */
    public static String mid(String str, int pos, int len) {
        if (str == null) {
            return null;
        }
        if (len < 0 || pos > str.length()) {
            return "";
        }
        if (pos < 0) {
            pos = 0;
        }
        if (str.length() <= (pos + len)) {
            return str.substring(pos);
        }
        return str.substring(pos, pos + len);
    }

}
