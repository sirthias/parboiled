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

package org.parboiled.errorhandling;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.common.Formatter;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

/**
 * A {@link ParseErrorHandler} that reports the first parse error if the input does not conform to the rule grammar.
 * It initiates at most one parsing rerun (in the case that the input is invalid) and is only a few percent slower
 * than the {@link BasicParseErrorHandler} on valid input. It is therefore the default {@link ParseErrorHandler} used by
 * {@link org.parboiled.BaseParser#parse(org.parboiled.Rule, String)}.
 *
 * @param <V>
 */
public class ReportFirstParseErrorHandler<V> implements ParseErrorHandler<V> {

    private enum State {
        Parsing, Seeking, Reporting
    }

    private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private State state;
    private InputLocation errorLocation;
    private MatcherPath<V> lastMatch;

    public ReportFirstParseErrorHandler() {
        this(new DefaultInvalidInputErrorFormatter<V>());
    }

    public ReportFirstParseErrorHandler(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public boolean matchRoot(@NotNull Supplier<MatcherContext<V>> rootContextProvider) {
        failedMatchers.clear();
        MatcherContext<V> rootContext = rootContextProvider.get();
        errorLocation = rootContext.getCurrentLocation();
        state = State.Parsing;

        if (rootContext.runMatcher()) {
            return true;
        }

        state = errorLocation.index == 0 ? State.Reporting : State.Seeking;

        rootContext = rootContextProvider.get();
        rootContext.runMatcher();
        Preconditions.checkState(state != State.Seeking);

        rootContext.getParseErrors().add(
                new InvalidInputError<V>(errorLocation, lastMatch, failedMatchers, invalidInputErrorFormatter)
        );
        return false;
    }

    public boolean match(MatcherContext<V> context) throws Throwable {
        if (context.getMatcher().match(context)) {
            handleMatch(context);
            return true;
        }

        if (state == State.Reporting && context.getCurrentLocation() == errorLocation) {
            failedMatchers.add(context.getPath());
        }
        return false;
    }

    public void handleMatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                if (errorLocation.index < context.getCurrentLocation().index) {
                    // record the last successful match, the current location might be a parse error
                    errorLocation = context.getCurrentLocation();
                }
                break;

            case Seeking:
                if (context.getCurrentLocation() == errorLocation) {
                    // we are back at the location we previously marked as the error location
                    lastMatch = context.getPath();
                    state = State.Reporting;
                }
                break;
        }
    }

}
