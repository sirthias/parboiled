/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled.errorhandling;

import org.parboiled.matchers.*;
import org.parboiled.support.Checks;

/**
 * Determines whether a matcher can legally succeed with an empty match.
 *
 * @param <V>
 */
public class CanMatchEmptyVisitor<V> implements MatcherVisitor<V, Boolean> {

    public Boolean visit(ActionMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(CharactersMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(CharIgnoreCaseMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(CharMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(CharRangeMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(EmptyMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(FirstOfMatcher<V> matcher) {
        for (Matcher<V> child : matcher.getChildren()) {
            if (child.accept(this)) return true;
        }
        return false;
    }

    public Boolean visit(OneOrMoreMatcher<V> matcher) {
        Checks.ensure(!matcher.subMatcher.accept(this),
                "Rule '%s' must not allow empty matches as sub-rule of an OneOrMore-rule", matcher.subMatcher);
        return false;
    }

    public Boolean visit(OptionalMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(SequenceMatcher<V> matcher) {
        for (Matcher<V> child : matcher.getChildren()) {
            if (!child.accept(this)) return false;
        }
        return true;
    }

    public Boolean visit(TestMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(TestNotMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(ZeroOrMoreMatcher<V> matcher) {
        Checks.ensure(!matcher.subMatcher.accept(this),
                "Rule '%s' must not allow empty matches as sub-rule of an ZeroOrMore-rule", matcher.subMatcher);
        return true;
    }

}