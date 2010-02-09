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

public class EmptyMatcherVisitor<V> implements MatcherVisitor<V> {

    public void visit(ActionMatcher<V> matcher) {}

    public void visit(AnyCharMatcher<V> matcher) {}

    public void visit(CharIgnoreCaseMatcher<V> matcher) {}

    public void visit(CharMatcher<V> matcher) {}

    public void visit(CharRangeMatcher<V> matcher) {}

    public void visit(EmptyMatcher<V> matcher) {}

    public void visit(FirstOfMatcher<V> matcher) {}

    public void visit(OneOrMoreMatcher<V> matcher) {}

    public void visit(OptionalMatcher<V> matcher) {}

    public void visit(SequenceMatcher<V> matcher) {}

    public void visit(TestMatcher<V> matcher) {}

    public void visit(TestNotMatcher<V> matcher) {}

    public void visit(ZeroOrMoreMatcher<V> matcher) {}

}
