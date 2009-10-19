package org.parboiled.utils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.*;

public final class StringUtils2 {

    public static final String NL = System.getProperty("line.separator");

    private StringUtils2() {}

    /**
     * Inserts line breaks at word boundaries. Guaranties that the resulting string
     * will not have lines longer than maxLineLength.
     *
     * @param text          the string to wrap
     * @param maxLineLength the max number of chars in a line
     * @return the wrapped text
     */
    @SuppressWarnings({"ConstantConditions"})
    public static String wordWrap(String text, int maxLineLength) {
        if (isEmpty(text)) return text;
        if (maxLineLength <= 0) throw new IllegalArgumentException("maxLineLength must be greater than zero");

        StringBuilder sb = new StringBuilder(text);
        int lineLen = 0;
        int lastWhiteSpace = -1;
        int lineStart = 0;

        for (int i = 0; i < sb.length(); i++) {
            char cursor = sb.charAt(i);
            lineLen++;

            if (cursor != '\n' && Character.isWhitespace(cursor)) {
                if (lastWhiteSpace == i - 1 || lineLen == 1) {
                    sb.deleteCharAt(i--);
                    lineLen--;
                } else {
                    lastWhiteSpace = i;
                }
                if (lineLen < maxLineLength) continue;
            }
            if (cursor == '\n' || lineLen == maxLineLength) {
                if (lineLen == maxLineLength && i < sb.length() - 1) {
                    i = lastWhiteSpace > lineStart && !Character
                            .isWhitespace(sb.charAt(i + 1)) ? lastWhiteSpace : i + 1;
                    if (Character.isWhitespace(sb.charAt(i))) {
                        sb.setCharAt(i, '\n');
                    } else {
                        sb.insert(i, '\n');
                    }
                }
                lineLen = 0;
                lineStart = i + 1;
            }
        }
        return sb.toString();
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

    /**
     * Escaped NEWLINE sequences ("\r\n" or "\n") with the respective escape characters.
     *
     * @param string the string
     * @return the string with newlines escaped.
     */
    public static String escapeNLs(String string) {
        return string == null ? "" : string.replace("\r\n", "\\n").replace("\n", "\\n");
    }

    /**
     * Converts the newlines in the given multi-line string into the system specific version.
     *
     * @param string the string
     * @return the corrected string
     */
    public static String convertToSystemNLs(String string) {
        if (string == null) return "";
        if ("\r\n".equals(NL)) {
            return string
                    .replace("\r\n", "\r\r")    // first, mask all existing \r\n
                    .replace("\n", "\r\n")      // correct all single \n
                    .replace("\r\r", "\r\n");   // finally unmask all old \r\n
        }
        if ("\n".equals(NL)) {
            return string.replace("\r\n", "\n");
        }
        throw new IllegalStateException();
    }

    /**
     * Gets the text of the line with the given number.
     *
     * @param text       the text to search through
     * @param lineNumber the line number starting with 0 for the first line
     * @return the text of the line or null, if the given line does not exist in the input
     */
    public static String getLine(String text, int lineNumber) {
        String line = null;
        if (text != null) {
            LineNumberReader reader = new LineNumberReader(new StringReader(text));
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
     * Finds all start indices of the given searchstring within the given string.
     *
     * @param string       the string to search through
     * @param searchString the string to search
     * @return an array of all found searchstring start indices
     */
    public static int[] findAll(String string, String searchString) {
        if (isEmpty(string) || isEmpty(searchString)) return new int[0];
        List<Integer> indices = new ArrayList<Integer>();
        int searchStringIndex = 0;
        for (int stringIndex = 0; stringIndex < string.length(); stringIndex++) {
            if (string.charAt(stringIndex) == searchString.charAt(searchStringIndex)) {
                searchStringIndex++;
                if (searchStringIndex < searchString.length()) continue;
                indices.add(stringIndex - searchStringIndex + 1);
            } else {
                // we need to rewind the characters already matched in order to reattempt a later match
                stringIndex -= searchStringIndex;
            }
            searchStringIndex = 0;
        }
        int[] array = new int[indices.size()];
        for (int i = 0; i < array.length; i++) array[i] = indices.get(i);
        return array;
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
     * @since 2.3
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
     * <p>NOTE: This method changed in Lang version 2.0.
     * It no longer trims the String.
     * That functionality is available in isBlank().</p>
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
     * @since 2.4
     */
    public static int length(String str) {
        return str == null ? 0 : str.length();
    }

    /**
     * <p>Compares two Strings, returning <code>true</code> if they are equal ignoring
     * the case.</p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered equal. Comparison is case insensitive.</p>
     *
     * <pre>
     * StringUtils.equalsIgnoreCase(null, null)   = true
     * StringUtils.equalsIgnoreCase(null, "abc")  = false
     * StringUtils.equalsIgnoreCase("abc", null)  = false
     * StringUtils.equalsIgnoreCase("abc", "abc") = true
     * StringUtils.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @see java.lang.String#equalsIgnoreCase(String)
     * @param str1  the first String, may be null
     * @param str2  the second String, may be null
     * @return <code>true</code> if the Strings are equal, case insensitive, or
     *  both <code>null</code>
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

}
