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
import org.parboiled.errors.ParseError;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.DefaultInputBuffer;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.ParsingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The most basic of all {@link ParseRunner} implementations. It runs a rule against a given input text and builds a
 * corresponding {@link ParsingResult} instance. However, it does not report any parse errors nor recover from them.
 * Instead it simply marks the ParsingResult as "unmatched" if the input is not valid with regard to the rule grammar.
 * It never causes the parser to perform more than one parsing run and is the fastest way to determine
 * whether a given input conforms to the rule grammar.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class BasicParseRunner<V> implements ParseRunner<V> {

    protected final List<ParseError> parseErrors = new ArrayList<ParseError>();
    protected final Matcher<V> rootMatcher;
    protected InputBuffer inputBuffer;
    protected MatcherContext<V> rootContext;
    protected boolean matched;

    /**
     * Create a new BasicParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run()} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     */
    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new BasicParseRunner<V>(rule, input).run();
    }

    /**
     * Creates a new BasicParseRunner instance for the given rule and input text.
     *
     * @param rule  the parser rule
     * @param input the input text
     */
    @SuppressWarnings({"unchecked"})
    public BasicParseRunner(@NotNull Rule rule, @NotNull String input) {
        this.rootMatcher = (Matcher<V>) rule;
        this.inputBuffer = new DefaultInputBuffer(input);
    }

    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> run() {
        if (rootContext == null) {
            matched = runRootContext();
        }
        return new ParsingResult<V>(matched, rootContext.getNode(), parseErrors, inputBuffer);
    }

    protected boolean runRootContext() {
        return runRootContext(new Handler<V>(), true);
    }

    protected boolean runRootContext(MatchHandler<V> handler, boolean fastStringMatching) {
        rootContext = new MatcherContext<V>(inputBuffer, parseErrors, handler, rootMatcher, fastStringMatching);
        return handler.matchRoot(rootContext);
    }

    /**
     * The most trivial {@link MatchHandler} implementation.
     * Simply delegates to the given Context for performing the match, without any additional logic.
     *
     * @param <V> the type of the value field of a parse tree node
     */
    public static final class Handler<V> implements MatchHandler<V> {

        public boolean matchRoot(MatcherContext<V> rootContext) {
            return rootContext.runMatcher();
        }

        public boolean match(MatcherContext<V> context) {
            return context.getMatcher().match(context);
        }

    }

}
