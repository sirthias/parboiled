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

import java.util.List;

/**
 * A {@link Matcher} trying all of its submatchers in sequence and succeeding when the first submatcher succeeds.
 */
public class FirstOfMatcher extends CustomDefaultLabelMatcher<FirstOfMatcher> {

    public FirstOfMatcher(Rule[] subRules) {
        super(checkArgNotNull(subRules, "subRules"), "FirstOf");
    }

    @SuppressWarnings( {"ForLoopReplaceableByForEach"})
    public boolean match(MatcherContext context) {
        List<Matcher> children = getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Matcher matcher = children.get(i);
            if (matcher.getSubContext(context).runMatcher()) {
                context.createNode();
                return true;
            }
        }
        return false;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }
}