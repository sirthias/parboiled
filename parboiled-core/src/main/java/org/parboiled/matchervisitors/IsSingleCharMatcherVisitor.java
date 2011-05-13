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

/**
 * A {@link MatcherVisitor} determining whether a matcher is a basic single character matcher.
 */
public class IsSingleCharMatcherVisitor implements MatcherVisitor<Boolean> {

    public Boolean visit(ActionMatcher matcher) {
        return false;
    }

    public Boolean visit(AnyMatcher matcher) {
        return true;
    }

    public Boolean visit(CharIgnoreCaseMatcher matcher) {
        return true;
    }

    public Boolean visit(CharMatcher matcher) {
        return true;
    }

    public Boolean visit(CharRangeMatcher matcher) {
        return true;
    }

    public Boolean visit(AnyOfMatcher matcher) {
        return true;
    }

    public Boolean visit(CustomMatcher matcher) {
        return matcher.isSingleCharMatcher();
    }

    public Boolean visit(EmptyMatcher matcher) {
        return false;
    }

    public Boolean visit(FirstOfMatcher matcher) {
        return false;
    }

    public Boolean visit(NothingMatcher matcher) {
        return false;
    }

    public Boolean visit(OneOrMoreMatcher matcher) {
        return false;
    }

    public Boolean visit(OptionalMatcher matcher) {
        return false;
    }

    public Boolean visit(SequenceMatcher matcher) {
        return false;
    }

    public Boolean visit(TestMatcher matcher) {
        return false;
    }

    public Boolean visit(TestNotMatcher matcher) {
        return false;
    }

    public Boolean visit(ZeroOrMoreMatcher matcher) {
        return false;
    }

}