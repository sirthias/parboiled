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

package org.parboiled;

import org.parboiled.matchers.*;

public interface MatcherVisitor<V> {

    void visitActionMatcher(ActionMatcher<V> matcher);

    void visitAnyCharMatcher(AnyCharMatcher<V> matcher);

    void visitCharIgnoreCaseMatcher(CharIgnoreCaseMatcher<V> matcher);

    void visitCharMatcher(CharMatcher<V> matcher);

    void visitCharRangeMatcher(CharRangeMatcher<V> matcher);

    void visitEmptyMatcher(EmptyMatcher<V> matcher);

    void visitFirstOfMatcher(FirstOfMatcher<V> matcher);

    void visitOneOrMoreMatcher(OneOrMoreMatcher<V> matcher);

    void visitOptionalMatcher(OptionalMatcher<V> matcher);

    void visitSequenceMatcher(SequenceMatcher<V> matcher);

    void visitTestMatcher(TestMatcher<V> matcher);

    void visitZeroOrMoreMatcher(ZeroOrMoreMatcher<V> matcher);

}
