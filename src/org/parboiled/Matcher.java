package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.DGraphNode;

import java.util.Set;

public interface Matcher extends DGraphNode<Matcher> {

    /**
     * @return the label of the matcher
     */
    String getLabel();

    /**
     * @return a string describing what content is expected by this matcher
     */
    String getExpectedString();

    /**
     * Try a match on the given MatcherContext.
     *
     * @param context  the MatcherContext
     * @param enforced whether this match is required to succeed
     * @return true if the match was successful
     */
    boolean match(@NotNull MatcherContext context, boolean enforced);

    /**
     * Adds all characters that this matcher can legally start a match with to the given set.
     *
     * @param firstCharSet the set
     * @return true if the added set of types is complete, false if an empty match would also be legal
     */
    boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet);

}