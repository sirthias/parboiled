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

public class RecoveringParseErrorHandler<V> implements ParseErrorHandler<V> {

    private enum State {
        Parsing, SeekingToReport, Reporting, SeekingToRecover, Recovering, InRecovery
    }

    private final List<MatcherPath<V>> failedMatchers = new ArrayList<MatcherPath<V>>();
    private final DefaultRecoveryRuleVisitor<V> defaultRecoveryVisitor;
    private final Formatter<InvalidInputError<V>> invalidInputErrorFormatter;
    private List<InputLocation> errorLocations = new ArrayList<InputLocation>();
    private int currentErrorIndex;
    private InputLocation currentErrorLocation;
    private State state = State.Parsing;
    private MatcherContext<V> rootContext;
    private MatcherPath<V> lastMatch;

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

    public void beforeParsingRun(MatcherContext<V> rootContext) {
        this.rootContext = rootContext;
        if (errorLocations.isEmpty()) {
            errorLocations.add(rootContext.getCurrentLocation());
        }
        currentErrorIndex = 0;
        currentErrorLocation = errorLocations.get(0);
    }

    public void handleMatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                if (currentErrorLocation.index < context.getCurrentLocation().index) {
                    // record the last successful match, the current location might be a parse error
                    currentErrorLocation = context.getCurrentLocation();
                }
                break;

            case SeekingToReport:
            case SeekingToRecover:
                if (context.getCurrentLocation().index == currentErrorLocation.index) {
                    // we are back at the location we previously marked as the error location
                    lastMatch = context.getPath();
                    state = state == State.SeekingToReport ? State.Reporting : State.Recovering;
                }
                break;
        }
    }

    public boolean handleMismatch(MatcherContext<V> context) {
        switch (state) {
            case Parsing:
                if (context == rootContext) {
                    if (currentErrorLocation.index > 0) {
                        errorLocations.set(currentErrorIndex, currentErrorLocation);
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

    public boolean handleMismatchWhileReporting(MatcherContext<V> context) {
        if (context.getCurrentLocation().index == currentErrorLocation.index) {
            failedMatchers.add(context.getPath());
        }
        if (context == rootContext) {
            createParseError();
            if (currentErrorLocation.index > 0) {
                state = State.SeekingToRecover;
            } else {
                // we mismatched the very first character, recover from it
                return handleMismatchWhileRecovering(context);
            }
        }
        return false;
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
            state = State.Parsing;
            return true;
        }
        return false;
    }

    public boolean recover(MatcherContext<V> failedMatcherContext) {
        Matcher<V> recoveryRule = getRecoveryRule(failedMatcherContext);
        state = State.InRecovery;
        if (recoveryRule != null && runRecovery(recoveryRule, failedMatcherContext)) {
            state = getPostRecoveryState();
            return true;
        }
        state = State.Recovering;
        return false;
    }

    @SuppressWarnings({"unchecked"})
    private Matcher<V> getRecoveryRule(MatcherContext<V> failedMatcherContext) {
        Matcher<V> recoveryRule = failedMatcherContext.getMatcher().getRecoveryMatcher();
        if (recoveryRule == null) {
            defaultRecoveryVisitor.setContext(failedMatcherContext);
            defaultRecoveryVisitor.setLastMatch(lastMatch);
            defaultRecoveryVisitor.setErrorLocation(currentErrorLocation);
            recoveryRule = (Matcher<V>) failedMatcherContext.getMatcher().accept(defaultRecoveryVisitor);
        }
        return recoveryRule;
    }

    @SuppressWarnings({"SimplifiableIfStatement"})
    private boolean runRecovery(Matcher<V> recoveryRule, MatcherContext<V> failedContext) {
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

    private boolean runStandardRecovery(Matcher<V> recoveryRule, MatcherContext<V> failedContext) {
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
    private void postRootRecoveryFix() {
        Node<V> rootNode = rootContext.getNodeByPath(rootContext.getMatcher().getLabel());
        if (rootNode != null) {
            rootContext.getSubNodes().remove(rootNode);
            rootContext.getSubNodes().addAll(rootNode.getChildren());
        }
        rootContext.createNode();
    }

    private State getPostRecoveryState() {
        currentErrorIndex++;
        if (currentErrorIndex == errorLocations.size()) {
            errorLocations.add(currentErrorLocation);
            return State.Parsing;
        }
        currentErrorLocation = errorLocations.get(currentErrorIndex);
        return State.SeekingToRecover;
    }

    public boolean isRerunRequested(MatcherContext<V> context) {
        return state != State.Parsing;
    }

    public void createParseError() {
        rootContext.getParseErrors().add(
                new InvalidInputError<V>(currentErrorLocation, lastMatch, failedMatchers, invalidInputErrorFormatter)
        );
    }

    @SuppressWarnings({"unchecked"})
    private void matchIllegalUntilEOI() {
        BaseParser<V> parser = rootContext.getParser();
        Matcher<V> matchToEOI = (Matcher<V>)parser.zeroOrMore(parser.any()).asLeaf().label(Parboiled.ILLEGAL);
        Preconditions.checkState(rootContext.getSubContext(matchToEOI).runMatcher());
        rootContext.createNode();
    }

}