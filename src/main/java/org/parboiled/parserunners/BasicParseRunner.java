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
import org.parboiled.buffers.DefaultInputBuffer;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.errors.ParseError;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The most basic of all {@link ParseRunner} implementations. It runs a rule against a given input text and builds a
 * corresponding {@link ParsingResult} instance. However, it does not report any parse errors nor recover from them.
 * Instead it simply marks the ParsingResult as "unmatched" if the input is not valid with regard to the rule grammar.
 * It never causes the parser to perform more than one parsing run and is the fastest way to determine
 * whether a given input conforms to the rule grammar.
 */
public class BasicParseRunner<V> implements ParseRunner<V> {

    protected final List<ParseError> parseErrors = new ArrayList<ParseError>();
    protected final ValueStack<V> valueStack;
    protected final Object initialValueStackSnapshot;
    public final Matcher rootMatcher;
    protected InputBuffer inputBuffer;
    protected MatcherContext<V> rootContext;
    protected boolean matched;

    /**
     * Create a new BasicParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run(String)} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     */
    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new BasicParseRunner<V>(rule).run(input);
    }

    /**
     * Creates a new BasicParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public BasicParseRunner(@NotNull Rule rule) {
        this(rule, new DefaultValueStack<V>());
    }

    /**
     * Creates a new BasicParseRunner instance for the given rule using the given ValueStack instance.
     *
     * @param rule       the parser rule
     * @param valueStack the value stack
     */
    public BasicParseRunner(@NotNull Rule rule, @NotNull ValueStack<V> valueStack) {
        this.rootMatcher = (Matcher) rule;
        this.valueStack = valueStack;
        this.initialValueStackSnapshot = valueStack.takeSnapshot();
    }

    public ParsingResult<V> run(@NotNull String input) {
        return run(input.toCharArray());
    }

    public ParsingResult<V> run(@NotNull char[] input) {
        return run(new DefaultInputBuffer(input));
    }

    public ParsingResult<V> run(@NotNull InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
        matched = runRootContext();
        return new ParsingResult<V>(matched, rootContext.getNode(), rootContext.getValueStack(), parseErrors,
                this.inputBuffer);
    }

    protected boolean runRootContext() {
        return runRootContext(new Handler(), true);
    }

    protected boolean runRootContext(MatchHandler handler, boolean fastStringMatching) {
        valueStack.restoreSnapshot(initialValueStackSnapshot);
        rootContext = new MatcherContext<V>(inputBuffer, valueStack, parseErrors, handler, rootMatcher,
                fastStringMatching);
        return handler.matchRoot(rootContext);
    }

    /**
     * The most trivial {@link MatchHandler} implementation.
     * Simply delegates to the given Context for performing the match, without any additional logic.
     */
    public static final class Handler implements MatchHandler {

        public boolean matchRoot(MatcherContext<?> rootContext) {
            return rootContext.runMatcher();
        }

        public boolean match(MatcherContext<?> context) {
            return context.getMatcher().match(context);
        }

    }

}
