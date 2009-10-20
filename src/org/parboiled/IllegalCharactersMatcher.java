package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.ImmutableList;

import java.util.List;
import java.util.Set;

class IllegalCharactersMatcher implements Matcher {
    private final String expected;
    private final Set<Character> followerSet;

    /**
     * Creates an IllegalCharactersMatcher that will match exactly one character.
     * @param expected a string describing the expected content
     */
    public IllegalCharactersMatcher(@NotNull String expected) {
        this(expected, null);
    }

    /**
     * Creates an IllegalCharactersMatcher that will match all characters not in the given follower set.
     * @param expected a string describing the expected content
     * @param followerSet the set of characters up to which illegal characters will be matched
     */
    public IllegalCharactersMatcher(@NotNull String expected, Set<Character> followerSet) {
        this.expected = expected;
        this.followerSet = followerSet;
    }

    public String getLabel() {
        return "!ILLEGAL!";
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        context.addUnexpectedInputError(context.getCurrentLocation().currentChar, expected);
        do {
            context.advanceInputLocation();
        } while (followerSet != null && !followerSet.contains(context.getCurrentLocation().currentChar));
        context.createNode();
        return true;
    }

    @NotNull
    public List<Matcher> getChildren() {
        return ImmutableList.of();
    }

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        throw new IllegalStateException(); // should never be called
    }

    public String getExpectedString() {
        throw new UnsupportedOperationException();
    }
}
