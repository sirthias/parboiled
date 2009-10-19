package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Chars;
import org.parboiled.support.InputLocation;

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
        InputLocation currentLocation = context.getCurrentLocation();
        if (cLow == Chars.ANY || currentLocation.currentChar >= cLow && currentLocation.currentChar <= cHigh) {
            context.setCurrentLocation(currentLocation.advance());
        } else {
            if (!enforced) return false;
            context.addUnexpectedInputError(getLabel());
        }
        context.createNode();
        return true;
    }

}