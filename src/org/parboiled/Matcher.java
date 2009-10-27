package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.utils.DGraphNode;

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
     * @return all characters that this matcher can legally start a match with. Contains Chars.EMPTY if the
     *         matcher can legally match nothing.
     */
    Characters getStarterChars();

}