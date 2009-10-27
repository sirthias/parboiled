package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;

class CharMatcher extends AbstractMatcher {

    public final char character;

    public CharMatcher(char character) {
        this.character = character;
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        if (label != null) return label;

        switch (character) {
            case Chars.EOF:
                return "EOF";
            case Chars.ANY:
                return "ANY";
            case Chars.EMPTY:
                return "EMPTY";
            default:
                return "\'" + character + '\'';
        }
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        if (character == Chars.ANY || character == Chars.EMPTY ||
                context.getCurrentLocation().currentChar == character) {
            if (character != Chars.EMPTY) context.advanceInputLocation();
            context.createNode();
            return true;
        }
        return false;
    }

    public Characters getStarterChars() {
        return Characters.of(character);
    }

}