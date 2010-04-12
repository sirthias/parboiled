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
import org.parboiled.support.*;

import java.util.List;

/**
 * A {@link ParseRunner} implementation that is able to recover from {@link InvalidInputError}s in the input and therefore
 * report more than just the first {@link InvalidInputError} if the input does not conform to the rule grammar.
 * Error recovery is done by attempting to either delete an error character or insert a potentially missing character,
 * whereby this implementation is able to determine itself which of these options is the best strategy.
 * If the parse error cannot be overcome by either deleting or inserting one character a resynchronization rule is
 * determined and the parsing process resynchronized, so that parsing can still continue.
 * In this way the RecoveringParseRunner is able to completely parse most input texts. Only if the parser cannot even
 * start matching the root rule will it return an unmatched {@link ParsingResult}.
 * If the input is error free this {@link ParseRunner} implementation will only perform one parsing run.
 * However, if there are {@link InvalidInputError}s in the input potentially many more runs are performed to properly
 * report all errors and test the various recovery strategies.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class RecoveringParseRunner<V> extends BasicParseRunner<V> {

    private InputLocation errorLocation;
    private InvalidInputError<V> currentError;

    /**
     * Create a new RecoveringParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run()} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     */
    public static <V> ParsingResult<V> run(@NotNull Rule rule, @NotNull String input) {
        return new RecoveringParseRunner<V>(rule, input).run();
    }

    /**
     * Creates a new RecoveringParseRunner instance for the given rule and input text.
     *
     * @param rule  the parser rule
     * @param input the input text
     */
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

    protected void performErrorReportingRun() {
        ReportingParseRunner.Handler<V> handler = new ReportingParseRunner.Handler<V>(errorLocation, getInnerHandler());
        runRootContext(handler);
        currentError = handler.getParseError();
    }

    protected MatchHandler<V> getInnerHandler() {
        return errorLocation != null && errorLocation.getIndex() > 0 ?
                new Handler<V>(currentError) : new BasicParseRunner.Handler<V>();
    }

    protected boolean fixError(InputLocation fixLocation) {
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
                preFixLocation.insertNext(Characters.DEL_ERROR);
                preFixLocation.getNext().removeNext();
                errorLocation = nextErrorAfterDeletion;
            } else {
                // we need to insert the characters in reverse order, since we insert twice on the same location
                preFixLocation.insertNext(bestInsertionCharacter);
                preFixLocation.insertNext(Characters.INS_ERROR);
                errorLocation = nextErrorAfterBestInsertion;
            }
        } else {
            // we can't fix the error with a single char fix, so fall back to resynchronization
            preFixLocation.insertNext(Characters.RESYNC);
            attemptRecordingMatch(); // find the next parse error
        }
        return true;
    }

    // skip over all illegal chars that we cannot start a root match with
    protected boolean fixIllegalStarterChars() {
        int count = 0;
        while (errorLocation.getChar() != Characters.EOI &&
                !rootMatcher.accept(new IsStarterCharVisitor<V>(errorLocation.getChar()))) {
            errorLocation = errorLocation.advance(inputBuffer);
            count++;
        }
        if (count == 0 || errorLocation.getChar() == Characters.EOI) return false;
        currentError.setErrorCharCount(count);
        startLocation = errorLocation;
        return true;
    }

    protected InputLocation findPreviousLocation(InputLocation fixLocation) {
        InputLocation location = startLocation;
        while (location != null && location.getNext() != fixLocation) {
            location = location.getNext();
        }
        return location;
    }

    protected boolean tryFixBySingleCharDeletion(@NotNull InputLocation preFixLocation) {
        preFixLocation.insertNext(Characters.DEL_ERROR);
        InputLocation saved = preFixLocation.getNext().removeNext();
        boolean nowErrorFree = attemptRecordingMatch();
        if (!nowErrorFree) {
            preFixLocation.removeNext();
            preFixLocation.insertNext(saved);
        }
        return nowErrorFree;
    }

    @SuppressWarnings({"ConstantConditions"})
    protected Character findBestSingleCharInsertion(@NotNull InputLocation preFixLocation) {
        GetAStarterCharVisitor<V> getAStarterCharVisitor = new GetAStarterCharVisitor<V>();
        InputLocation bestNextErrorLocation = startLocation; // initialize with the "worst" next error location
        Character bestChar = null;
        for (MatcherPath<V> failedMatcherPath : currentError.getFailedMatchers()) {
            Character starterChar = failedMatcherPath.getHead().accept(getAStarterCharVisitor);
            Preconditions.checkState(starterChar != null); // we should only have single character matchers
            if (starterChar == Characters.EOI) {
                continue; // we should never conjure up an EOI character (that would be cheating :)
            }
            preFixLocation.insertNext(starterChar);
            preFixLocation.insertNext(Characters.INS_ERROR);
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

    /**
     * A {@link MatchHandler} implementation that recognized the three special error recovery characters
     * {@link Characters#DEL_ERROR}, {@link Characters#INS_ERROR} and {@link Characters#RESYNC} to overcome
     * {@link InvalidInputError}s at the respective {@link InputLocation}s.
     *
     * @param <V> the type of the value field of a parse tree node
     */
    public static class Handler<V> implements MatchHandler<V> {
        private final IsSingleCharMatcherVisitor<V> isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor<V>();
        private final InvalidInputError<V> currentError;
        private InputLocation fringeLocation;
        private MatcherPath<V> lastMatchPath;

        /**
         * Creates a new Handler. If a non-null InvalidInputError is given the handler will set its errorCharCount
         * to the correct number if the error corresponds to an error that can only be overcome by resynchronizing.
         *
         * @param currentError an optional InvalidInputError whose errorCharCount to set during resyncing
         */
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
            return fringeLocation != null && fringeLocation.getChar() == Characters.RESYNC &&
                    matcher instanceof SequenceMatcher &&
                    context.getCurrentLocation() != context.getStartLocation() &&
                    context.getPath().isPrefixOf(lastMatchPath) &&
                    resynchronize(context);
        }

        protected void updateFringeLocation(MatcherContext<V> context) {
            if (fringeLocation == null ||
                    fringeLocation.getIndex() < context.getCurrentLocation().getIndex()) {
                fringeLocation = context.getCurrentLocation();
                lastMatchPath = context.getPath();
            }
        }

        protected boolean isErrorLocation(MatcherContext<V> context) {
            char c = context.getCurrentLocation().getChar();
            return c == Characters.DEL_ERROR || c == Characters.INS_ERROR;
        }

        protected boolean willMatchError(MatcherContext<V> context) {
            InputLocation preSkipLocation = context.getCurrentLocation();
            while (isErrorLocation(context)) {
                context.advanceInputLocation();
            }
            if (!context.getSubContext(new TestMatcher<V>(context.getMatcher())).runMatcher()) {
                // if we wouldn't succeed with the match do not swallow the ERROR char
                context.setCurrentLocation(preSkipLocation);
                return false;
            }
            context.setStartLocation(context.getCurrentLocation());
            context.clearNodeSuppression();
            if (preSkipLocation.getChar() == Characters.INS_ERROR) {
                context.markError();
            } else {
                if (context.getParent() != null) context.getParent().markError();
            }
            return true;
        }

        protected boolean resynchronize(MatcherContext<V> context) {
            context.clearNodeSuppression();
            context.markError();

            // create a node for the failed Sequence, taking ownership of all sub nodes created so far
            context.createNode();

            // skip over all characters that are not legal followers of the failed Sequence
            context.advanceInputLocation(); // gobble RESYNC marker
            List<Matcher<V>> followMatchers = new FollowMatchersVisitor<V>().getFollowMatchers(context);
            int resyncCharCount = gobbleIllegalCharacters(context, followMatchers);

            if (currentError != null &&
                    currentError.getErrorLocation().getIndex() == fringeLocation.getIndex() && resyncCharCount > 1) {
                currentError.setErrorCharCount(resyncCharCount);
            }

            return true;
        }

        protected int gobbleIllegalCharacters(MatcherContext<V> context, List<Matcher<V>> followMatchers) {
            int count = 0;
            while_loop:
            while (true) {
                char currentChar = context.getCurrentLocation().getChar();
                if (currentChar == Characters.EOI) break;
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
