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

import org.parboiled.MatcherContext;
import org.parboiled.common.Formatter;
import org.parboiled.common.Preconditions;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

public class ReportFirstParseErrorHandler<V> implements ParseErrorHandler<V> {

    private enum State {
        Parsing, Seeking, Reporting
    }

    private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private State state = State.Parsing;
    private MatcherContext<V> rootContext;
    private InputLocation errorLocation;
    private MatcherPath<V> lastMatch;

    public ReportFirstParseErrorHandler() {
        this(new DefaultInvalidInputErrorFormatter<V>());
    }

    public ReportFirstParseErrorHandler(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public void beforeParsingRun(MatcherContext<V> rootContext) {
        this.rootContext = rootContext;
        if (errorLocation == null) errorLocation = rootContext.getCurrentLocation();
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
                if (context.getCurrentLocation().index == errorLocation.index) {
                    // we are back at the location we previously marked as the error location
                    lastMatch = context.getPath();
                    state = State.Reporting;
                }
                break;
        }
    }

    public boolean handleMismatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                if (context == rootContext) {
                    if (errorLocation.index > 0) {
                        state = State.Seeking;
                    } else {
                        // mismatched the very first character
                        handleMismatchWhileReporting(context);
                    }
                }
                break;

            case Seeking:
                Preconditions.checkState(context != rootContext);
                break;

            case Reporting:
                handleMismatchWhileReporting(context);
                break;
        }
        return false; // never "overrule" a mismatch, since we don't recover
    }

    private void handleMismatchWhileReporting(MatcherContext<V> context) {
        if (context.getCurrentLocation().index == errorLocation.index) {
            failedMatchers.add(context.getPath());
        }
        if (context == rootContext) {
            createParseError();
        }
    }

    public boolean isRerunRequested(MatcherContext<V> context) {
        return state == State.Seeking;
    }

    public void createParseError() {
        rootContext.getParseErrors().add(
                new InvalidInputError<V>(errorLocation, lastMatch, failedMatchers, invalidInputErrorFormatter)
        );
    }

}
