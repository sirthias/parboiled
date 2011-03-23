/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import static org.parboiled.common.Preconditions.*;
import org.parboiled.Rule;
import org.parboiled.matchervisitors.MatcherVisitor;

/**
 * Base class of custom matcher implementations. If you want to implement custom matchers they have to be derived
 * from this class. Instances of derived classes can be directly used in rule defining expressions.
 * Caution: For performance reasons the parsing engine delegates the task of taking and restoring value stack
 * snapshots to the matchers. If your custom matcher can run parser actions underneath it your custom matcher
 * implementation therefore has to take care of value stack managment itselves!
 * (See the implementation of the SequenceMatcher for hints on how to do this!)
 */
public abstract class CustomMatcher extends AbstractMatcher {

    protected CustomMatcher(String label) {
        super(label);
    }

    protected CustomMatcher(Rule subRule, String label) {
        super(checkArgNotNull(subRule, "subRule"), label);
    }

    protected CustomMatcher(Rule[] subRules, String label) {
        super(checkArgNotNull(subRules, "subRules"), label);
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

    /**
     * Returns one of possibly several chars that a match can start with.
     *
     * @return a starter char
     */
    public abstract char getStarterChar();

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }
}
