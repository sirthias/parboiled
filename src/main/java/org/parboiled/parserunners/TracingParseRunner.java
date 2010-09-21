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
import org.parboiled.matchers.Matcher;
import org.parboiled.support.InputBuffer;
import org.parboiled.trees.Filter;
import org.parboiled.trees.Filters;
import org.parboiled.trees.Printability;

/**
 * A {@link org.parboiled.parserunners.ParseRunner} implementation used for debugging purposes.
 * It exhibits the same behavior as the {@link ReportingParseRunner} but collects debugging information as to which
 * rules did match and which didn't.
 */
public class TracingParseRunner<V> extends BasicParseRunner<V> {

    private final StringBuilder log = new StringBuilder();
    private final Filter<Matcher> filter;

    /**
     * Creates a new TracingParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public TracingParseRunner(@NotNull Rule rule) {
        this(rule, Filters.<Matcher>all());
    }

    /**
     * Creates a new TracingParseRunner instance for the given rule.
     * If the given filter is not null it will be used to select the matchers to print tracing statements for.
     * The Printab
     *
     * @param rule   the parser rule
     * @param filter the matcher filter selecting the matchers to print tracing statements for.
     */
    public TracingParseRunner(@NotNull Rule rule, @NotNull Filter<Matcher> filter) {
        super(rule);
        this.filter = filter;
    }

    public String getLog() {
        return log.toString();
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    protected boolean runRootContext() {
        // run a basic match
        if (runRootContext(new Handler(rootMatcher, log, filter), true)) {
            return true;
        }

        // ok, we have a parse error, so run again without fast string matching and with our recording handler
        RecordingParseRunner.Handler recordingHandler = new RecordingParseRunner.Handler();
        if (runRootContext(recordingHandler, false)) {
            throw new IllegalStateException(); // we failed before so we should really be failing again
        }

        // finally perform a third, reporting run (now that we know the error location)
        ReportingParseRunner.Handler reportingHandler = new ReportingParseRunner.Handler(
                recordingHandler.getErrorIndex());
        if (runRootContext(reportingHandler, false)) {
            throw new IllegalStateException(); // we failed before so we should really be failing again
        }

        return false;
    }

    /**
     * A {@link org.parboiled.MatchHandler} implementation that reports the {@link org.parboiled.errors.InvalidInputError} at a given error index.
     * For the actual matching this handler relies on another, inner {@link org.parboiled.MatchHandler} instance it delegates to.
     */
    public static class Handler implements MatchHandler {
        private final Matcher rootMatcher;
        private final StringBuilder log;
        private final Filter<Matcher> filter;
        private boolean dontPrint;

        public Handler(Matcher rootMatcher, StringBuilder log, Filter<Matcher> filter) {
            this.rootMatcher = rootMatcher;
            this.log = log;
            this.filter = filter;
        }

        public boolean matchRoot(MatcherContext<?> rootContext) {
            log.setLength(0);
            log.append("Starting match on rule '").append(rootMatcher).append("'").append('\n');
            return rootContext.runMatcher();
        }

        public boolean match(MatcherContext<?> context) {
            Matcher matcher = context.getMatcher();
            if (dontPrint) {
                return matcher.match(context); 
            }
            boolean matched;
            switch (filter.apply(matcher)) {
                case PrintAndDescend:
                    matched = matcher.match(context);
                    print(context, matched);
                    break;
                case Print:
                    boolean oldDontPrint = dontPrint;
                    dontPrint = true;
                    matched = matcher.match(context);
                    print(context, matched);
                    dontPrint = oldDontPrint;
                    break;
                case Descend:
                    matched = matcher.match(context);
                    break;
                case Skip:
                    oldDontPrint = dontPrint;
                    dontPrint = true;
                    matched = matcher.match(context);
                    dontPrint = oldDontPrint;
                    break;
                default:
                    throw new IllegalStateException();
            }
            return matched;
        }

        private void print(MatcherContext<?> context, boolean matched) {
            InputBuffer.Position pos = context.getInputBuffer().getPosition(context.getCurrentIndex());
            log.append(context.getPath()).append(": ")
                    .append(matched ? "matched" : "failed")
                    .append(", cursor is at line ")
                    .append(pos.line)
                    .append(", col ")
                    .append(pos.column)
                    .append(": \"")
                    .append(context.getInputBuffer().extractLine(pos.line).substring(0, pos.column - 1))
                    .append("\"\n");
        }

    }

}

