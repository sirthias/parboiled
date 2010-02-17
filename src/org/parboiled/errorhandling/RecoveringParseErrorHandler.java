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
import org.parboiled.BaseParser;
import org.parboiled.MatcherContext;
import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.common.Formatter;
import org.parboiled.common.Preconditions;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.TestMatcher;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link ParseErrorHandler} that tries to recover from parse errors and is therefore capable of reporting all
 * errors found in the input. Since it needs to performs several parsing reruns in order to be able to report and
 * recover from parse errors it is considerable slower than the {@link NopParseErrorHandler} and the
 * {@link ReportFirstParseErrorHandler} on invalid input.
 * It initiates at most one parsing rerun (in the case that the input is invalid) and is only a few percent slower
 * than the {@link NopParseErrorHandler} on valid input. On valid input it performs about the same as the
 * {@link ReportFirstParseErrorHandler}.
 *
 * @param <V>
 */
public class RecoveringParseErrorHandler<V> implements ParseErrorHandler<V> {

    private enum State {
        Parsing,
        SeekingToReport,
        Reporting,
        SeekingToRecover,
        Recovering,
        InRecovery
    }

    private final DefaultRecoveryRuleVisitor<V> defaultRecoveryVisitor;
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private State state;
    private MatcherContext<V> rootContext;
    private RecoveryRecord<V> firstRecord;
    private RecoveryRecord<V> currentRecord;

