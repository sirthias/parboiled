package org.parboiled;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

interface FollowMatcher {

    /**
     * Adds all token types that can legally follow the match currently being evaluated to the given set.
     * @param context the matcher context
     * @param followerSet the set
     * @return true if the added set of types is complete, false if an empty match would also be legal
     */
    boolean collectCurrentFollowerSet(MatcherContext context, @NotNull Set<Character> followerSet);

}
