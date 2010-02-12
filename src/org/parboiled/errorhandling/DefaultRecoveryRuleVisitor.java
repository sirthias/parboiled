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

import org.jetbrains.annotations.NotNull;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.ContextAware;
import org.parboiled.Rule;
import org.parboiled.matchers.*;

public class DefaultRecoveryRuleVisitor<V> implements MatcherVisitor<V, Rule>, ContextAware<V> {

    private Context<V> context;

    public void setContext(@NotNull Context<V> context) {
        this.context = context;
    }

    public Rule visit(ActionMatcher<V> matcher) {
        return null;
    }

    public Rule visit(AnyCharMatcher<V> matcher) {
        return context.getParser().singleCharErrorRecovery(matcher);
    }

    public Rule visit(CharIgnoreCaseMatcher<V> matcher) {
        return context.getParser().singleCharErrorRecovery(matcher);
    }

    public Rule visit(CharMatcher<V> matcher) {
        return context.getParser().singleCharErrorRecovery(matcher);
    }

    public Rule visit(CharRangeMatcher<V> matcher) {
        return context.getParser().singleCharErrorRecovery(matcher);
    }

    public Rule visit(EmptyMatcher<V> matcher) {
        throw new IllegalStateException(); // EmptyMatchers should never cause a mismatch
    }

    public Rule visit(FirstOfMatcher<V> matcher) {
        return null;
    }

    public Rule visit(OneOrMoreMatcher<V> matcher) {
        return null;
    }

    public Rule visit(OptionalMatcher<V> matcher) {
        throw new IllegalStateException(); // OptionalMatchers should never cause a mismatch
    }

    public Rule visit(SequenceMatcher<V> matcher) {
        BaseParser<V> parser = context.getParser();
        return parser.firstOf(
                parser.singleCharErrorRecovery(matcher),
                parser.resynchronize(context)
        ).withoutNode().label("sequenceRecovery");
    }

    public Rule visit(TestMatcher<V> matcher) {
        return null;
    }

    public Rule visit(TestNotMatcher<V> matcher) {
        return null;
    }

    public Rule visit(ZeroOrMoreMatcher<V> matcher) {
        throw new IllegalStateException(); // ZeroOrMoreMatchers should never cause a mismatch
    }

}