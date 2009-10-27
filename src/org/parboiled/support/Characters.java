package org.parboiled.support;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * An immutable, set-like aggregation of (relatively few) characters that allows for in inverted semantic (all chars
 * except these few) and proper containment of the EMPTY char (which is not part of ANY char).
 */
public class Characters {

    private static final char[] NO_CHARS = new char[0];
    public static final Characters NONE = new Characters(false, NO_CHARS);
    public static final Characters ALL = new Characters(true, NO_CHARS);
    public static final Characters ONLY_EMPTY = Characters.of(Chars.EMPTY);
    public static final Characters ALL_EXCEPT_EMPTY = Characters.allBut(Chars.EMPTY);

    // if the set is negative its semantics change from "includes all characters in the set" to
    // "includes all characters not in the set"
    private final boolean negative;
    private final char[] chars;

    private Characters(boolean negative, @NotNull char[] chars) {
        this.negative = negative;
        this.chars = chars;
    }

    @NotNull
    public Characters add(char c) {
        if (c == Chars.ANY) {
            return contains(Chars.EMPTY) ? Characters.ALL : Characters.ALL_EXCEPT_EMPTY;
        }
        return negative ? removeFromChars(c) : addToChars(c);
    }

    @NotNull
    public Characters remove(char c) {
        if (c == Chars.ANY) {
            return contains(Chars.EMPTY) ? Characters.ONLY_EMPTY : Characters.NONE;
        }
        return negative ? addToChars(c) : removeFromChars(c);
    }

    public boolean contains(char c) {
        if (c == Chars.ANY) {
            return !negative && (chars.length == 0 || (chars.length == 1 && contains(Chars.EMPTY)));
        }
        return indexOf(chars, c) == -1 ? negative : !negative;
    }

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
                case Chars.EOF:
                    sb.append("EOF");
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

    @NotNull
    public static Characters of(char c) {
        return new Characters(false, new char[] {c});
    }

    @NotNull
    public static Characters of(char... chars) {
        return new Characters(false, chars.clone());
    }

    @NotNull
    public static Characters allBut(char c) {
        return new Characters(true, new char[] {c});
    }

    @NotNull
    public static Characters allBut(char... chars) {
        return new Characters(true, chars.clone());
    }

}
