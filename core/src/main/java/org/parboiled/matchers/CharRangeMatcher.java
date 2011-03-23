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
import static org.parboiled.common.StringUtils.escape;

import org.parboiled.MatcherContext;
import org.parboiled.matchervisitors.MatcherVisitor;

/**
 * A {@link Matcher} matching a single character out of a given range of characters.
 */
public class CharRangeMatcher extends AbstractMatcher {
    public final char cLow;
    public final char cHigh;

    public CharRangeMatcher(char cLow, char cHigh) {
        super(escape(cLow) + ".." + escape(cHigh));
        checkArgument(cLow < cHigh);
        this.cLow = cLow;
        this.cHigh = cHigh;
    }

    public boolean match(MatcherContext context) {
        char c = context.getCurrentChar();
        if (c < cLow || c > cHigh) return false;

        context.advanceIndex(1);
        context.createNode();
        return true;
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        return visitor.visit(this);
    }

}