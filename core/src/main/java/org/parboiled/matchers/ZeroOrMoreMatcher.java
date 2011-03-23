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
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.errors.GrammarException;
import org.parboiled.matchervisitors.MatcherVisitor;

/**
 * A {@link Matcher} that repeatedly tries its submatcher against the input. Always succeeds.
 */
public class ZeroOrMoreMatcher extends CustomDefaultLabelMatcher<ZeroOrMoreMatcher> {
    public final Matcher subMatcher;

    public ZeroOrMoreMatcher(Rule subRule) {
        super(checkArgNotNull(subRule, "subRule"), "ZeroOrMore");
        this.subMatcher = getChildren().get(0);
    }

    public boolean match(MatcherContext context) {
        checkArgNotNull(context, "context");
        int lastIndex = context.getCurrentIndex();
        while (subMatcher.getSubContext(context).runMatcher()) {
            int currentLocation = context.getCurrentIndex();
            if (currentLocation == lastIndex) {
                throw new GrammarException("The inner rule of ZeroOrMore rule '%s' must not allow empty matches",
                        context.getPath());
            }
            lastIndex = currentLocation;
        }

        context.createNode();
        return true;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }

}