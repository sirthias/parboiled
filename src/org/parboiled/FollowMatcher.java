package org.parboiled;

import org.parboiled.support.Characters;

/**
 * Matchers that match sequences of other matchers also implement this interface which provides access to the set
 * of all characters that can legally follow the currently running match.
 * Used during parse error recovery.
 */
interface FollowMatcher {

    /**
     * @param context the current context
     * @return all chars that can legally follow the match currently being evaluated.
     *         Contains (also) Chars.EMPTY if this matcher does not require more characters to be matched.
     */
    Characters getFollowerChars(MatcherContext context);

}
