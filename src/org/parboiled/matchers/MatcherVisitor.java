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

package org.parboiled.matchers;

public interface MatcherVisitor<V> {

    void visit(ActionMatcher<V> matcher);

    void visit(AnyCharMatcher<V> matcher);

    void visit(CharIgnoreCaseMatcher<V> matcher);

    void visit(CharMatcher<V> matcher);

    void visit(CharRangeMatcher<V> matcher);

    void visit(EmptyMatcher<V> matcher);

    void visit(FirstOfMatcher<V> matcher);

    void visit(OneOrMoreMatcher<V> matcher);

    void visit(OptionalMatcher<V> matcher);

    void visit(SequenceMatcher<V> matcher);

    void visit(TestMatcher<V> matcher);

    void visit(TestNotMatcher<V> matcher);

    void visit(ZeroOrMoreMatcher<V> matcher);

}
