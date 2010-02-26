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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.SequenceMatcher;
import org.parboiled.matchers.TestMatcher;
import org.parboiled.matchervisitors.FollowMatchersVisitor;
import org.parboiled.matchervisitors.GetAStarterCharVisitor;
import org.parboiled.matchervisitors.IsSingleCharMatcherVisitor;
import org.parboiled.matchervisitors.IsStarterCharVisitor;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

import java.util.List;

public class RecoveringParseRunner<V> extends BasicParseRunner<V> {

    private InputLocation errorLocation;
    private InvalidInputError<V> currentError;

    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new RecoveringParseRunner<V>(rule, input).run();
    }

    public RecoveringParseRunner(@NotNull Rule rule, @NotNull String input) {
        super(rule, input);
    }

    @Override
    protected boolean runRootContext() {
        if (!attemptRecordingMatch()) {
            do {
                performErrorReportingRun();
                if (!fixError(errorLocation)) {
                    return false;
                }
            } while (errorLocation != null);
        }
        return true;
    }

    protected boolean attemptRecordingMatch() {
        RecordingParseRunner.Handler<V> handler = new RecordingParseRunner.Handler<V>(getInnerHandler());
        boolean matched = runRootContext(handler);
        errorLocation = handler.getErrorLocation();
        return matched;
    }

    private void performErrorReportingRun() {
        ReportingParseRunner.Handler<V> handler = new ReportingParseRunner.Handler<V>(errorLocation, getInnerHandler());
        runRootContext(handler);
        currentError = handler.getParseError();
    }

    private MatchHandler<V> getInnerHandler() {
        return errorLocation != null && errorLocation.getIndex() > 0 ?
                new Handler<V>(currentError) : new BasicParseRunner.Handler<V>();
    }

    private boolean fixError(InputLocation fixLocation) {
        if (errorLocation.getIndex() == 0) {
            if (!fixIllegalStarterChars()) return false;
            attemptRecordingMatch();
            return true;
        }

        InputLocation preFixLocation = findPreviousLocation(fixLocation);
        fixLocation.advance(inputBuffer); // fetch the next input char, so the removal operation works

        if (tryFixBySingleCharDeletion(preFixLocation)) return true;
        InputLocation nextErrorAfterDeletion = errorLocation;

        Character bestInsertionCharacter = findBestSingleCharInsertion(preFixLocation);
        if (bestInsertionCharacter == null) return true;
        InputLocation nextErrorAfterBestInsertion = errorLocation;

        int nextErrorAfterBestSingleCharFix = Math.max(
                nextErrorAfterDeletion.getIndex(),
                nextErrorAfterBestInsertion.getIndex()
        );
        if (nextErrorAfterBestSingleCharFix > fixLocation.getIndex()) {
            // we are able to overcome the error with a single char fix, so apply the best one found
            if (nextErrorAfterDeletion.getIndex() >= nextErrorAfterBestInsertion.getIndex()) {
                preFixLocation.insertNext(Parboiled.DEL_ERROR);
                preFixLocation.getNext().removeNext();
                errorLocation = nextErrorAfterDeletion;
            } else {
                preFixLocation.insertNext(bestInsertionCharacter);
                preFixLocation.insertNext(Parboiled.INS_ERROR);
                errorLocation = nextErrorAfterBestInsertion;
            }
        } else {
            // we can't fix the error with a single char fix, so fall back to resynchronization
            preFixLocation.insertNext(Parboiled.RESYNC);
            attemptRecordingMatch();
        }
        return true;
    }

    // skip over all illegal chars that we cannot start a root match with
    protected boolean fixIllegalStarterChars() {
        int count = 0;
        while (errorLocation.getChar() != Parboiled.EOI &&
                !rootMatcher.accept(new IsStarterCharVisitor<V>(errorLocation.getChar()))) {
            errorLocation = errorLocation.advance(inputBuffer);
            count++;
        }
        if (count == 0) return false;
        currentError.setErrorCharCount(count);
        startLocation = errorLocation;
        return true;
    }

    private InputLocation findPreviousLocation(InputLocation fixLocation) {
        InputLocation location = startLocation;
        while (location != null && location.getNext() != fixLocation) {
            location = location.getNext();
        }
        return location;
    }

    private boolean tryFixBySingleCharDeletion(@NotNull InputLocation preFixLocation) {
        preFixLocation.insertNext(Parboiled.DEL_ERROR);
        InputLocation saved = preFixLocation.getNext().removeNext();
        boolean nowErrorFree = attemptRecordingMatch();
        if (!nowErrorFree) {
            preFixLocation.removeNext();
            preFixLocation.insertNext(saved);
        }
        return nowErrorFree;
    }

    @SuppressWarnings({"ConstantConditions"})
    private Character findBestSingleCharInsertion(@NotNull InputLocation preFixLocation) {
        GetAStarterCharVisitor<V> getAStarterCharVisitor = new GetAStarterCharVisitor<V>();
        InputLocation bestNextErrorLocation = startLocation; // initialize with the "worst" next error location
        Character bestChar = null;
        for (MatcherPath<V> failedMatcherPath : currentError.getFailedMatchers()) {
            Character starterChar = failedMatcherPath.getHead().accept(getAStarterCharVisitor);
            Preconditions.checkState(starterChar != null); // we should only have single character matchers
            if (starterChar == Parboiled.EOI) {
                continue; // we should never conjure up an EOI character (that would be cheating :)
            }
            preFixLocation.insertNext(starterChar);
            preFixLocation.insertNext(Parboiled.INS_ERROR);
            if (attemptRecordingMatch()) {
                return null; // success, exit immediately
            }
            if (bestNextErrorLocation == null || bestNextErrorLocation.getIndex() < errorLocation.getIndex()) {
                bestNextErrorLocation = errorLocation;
                bestChar = starterChar;
            }
            preFixLocation.removeNext();
            preFixLocation.removeNext();
        }
        errorLocation = bestNextErrorLocation;
        return bestChar;
    }

    public static class Handler<V> implements MatchHandler<V> {
        private final IsSingleCharMatcherVisitor<V> isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor<V>();
        private final InvalidInputError<V> currentError;
        private InputLocation fringeLocation;
        private MatcherPath<V> lastMatchPath;

        public Handler(InvalidInputError<V> currentError) {
            this.currentError = currentError;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            return rootContext.runMatcher();
        }

        public boolean match(MatcherContext<V> context) {
            Matcher<V> matcher = context.getMatcher();
            if (matcher.accept(isSingleCharMatcherVisitor)) {
                if (!isErrorLocation(context) || willMatchError(context)) {
                    if (matcher.match(context)) {
                        updateFringeLocation(context);
                        return true;
                    }
                }
                return false;
            }

            if (matcher.match(context)) {
                return true;
            }

            // if we didn't match we might have to resynchronize, however we only resynchronize
            // if we are at a RESYNC location and the matcher is a SequenceMatchers that has already
            // matched at least one character and that is a parent of the last match
            return fringeLocation != null && fringeLocation.getChar() == Parboiled.RESYNC &&
                    matcher instanceof SequenceMatcher &&
                    context.getCurrentLocation() != context.getStartLocation() &&
                    context.getPath().isPrefixOf(lastMatchPath) &&
                    resynchronize(context);
        }

        private void updateFringeLocation(MatcherContext<V> context) {
            if (fringeLocation == null ||
                    fringeLocation.getIndex() < context.getCurrentLocation().getIndex()) {
                fringeLocation = context.getCurrentLocation();
                lastMatchPath = context.getPath();
            }
        }

        private boolean isErrorLocation(MatcherContext<V> context) {
            char c = context.getCurrentLocation().getChar();
            return c == Parboiled.DEL_ERROR || c == Parboiled.INS_ERROR;
        }

        private boolean willMatchError(MatcherContext<V> context) {
            InputLocation preSkipLocation = context.getCurrentLocation();
            while (isErrorLocation(context)) {
                context.advanceInputLocation();
            }
            if (!context.getSubContext(new TestMatcher<V>((Rule) context.getMatcher())).runMatcher()) {
                // if we wouldn't succeed with the match do not swallow the ERROR char
                context.setCurrentLocation(preSkipLocation);
                return false;
            }
            context.setStartLocation(context.getCurrentLocation());
            context.clearBelowLeafLevelMarker();
            if (preSkipLocation.getChar() == Parboiled.INS_ERROR) {
                context.markError();
            } else {
                if (context.getParent() != null) context.getParent().markError();
            }
            return true;
        }

        private boolean resynchronize(MatcherContext<V> context) {
            context.clearBelowLeafLevelMarker();
            context.markError();

            // create a node for the failed sequence, taking ownership of all sub nodes created so far
            context.createNode();

            // skip over all characters that are not legal followers of the failed sequence
            context.advanceInputLocation(); // gobble RESYNC marker
            List<Matcher<V>> followMatchers = new FollowMatchersVisitor<V>().getFollowMatchers(context);
            int resyncCharCount = gobbleIllegalCharacters(context, followMatchers);

            if (currentError.getErrorLocation().getIndex() == fringeLocation.getIndex() && resyncCharCount > 1) {
                currentError.setErrorCharCount(resyncCharCount);
            }

            return true;
        }

        private int gobbleIllegalCharacters(MatcherContext<V> context, List<Matcher<V>> followMatchers) {
            int count = 0;
            while_loop:
            while (true) {
                char currentChar = context.getCurrentLocation().getChar();
                if (currentChar == Parboiled.EOI) break;
                for (Matcher<V> followMatcher : followMatchers) {
                    if (followMatcher.accept(new IsStarterCharVisitor<V>(currentChar))) {
                        break while_loop;
                    }
                }
                context.advanceInputLocation();
                count++;
            }
            return count;
        }
    }

}
