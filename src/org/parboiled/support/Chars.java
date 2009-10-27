package org.parboiled.support;

public class Chars {

    private Chars() {}

    // two special, reserved Unicode non-characters (guaranteed to never actually denote a real char)
    // we use for special meaning
    public static final char EOF = '\uFFFF';
    public static final char ANY = '\uFFFE';
    public static final char EMPTY = '\uFFFD';

    public static boolean isSpecial(char c) {
        return c == EOF || c == ANY || c == EMPTY;
    }

}
