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

import java.util.Arrays;

/**
 * An immutable, set-like aggregation of (relatively few) characters that allows for an inverted semantic (all chars
 * except these few) and proper treatment of the special characters defined in {@link Chars}.
 */
public class Characters {

    private static final char[] NO_CHARS = new char[0];
    public static final Characters NONE = new Characters(false, NO_CHARS);
    public static final Characters ONLY_EOI = Characters.of(Chars.EOI);
    public static final Characters ONLY_EMPTY = Characters.of(Chars.EMPTY);
    public static final Characters ONLY_EOI_AND_EMPTY = Characters.of(Chars.EOI, Chars.EMPTY);
    public static final Characters ALL = new Characters(true, NO_CHARS);
    public static final Characters ALL_EXCEPT_EOI = Characters.allBut(Chars.EOI);
    public static final Characters ALL_EXCEPT_EMPTY = Characters.allBut(Chars.EMPTY);
    public static final Characters ALL_EXCEPT_EOI_AND_EMPTY = Characters.allBut(Chars.EOI, Chars.EMPTY);

    // if the set is negative its semantics change from "includes all characters in the set" to
    // "includes all characters not in the set"
    private final boolean negative;
    private final char[] chars;

    private Characters(boolean negative, @NotNull char[] chars) {
        this.negative = negative;
        this.chars = chars;
    }

    /**
     * Adds the given character to the set. If c is Chars.ANY all characters except Chars.EMPTY will be added.
     *
     * @param c the character to add
     * @return a new Characters object
     */
    @NotNull
    public Characters add(char c) {
        if (c == Chars.ANY) {
            return contains(Chars.EOI) ?
                    (contains(Chars.EMPTY) ? Characters.ALL : Characters.ALL_EXCEPT_EMPTY) :
                    (contains(Chars.EMPTY) ? Characters.ALL_EXCEPT_EOI : Characters.ALL_EXCEPT_EOI_AND_EMPTY);
        }
        return negative ? removeFromChars(c) : addToChars(c);
    }

    /**
     * Removes the given character to the set. If c is Chars.ANY all characters except Chars.EMPTY will be removed.
     *
     * @param c the character to remove
     * @return a new Characters object
     */
    @NotNull
    public Characters remove(char c) {
        if (c == Chars.ANY) {
            return contains(Chars.EOI) ?
                    (contains(Chars.EMPTY) ? Characters.ONLY_EOI_AND_EMPTY : Characters.ONLY_EOI) :
                    (contains(Chars.EMPTY) ? Characters.ONLY_EMPTY : Characters.NONE);
        }
        return negative ? addToChars(c) : removeFromChars(c);
    }

    /**
     * Checks whether this instance contains the given character.
     * If c is Chars.ANY the method will only return true if all non-EOI and non-EMPTY characters are actually included.
     *
     * @param c the character to check for
     * @return true if this instance contains c
     */
    public boolean contains(char c) {
        if (c == Chars.ANY) {
            return negative && (chars.length == 0 || equals(Characters.ONLY_EOI) || equals(
                    Characters.ONLY_EMPTY) || equals(Characters.ONLY_EOI_AND_EMPTY));
        }
        return indexOf(chars, c) == -1 ? negative : !negative;
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
        if (!negative && !other.negative) {
            return addToChars(other.chars);
        }
        if (negative && other.negative) {
            return retainAllChars(other.chars);
        }
        return negative ? removeFromChars(other.chars) : other.removeFromChars(chars);
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
        if (!negative && !other.negative) {
            return removeFromChars(other.chars);
        }
        if (negative && other.negative) {
            return new Characters(false, other.removeFromChars(chars).chars);
        }
        return negative ? addToChars(other.chars) : retainAllChars(other.chars);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(negative ? "![" : "[");
        for (int i = 0; i < chars.length; i++) {
            if (i > 0) sb.append(',');
            char c = chars[i];
            switch (c) {
                case Chars.EOI:
                    sb.append("EOI");
                    break;
                case Chars.EMPTY:
                    sb.append("EMPTY");
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Characters)) return false;
        Characters that = (Characters) o;
        return negative == that.negative && equivalent(chars, that.chars);
    }

    @Override
    public int hashCode() {
        int result = (negative ? 1 : 0);
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
        System.arraycopy(chars, 0, newChars, 1, chars.length);
        newChars[0] = c;
        return new Characters(negative, newChars);
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
        if (chars.length == 1) return negative ? Characters.ALL : Characters.NONE;
        char[] newChars = new char[chars.length - 1];
        System.arraycopy(chars, 0, newChars, 0, ix);
        System.arraycopy(chars, ix + 1, newChars, ix, chars.length - ix - 1);
        return new Characters(negative, newChars);
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
     * Creates a new Characters instance containing only the given char or, if c is {@link Chars#ANY},
     * or all non-EOI, non-EMPTY characters.
     *
     * @param c the char
     * @return a new Characters object
     */
    @NotNull
    public static Characters of(char c) {
        return c == Chars.ANY ? Characters.ALL_EXCEPT_EOI_AND_EMPTY : new Characters(false, new char[] {c});
    }

    /**
     * Creates a new Characters instance containing only the given chars.
     * {@link Chars#ANY} will be expanded to all non-EOI, non-EMPTY characters if it is contained in the given array.
     *
     * @param chars the chars
     * @return a new Characters object
     */
    @NotNull
    public static Characters of(char... chars) {
        return indexOf(chars, Chars.ANY) == -1 ? new Characters(false, chars.clone()) :
                indexOf(chars, Chars.EOI) == -1 ?
                        (indexOf(chars, Chars.EMPTY) == -1 ?
                                Characters.ALL_EXCEPT_EOI_AND_EMPTY : Characters.ALL_EXCEPT_EMPTY) :
                        (indexOf(chars, Chars.EMPTY) == -1 ?
                                Characters.ALL_EXCEPT_EOI : Characters.ALL);
    }

    /**
     * Creates a new Characters instance containing all characters (including {@link Chars#EOI} and {@link Chars#EMPTY})
     * minus the given one.
     *
     * @param c the char to NOT include
     * @return a new Characters object
     */
    @NotNull
    public static Characters allBut(char c) {
        return new Characters(true, new char[] {c});
    }

    /**
     * Creates a new Characters instance containing all characters (including {@link Chars#EOI} and {@link Chars#EMPTY})
     * minus the given ones.
     *
     * @param chars the chars to NOT include
     * @return a new Characters object
     */
    @NotNull
    public static Characters allBut(char... chars) {
        return new Characters(true, chars.clone());
    }

}
