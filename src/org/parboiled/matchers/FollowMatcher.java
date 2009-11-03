/*
 * Copyright (C) 2009 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.matchers;

import org.parboiled.support.Characters;
import org.parboiled.MatcherContext;

/**
 * Matchers that match sequences of other matchers also implement this interface which provides access to the set
 * of all characters that can legally follow the currently running match.
 * Used during parse error recovery.
 */
public interface FollowMatcher<V> {

    /**
     * @param context the current context
     * @return all chars that can legally follow the match currently being evaluated.
     *         Contains (also) Chars.EMPTY if this matcher does not require more characters to be matched.
     */
    Characters getFollowerChars(MatcherContext<V> context);

}
