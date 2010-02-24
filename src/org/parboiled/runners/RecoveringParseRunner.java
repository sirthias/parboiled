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
import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.*;
import org.parboiled.support.Characters;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

public class RecoveringParseRunner<V> extends BasicParseRunner<V> {

    public static final char ANY = '\uFFFE';
    public static final char RESYNC = '\uFDEF';

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
        MatchHandler<V> innerHandler = errorLocation != null ? new Handler<V>(
                currentError) : new BasicParseRunner.Handler<V>();
        RecordingParseRunner.Handler<V> handler = new RecordingParseRunner.Handler<V>(innerHandler);
        boolean matched = runRootContext(handler);
        errorLocation = handler.getErrorLocation();
        return matched;
    }

    private void performErrorReportingRun() {
        ReportingParseRunner.Handler<V> handler =
                new ReportingParseRunner.Handler<V>(errorLocation, new Handler<V>(currentError));
        runRootContext(handler);
        currentError = handler.getParseError();
    }

    private boolean fixError(InputLocation fixLocation) {
        if (errorLocation.getIndex() == 0) {
            if (!fixIllegalStarterChars()) return false;
            attemptRecordingMatch();
            return true;
        }

        InputLocation preFixLocation = findPreviousLocation(fixLocation);

        if (tryFixBySingleCharDeletion(preFixLocation)) return true;
        InputLocation nextErrorAfterDeletion = errorLocation;

        if (tryFixBySingleCharInsertion(preFixLocation, ANY)) return true;
        InputLocation nextErrorAfterInsertion = errorLocation;

        if (tryFixBySingleCharInsertion(fixLocation, RESYNC)) return true;
        InputLocation nextErrorAfterResync = errorLocation;

        // test which fix option performs best and go for it
        if (nextErrorAfterDeletion.getIndex() >=
                Math.max(nextErrorAfterInsertion.getIndex(), nextErrorAfterResync.getIndex())) {
            preFixLocation.removeAfter();
            errorLocation = nextErrorAfterDeletion;
        } else if (nextErrorAfterInsertion.getIndex() >= nextErrorAfterResync.getIndex()) {
            preFixLocation.insertAfter(ANY);
            errorLocation = nextErrorAfterInsertion;
        } else {
            preFixLocation.insertAfter(RESYNC);
            errorLocation = nextErrorAfterResync;
        }
        return true;
    }

    // skip over all illegal chars that we cannot start a root match with
    protected boolean fixIllegalStarterChars() {
        Characters starterChars = rootMatcher.accept(new StarterCharsVisitor<V>());
        while (!starterChars.contains(errorLocation.getChar())) {
            errorLocation = errorLocation.advance(inputBuffer);
            if (errorLocation.getChar() == Parboiled.EOI) return false;
        }
        currentError.setErrorCharCount(errorLocation.getIndex() - startLocation.getIndex());
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
        InputLocation saved = preFixLocation.removeAfter();
        boolean nowErrorFree = attemptRecordingMatch();
        if (!nowErrorFree) {
            preFixLocation.insertAfter(saved); // undo remove
        }
        return nowErrorFree;
    }

    private boolean tryFixBySingleCharInsertion(@NotNull InputLocation preFixLocation, char character) {
        preFixLocation.insertAfter(character);
        boolean nowErrorFree = attemptRecordingMatch();
        if (!nowErrorFree) {
            preFixLocation.removeAfter(); // undo char insertion
        }
        return nowErrorFree;
    }

    public static class Handler<V> implements MatchHandler<V> {
        private InputLocation lastMatchLoc;
        private MatcherPath<V> lastMatchPath;
        private final InvalidInputError<V> currentError;

        public Handler(InvalidInputError<V> currentError) {
            this.currentError = currentError;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            return rootContext.runMatcher();
        }

        @SuppressWarnings({"SimplifiableIfStatement"})
        public boolean match(MatcherContext<V> context) throws Throwable {
            if (context.getMatcher().match(context)) {
                if (lastMatchLoc == null || lastMatchLoc.getIndex() < context.getCurrentLocation().getIndex()) {
                    lastMatchLoc = context.getCurrentLocation();
                    lastMatchPath = context.getPath();
                }
                return true;
            }
            char currentChar = context.getCurrentLocation().getChar();
            if (currentChar == ANY || currentChar == RESYNC) {
                RecoveryVisitor<V> recoveryVisitor = new RecoveryVisitor<V>(context, lastMatchLoc, lastMatchPath);
                boolean recovered = context.getMatcher().accept(recoveryVisitor);
                if (recoveryVisitor.resyncCharCount > 1 &&
                        currentError.getErrorLocation().getIndex() == lastMatchLoc.getIndex() + 1) {
                    currentError.setErrorCharCount(recoveryVisitor.resyncCharCount);
                }
                return recovered;
            }
            return false;
        }

    }

    private static class RecoveryVisitor<V> extends DefaultMatcherVisitor<V, Boolean> {
        private final MatcherContext<V> context;
        private final InputLocation lastMatchLoc;
        private final MatcherPath<V> lastMatchPath;
        public int resyncCharCount;

        public RecoveryVisitor(@NotNull MatcherContext<V> context, @NotNull InputLocation lastMatchLoc,
                               @NotNull MatcherPath<V> lastMatchPath) {
            this.context = context;
            this.lastMatchLoc = lastMatchLoc;
            this.lastMatchPath = lastMatchPath;
        }

        @Override
        public Boolean visit(CharactersMatcher<V> matcher) {
            return visitSingleCharMatcher();
        }

        @Override
        public Boolean visit(CharIgnoreCaseMatcher<V> matcher) {
            return visitSingleCharMatcher();
        }

        @Override
        public Boolean visit(CharMatcher<V> matcher) {
            return visitSingleCharMatcher();
        }

        @Override
        public Boolean visit(CharRangeMatcher<V> matcher) {
            return visitSingleCharMatcher();
        }

        private boolean visitSingleCharMatcher() {
            if (context.getCurrentLocation().getChar() == ANY) {
                context.advanceInputLocation();
                context.createNode();
                return true;
            }
            return false;
        }

        @Override
        public Boolean visit(SequenceMatcher<V> matcher) {
            return isResynchronizationSequence() && resynchronize();
        }

        @Override
        public Boolean defaultValue(AbstractMatcher<V> matcher) {
            return false;
        }

        @SuppressWarnings({"RedundantIfStatement"})
        private boolean isResynchronizationSequence() {
            // don't resync if we are not currently at a RESYNC marker
            if (lastMatchLoc.getChar() != RESYNC) return false;

            // don't resync if the sequence has not already matched something
            if (context.getCurrentLocation() == context.getStartLocation()) return false;

            // don't resync if we are not a parent sequence of the last match
            if (!context.getPath().isPrefixOf(lastMatchPath)) return false;

            return true;
        }

        private boolean resynchronize() {
            // create a node for the failed sequence, taking ownership of all sub nodes created so far
            context.createNode();

            // skip over all characters that are not legal followers of the failed sequence
            Characters followerChars = getStarterCharsOfFollowers();
            while (!followerChars.contains(context.getCurrentLocation().getChar())) {
                context.advanceInputLocation();
                resyncCharCount++;
            }
            return true;
        }

        private Characters getStarterCharsOfFollowers() {
            StarterCharsVisitor<V> starterCharsVisitor = new StarterCharsVisitor<V>();
            FollowMatchersVisitor<V> followMatchersVisitor = new FollowMatchersVisitor<V>();
            Characters starterChars = Characters.NONE;
            for (Matcher<V> followMatcher : followMatchersVisitor.getFollowMatchers(context)) {
                starterChars = starterChars.add(followMatcher.accept(starterCharsVisitor));
            }
            return starterChars;
        }

    }

}
