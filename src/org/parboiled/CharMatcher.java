package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Chars;
import org.parboiled.support.InputBuffer;

class CharMatcher extends AbstractMatcher {

    public final char cLow;
    public final char cHigh;

    public CharMatcher(char character) {
        this.cLow = character;
        this.cHigh = character;
    }

    public CharMatcher(char cLow, char cHigh) {
        this.cLow = cLow;
        this.cHigh = cHigh;
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        if (label != null) return label;

        if (cLow == Chars.EOF) return "EOF";
        if (cLow == Chars.ANY) return "ANY";
        return cLow == cHigh ? '\'' + String.valueOf(cLow) + '\'' : cLow + ".." + cHigh;
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        InputBuffer input = context.getInputBuffer();

        char c = input.LA();
        if (cLow == Chars.ANY || c >= cLow && c <= cHigh) {
            input.consume();
        } else {
            if (!enforced) return false;
            context.addUnexpectedInputError(getLabel());
        }
        context.createNode();
        return true;
    }

}