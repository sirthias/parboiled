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

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.trees.GraphNode;
import org.parboiled.MatcherContext;

/**
 * A Matcher instance is responsible for "executing" a specific Rule instance, i.e. it implements the actual
 * rule type specific matching logic.
 */
public interface Matcher<V> extends GraphNode<Matcher<V>> {

    /**
     * @return the label of the matcher (which is identical to the label of the Rule this matcher matches)
     */
    String getLabel();

    /**
     * @return a string describing what content is expected by this matcher
     */
    String getExpectedString();

    /**
     * Tries a match on the given MatcherContext.
     *
     * @param context  the MatcherContext
     * @param enforced whether this match is required to succeed
     * @return true if the match was successful
     */
    boolean match(@NotNull MatcherContext<V> context, boolean enforced);

    /**
     * @return all characters that this matcher can legally start a match with. Contains Chars.EMPTY if the
     *         matcher can legally match nothing.
     */
    Characters getStarterChars();

}