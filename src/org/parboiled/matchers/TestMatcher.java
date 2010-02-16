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
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.exceptions.GrammarException;
import org.parboiled.support.InputLocation;

/**
 * A special Matcher not actually matching any input but rather trying its sub matcher against the current input
 * position. Succeeds if the sub matcher would succeed.
 *
 * @param <V>
 */
public class TestMatcher<V> extends AbstractMatcher<V> {

    public final Matcher<V> subMatcher;

    public TestMatcher(@NotNull Rule subRule) {
        super(subRule);
        this.subMatcher = getChildren().get(0);
    }

    @Override
    public String getLabel() {
        return hasLabel() ? super.getLabel() : "&(" + subMatcher + ")";
    }

    @Override
    public Rule withoutNode() {
        throw new GrammarException("test rules cannot be marked as withoutNode-rules, " +
                "they never create parse tree nodes");
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        InputLocation lastLocation = context.getCurrentLocation();
        if (context.getSubContext(subMatcher).runMatcher()) {
            context.setCurrentLocation(lastLocation); // reset location, test matchers never advance
            return true;
        }
        context.setCurrentLocation(lastLocation); // reset location, test matchers never advance
        return false;
    }

    public <R> R accept(@NotNull MatcherVisitor<V, R> visitor) {
        return visitor.visit(this);
    }

}