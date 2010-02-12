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

import org.parboiled.BaseParser;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.matchers.*;

public class RecoveringParseErrorHandler<V> extends DefaultMatcherVisitor<V, Rule> implements ParseErrorHandler<V> {

    private MatcherContext<V> context;

    public void beforeParsingRun(MatcherContext<V> rootContext) {

    }

    public void handleMatch(MatcherContext<V> context) {

    }

    public boolean handleMismatch(MatcherContext<V> context) {
        return false;
    }

    public boolean isRerunRequested(MatcherContext<V> rootContext) {
        return false;
    }

    @SuppressWarnings({"unchecked"})
    public boolean handleParseError(MatcherContext<V> context) {
        Matcher<V> failedMatcher = context.getMatcher();
        Matcher<V> recoveryRule = failedMatcher.getRecoveryMatcher();

        if (recoveryRule == null) {
            this.context = context;
            recoveryRule = (Matcher<V>) failedMatcher.accept(this);
        }
        return recoveryRule != null && context.getSubContext(recoveryRule).runMatcher();
    }

    @Override
    public Rule visit(SequenceMatcher<V> matcher) {
        BaseParser<V> parser = context.getParser();
        return parser.firstOf(
                parser.singleCharErrorRecovery(matcher),
                parser.resynchronize(context)
        ).withoutNode();
    }

    @Override
    public Rule visit(EmptyMatcher<V> matcher) {
        throw new IllegalStateException(); // EmptyMatchers should never trigger a parse error
    }

    @Override
    public Rule visit(OptionalMatcher<V> matcher) {
        throw new IllegalStateException(); // OptionalMatchers should never trigger a parse error
    }

    @Override
    public Rule visit(ZeroOrMoreMatcher<V> matcher) {
        throw new IllegalStateException(); // ZeroOrMoreMatchers should never trigger a parse error
    }

    @Override
    public Rule defaultValue(AbstractMatcher<V> matcher) {
        return context.getParser().singleCharErrorRecovery(matcher);
    }

}