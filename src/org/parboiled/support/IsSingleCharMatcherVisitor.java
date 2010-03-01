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

package org.parboiled.support;

import org.parboiled.matchers.*;

/**
 * A {@link MatcherVisitor} determining whether a matcher is a basic single character matcher.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class IsSingleCharMatcherVisitor<V> implements MatcherVisitor<V, Boolean> {

    public Boolean visit(ActionMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(CharactersMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(CharIgnoreCaseMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(CharMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(CharRangeMatcher<V> matcher) {
        return true;
    }

    public Boolean visit(EmptyMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(FirstOfMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(OneOrMoreMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(OptionalMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(SequenceMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(TestMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(TestNotMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(ZeroOrMoreMatcher<V> matcher) {
        return false;
    }

}