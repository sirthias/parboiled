package org.parboiled.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
        if (StringUtils.isEmpty(text)) return text;
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
        if (StringUtils.isEmpty(string) || StringUtils.isEmpty(searchString)) return new int[0];
        List<Integer> indices = Lists.newArrayList();
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

    public static String toString(Map<String, Object> map) {
        if (map == null) return null;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey().replace("\\", "\\\\").replace("=", "\\="));
            sb.append(" = ");
            sb.append(String.valueOf(entry.getValue()).replace("\\", "\\\\").replace("=", "\\="));
            sb.append(NL);
        }
        return sb.toString();
    }

    @SuppressWarnings({"ConstantConditions"})
    public static Map<String, Object> mapFromString(String string) {
        Map<String, Object> map = Maps.newHashMap();
        if (StringUtils.isNotBlank(string)) {
            LineNumberReader reader = new LineNumberReader(new StringReader(string));
            try {
                String line = null;
                do {
                    if (StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        line = line.replace("\\\\", "\u0001").replace("\\=", "\u0002");
                        int[] splitIndex = findAll(line, "=");
                        if (splitIndex.length != 1) {
                            throw new RuntimeException("Malformed line " + reader.getLineNumber());
                        }
                        line = line.replace('\u0002', '=').replace('\u0001', '\\');
                        String key = line.substring(0, splitIndex[0]).trim();
                        String value = line.substring(splitIndex[0] + 1).trim();
                        map.put(key, value);
                    }
                    line = reader.readLine();
                } while (line != null);
            } catch (IOException e) {
                throw new IllegalStateException(); // we shouldn't get an IOException on a StringReader
            }
        }
        return map;
    }

}
