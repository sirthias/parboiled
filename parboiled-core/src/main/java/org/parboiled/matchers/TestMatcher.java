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
 * A special {@link Matcher} not actually matching any input but rather trying its submatcher against the current input
 * position. Succeeds if the submatcher would succeed.
 */
public class TestMatcher extends CustomDefaultLabelMatcher<TestMatcher> {
    public final Matcher subMatcher;

    public TestMatcher(Rule subRule) {
        super(checkArgNotNull(subRule, "subRule"), "Test");
        this.subMatcher = getChildren().get(0);
    }

    public boolean match(MatcherContext context) {
        int lastIndex = context.getCurrentIndex();
        Object valueStackSnapshot = context.getValueStack().takeSnapshot();

        if (!subMatcher.getSubContext(context).runMatcher()) return false;

        // reset location, Test matchers never advance
        context.setCurrentIndex(lastIndex);

        // erase all value stack changes the the submatcher could have made
        context.getValueStack().restoreSnapshot(valueStackSnapshot);
        return true;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }

}