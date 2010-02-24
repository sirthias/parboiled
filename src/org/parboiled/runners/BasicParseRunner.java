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

package org.parboiled.runners;

import org.jetbrains.annotations.NotNull;
import org.parboiled.*;
import org.parboiled.errors.ParseError;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParsingResult;

import java.util.ArrayList;
import java.util.List;

/**
 * The most trivial implementation of the {@link org.parboiled.ParseRunner} interface.
 * It does not report any parse errors nor recover from them. Therefore it never causes the parser to perform more
 * than one parsing run and is the faster way to determine whether a given input conforms to the rule grammar.
 *
 * @param <V>
 */
public class BasicParseRunner<V> implements ParseRunner<V> {

    protected final InputBuffer inputBuffer;
    protected final List<ParseError> parseErrors = new ArrayList<ParseError>();
    protected final Matcher<V> rootMatcher;
    protected InputLocation startLocation;
    protected MatcherContext<V> rootContext;
    protected boolean matched;

    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new BasicParseRunner<V>(rule, input).run();
    }

    @SuppressWarnings({"unchecked"})
    public BasicParseRunner(@NotNull Rule rule, @NotNull String input) {
        this.rootMatcher = (Matcher<V>) rule;
        this.inputBuffer = new InputBuffer(input);
        this.startLocation = new InputLocation(inputBuffer);
    }

    @SuppressWarnings({"unchecked"})
    public ParsingResult<V> run() {
        if (rootContext == null) {
            matched = runRootContext();
        }
        return new ParsingResult<V>(matched, rootContext.getNode(), parseErrors, inputBuffer,
                rootContext.getCurrentLocation().getRow() + 1);
    }

    protected boolean runRootContext() {
        return runRootContext(new Handler<V>());
    }

    protected boolean runRootContext(MatchHandler<V> handler) {
        createRootContext(handler);
        return handler.matchRoot(rootContext);
    }

    protected void createRootContext(MatchHandler<V> matchHandler) {
        rootContext = new MatcherContext<V>(inputBuffer, startLocation, parseErrors, matchHandler, rootMatcher);
    }

    public static final class Handler<V> implements MatchHandler<V> {

        public final boolean matchRoot(MatcherContext<V> rootContext) {
            return rootContext.runMatcher();
        }

        public final boolean match(MatcherContext<V> context) throws Throwable {
            return context.getMatcher().match(context);
        }

    }

}
