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

import java.util.Arrays;

@SuppressWarnings({"UnnecessaryParentheses"})
public class Base64 {

    // -------- FIELDS -------------------------------------------------------------------------------------------------

    private static Base64 RFC2045;
    private static Base64 CUSTOM;

    private final char[] CA;
    private final int[] IA;
    private final char fillChar;

    // -------- STATIC METHODS -----------------------------------------------------------------------------------------

    public static Base64 custom() {
        if (CUSTOM == null) {
            CUSTOM = new Base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+-_");
        }
        return CUSTOM;
    }

    public static Base64 rfc2045() {
        if (RFC2045 == null) {
            RFC2045 = new Base64("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=");
        }
        return RFC2045;
    }

    // -------- CONSTRUCTORS -------------------------------------------------------------------------------------------

    public Base64(String alphabet) {
        if (alphabet == null || alphabet.length() != 65) throw new IllegalArgumentException();
        CA = alphabet.substring(0, 64).toCharArray();
        IA = new int[256];
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++) {
            IA[CA[i]] = i;
        }
        fillChar = alphabet.charAt(64);
        IA[fillChar] = 0;
    }

    // -------- OTHER METHODS ------------------------------------------------------------------------------------------

    /**
     * Decodes a BASE64 encoded char array. All illegal characters will be ignored and can handle both arrays with
     * and without line separators.
     *
     * @param sArr The source array. <code>null</code> or length 0 will return an empty array.
     * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
     *         (including '=') isn't divideable by 4.  (I.e. definitely corrupted).
     */
    public final byte[] decode(char[] sArr) {
        // Check special case
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new byte[0];
        }

        // Count illegal characters (including '\r', '\n') to know what size the returned array will be,
        // so we don't have to reallocate & copy it later.
        int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
        for (
                int i = 0; i <
                sLen; i++)  // If input is "pure" (I.e. no line separators or illegal chars) base64 this loop can be commented out.
        {
            if (IA[sArr[i]] < 0) {
                sepCnt++;
            }
        }

        // Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }

        int pad = 0;
        for (int i = sLen; i > 1 && IA[sArr[--i]] <= 0;) {
            if (sArr[i] == fillChar) {
                pad++;
            }
        }

        int len = ((sLen - sepCnt) * 6 >> 3) - pad;

        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        for (int s = 0, d = 0; d < len;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = 0;
            for (int j = 0; j < 4; j++) {   // j only increased if a valid char was found.
                int c = IA[sArr[s++]];
                if (c >= 0) {
                    i |= c << (18 - j * 6);
                } else {
                    j--;
                }
            }
            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            if (d < len) {
                dArr[d++] = (byte) (i >> 8);
                if (d < len) {
                    dArr[d++] = (byte) i;
                }
            }
        }
        return dArr;
    }

    /**
     * Decodes a BASE64 encoded byte array. All illegal characters will be ignored and can handle both arrays with
     * and without line separators.
     *
     * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
     * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
     *         (including '=') isn't divideable by 4. (I.e. definitely corrupted).
     */
    public final byte[] decode(byte[] sArr) {
        // Check special case
        int sLen = sArr.length;

        // Count illegal characters (including '\r', '\n') to know what size the returned array will be,
        // so we don't have to reallocate & copy it later.
        int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
        for (
                int i = 0; i <
                sLen; i++)      // If input is "pure" (I.e. no line separators or illegal chars) base64 this loop can be commented out.
        {
            if (IA[sArr[i] & 0xff] < 0) {
                sepCnt++;
            }
        }

        // Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }

        int pad = 0;
        for (int i = sLen; i > 1 && IA[sArr[--i] & 0xff] <= 0;) {
            if (sArr[i] == fillChar) {
                pad++;
            }
        }

        int len = ((sLen - sepCnt) * 6 >> 3) - pad;

        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        for (int s = 0, d = 0; d < len;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = 0;
            for (int j = 0; j < 4; j++) {   // j only increased if a valid char was found.
                int c = IA[sArr[s++] & 0xff];
                if (c >= 0) {
                    i |= c << (18 - j * 6);
                } else {
                    j--;
                }
            }

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            if (d < len) {
                dArr[d++] = (byte) (i >> 8);
                if (d < len) {
                    dArr[d++] = (byte) i;
                }
            }
        }

        return dArr;
    }

    /**
     * Decodes a BASE64 encoded <code>String</code>. All illegal characters will be ignored and can handle both strings with
     * and without line separators.<br>
     * <b>Note!</b> It can be up to about 2x the speed to call <code>decode(str.toCharArray())</code> instead. That
     * will create a temporary array though. This version will use <code>str.charAt(i)</code> to iterate the string.
     *
     * @param str The source string. <code>null</code> or length 0 will return an empty array.
     * @return The decoded array of bytes. May be of length 0. Will be <code>null</code> if the legal characters
     *         (including '=') isn't divideable by 4.  (I.e. definitely corrupted).
     */
    public final byte[] decode(String str) {
        // Check special case
        int sLen = str != null ? str.length() : 0;
        if (sLen == 0) {
            return new byte[0];
        }

        // Count illegal characters (including '\r', '\n') to know what size the returned array will be,
        // so we don't have to reallocate & copy it later.
        int sepCnt = 0; // Number of separator characters. (Actually illegal characters, but that's a bonus...)
        for (
                int i = 0; i <
                sLen; i++)  // If input is "pure" (I.e. no line separators or illegal chars) base64 this loop can be commented out.
        {
            if (IA[str.charAt(i)] < 0) {
                sepCnt++;
            }
        }

        // Check so that legal chars (including '=') are evenly divideable by 4 as specified in RFC 2045.
        if ((sLen - sepCnt) % 4 != 0) {
            return null;
        }

        // Count '=' at end
        int pad = 0;
        for (int i = sLen; i > 1 && IA[str.charAt(--i)] <= 0;) {
            if (str.charAt(i) == fillChar) {
                pad++;
            }
        }

        int len = ((sLen - sepCnt) * 6 >> 3) - pad;

        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        for (int s = 0, d = 0; d < len;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = 0;
            for (int j = 0; j < 4; j++) {   // j only increased if a valid char was found.
                int c = IA[str.charAt(s++)];
                if (c >= 0) {
                    i |= c << (18 - j * 6);
                } else {
                    j--;
                }
            }
            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            if (d < len) {
                dArr[d++] = (byte) (i >> 8);
                if (d < len) {
                    dArr[d++] = (byte) i;
                }
            }
        }
        return dArr;
    }

    /**
     * Decodes a BASE64 encoded char array that is known to be resonably well formatted. The method is about twice as
     * fast as {@link #decode(char[])}. The preconditions are:<br>
     * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
     * + Line separator must be "\r\n", as specified in RFC 2045
     * + The array must not contain illegal characters within the encoded string<br>
     * + The array CAN have illegal characters at the beginning and end, those will be dealt with appropriately.<br>
     *
     * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
     * @return The decoded array of bytes. May be of length 0.
     */
    public final byte[] decodeFast(char[] sArr) {
        // Check special case
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }

        int sIx = 0, eIx = sLen - 1;    // Start and end index after trimming.

        // Trim illegal chars from start
        while (sIx < eIx && IA[sArr[sIx]] < 0) {
            sIx++;
        }

        // Trim illegal chars from end
        while (eIx > 0 && IA[sArr[eIx]] < 0) {
            eIx--;
        }

        // get the padding count (=) (0, 1 or 2)
        int pad = sArr[eIx] == fillChar ? (sArr[eIx - 1] == fillChar ? 2 : 1) : 0;  // Count '=' at end.
        int cCnt = eIx - sIx + 1;   // Content count including possible separators
        int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        // Decode all but the last 0 - 2 bytes.
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = IA[sArr[sIx++]] << 18 | IA[sArr[sIx++]] << 12 | IA[sArr[sIx++]] << 6 | IA[sArr[sIx++]];

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;

            // If line separator, jump over it.
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            // Decode last 1-3 bytes (incl '=') into 1-3 bytes
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[sArr[sIx++]] << (18 - j * 6);
            }

            for (int r = 16; d < len; r -= 8) {
                dArr[d++] = (byte) (i >> r);
            }
        }

        return dArr;
    }

    /**
     * Decodes a BASE64 encoded byte array that is known to be resonably well formatted. The method is about twice as
     * fast as {@link #decode(byte[])}. The preconditions are:<br>
     * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
     * + Line separator must be "\r\n", as specified in RFC 2045
     * + The array must not contain illegal characters within the encoded string<br>
     * + The array CAN have illegal characters at the beginning and end, those will be dealt with appropriately.<br>
     *
     * @param sArr The source array. Length 0 will return an empty array. <code>null</code> will throw an exception.
     * @return The decoded array of bytes. May be of length 0.
     */
    public final byte[] decodeFast(byte[] sArr) {
        // Check special case
        int sLen = sArr.length;
        if (sLen == 0) {
            return new byte[0];
        }

        int sIx = 0, eIx = sLen - 1;    // Start and end index after trimming.

        // Trim illegal chars from start
        while (sIx < eIx && IA[sArr[sIx] & 0xff] < 0) {
            sIx++;
        }

        // Trim illegal chars from end
        while (eIx > 0 && IA[sArr[eIx] & 0xff] < 0) {
            eIx--;
        }

        // get the padding count (=) (0, 1 or 2)
        int pad = sArr[eIx] == fillChar ? (sArr[eIx - 1] == fillChar ? 2 : 1) : 0;  // Count '=' at end.
        int cCnt = eIx - sIx + 1;   // Content count including possible separators
        int sepCnt = sLen > 76 ? (sArr[76] == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        // Decode all but the last 0 - 2 bytes.
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = IA[sArr[sIx++]] << 18 | IA[sArr[sIx++]] << 12 | IA[sArr[sIx++]] << 6 | IA[sArr[sIx++]];

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;

            // If line separator, jump over it.
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            // Decode last 1-3 bytes (incl '=') into 1-3 bytes
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[sArr[sIx++]] << (18 - j * 6);
            }

            for (int r = 16; d < len; r -= 8) {
                dArr[d++] = (byte) (i >> r);
            }
        }

        return dArr;
    }

    /**
     * Decodes a BASE64 encoded string that is known to be resonably well formatted. The method is about twice as
     * fast as {@link #decode(String)}. The preconditions are:<br>
     * + The array must have a line length of 76 chars OR no line separators at all (one line).<br>
     * + Line separator must be "\r\n", as specified in RFC 2045
     * + The array must not contain illegal characters within the encoded string<br>
     * + The array CAN have illegal characters at the beginning and end, those will be dealt with appropriately.<br>
     *
     * @param s The source string. Length 0 will return an empty array. <code>null</code> will throw an exception.
     * @return The decoded array of bytes. May be of length 0.
     */
    public final byte[] decodeFast(String s) {
        // Check special case
        int sLen = s.length();
        if (sLen == 0) {
            return new byte[0];
        }

        int sIx = 0, eIx = sLen - 1;    // Start and end index after trimming.

        // Trim illegal chars from start
        while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0) {
            sIx++;
        }

        // Trim illegal chars from end
        while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0) {
            eIx--;
        }

        // get the padding count (=) (0, 1 or 2)
        int pad = s.charAt(eIx) == fillChar ? (s.charAt(eIx - 1) == fillChar ? 2 : 1) : 0;  // Count '=' at end.
        int cCnt = eIx - sIx + 1;   // Content count including possible separators
        int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        // Decode all but the last 0 - 2 bytes.
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6 | IA[s
                    .charAt(sIx++)];

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;

            // If line separator, jump over it.
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            // Decode last 1-3 bytes (incl '=') into 1-3 bytes
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++) {
                i |= IA[s.charAt(sIx++)] << (18 - j * 6);
            }

            for (int r = 16; d < len; r -= 8) {
                dArr[d++] = (byte) (i >> r);
            }
        }

        return dArr;
    }

    // ****************************************************************************************
    // *  byte[] version
    // ****************************************************************************************

    /**
     * Encodes a raw byte array into a BASE64 <code>byte[]</code> representation i accordance with RFC 2045.
     *
     * @param sArr    The bytes to convert. If <code>null</code> or length 0 an empty array will be returned.
     * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
     *                No line separator will be in breach of RFC 2045 which specifies max 76 per line but will be a
     *                little faster.
     * @return A BASE64 encoded array. Never <code>null</code>.
     */
    public final byte[] encodeToByte(byte[] sArr, boolean lineSep) {
        // Check special case
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new byte[0];
        }

        int eLen = (sLen / 3) * 3;                              // Length of even 24-bits.
        int cCnt = ((sLen - 1) / 3 + 1) << 2;                   // Returned character count
        int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0); // Length of returned array
        byte[] dArr = new byte[dLen];

        // Encode even 24-bits
        for (int s = 0, d = 0, cc = 0; s < eLen;) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

            // Encode the int into four chars
            dArr[d++] = (byte) CA[(i >>> 18) & 0x3f];
            dArr[d++] = (byte) CA[(i >>> 12) & 0x3f];
            dArr[d++] = (byte) CA[(i >>> 6) & 0x3f];
            dArr[d++] = (byte) CA[i & 0x3f];

            // Add optional line separator
            if (lineSep && ++cc == 19 && d < dLen - 2) {
                dArr[d++] = '\r';
                dArr[d++] = '\n';
                cc = 0;
            }
        }

        // Pad and encode last bits if source isn't an even 24 bits.
        int left = sLen - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

            // Set last four chars
            dArr[dLen - 4] = (byte) CA[i >> 12];
            dArr[dLen - 3] = (byte) CA[(i >>> 6) & 0x3f];
            dArr[dLen - 2] = left == 2 ? (byte) CA[i & 0x3f] : (byte) fillChar;
            dArr[dLen - 1] = (byte) fillChar;
        }
        return dArr;
    }

    // ****************************************************************************************
    // * String version
    // ****************************************************************************************

    /**
     * Encodes a raw byte array into a BASE64 <code>String</code> representation in accordance with RFC 2045.
     *
     * @param sArr    The bytes to convert. If <code>null</code> or length 0 an empty array will be returned.
     * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
     *                No line separator will be in breach of RFC 2045 which specifies max 76 per line but will be a
     *                little faster.
     * @return A BASE64 encoded array. Never <code>null</code>.
     */
    public final String encodeToString(byte[] sArr, boolean lineSep) {
        // Reuse char[] since we can't create a String incrementally anyway and StringBuffer/Builder would be slower.
        return new String(encodeToChar(sArr, lineSep));
    }

    // ****************************************************************************************
    // *  char[] version
    // ****************************************************************************************

    /**
     * Encodes a raw byte array into a BASE64 <code>char[]</code> representation i accordance with RFC 2045.
     *
     * @param sArr    The bytes to convert. If <code>null</code> or length 0 an empty array will be returned.
     * @param lineSep Optional "\r\n" after 76 characters, unless end of file.<br>
     *                No line separator will be in breach of RFC 2045 which specifies max 76 per line but will be a
     *                little faster.
     * @return A BASE64 encoded array. Never <code>null</code>.
     */
    public final char[] encodeToChar(byte[] sArr, boolean lineSep) {
        // Check special case
        int sLen = sArr != null ? sArr.length : 0;
        if (sLen == 0) {
            return new char[0];
        }

        int eLen = (sLen / 3) * 3;              // Length of even 24-bits.
        int cCnt = ((sLen - 1) / 3 + 1) << 2;   // Returned character count
        int dLen = cCnt + (lineSep ? (cCnt - 1) / 76 << 1 : 0); // Length of returned array
        char[] dArr = new char[dLen];

        // Encode even 24-bits
        for (int s = 0, d = 0, cc = 0; s < eLen;) {
            // Copy next three bytes into lower 24 bits of int, paying attension to sign.
            int i = (sArr[s++] & 0xff) << 16 | (sArr[s++] & 0xff) << 8 | (sArr[s++] & 0xff);

            // Encode the int into four chars
            dArr[d++] = CA[(i >>> 18) & 0x3f];
            dArr[d++] = CA[(i >>> 12) & 0x3f];
            dArr[d++] = CA[(i >>> 6) & 0x3f];
            dArr[d++] = CA[i & 0x3f];

            // Add optional line separator
            if (lineSep && ++cc == 19 && d < dLen - 2) {
                dArr[d++] = '\r';
                dArr[d++] = '\n';
                cc = 0;
            }
        }

        // Pad and encode last bits if source isn't even 24 bits.
        int left = sLen - eLen; // 0 - 2.
        if (left > 0) {
            // Prepare the int
            int i = ((sArr[eLen] & 0xff) << 10) | (left == 2 ? ((sArr[sLen - 1] & 0xff) << 2) : 0);

            // Set last four chars
            dArr[dLen - 4] = CA[i >> 12];
            dArr[dLen - 3] = CA[(i >>> 6) & 0x3f];
            dArr[dLen - 2] = left == 2 ? CA[i & 0x3f] : fillChar;
            dArr[dLen - 1] = fillChar;
        }
        return dArr;
    }

    public char[] getAlphabet() {
        return CA;
    }
}