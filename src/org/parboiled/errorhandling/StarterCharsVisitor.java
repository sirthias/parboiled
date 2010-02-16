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
import org.parboiled.support.Characters;

/**
 * A MatcherVisitor that returns all characters that a matcher can legally start a match with.
 *
 * @param <V>
 */
public class StarterCharsVisitor<V> implements MatcherVisitor<V, Characters> {

    private final CanMatchEmptyVisitor<V> canMatchEmptyVisitor = new CanMatchEmptyVisitor<V>();

    public Characters visit(ActionMatcher<V> matcher) {
        return Characters.NONE;
    }

    public Characters visit(CharactersMatcher<V> matcher) {
        return matcher.characters;
    }

    public Characters visit(CharIgnoreCaseMatcher<V> matcher) {
        return Characters.of(matcher.charLow, matcher.charUp);
    }

    public Characters visit(CharMatcher<V> matcher) {
        return Characters.of(matcher.character);
    }

    public Characters visit(CharRangeMatcher<V> matcher) {
        Characters chars = Characters.NONE;
        for (char c = matcher.cLow; c <= matcher.cHigh; c++) {
            chars = chars.add(c);
        }
        return chars;
    }

    public Characters visit(EmptyMatcher<V> matcher) {
        return Characters.NONE;
    }

    public Characters visit(FirstOfMatcher<V> matcher) {
        Characters chars = Characters.NONE;
        for (Matcher<V> child : matcher.getChildren()) {
            chars = chars.add(child.accept(this));
        }
        return chars;
    }

    public Characters visit(OneOrMoreMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

    public Characters visit(OptionalMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

    public Characters visit(SequenceMatcher<V> matcher) {
        Characters chars = Characters.NONE;
        for (Matcher<V> child : matcher.getChildren()) {
            chars = chars.add(child.accept(this));
            if (!child.accept(canMatchEmptyVisitor)) break;
        }
        return chars;
    }

    public Characters visit(TestMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

    public Characters visit(TestNotMatcher<V> matcher) {
        return Characters.ALL.remove(matcher.subMatcher.accept(this));
    }

    public Characters visit(ZeroOrMoreMatcher<V> matcher) {
        return matcher.subMatcher.accept(this);
    }

}
