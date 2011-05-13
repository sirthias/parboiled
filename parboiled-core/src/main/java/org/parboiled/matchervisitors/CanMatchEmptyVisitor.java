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

package org.parboiled.matchervisitors;

import org.parboiled.matchers.ActionMatcher;
import org.parboiled.matchers.*;
import org.parboiled.support.Checks;

/**
 * A {@link MatcherVisitor} determining whether a matcher can legally succeed with an empty match.
 */
public class CanMatchEmptyVisitor implements MatcherVisitor<Boolean> {

    public Boolean visit(ActionMatcher matcher) {
        return true;
    }

    public Boolean visit(AnyMatcher matcher) {
        return false;
    }

    public Boolean visit(CharIgnoreCaseMatcher matcher) {
        return false;
    }

    public Boolean visit(CharMatcher matcher) {
        return false;
    }

    public Boolean visit(CharRangeMatcher matcher) {
        return false;
    }

    public Boolean visit(AnyOfMatcher matcher) {
        return false;
    }

    public Boolean visit(CustomMatcher matcher) {
        return matcher.canMatchEmpty();
    }

    public Boolean visit(EmptyMatcher matcher) {
        return true;
    }

    public Boolean visit(FirstOfMatcher matcher) {
        for (Matcher child : matcher.getChildren()) {
            if (child.accept(this)) return true;
        }
        return false;
    }

    public Boolean visit(NothingMatcher matcher) {
        return false;
    }

    public Boolean visit(OneOrMoreMatcher matcher) {
        Checks.ensure(!matcher.subMatcher.accept(this),
                "Rule '%s' must not allow empty matches as sub-rule of an OneOrMore-rule", matcher.subMatcher);
        return false;
    }

    public Boolean visit(OptionalMatcher matcher) {
        return true;
    }

    public Boolean visit(SequenceMatcher matcher) {
        for (Matcher child : matcher.getChildren()) {
            if (!child.accept(this)) return false;
        }
        return true;
    }

    public Boolean visit(TestMatcher matcher) {
        return true;
    }

    public Boolean visit(TestNotMatcher matcher) {
        return true;
    }

    public Boolean visit(ZeroOrMoreMatcher matcher) {
        Checks.ensure(!matcher.subMatcher.accept(this),
                "Rule '%s' must not allow empty matches as sub-rule of an ZeroOrMore-rule", matcher.subMatcher);
        return true;
    }

}