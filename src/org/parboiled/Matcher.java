package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.DGraphNode;

public interface Matcher extends DGraphNode<Matcher> {

    /**
     * @return the label of the matcher
     */
    String getLabel();

    /**
     * @return true if this matcher is enforced
     */
    boolean isEnforced();

    /**
     * Try a match on the given MatcherContext.
     *
     * @param context  the MatcherContext
     * @param enforced true if this match is enforced
     * @return true if the match was successful
     */
    boolean match(@NotNull MatcherContext context, boolean enforced);

}