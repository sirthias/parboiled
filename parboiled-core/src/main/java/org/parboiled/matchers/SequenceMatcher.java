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
 * A {@link Matcher} that executes all of its submatchers in sequence and only succeeds if all submatchers succeed.
 */
public class SequenceMatcher extends CustomDefaultLabelMatcher<SequenceMatcher> {

    public SequenceMatcher(Rule[] subRules) {
        super(checkArgNotNull(subRules, "subRules"), "Sequence");
    }

    public boolean match(MatcherContext context) {
        Object valueStackSnapshot = context.getValueStack().takeSnapshot();

        List<Matcher> children = getChildren();
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Matcher matcher = children.get(i);

            // remember the current index in the context, so we can access it for building the current follower set
            context.setIntTag(i);

            if (!matcher.getSubContext(context).runMatcher()) {
                // rule failed, so invalidate all stack actions the rule might have done
                context.getValueStack().restoreSnapshot(valueStackSnapshot);
                return false;
            }
        }
        context.createNode();
        return true;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }

}
