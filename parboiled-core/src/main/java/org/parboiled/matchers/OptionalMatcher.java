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
import org.parboiled.matchervisitors.MatcherVisitor;

/**
 * A {@link Matcher} that tries its submatcher once against the input and always succeeds.
 */
public class OptionalMatcher extends CustomDefaultLabelMatcher<OptionalMatcher> {
    public final Matcher subMatcher;

    public OptionalMatcher(Rule subRule) {
        super(checkArgNotNull(subRule, "subRule"), "Optional");
        this.subMatcher = getChildren().get(0);
    }

    public boolean match(MatcherContext context) {
        subMatcher.getSubContext(context).runMatcher();
        context.createNode();
        return true;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }
}
