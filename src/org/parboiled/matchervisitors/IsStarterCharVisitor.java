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

package org.parboiled.matchervisitors;

import org.parboiled.matchers.*;

/**
 * A MatcherVisitor that determines whether a matcher can start a match with a given char.
 *
 * @param <V>
 */
public class IsStarterCharVisitor<V> implements MatcherVisitor<V, Boolean> {

    private final CanMatchEmptyVisitor<V> canMatchEmptyVisitor = new CanMatchEmptyVisitor<V>();
    private final char starterChar;

    public IsStarterCharVisitor(char starterChar) {
        this.starterChar = starterChar;
    }

    public Boolean visit(ActionMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(CharactersMatcher<V> matcher) {
        return matcher.characters.contains(starterChar);
    }

    public Boolean visit(CharIgnoreCaseMatcher<V> matcher) {
        return matcher.charLow == starterChar || matcher.charUp == starterChar;
    }

    public Boolean visit(CharMatcher<V> matcher) {
        return matcher.character == starterChar;
    }

    public Boolean visit(CharRangeMatcher<V> matcher) {
        return matcher.cLow <= starterChar && starterChar <= matcher.cHigh;
    }

    public Boolean visit(EmptyMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(FirstOfMatcher<V> matcher) {
        for (Matcher<V> child : matcher.getChildren()) {
            if (child.accept(this)) return true;
        }
        return false;
    }

    public Boolean visit(OneOrMoreMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

    public Boolean visit(OptionalMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

    public Boolean visit(SequenceMatcher<V> matcher) {
        for (Matcher<V> child : matcher.getChildren()) {
            if (child.accept(this)) return true;
            if (!child.accept(canMatchEmptyVisitor)) break;
        }
        return false;
    }

    public Boolean visit(TestMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

    public Boolean visit(TestNotMatcher<V> matcher) {
        return false;
    }

    public Boolean visit(ZeroOrMoreMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

}
