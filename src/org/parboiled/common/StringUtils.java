/*
 * Copyright (C) 2009 Mathias Doenitz
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

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.*;

/**
 * General utility methods for string manipulation.
 */
public final class StringUtils {

    public static final String NL = System.getProperty("line.separator");

    private StringUtils() {}

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

    /**
     * Escapes newline sequences ("\r\n" or "\n") with the respective escape characters.
     *
     * @param string the string
     * @return the string with newlines escaped.
     */
    public static String escapeNLs(String string) {
        return string == null ? "" : string.replace("\r\n", "\\n").replace("\n", "\\n");
    }

    /**
     * Gets the text of the line with the given number.
     *
     * @param text       the text to search through
     * @param lineNumber the line number starting with 0 for the first line
     * @return the text of the line or null, if the given line does not exist in the input
     */
    public static String getLine(char[] text, int lineNumber) {
        String line = null;
        if (text != null) {
            LineNumberReader reader = new LineNumberReader(new CharArrayReader(text));
            try {
                while (reader.getLineNumber() <= lineNumber) {
                    line = reader.readLine();
                    if (line == null) return null;
                }
            } catch (IOException e) {
                throw new IllegalStateException(); // we shouldn't get an IOException on a StringReader
            }
        }
        return line;
    }

    /**
     * <p>Joins the elements of the provided <code>Collection</code> into
     * a single String containing the provided elements.</p>
     * <p/>
     * <p>No delimiter is added before or after the list.
     * A <code>null</code> separator is the same as an empty String ("").</p>
     *
     * @param collection the <code>Collection</code> of values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     */
    public static String join(Collection collection, String separator) {
        return collection == null ? null : join(collection.iterator(), separator);
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

        // endIndex - startIndex > 0:   Len = NofStrings *(len(firstString) + len(separator))
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
     *
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

}
