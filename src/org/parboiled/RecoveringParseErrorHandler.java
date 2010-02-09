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

public class RecoveringParseErrorHandler<V> extends EmptyMatcherVisitor<V> implements ParseErrorHandler<V> {

    private final ReportingParseErrorHandler<V> reportingHandler = new ReportingParseErrorHandler<V>();

    private MatcherContext<V> context;
    private Matcher<V> recoveryRule;

    public boolean handleParseError(MatcherContext<V> context) {
        this.context = context;
        reportingHandler.handleParseError(context);
        Matcher<V> failedMatcher = context.getMatcher();
        recoveryRule = failedMatcher.getRecoveryMatcher();

        if (recoveryRule == null) {
            failedMatcher.accept(this);
        }
        return context.getSubContext(recoveryRule).runMatcher();
    }

    @Override
    public void visit(AnyCharMatcher<V> matcher) {
        visitSingleCharMatcher(matcher);
    }

    @Override
    public void visit(CharMatcher<V> matcher) {
        visitSingleCharMatcher(matcher);
    }

    @Override
    public void visit(CharIgnoreCaseMatcher<V> matcher) {
        visitSingleCharMatcher(matcher);
    }

    @Override
    public void visit(CharRangeMatcher<V> matcher) {
        visitSingleCharMatcher(matcher);
    }

    @SuppressWarnings({"unchecked", "UnusedDeclaration"})
    public void visitSingleCharMatcher(Matcher<V> matcher) {
        recoveryRule = (Matcher<V>) context.getParser().defaultSingleCharRecoveryRule(context);
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public void visit(SequenceMatcher<V> matcher) {
        recoveryRule = (Matcher<V>) context.getParser().defaultSequenceRecoveryRule(context);
    }

}