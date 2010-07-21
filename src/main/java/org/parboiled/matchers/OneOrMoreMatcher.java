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
import org.parboiled.errors.GrammarException;

/**
 * A {@link Matcher} that repeatedly tries its sub matcher against the input.
 * Succeeds if its sub matcher succeeds at least once.
 */
public class OneOrMoreMatcher extends AbstractMatcher {

    public final Matcher subMatcher;

    public OneOrMoreMatcher(@NotNull Rule subRule) {
        super(subRule);
        this.subMatcher = getChildren().get(0);
    }

    public boolean match(@NotNull MatcherContext context) {
        boolean matched = subMatcher.getSubContext(context).runMatcher();
        if (!matched) return false;

        // collect all further matches as well
        int lastIndex = context.getCurrentIndex();
        while (subMatcher.getSubContext(context).runMatcher()) {
            int currentIndex = context.getCurrentIndex();
            if (currentIndex == lastIndex) {
                throw new GrammarException("The inner rule of OneOrMore rule '%s' must not allow empty matches",
                        context.getPath());
            }
            lastIndex = currentIndex;
        }

        context.createNode();
        return true;
    }

    public <R> R accept(@NotNull MatcherVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
