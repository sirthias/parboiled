package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.utils.ImmutableList;

import java.util.List;

class IllegalCharactersMatcher implements Matcher {
    private final String expected;
    private final Characters stopMatchChars;

    /**
     * Creates an IllegalCharactersMatcher that will match all characters not in the given follower set.
     *
     * @param expected       a string describing the expected content
     * @param stopMatchChars the set of characters up to which illegal characters will be matched
     */
    public IllegalCharactersMatcher(@NotNull String expected, @NotNull Characters stopMatchChars) {
        this.expected = expected;
        this.stopMatchChars = stopMatchChars;
    }

    public String getLabel() {
        return "!ILLEGAL!";
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        context.addUnexpectedInputError(context.getCurrentLocation().currentChar, expected);
        do {
            context.advanceInputLocation();
        } while (!stopMatchChars.contains(context.getCurrentLocation().currentChar));
        context.createNode();
        return true;
    }

    @NotNull
    public List<Matcher> getChildren() {
        return ImmutableList.of();
    }

    public Characters getStarterChars() {
        throw new IllegalStateException(); // should never be called
    }

    public String getExpectedString() {
        throw new UnsupportedOperationException();
    }
}
