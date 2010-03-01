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

import org.parboiled.matchers.CharIgnoreCaseMatcher;
import org.parboiled.matchers.CharMatcher;
import org.parboiled.matchers.CharRangeMatcher;
import org.parboiled.matchers.CharactersMatcher;

import java.util.Random;

public class GetAStarterCharVisitor<V> extends DefaultMatcherVisitor<V, Character> {

    @Override
    public Character visit(CharactersMatcher<V> matcher) {
        Characters characters = matcher.characters;
        if (!characters.isSubtractive()) {
            return characters.getChars()[0];
        }

        // for substractive sets we try to randomly choose a fitting character
        Random random = new Random();
        char c;
        do {
            c = (char) random.nextInt(Character.MAX_VALUE);
        } while (!Character.isDefined(c) || !characters.contains(c));
        return c;
    }

    @Override
    public Character visit(CharIgnoreCaseMatcher<V> matcher) {
        return matcher.charLow;
    }

    @Override
    public Character visit(CharMatcher<V> matcher) {
        return matcher.character;
    }

    @Override
    public Character visit(CharRangeMatcher<V> matcher) {
        return matcher.cLow;
    }
}
