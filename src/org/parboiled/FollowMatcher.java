package org.parboiled;

import org.parboiled.support.Characters;

interface FollowMatcher {

    /**
     * @param context the current context
     * @return all chars that can legally follow the match currently being evaluated.
     *         Contains (also) Chars.EMPTY if this matcher does not require more characters to be matched.
     */
    Characters getFollowerChars(MatcherContext context);

}
