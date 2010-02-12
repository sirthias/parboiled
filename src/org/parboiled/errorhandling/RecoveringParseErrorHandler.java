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
import org.parboiled.Rule;
import org.parboiled.ContextAware;
import org.parboiled.common.Formatter;
import org.parboiled.common.Preconditions;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.MatcherVisitor;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

public class RecoveringParseErrorHandler<V> implements ParseErrorHandler<V> {

    private enum State {
        Parsing, SeekingToReport, Reporting, SeekingToRecover, Recovering, InRecovery
    }

    private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private final MatcherVisitor<V, Rule> defaultRecoveryVisitor;
    private State state = State.Parsing;
    private MatcherContext<V> rootContext;
    private InputLocation errorLocation;
    private MatcherPath<V> lastMatch;

    public RecoveringParseErrorHandler() {
        this(new DefaultRecoveryRuleVisitor<V>(), new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringParseErrorHandler(MatcherVisitor<V, Rule> defaultRecoveryVisitor) {
        this(defaultRecoveryVisitor, new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringParseErrorHandler(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this(new DefaultRecoveryRuleVisitor<V>(), invalidInputErrorFormatter);
    }

    public RecoveringParseErrorHandler(@NotNull MatcherVisitor<V, Rule> defaultRecoveryVisitor,
                                       @NotNull Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.defaultRecoveryVisitor = defaultRecoveryVisitor;
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

            case SeekingToReport:
                if (context.getCurrentLocation().index == errorLocation.index) {
                    // we are back at the location we previously marked as the error location
                    lastMatch = context.getPath();
                    state = State.Reporting;
                }
                break;

            case SeekingToRecover:
                if (context.getCurrentLocation().index == errorLocation.index) {
                    // we are back at the location we previously marked as the error location
                    state = State.Recovering;
                }
                break;
        }
    }

    public boolean handleMismatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                if (context == rootContext) {
                    if (errorLocation.index > 0) {
                        state = State.SeekingToReport;
                    } else {
                        // we mismatched the very first character, report it
                        return handleMismatchWhileReporting(context);
                    }
                }
                break;

            case SeekingToReport:
            case SeekingToRecover:
                Preconditions.checkState(context != rootContext);
                break;

            case Reporting:
                return handleMismatchWhileReporting(context);

            case Recovering:
                return handleMismatchWhileRecovering(context);
        }
        return false;
    }

    private boolean handleMismatchWhileReporting(MatcherContext<V> context) {
        if (context.getCurrentLocation().index == errorLocation.index) {
            failedMatchers.add(context.getPath());
        }
        if (context == rootContext) {
            createParseError();
            if (errorLocation.index > 0) {
                state = State.SeekingToRecover;
            } else {
                // we mismatched the very first character, recover from it
                return handleMismatchWhileRecovering(context);
            }
        }
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private boolean handleMismatchWhileRecovering(MatcherContext<V> context) {
        Matcher<V> failedMatcher = context.getMatcher();
        Matcher<V> recoveryRule = failedMatcher.getRecoveryMatcher();
        if (recoveryRule == null) {
            if (defaultRecoveryVisitor instanceof ContextAware) {
                ((ContextAware)defaultRecoveryVisitor).setContext(context);
            }
            recoveryRule = (Matcher<V>) failedMatcher.accept(defaultRecoveryVisitor);
        }

        state = State.InRecovery;
        boolean recovered = recoveryRule != null && context.getSubContext(recoveryRule).runMatcher();
        state = recovered ? State.Parsing : State.Recovering;

        return recovered;
    }

    public boolean isRerunRequested(MatcherContext<V> context) {
        return state != State.Parsing;
    }

    public void createParseError() {
        rootContext.getParseErrors().add(
                new InvalidInputError<V>(errorLocation, lastMatch, failedMatchers, invalidInputErrorFormatter)
        );
    }

}