/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import org.parboiled.Context;
import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.*;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.Position;

import static org.parboiled.common.Preconditions.checkArgNotNull;

/**
 * A {@link org.parboiled.parserunners.ParseRunner} implementation used for debugging purposes.
 * It exhibits the same behavior as the {@link ReportingParseRunner} but collects debugging information as to which
 * rules did match and which didn't.
 */
public class TracingParseRunner<V> extends ReportingParseRunner<V> implements MatchHandler {
    private Predicate<Tuple2<Context<?>, Boolean>> filter;
    private Sink<String> log;
    private MatcherPath lastPath;
    private int line;

    /**
     * Creates a new TracingParseRunner instance without filter and a console log for the given rule.
     *
     * @param rule the parser rule
     */
    public TracingParseRunner(Rule rule) {
        super(rule);
    }

    /**
     * Attaches the given filter to this TracingParseRunner instance.
     * The given filter is used to select the matchers to print tracing statements for.
     * NOTE: The given filter must be of type Predicate<Tuple2<Context<?>, Boolean>>. The reason this type is not
     * directly specified in the constructors signature is that this would make predicate expressions using the
     * {@link Predicates} operations and the predefined predicate constructors in {@link org.parboiled.support.Filters}
     * much more cumbersome to write (due to Java limited type parameters inference logic you would have to explicitly
     * state the type parameters in many places).
     *
     * @param filter the matcher filter selecting the matchers to print tracing statements for. Must be of type
     *               Predicate<Tuple2<Context<?>, Boolean>>.
     * @return this instance
     */
    @SuppressWarnings( {"unchecked"})
    public TracingParseRunner<V> withFilter(Predicate<?> filter) {
        this.filter = (Predicate<Tuple2<Context<?>, Boolean>>) checkArgNotNull(filter, "filter");
        return this;
    }

    public Predicate<Tuple2<Context<?>, Boolean>> getFilter() {
        if (filter == null) {
            withFilter(Predicates.alwaysTrue());
        }
        return filter;
    }

    /**
     * Attaches the given log to this TracingParseRunner instance.
     *
     * @param log the log to use
     * @return this instance
     */
    public TracingParseRunner<V> withLog(Sink<String> log) {
        this.log = log;
        return this;
    }

    public Sink<String> getLog() {
        if (log == null) {
            withLog(new ConsoleSink());
        }
        return log;
    }

    @Override
    protected ParsingResult<V> runBasicMatch(InputBuffer inputBuffer) {
        getLog().receive("Starting new parsing run\n");
        lastPath = null;

        MatcherContext<V> rootContext = createRootContext(inputBuffer, this, true);
        boolean matched = rootContext.runMatcher();
        return createParsingResult(matched, rootContext);
    }

    @SuppressWarnings( {"unchecked"})
    public boolean match(MatcherContext<?> context) {
        Matcher matcher = context.getMatcher();
        boolean matched = matcher.match(context);
        if (getFilter().apply(new Tuple2<Context<?>, Boolean>(context, matched))) {
            line++;
            print(context, matched); // set line-dependent breakpoint here
        }
        return matched;
    }

    private void print(MatcherContext<?> context, boolean matched) {
        Position pos = context.getInputBuffer().getPosition(context.getCurrentIndex());
        MatcherPath path = context.getPath();
        MatcherPath prefix = lastPath != null ? path.commonPrefix(lastPath) : null;
        if (prefix != null && prefix.length() > 1) getLog().receive("..(" + (prefix.length() - 1) + ")../");
        getLog().receive(path.toString(prefix != null ? prefix.parent : null));
        String line = context.getInputBuffer().extractLine(pos.line);
        getLog().receive(", " + (matched ? "matched" : "failed") + ", cursor at " + pos.line + ':' + pos.column +
                " after \"" + line.substring(0, Math.min(line.length(), pos.column - 1)) + "\"\n");
        lastPath = path;
    }
}

