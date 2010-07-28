/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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
import org.parboiled.Rule;
import org.parboiled.support.MatcherVisitor;

/**
 * Base class of custom matcher implementations. If you want to implement custom matchers they have to be derived
 * from this class. Instances of derived classes can be directly used in rule defining expressions.
 * Caution: For performance reasons the parsing engine delegates the task of taking and restoring value stack
 * snapshots to the matchers. Your custom matcher implementations therefore have to take care of value stack managment
 * themselves! (See the implementation of the SequenceMatcher for hints on how to do this!) 
 */
public abstract class CustomMatcher extends AbstractMatcher {

    protected CustomMatcher() {
    }

    protected CustomMatcher(@NotNull Rule subRule) {
        super(subRule);
    }

    protected CustomMatcher(@NotNull Rule[] subRules) {
        super(subRules);
    }

    /**
     * Determines whether this matcher instance always matches exactly one character.
     *
     * @return true if this matcher always matches exactly one character
     */
    public abstract boolean isSingleCharMatcher();

    /**
     * Determines whether this matcher instance allows empty matches.
     *
     * @return true if this matcher instance allows empty matches
     */
    public abstract boolean canMatchEmpty();

    /**
     * Determines whether this matcher instance can start a match with the given char.
     *
     * @param c the char
     * @return true if this matcher instance can start a match with the given char.
     */
    public abstract boolean isStarterChar(char c);

    public <R> R accept(@NotNull MatcherVisitor<R> visitor) {
        return visitor.visit(this);
    }

}
