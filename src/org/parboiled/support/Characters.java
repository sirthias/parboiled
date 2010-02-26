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

package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.common.StringUtils;

import java.util.Arrays;
import java.util.Random;

/**
 * An immutable, set-like aggregation of (relatively few) characters that allows for an inverted semantic (all chars
 * except these few).
 */
public class Characters {

    private static final char[] NO_CHARS = new char[0];
    public static final Characters NONE = new Characters(false, NO_CHARS);
    public static final Characters ALL = new Characters(true, NO_CHARS);

    // if the set is subtractive its semantics change from "includes all characters in the set" to
    // "includes all characters not in the set"
    private final boolean subtractive;
    private final char[] chars;

    private Characters(boolean subtractive, @NotNull char[] chars) {
        this.subtractive = subtractive;
        this.chars = chars;
    }

    /**
     * @return true if the set is subtractive
     */
    public boolean isSubtractive() {
        return subtractive;
    }

    /**
     * Returns the characters in this set, if it is additive.
     * If the set is subtractive the method returns the characters <b>not</b> in the set.
     *
     * @return the characters
     */
    public char[] getChars() {
        return chars;
    }

    /**
     * Adds the given character to the set. If c is Chars.ANY all characters except Chars.EMPTY will be added.
     *
     * @param c the character to add
     * @return a new Characters object
     */
    @NotNull
    public Characters add(char c) {
        return subtractive ? removeFromChars(c) : addToChars(c);
    }

    /**
     * Removes the given character to the set. If c is Chars.ANY all characters except Chars.EMPTY will be removed.
     *
     * @param c the character to remove
     * @return a new Characters object
     */
    @NotNull
    public Characters remove(char c) {
        return subtractive ? addToChars(c) : removeFromChars(c);
    }

    /**
     * Checks whether this instance contains the given character.
     * If c is Chars.ANY the method will only return true if all non-EOI and non-EMPTY characters are actually included.
     *
     * @param c the character to check for
     * @return true if this instance contains c
     */
    public boolean contains(char c) {
        return indexOf(chars, c) == -1 ? subtractive : !subtractive;
    }

    /**
     * Returns a new Characters object containing all the characters of this instance plus all characters of the
     * given instance.
     *
     * @param other the other Characters to add
     * @return a new Characters object
     */
    @NotNull
    public Characters add(@NotNull Characters other) {
        if (!subtractive && !other.subtractive) {
            return addToChars(other.chars);
        }
        if (subtractive && other.subtractive) {
            return retainAllChars(other.chars);
        }
        return subtractive ? removeFromChars(other.chars) : other.removeFromChars(chars);
    }

    /**
     * Returns a new Characters object containing all the characters of this instance minus all characters of the
     * given instance.
     *
     * @param other the other Characters to remove
     * @return a new Characters object
     */
    @NotNull
    public Characters remove(@NotNull Characters other) {
        if (!subtractive && !other.subtractive) {
            return removeFromChars(other.chars);
        }
        if (subtractive && other.subtractive) {
            return new Characters(false, other.removeFromChars(chars).chars);
        }
        return subtractive ? addToChars(other.chars) : retainAllChars(other.chars);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(subtractive ? "![" : "[");
        for (int i = 0; i < chars.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(StringUtils.escape(chars[i]));
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Characters)) return false;
        Characters that = (Characters) o;
        return subtractive == that.subtractive && equivalent(chars, that.chars);
    }

    @Override
    public int hashCode() {
        int result = (subtractive ? 1 : 0);
        result = 31 * result + Arrays.hashCode(chars);
        return result;
    }

    @NotNull
    private Characters addToChars(char[] chs) {
        Characters characters = this;
        for (char c : chs) {
            characters = characters.addToChars(c);
        }
        return characters;
    }

    @NotNull
    private Characters addToChars(char c) {
        if (indexOf(chars, c) != -1) return this;
        char[] newChars = new char[chars.length + 1];
        System.arraycopy(chars, 0, newChars, 0, chars.length);
        newChars[chars.length] = c;
        return new Characters(subtractive, newChars);
    }

    @NotNull
    private Characters removeFromChars(char[] chs) {
        Characters characters = this;
        for (char c : chs) {
            characters = characters.removeFromChars(c);
        }
        return characters;
    }

    @NotNull
    private Characters removeFromChars(char c) {
        int ix = indexOf(chars, c);
        if (ix == -1) return this;
        if (chars.length == 1) return subtractive ? Characters.ALL : Characters.NONE;
        char[] newChars = new char[chars.length - 1];
        System.arraycopy(chars, 0, newChars, 0, ix);
        System.arraycopy(chars, ix + 1, newChars, ix, chars.length - ix - 1);
        return new Characters(subtractive, newChars);
    }

    @NotNull
    private Characters retainAllChars(char[] chs) {
        Characters characters = this;
        for (char c : chars) {
            if (indexOf(chs, c) == -1) {
                characters = characters.removeFromChars(c);
            }
        }
        return characters;
    }

    private static int indexOf(char[] chars, char c) {
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == c) return i;
        }
        return -1;
    }

    // order independent Array.equals()
    private static boolean equivalent(@NotNull char[] a, @NotNull char[] b) {
        if (a == b) return true;
        int length = a.length;
        if (b.length != length) return false;

        outer:
        for (int i = 0; i < length; i++) {
            char ac = a[i];
            for (int j = 0; j < length; j++) {
                if (ac == b[j]) continue outer;
            }
            return false;
        }
        return true;
    }

    /**
     * Creates a new Characters instance containing only the given char or.
     *
     * @param c the char
     * @return a new Characters object
     */
    @NotNull
    public static Characters of(char c) {
        return new Characters(false, new char[] {c});
    }

    /**
     * Creates a new Characters instance containing only the given chars.
     *
     * @param chars the chars
     * @return a new Characters object
     */
    @NotNull
    public static Characters of(char... chars) {
        return chars.length == 0 ? Characters.NONE : new Characters(false, chars.clone());
    }

    /**
     * Creates a new Characters instance containing only the given chars.
     *
     * @param chars the chars
     * @return a new Characters object
     */
    @NotNull
    public static Characters of(String chars) {
        return StringUtils.isEmpty(chars) ? Characters.NONE : new Characters(false, chars.toCharArray());
    }

    /**
     * Creates a new Characters instance containing all characters minus the given one.
     *
     * @param c the char to NOT include
     * @return a new Characters object
     */
    @NotNull
    public static Characters allBut(char c) {
        return new Characters(true, new char[] {c});
    }

    /**
     * Creates a new Characters instance containing all characters minus the given ones.
     *
     * @param chars the chars to NOT include
     * @return a new Characters object
     */
    @NotNull
    public static Characters allBut(char... chars) {
        return chars.length == 0 ? Characters.ALL : new Characters(true, chars.clone());
    }

    /**
     * Creates a new Characters instance containing all characters minus the given ones.
     *
     * @param chars the chars to NOT include
     * @return a new Characters object
     */
    @NotNull
    public static Characters allBut(String chars) {
        return StringUtils.isEmpty(chars) ? Characters.ALL : new Characters(true, chars.toCharArray());
    }

    /**
     * Finds a character that is in the set and returns it. Guaranteed to succeed unless the set is empty,
     * in which case this method returns null.
     *
     * @return a character that is in this set
     */
    public Character getRepresentative() {
        if (equals(Characters.NONE)) return null;
        if (isSubtractive()) {
            Random random = new Random();
            while (true) {
                char c = (char) random.nextInt(Character.MIN_SUPPLEMENTARY_CODE_POINT);
                if (contains(c)) return c;
            }
        }
        return chars[0];
    }
}
