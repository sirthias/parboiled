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
import com.google.common.base.Preconditions;

public class RecoveringParseRunner<V> extends BasicParseRunner<V> {

    public static final char SKIP = '\uFDED';
    public static final char ANY = '\uFDEE';
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

        if (tryFixBySingleCharInsertion(preFixLocation, ANY)) return true;
        InputLocation nextErrorAfterInsertion = errorLocation;

        if (tryFixBySingleCharInsertion(fixLocation, RESYNC)) return true;
        InputLocation nextErrorAfterResync = errorLocation;

        // test which fix option performs best and go for it
        if (nextErrorAfterDeletion.getIndex() >=
                Math.max(nextErrorAfterInsertion.getIndex(), nextErrorAfterResync.getIndex())) {
            preFixLocation.removeNext();
            errorLocation = nextErrorAfterDeletion;
        } else if (nextErrorAfterInsertion.getIndex() >= nextErrorAfterResync.getIndex()) {
            preFixLocation.insertNext(ANY);
            errorLocation = nextErrorAfterInsertion;
        } else {
            preFixLocation.insertNext(RESYNC);
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
        preFixLocation.insertNext(SKIP);
        InputLocation saved = preFixLocation.getNext().removeNext();
        boolean nowErrorFree = attemptRecordingMatch();
        if (!nowErrorFree) {
            preFixLocation.removeNext();
            preFixLocation.insertNext(saved);
        }
        return nowErrorFree;
    }

    private boolean tryFixBySingleCharInsertion(@NotNull InputLocation preFixLocation, char character) {
        preFixLocation.insertNext(character);
        boolean nowErrorFree = attemptRecordingMatch();
        if (!nowErrorFree) {
            preFixLocation.removeNext();
        }
        return nowErrorFree;
    }

    public static class Handler<V> implements MatchHandler<V> {
        private final InvalidInputError<V> currentError;
        private InputLocation fringeLocation;
        private MatcherPath<V> lastMatchPath;

        public Handler(InvalidInputError<V> currentError) {
            this.currentError = currentError;
        }

        public boolean matchRoot(MatcherContext<V> rootContext) {
            return rootContext.runMatcher();
        }

        @SuppressWarnings({"SimplifiableIfStatement"})
        public boolean match(MatcherContext<V> context) {
            Matcher<V> matcher = context.getMatcher();
            if (matcher instanceof CharMatcher || matcher instanceof CharRangeMatcher ||
                    matcher instanceof CharactersMatcher || matcher instanceof CharIgnoreCaseMatcher) {
                switch (context.getCurrentLocation().getChar()) {
                    case ANY:
                        if (!matchAny(context)) return false;
                        break;

                    case SKIP:
                        if (!skipLocation(context)) return false;
                        break;

                    default:
                        if (!matcher.match(context)) return false;
                        break;
                }
                if (fringeLocation == null || fringeLocation.getIndex() < context.getCurrentLocation().getIndex()) {
                    fringeLocation = context.getCurrentLocation();
                    lastMatchPath = context.getPath();
                }
                return true;
            }

            if (matcher.match(context)) {
                return true;
            }

            // if we didn't match we might have to resynchronize, however we only resynchronize
            // if we are at a RESYNC location and the matcher is a SequenceMatchers that has already
            // matched at least one character and that is a parent of the last match
            return fringeLocation.getChar() == RESYNC &&
                    matcher instanceof SequenceMatcher &&
                    context.getCurrentLocation() != context.getStartLocation() &&
                    context.getPath().isPrefixOf(lastMatchPath) &&
                    resynchronize(context);
        }

        private boolean matchAny(MatcherContext<V> context) {
            // Characters followerChars = getStarterCharsOfFollowers(context);
            context.advanceInputLocation();
            context.markError();
            context.createNode();
            return true;
        }

        private boolean skipLocation(MatcherContext<V> context) {
            InputLocation preSkipLocation = context.getCurrentLocation();
            while (context.getCurrentLocation().getChar() == SKIP) {
                context.advanceInputLocation();
            }
            if (!context.getSubContext(new TestMatcher<V>((Rule) context.getMatcher())).runMatcher()) {
                // if we wouldn't succeed with the match do not swallow the SKIP char
                context.setCurrentLocation(preSkipLocation);
                return false;
            }
            context.setStartLocation(context.getCurrentLocation());
            context.clearBelowLeafLevelMarker();
            if (context.getParent() != null) context.getParent().markError();
            Preconditions.checkState(context.getMatcher().match(context));
            return true;
        }

        private boolean resynchronize(MatcherContext<V> context) {
            context.markError();

            // create a node for the failed sequence, taking ownership of all sub nodes created so far
            context.createNode();

            // skip over all characters that are not legal followers of the failed sequence
            int resyncCharCount = 0;
            Characters followerChars = getStarterCharsOfFollowers(context);
            while (!followerChars.contains(context.getCurrentLocation().getChar())) {
                context.advanceInputLocation();
                resyncCharCount++;
            }
            if (currentError.getErrorLocation() == fringeLocation && resyncCharCount > 1) {
                currentError.setErrorCharCount(resyncCharCount);
            }

            return true;
        }

        private Characters getStarterCharsOfFollowers(MatcherContext<V> context) {
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
