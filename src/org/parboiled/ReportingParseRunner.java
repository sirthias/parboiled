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
import org.parboiled.support.IsSingleCharMatcherVisitor;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ParseRunner} implementation that properly reports the first {@link InvalidInputError} if the input
 * does not conform to the rule grammar.
 * It performs exactly as the {@link BasicParseRunner} on valid input, however, on invalid input two more parsing
 * runs are initiated: one for recording the first parse error and one for collecting the error report information.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class ReportingParseRunner<V> extends BasicParseRunner<V> {

    /**
     * Create a new ReportingParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run()} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     */
    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new ReportingParseRunner<V>(rule, input).run();
    }

    /**
     * Creates a new ReportingParseRunner instance for the given rule and input text.
     *
     * @param rule  the parser rule
     * @param input the input text
     */
    public ReportingParseRunner(@NotNull Rule rule, @NotNull String input) {
        super(rule, input);
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    protected boolean runRootContext() {
        MatchHandler<V> handler = new BasicParseRunner.Handler<V>();
        if (runRootContext(handler)) {
            return true;
        }
        RecordingParseRunner.Handler<V> recordingHandler = new RecordingParseRunner.Handler<V>();
        if (runRootContext(recordingHandler)) {
            throw new IllegalStateException(); // we failed before so we must fail again
        }
        return runRootContext(new Handler<V>(recordingHandler.getErrorIndex()));
    }

    /**
     * A {@link MatchHandler} implementation that reports the {@link InvalidInputError} at a given error index.
     * For the actual matching this handler relies on another, inner {@link MatchHandler} instance it delegates to.
     *
     * @param <V> the type of the value field of a parse tree node
     */
    public static class Handler<V> implements MatchHandler<V> {
        private final IsSingleCharMatcherVisitor<V> isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor<V>();
        private final int errorIndex;
        private final MatchHandler<V> inner;
        private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
        private MatcherPath<V> lastMatch;
        private InvalidInputError<V> parseError;
        private boolean seeking;

        /**
         * Create a new handler that can report the {@link InvalidInputError} at the given error index.
         * It relies on a new {@link BasicParseRunner.Handler} instance for the actual matching.
         *
         * @param errorIndex the InputLocation of the error to be reported
         */
        public Handler(int errorIndex) {
            this(errorIndex, new BasicParseRunner.Handler<V>());
        }

        /**
         * Create a new handler that can report the {@link InvalidInputError} at the given error index.
         * It relies on the given {@link MatchHandler} instance for the actual matching.
         *
         * @param errorIndex the InputLocation of the error to be reported
         * @param inner         the inner MatchHandler to use
         */
        public Handler(int errorIndex, @NotNull MatchHandler<V> inner) {
            this.errorIndex = errorIndex;
            this.inner = inner;
        }

        /**
         * Returns the {@link InvalidInputError} instance that was created during the reporting run.
         *
         * @return the InvalidInputError
         */
        public InvalidInputError<V> getParseError() {
            return parseError;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            failedMatchers.clear();
            seeking = errorIndex > 0;
            inner.matchRoot(rootContext);

            parseError =
                    new InvalidInputError<V>(rootContext.getInputBuffer(), errorIndex, lastMatch, failedMatchers, null);
            rootContext.getParseErrors().add(parseError);
            return false;
        }

        public boolean match(MatcherContext<V> context) {
            boolean matched = inner.match(context);
            if (context.getCurrentIndex() == errorIndex) {
                if (matched && seeking) {
                    lastMatch = context.getPath();
                    seeking = false;
                }
                if (!matched && !seeking && context.getMatcher().accept(isSingleCharMatcherVisitor)) {
                    failedMatchers.add(context.getPath());
                }
            }
            return matched;
        }

    }

}

