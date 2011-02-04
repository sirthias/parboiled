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

import static org.parboiled.common.Preconditions.*;
import org.parboiled.Context;
import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.common.*;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.Position;

/**
 * A {@link org.parboiled.parserunners.ParseRunner} implementation used for debugging purposes.
 * It exhibits the same behavior as the {@link ReportingParseRunner} but collects debugging information as to which
 * rules did match and which didn't.
 */
public class TracingParseRunner<V> extends BasicParseRunner<V> {
    public final Predicate<Tuple2<Context<?>, Boolean>> filter;
    public final Sink<String> log;
    
    /**
     * Creates a new TracingParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public TracingParseRunner(Rule rule) {
        this(rule, Predicates.alwaysTrue());
    }

    /**
     * Creates a new TracingParseRunner instance for the given rule.
     * The given filter is used to select the matchers to print tracing statements for.
     * NOTE: The given filter must be of type Predicate<Tuple2<Context<?>, Boolean>>. The reason this type is not
     * directly specified in the constructors signature is that this would make predicate expressions using the
     * {@link Predicates} operations and the predefined predicate constructors in {@link org.parboiled.support.Filters}
     * much more cumbersome to write (due to Java limited type parameters inference logic you would have to explicitly
     * state the type parameters in many places).
     *
     * @param rule   the parser rule
     * @param filter the matcher filter selecting the matchers to print tracing statements for. Must be of type
     *               Predicate<Tuple2<Context<?>, Boolean>>.
     */
    @SuppressWarnings({"unchecked"})
    public TracingParseRunner(Rule rule, Predicate<?> filter) {
        this(rule, filter, new ConsoleSink());
    }
    
    @SuppressWarnings( {"unchecked"})
    public TracingParseRunner(Rule rule, Predicate<?> filter, Sink<String> log) {
        super(checkArgNotNull(rule, "rule"));
        this.filter = (Predicate<Tuple2<Context<?>, Boolean>>) checkArgNotNull(filter, "filter");
        this.log = log;
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    @Override
    protected boolean runRootContext() {
        // run a basic match
        if (runRootContext(new Handler(filter, log), true)) {
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
        private final Predicate<Tuple2<Context<?>, Boolean>> filter;
        private final Sink<String> log;
        private MatcherPath lastPath;

        public Handler(Predicate<Tuple2<Context<?>, Boolean>> filter, Sink<String> log) {
            this.filter = filter;
            this.log = log;
        }

        public boolean matchRoot(MatcherContext<?> rootContext) {
            log.receive("Starting new parsing run\n");
            lastPath = null;
            return rootContext.runMatcher();
        }

        @SuppressWarnings({"unchecked"})
        public boolean match(MatcherContext<?> context) {
            Matcher matcher = context.getMatcher();
            boolean matched = matcher.match(context);
            if (filter.apply(new Tuple2<Context<?>, Boolean>(context, matched))) {
                print(context, matched);
            }
            return matched;
        }

        private void print(MatcherContext<?> context, boolean matched) {
            Position pos = context.getInputBuffer().getPosition(context.getCurrentIndex());
            MatcherPath path = context.getPath();
            MatcherPath prefix = lastPath != null ? path.commonPrefix(lastPath) : null;
            if (prefix != null && prefix.length() > 1) log.receive("..(" + (prefix.length() - 1) + ")../");
            log.receive(path.toString(prefix != null ? prefix.parent : null));
            String line = context.getInputBuffer().extractLine(pos.line);
            log.receive(", " + (matched ? "matched" : "failed") + ", cursor at " + pos.line + ':' + pos.column +
                    " after \"" + line.substring(0, Math.min(line.length(), pos.column - 1)) +  "\"\n");
            lastPath = path;
        }
    }
}

