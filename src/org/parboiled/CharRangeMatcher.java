package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.Chars;
import org.parboiled.support.InputLocation;
import org.parboiled.utils.Preconditions;

class CharRangeMatcher extends AbstractMatcher {

    public final char cLow;
    public final char cHigh;

    public CharRangeMatcher(char cLow, char cHigh) {
        Preconditions.checkArgument(cLow < cHigh && !Chars.isSpecial(cLow) && !Chars.isSpecial(cHigh));
        this.cLow = cLow;
        this.cHigh = cHigh;
    }

    @Override
    public String getLabel() {
        String label = super.getLabel();
        return label != null ? label : cLow + ".." + cHigh;
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        InputLocation currentLocation = context.getCurrentLocation();
        if (currentLocation.currentChar < cLow || currentLocation.currentChar > cHigh) return false;

        context.advanceInputLocation();
        context.createNode();
        return true;
    }

    public Characters getStarterChars() {
        Characters chars = Characters.NONE;
        for (char c = cLow; c <= cHigh; c++) {
            chars = chars.add(c);
        }
        return chars;
    }

}