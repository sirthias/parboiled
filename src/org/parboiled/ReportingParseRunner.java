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

import org.jetbrains.annotations.NotNull;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.TestNotMatcher;
import org.parboiled.matchervisitors.IsSingleCharMatcherVisitor;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link org.parboiled.ParseRunner} that reports the first parse error if the input does not conform to the rule grammar.
 * It initiates at most one parsing rerun (in the case that the input is invalid) and is only a few percent slower
 * than the {@link BasicParseRunner} on valid input.
 *
 * @param <V>
 */
public class ReportingParseRunner<V> extends BasicParseRunner<V> {

    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new ReportingParseRunner<V>(rule, input).run();
    }

    public ReportingParseRunner(@NotNull Rule rule, @NotNull String input) {
        super(rule, input);
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    protected boolean runRootContext() {
        RecordingParseRunner.Handler<V> handler = new RecordingParseRunner.Handler<V>();
        if (runRootContext(handler)) {
            return true;
        }
        return runRootContext(new Handler<V>(handler.getErrorLocation()));
    }

    public static class Handler<V> implements MatchHandler<V> {
        private final IsSingleCharMatcherVisitor<V> isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor<V>();
        private final InputLocation errorLocation;
        private final MatchHandler<V> inner;
        private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
        private MatcherPath<V> lastMatch;
        private InvalidInputError<V> parseError;
        private boolean seeking;

        public Handler(@NotNull InputLocation errorLocation) {
            this(errorLocation, new BasicParseRunner.Handler<V>());
        }

        public Handler(@NotNull InputLocation errorLocation,
                       @NotNull MatchHandler<V> inner) {
            this.errorLocation = errorLocation;
            this.inner = inner;
        }

        public InvalidInputError<V> getParseError() {
            return parseError;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            failedMatchers.clear();
            seeking = errorLocation.getIndex() > 0;
            inner.matchRoot(rootContext);

            parseError = new InvalidInputError<V>(errorLocation, lastMatch, failedMatchers, null);
            rootContext.getParseErrors().add(parseError);
            return false;
        }

        public boolean match(MatcherContext<V> context) {
            boolean matched = inner.match(context);
            if (context.getCurrentLocation() == errorLocation) {
                if (matched && seeking) {
                    lastMatch = context.getPath();
                    seeking = false;
                }
                if (!matched && !seeking && context.getMatcher().accept(isSingleCharMatcherVisitor) &&
                        notTestNot(context)) {
                    failedMatchers.add(context.getPath());
                }
            }
            return matched;
        }

        private boolean notTestNot(MatcherContext<V> context) {
            return !(context.getMatcher() instanceof TestNotMatcher) &&
                    (context.getParent() == null || notTestNot(context.getParent()));
        }

    }

}

