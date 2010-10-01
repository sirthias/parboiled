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

package org.parboiled.parserunners;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchervisitors.IsSingleCharMatcherVisitor;
import org.parboiled.support.*;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ParseRunner} implementation that properly reports the first {@link InvalidInputError} if the input
 * does not conform to the rule grammar.
 * It performs exactly as the {@link BasicParseRunner} on valid input, however, on invalid input two more parsing
 * runs are initiated: one for recording the first parse error and one for collecting the error report information.
 */
public class ReportingParseRunner<V> extends BasicParseRunner<V> {

    /**
     * Create a new ReportingParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run(String)} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     */
    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new ReportingParseRunner<V>(rule).run(input);
    }

    /**
     * Creates a new ReportingParseRunner instance for the given rule.
     *
     * @param rule  the parser rule
     */
    public ReportingParseRunner(@NotNull Rule rule) {
        super(rule);
    }

    /**
     * Creates a new ReportingParseRunner instance for the given rule using the given ValueStack instance.
     *
     * @param rule  the parser rule
     * @param valueStack  the value stack
     */
    public ReportingParseRunner(@NotNull Rule rule, @NotNull ValueStack<V> valueStack) {
        super(rule, valueStack);
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    protected boolean runRootContext() {
        // run a basic match
        if (super.runRootContext()) {
            return true;
        }

        // ok, we have a parse error, so run again without fast string matching and with our recording handler
        RecordingParseRunner.Handler recordingHandler = new RecordingParseRunner.Handler();
        if (runRootContext(recordingHandler, false)) {
            throw new IllegalStateException(); // we failed before so we should really be failing again
        }

        // finally perform a third, reporting run (now that we know the error location)
        Handler reportingHandler = new Handler(recordingHandler.getErrorIndex());
        if (runRootContext(reportingHandler, false)) {
            throw new IllegalStateException(); // we failed before so we should really be failing again
        }

        return false;
    }

    /**
     * A {@link org.parboiled.MatchHandler} implementation that reports the {@link InvalidInputError} at a given error index.
     * For the actual matching this handler relies on another, inner {@link org.parboiled.MatchHandler} instance it delegates to.
     */
    public static class Handler implements MatchHandler {
        private final IsSingleCharMatcherVisitor isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor();
        private final int errorIndex;
        private final MatchHandler inner;
        private final List<MatcherPath> failedMatchers = new ArrayList<MatcherPath>();
        private InvalidInputError parseError;
        private boolean seeking;

        /**
         * Create a new handler that can report the {@link InvalidInputError} at the given error index.
         * It relies on a new {@link BasicParseRunner.Handler} instance for the actual matching.
         *
         * @param errorIndex the InputLocation of the error to be reported
         */
        public Handler(int errorIndex) {
            this(errorIndex, new BasicParseRunner.Handler());
        }

        /**
         * Create a new handler that can report the {@link InvalidInputError} at the given error index.
         * It relies on the given {@link MatchHandler} instance for the actual matching.
         *
         * @param errorIndex the InputLocation of the error to be reported
         * @param inner      the inner MatchHandler to use
         */
        public Handler(int errorIndex, @NotNull MatchHandler inner) {
            this.errorIndex = errorIndex;
            this.inner = inner;
        }

        /**
         * Returns the {@link InvalidInputError} instance that was created during the reporting run.
         *
         * @return the InvalidInputError
         */
        public InvalidInputError getParseError() {
            return parseError;
        }

        public boolean matchRoot(MatcherContext<?> rootContext) {
            failedMatchers.clear();
            seeking = errorIndex > 0;
            inner.matchRoot(rootContext);

            parseError = new InvalidInputError(rootContext.getInputBuffer(), errorIndex, failedMatchers, null);
            rootContext.getParseErrors().add(parseError);
            return false;
        }

        public boolean match(MatcherContext<?> context) {
            boolean matched = inner.match(context);
            if (context.getCurrentIndex() == errorIndex) {
                if (matched && seeking) {
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

