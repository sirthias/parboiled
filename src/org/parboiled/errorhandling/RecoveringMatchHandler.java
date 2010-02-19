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

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Node;
import org.parboiled.MatchHandler;
import org.parboiled.common.Formatter;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.TestMatcher;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link org.parboiled.MatchHandler} that tries to recover from parse errors and is therefore capable of reporting all
 * errors found in the input. Since it needs to performs several parsing reruns in order to be able to report and
 * recover from parse errors it is considerable slower than the {@link BasicMatchHandler} and the
 * {@link ReportFirstMatchHandler} on invalid input.
 * It initiates at most one parsing rerun (in the case that the input is invalid) and is only a few percent slower
 * than the {@link BasicMatchHandler} on valid input. On valid input it performs about the same as the
 * {@link ReportFirstMatchHandler}.
 *
 * @param <V>
 */
public class RecoveringMatchHandler<V> implements MatchHandler<V> {

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

    public RecoveringMatchHandler() {
        this(new DefaultRecoveryRuleVisitorImpl<V>(), new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringMatchHandler(DefaultRecoveryRuleVisitor<V> defaultRecoveryVisitor) {
        this(defaultRecoveryVisitor, new DefaultInvalidInputErrorFormatter<V>());
    }

    public RecoveringMatchHandler(Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this(new DefaultRecoveryRuleVisitorImpl<V>(), invalidInputErrorFormatter);
    }

    public RecoveringMatchHandler(@NotNull DefaultRecoveryRuleVisitor<V> defaultRecoveryVisitor,
                                       @NotNull Formatter<InvalidInputError<V>> invalidInputErrorFormatter) {
        this.defaultRecoveryVisitor = defaultRecoveryVisitor;
        this.invalidInputErrorFormatter = invalidInputErrorFormatter;
    }

    public boolean matchRoot(@NotNull Supplier<MatcherContext<V>> rootContextProvider) {
        rootContext = rootContextProvider.get();
        firstRecord = new RecoveryRecord<V>();
        firstRecord.errorLocation = rootContext.getCurrentLocation();
        state = State.Parsing;
        do {
            currentRecord = firstRecord;
            if (rootContext.runMatcher()) {
                return true;
            }
            handleRootMismatch();
            rootContext = rootContextProvider.get();
        } while (state != State.Parsing);
        return false;
    }

    public boolean match(MatcherContext<V> context) throws Throwable {
        if (context.getMatcher().match(context)) {
            handleMatch(context);
            return true;
        }
        return handleMismatch(context);
    }

    public void handleMatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                currentRecord.advanceErrorLocation(context.getCurrentLocation());
                break;

            case SeekingToReport:
                if (currentRecord.errorLocation == context.getCurrentLocation()) {
                    currentRecord.lastMatch = context.getPath();
                    state = State.Reporting;
                }
                break;

            case SeekingToRecover:
                if (currentRecord.errorLocation == context.getCurrentLocation()) {
                    state = State.Recovering;
                }
                break;
        }
    }

    public boolean handleMismatch(MatcherContext<V> context) {
        switch (state) {
            case Reporting:
                if (currentRecord.errorLocation == context.getCurrentLocation()) {
                    currentRecord.failedMatcherPaths.add(context.getPath());
                }
                break;

            case Recovering:
                if (recover(context)) {
                    return true;
                }
                break;
        }
        return false;
    }

    private void handleRootMismatch() {
        switch (state) {
            case Parsing:
                seekTo(currentRecord == firstRecord ? State.Reporting : State.Recovering);
                break;

            case SeekingToReport:
            case SeekingToRecover:
                throw new IllegalStateException();

            case Reporting:
                rootContext.getParseErrors().add(currentRecord.createParseError(invalidInputErrorFormatter));
                seekTo(State.Recovering);
                break;
        }
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
            if (failedMatcherContext != rootContext) {
                defaultRecoveryVisitor.setContext(failedMatcherContext);
                defaultRecoveryVisitor.setLastMatch(currentRecord.lastMatch);
                defaultRecoveryVisitor.setErrorLocation(currentRecord.errorLocation);
                recoveryRule = (Matcher<V>) failedMatcherContext.getMatcher().accept(defaultRecoveryVisitor);
            } else {
                recoveryRule = (Matcher<V>) rootContext.getParser().resynchronize(rootContext, Integer.MAX_VALUE);
            }
        }
        return recoveryRule;
    }

    public boolean runRecovery(Matcher<V> recoveryRule, MatcherContext<V> failedContext) {
        MatcherContext<V> recoveryContext = failedContext.getSubContext(recoveryRule);
        recoveryContext.clearSubLeafNodeSuppression();
        if (!recoveryContext.runMatcher()) return false;
        if (failedContext == rootContext) {
            postRootRecoveryFix();
        } else if (!(failedContext.getMatcher() instanceof TestMatcher)) {
            failedContext.getParent().addChildNodes(failedContext.getSubNodes());
        }
        return true;
    }

    // it was directly the root matcher that failed, we need some special treatment since in this case
    // the illegal nodes must be INSIDE the node instead of outside
    public void postRootRecoveryFix() {
        Node<V> rootNode = rootContext.getNodeByPath(rootContext.getMatcher().getLabel());
        if (rootNode != null) {
            List<Node<V>> nodes = Lists.newArrayList(rootContext.getSubNodes());
            nodes.remove(rootNode);
            nodes.addAll(rootNode.getChildren());
            rootContext.setSubNodes(nodes);
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

        public void advanceErrorLocation(InputLocation currentLocation) {
            if (errorLocation.index < currentLocation.index) {
                errorLocation = currentLocation;
            }
        }
    }

}