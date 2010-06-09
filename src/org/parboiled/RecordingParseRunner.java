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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.TestNotMatcher;
import org.parboiled.support.ParsingResult;

/**
 * A {@link ParseRunner} implementation that records the location of the first {@link InvalidInputError} found,
 * without actually creating a {@link InvalidInputError} instance and adding it to the list of ParseErrors.
 * It never causes the parser to perform more than one parsing run and is rarely used directly. Instead its functionality
 * is relied upon by the {@link ReportingParseRunner} and {@link RecoveringParseRunner} classes.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class RecordingParseRunner<V> extends BasicParseRunner<V> {

    private Handler<V> handler;

    /**
     * Create a new RecordingParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run()} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     */
    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new RecordingParseRunner<V>(rule, input).run();
    }

    /**
     * Creates a new RecordingParseRunner instance for the given rule and input text.
     *
     * @param rule  the parser rule
     * @param input the input text
     */
    public RecordingParseRunner(@NotNull Rule rule, @NotNull String input) {
        super(rule, input);
    }

    @Override
    protected boolean runRootContext() {
        handler = new Handler<V>();
        return runRootContext(handler);
    }

    /**
     * Returns the index of the first {@link InvalidInputError} in the input text.
     * Must not be called before the {@link #run()} has been called.
     *
     * @return the index of the first parse error or -1, if the input is error free.
     */
    public int getErrorIndex() {
        Preconditions.checkState(handler != null, "getErrorIndex() called before run()");
        return handler.getErrorIndex();
    }

    /**
     * A {@link MatchHandler} implementation keeping track of the furthest match in the current input buffer,
     * and therefore the first location corresponding to an {@link InvalidInputError}.
     * For the actual matching this handler relies on another, inner {@link MatchHandler} instance it delegates to.
     *
     * @param <V> the type of the value field of a parse tree node
     */
    public static class Handler<V> implements MatchHandler<V> {
        private int errorIndex;
        private final MatchHandler<V> inner;

        /**
         * Creates a new Handler which delegates to a {@link BasicParseRunner.Handler} instance.
         */
        public Handler() {
            this(new BasicParseRunner.Handler<V>());
        }

        /**
         * Creates a new Handler which delegates to the given MatchHandler instance.
         *
         * @param inner the inner instance to delegate to
         */
        public Handler(@NotNull MatchHandler<V> inner) {
            this.inner = inner;
        }

        /**
         * Returns the index of the first {@link InvalidInputError} in the input text.
         *
         * @return the index of the first parse error or -1, if the input is error free.
         */
        public int getErrorIndex() {
            return errorIndex;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            errorIndex = rootContext.getCurrentIndex();
            if (inner.matchRoot(rootContext)) {
                errorIndex = -1;
                return true;
            }
            return false;
        }

        public boolean match(MatcherContext<V> context) {
            if (inner.match(context)) {
                if (errorIndex < context.getCurrentIndex() && notTestNot(context)) {
                    errorIndex = context.getCurrentIndex();
                }
                return true;
            }
            return false;
        }

        private boolean notTestNot(MatcherContext<V> context) {
            return !(context.getMatcher() instanceof TestNotMatcher) &&
                    (context.getParent() == null || notTestNot(context.getParent()));
        }
    }

}