    public RecoveringParseErrorHandler() {
        this(new DefaultRecoveryRuleVisitorImpl<V>(), new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringParseErrorHandler(DefaultRecoveryRuleVisitor<V> defaultRecoveryVisitor) {
        this(defaultRecoveryVisitor, new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringParseErrorHandler(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this(new DefaultRecoveryRuleVisitorImpl<V>(), invalidInputErrorFormatter);
    }

    public RecoveringParseErrorHandler(@NotNull DefaultRecoveryRuleVisitor<V> defaultRecoveryVisitor,
                                       @NotNull Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.defaultRecoveryVisitor = defaultRecoveryVisitor;
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public void initialize() {
        firstRecord = new RecoveryRecord<V>();
        state = State.Parsing;
    }

    public void initializeBeforeParsingRerun(MatcherContext<V> rootContext) {
        this.rootContext = rootContext;
        currentRecord = firstRecord;
        if (currentRecord.errorLocation == null) currentRecord.errorLocation = rootContext.getCurrentLocation();
    }

    public void handleMatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                currentRecord.advanceErrorLocation(context.getCurrentLocation());
                break;

            case SeekingToReport:
                if (currentRecord.matchesLocation(context.getCurrentLocation())) {
                    currentRecord.lastMatch = context.getPath();
                    state = State.Reporting;
                }
                break;

            case SeekingToRecover:
                if (currentRecord.matchesLocation(context.getCurrentLocation())) {
                    state = State.Recovering;
                }
                break;
        }
    }

    public boolean handleMismatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                if (context == rootContext) {
                    if (currentRecord == firstRecord) {
                        seekTo(State.Reporting);
                    } else {
                        seekTo(State.Recovering);
                    }
                }
                break;

            case SeekingToReport:
            case SeekingToRecover:
                Preconditions.checkState(context != rootContext);
                break;

            case Reporting:
                handleMismatchWhileReporting(context);
                return false;

            case Recovering:
                return handleMismatchWhileRecovering(context);
        }
        return false;
    }

    public void handleMismatchWhileReporting(MatcherContext<V> context) {
        if (currentRecord.matchesLocation(context.getCurrentLocation())) {
            currentRecord.failedMatcherPaths.add(context.getPath());
        }
        if (context == rootContext) {
            rootContext.getParseErrors().add(currentRecord.createParseError(invalidInputErrorFormatter));
            seekTo(State.Recovering);
        }
    }

    @SuppressWarnings({"unchecked"})
    public boolean handleMismatchWhileRecovering(MatcherContext<V> failedMatcherContext) {
        if (recover(failedMatcherContext)) {
            return true;
        }
        if (failedMatcherContext == rootContext) {
            // we failed to recover, so simply mark all characters up until EOI as illegal
            state = State.InRecovery;
            matchIllegalUntilEOI();
            state = State.Parsing; // set state to "not repeating"
            return true;
        }
        return false;
    }

    public boolean recover(MatcherContext<V> failedMatcherContext) {
        Matcher<V> recoveryRule = getRecoveryRule(failedMatcherContext);
        state = State.InRecovery;
        if (recoveryRule != null && runRecovery(recoveryRule, failedMatcherContext)) {
            currentRecord = currentRecord.getNext();
            if (currentRecord.errorLocation == null) {
                // after recovery we don't have another error recorded, so continue with "innocent" parsing
                currentRecord.errorLocation = failedMatcherContext.getCurrentLocation();
                state = State.Parsing;
            } else if (currentRecord.parseError == null) {
                // after recovery we are now at an error that has not been reported so far
                state = State.SeekingToReport;
            } else {
                // after recovery we are now at the next error we need to recover from
                state = State.SeekingToRecover;
            }
            return true;
        }
        state = State.Recovering;
        return false;
    }

    @SuppressWarnings({"unchecked"})
    public Matcher<V> getRecoveryRule(MatcherContext<V> failedMatcherContext) {
        Matcher<V> recoveryRule = failedMatcherContext.getMatcher().getRecoveryMatcher();
        if (recoveryRule == null) {
            defaultRecoveryVisitor.setContext(failedMatcherContext);
            defaultRecoveryVisitor.setLastMatch(currentRecord.lastMatch);
            defaultRecoveryVisitor.setErrorLocation(currentRecord.errorLocation);
            recoveryRule = (Matcher<V>) failedMatcherContext.getMatcher().accept(defaultRecoveryVisitor);
        }
        return recoveryRule;
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    public boolean runRecovery(Matcher<V> recoveryRule, MatcherContext<V> failedContext) {
        if (failedContext.getParent() != null) {
            return runStandardRecovery(recoveryRule, failedContext);
        }
        Preconditions.checkState(failedContext == rootContext);
        if (failedContext.getSubContext(recoveryRule).runMatcher()) {
            postRootRecoveryFix();
            return true;
        }
        return false;
    }

    public boolean runStandardRecovery(Matcher<V> recoveryRule, MatcherContext<V> failedContext) {
        MatcherContext<V> recoveryContext = failedContext.getSubContext(recoveryRule);
        recoveryContext.clearSubLeafNodeSuppression();
        if (!recoveryContext.runMatcher()) return false;
        if (!(failedContext.getMatcher() instanceof TestMatcher) && failedContext.getSubNodes() != null) {
            failedContext.getParent().addChildNodes(failedContext.getSubNodes());
        }
        return true;
    }

    // it was directly the root matcher that failed, we need some special treatment since in this case
    // the illegal nodes must be INSIDE the node instead of outside
    public void postRootRecoveryFix() {
        Node<V> rootNode = rootContext.getNodeByPath(rootContext.getMatcher().getLabel());
        if (rootNode != null) {
            rootContext.getSubNodes().remove(rootNode);
            rootContext.getSubNodes().addAll(rootNode.getChildren());
        }
        rootContext.createNode();
    }

    public void seekTo(State targetState) {
        if (firstRecord.errorLocation.index == 0) {
            state = targetState;
        } else if (targetState == State.Reporting) {
            state = State.SeekingToReport;
        } else if (targetState == State.Recovering) {
            state = State.SeekingToRecover;
        } else {
            throw new IllegalStateException();
        }
    }

    public boolean isRerunRequested(MatcherContext<V> context) {
        return state != State.Parsing;
    }

    @SuppressWarnings({"unchecked"})
    public void matchIllegalUntilEOI() {
        BaseParser<V> parser = rootContext.getParser();
        Matcher<V> matchToEOI = (Matcher<V>) parser.zeroOrMore(parser.any()).asLeaf().label(Parboiled.ILLEGAL);
        Preconditions.checkState(rootContext.getSubContext(matchToEOI).runMatcher());
        rootContext.createNode();
    }

    public static class RecoveryRecord<V> {

        public final List<MatcherPath<V>> failedMatcherPaths = new ArrayList<MatcherPath<V>>();
        public InputLocation errorLocation;
        public MatcherPath<V> lastMatch;
        public InvalidInputError<V> parseError;
        public RecoveryRecord<V> next;

        public RecoveryRecord<V> getNext() {
            if (next == null) {
                next = new RecoveryRecord<V>();
            }
            return next;
        }

        public InvalidInputError<V> createParseError(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
            parseError = new InvalidInputError<V>(errorLocation, lastMatch, failedMatcherPaths,
                    invalidInputErrorFormatter);
            return parseError;
        }

        public boolean matchesLocation(InputLocation location) {
            return errorLocation.index == location.index;
        }

        public void advanceErrorLocation(InputLocation currentLocation) {
            if (errorLocation.index < currentLocation.index) {
                errorLocation = currentLocation;
            }
        }
    }

}