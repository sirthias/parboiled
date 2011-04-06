/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

package org.parboiled.parserunners;

import org.parboiled.MatchHandler;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.buffers.MutableInputBuffer;
import org.parboiled.common.ImmutableLinkedList;
import org.parboiled.common.ImmutableList;
import org.parboiled.common.Preconditions;
import org.parboiled.errors.InvalidInputError;
import org.parboiled.matchers.*;
import org.parboiled.matchervisitors.*;
import org.parboiled.support.Checks;
import org.parboiled.support.MatcherPath;
import org.parboiled.support.ParsingResult;

import java.util.ArrayList;
import java.util.List;

import static org.parboiled.common.Preconditions.checkArgNotNull;
import static org.parboiled.common.Preconditions.checkState;
import static org.parboiled.matchers.MatcherUtils.unwrap;
import static org.parboiled.support.Chars.*;

/**
 * A {@link org.parboiled.parserunners.ParseRunner} implementation that is able to recover from {@link org.parboiled.errors.InvalidInputError}s in the input and therefore
 * report more than just the first {@link org.parboiled.errors.InvalidInputError} if the input does not conform to the rule grammar.
 * Error recovery is done by attempting to either delete an error character, insert a potentially missing character
 * or do both at once (which is equivalent to a one char replace) whereby this implementation is able to determine
 * itself which of these options is the best strategy.
 * If the parse error cannot be overcome by either deleting, inserting or replacing one character a resynchronization
 * rule is determined and the parsing process resynchronized, so that parsing can still continue.
 * In this way the RecoveringParseRunner is able to completely parse all input texts (This ParseRunner never returns
 * an unmatched {@link org.parboiled.support.ParsingResult}).
 * If the input is error free this {@link org.parboiled.parserunners.ParseRunner} implementation will only perform one parsing run, with the same
 * speed as the {@link org.parboiled.parserunners.BasicParseRunner}. However, if there are {@link org.parboiled.errors.InvalidInputError}s in the input potentially
 * many more runs are performed to properly report all errors and test the various recovery strategies.
 */
public class RecoveringParseRunner<V> extends AbstractParseRunner<V> {
    
    public static class TimeoutException extends RuntimeException {
        public final Rule rule;
        public final InputBuffer inputBuffer;
        public final ParsingResult<?> lastParsingResult;

        public TimeoutException(Rule rule, InputBuffer inputBuffer, ParsingResult<?> lastParsingResult) {
            this.rule = rule;
            this.inputBuffer = inputBuffer;
            this.lastParsingResult = lastParsingResult;
        }
    }

    private final long timeout;
    private long startTimeStamp;
    private int errorIndex;
    private InvalidInputError currentError;
    private MutableInputBuffer buffer;
    private ParsingResult<V> lastParsingResult;
    private Matcher rootMatcherWithoutPTB; // the root matcher with parse tree building disabled

    /**
     * Create a new RecoveringParseRunner instance with the given rule and input text and returns the result of
     * its {@link #run(String)} method invocation.
     *
     * @param rule  the parser rule to run
     * @param input the input text to run on
     * @return the ParsingResult for the parsing run
     * @deprecated As of 0.11.0 you should use the "regular" constructor and one of the "run" methods rather than
     *             this static method. This method will be removed in one of the coming releases.
     */
    @Deprecated
    public static <V> ParsingResult<V> run(Rule rule, String input) {
        checkArgNotNull(rule, "rule");
        checkArgNotNull(input, "input");
        return new RecoveringParseRunner<V>(rule).run(input);
    }

    /**
     * Creates a new RecoveringParseRunner instance for the given rule.
     *
     * @param rule the parser rule
     */
    public RecoveringParseRunner(Rule rule) {
        this(rule, Long.MAX_VALUE);
    }
    
    /**
     * Creates a new RecoveringParseRunner instance for the given rule.
     * A parsing run will throw a TimeoutException if it takes longer than the given number if milliseconds. 
     *
     * @param rule the parser rule
     * @param timeout the timeout value in milliseconds
     */
    public RecoveringParseRunner(Rule rule, long timeout) {
        super(rule);
        this.timeout = timeout;
    }

    public ParsingResult<V> run(InputBuffer inputBuffer) {
        checkArgNotNull(inputBuffer, "inputBuffer");
        startTimeStamp = System.currentTimeMillis();
        resetValueStack();

        // first, run a basic match
        ParseRunner<V> basicRunner = new BasicParseRunner<V>(getRootMatcher())
                .withParseErrors(getParseErrors())
                .withValueStack(getValueStack());
        lastParsingResult = basicRunner.run(inputBuffer);

        if (!lastParsingResult.matched) {
            // for better performance disable parse tree building during the recovery runs
            rootMatcherWithoutPTB = (Matcher) getRootMatcher().suppressNode();

            // locate first error
            performLocatingRun(inputBuffer);
            checkState(errorIndex >= 0); // we failed before so we must fail again

            // in order to be able to apply fixes we need to wrap the input buffer with a mutability wrapper
            buffer = new MutableInputBuffer(inputBuffer);

            // report first error
            performReportingRun();

            // fix and report until done
            while (!fixError(errorIndex)) {
                performReportingRun();
            }

            // rerun once more with parse tree building enabled to create a parse tree for the fixed input
            if (!getRootMatcher().isNodeSuppressed()) {
                performFinalRun();
                checkState(lastParsingResult.matched);
            }
        }
        return lastParsingResult;
    }

    private boolean performLocatingRun(InputBuffer inputBuffer) {
        resetValueStack();
        ParseRunner<V> locatingRunner = new ErrorLocatingParseRunner<V>(rootMatcherWithoutPTB, getInnerHandler())
                .withParseErrors(getParseErrors())
                .withValueStack(getValueStack());
        lastParsingResult = locatingRunner.run(inputBuffer);
        errorIndex = lastParsingResult.matched ? -1 :
                getParseErrors().remove(getParseErrors().size() - 1).getStartIndex();
        return lastParsingResult.matched;
    }

    private void performReportingRun() {
        resetValueStack();
        ParseRunner<V> reportingRunner = new ErrorReportingParseRunner<V>(rootMatcherWithoutPTB, errorIndex,
                getInnerHandler())
                .withParseErrors(getParseErrors())
                .withValueStack(getValueStack());
        ParsingResult<V> result = reportingRunner.run(buffer);
        Preconditions.checkState(!result.matched); // we failed before so we should really be failing again
        currentError = (InvalidInputError) getParseErrors().get(getParseErrors().size() - 1);
    }

    private void performFinalRun() {
        resetValueStack();
        Handler handler = new Handler();
        MatcherContext<V> rootContext = createRootContext(buffer, handler, false);
        boolean matched = handler.match(rootContext);
        lastParsingResult = createParsingResult(matched, rootContext);
    }

    private MatchHandler getInnerHandler() {
        return errorIndex >= 0 ? new Handler() : null;
    }

    private boolean fixError(int fixIndex) {
        if (tryFixBySingleCharDeletion(fixIndex)) return true;
        int nextErrorAfterDeletion = errorIndex;

        Character bestInsertionCharacter = findBestSingleCharInsertion(fixIndex);
        if (bestInsertionCharacter == null) return true;
        int nextErrorAfterBestInsertion = errorIndex;

        Character bestReplacementCharacter = findBestSingleCharReplacement(fixIndex);
        if (bestReplacementCharacter == null) return true;
        int nextErrorAfterBestReplacement = errorIndex;

        int nextErrorAfterBestSingleCharFix =
                Math.max(Math.max(nextErrorAfterDeletion, nextErrorAfterBestInsertion), nextErrorAfterBestReplacement);
        if (nextErrorAfterBestSingleCharFix > fixIndex) {
            // we are able to overcome the error with a single char fix, so apply the best one found
            if (nextErrorAfterBestSingleCharFix == nextErrorAfterDeletion) {
                buffer.insertChar(fixIndex, DEL_ERROR);
                errorIndex = nextErrorAfterDeletion + 1;
                currentError.shiftIndexDeltaBy(1);
            } else if (nextErrorAfterBestSingleCharFix == nextErrorAfterBestInsertion) {
                // we need to insert the characters in reverse order, since we insert twice at the same location
                buffer.insertChar(fixIndex, bestInsertionCharacter);
                buffer.insertChar(fixIndex, INS_ERROR);
                errorIndex = nextErrorAfterBestInsertion + 2;
                currentError.shiftIndexDeltaBy(2);
            } else {
                // we need to insert the characters in reverse order, since we insert three times at the same location
                buffer.insertChar(fixIndex + 1, bestReplacementCharacter);
                buffer.insertChar(fixIndex + 1, INS_ERROR);
                buffer.insertChar(fixIndex, DEL_ERROR);
                errorIndex = nextErrorAfterBestReplacement + 5;
                currentError.shiftIndexDeltaBy(1);
            }
        } else {
            // we can't fix the error with a single char fix, so fall back to resynchronization
            if (buffer.charAt(fixIndex) == EOI) {
                buffer.insertChar(fixIndex, RESYNC_EOI);
                currentError.shiftIndexDeltaBy(1);
                return true;
            }
            buffer.insertChar(fixIndex, RESYNC);
            currentError.shiftIndexDeltaBy(1);
            performLocatingRun(buffer); // find the next parse error
        }
        return errorIndex == -1;
    }

    private boolean tryFixBySingleCharDeletion(int fixIndex) {
        buffer.insertChar(fixIndex, DEL_ERROR);
        boolean nowErrorFree = performLocatingRun(buffer);
        if (nowErrorFree) {
            currentError.shiftIndexDeltaBy(1); // compensate for the inserted DEL_ERROR char
        } else {
            buffer.undoCharInsertion(fixIndex);
            errorIndex = Math.max(errorIndex - 1, 0);
        }
        return nowErrorFree;
    }

    @SuppressWarnings( {"ConstantConditions"})
    private Character findBestSingleCharInsertion(int fixIndex) {
        GetStarterCharVisitor getStarterCharVisitor = new GetStarterCharVisitor();
        int bestNextErrorIndex = -1;
        Character bestChar = '\u0000'; // non-null default
        for (MatcherPath failedMatcherPath : currentError.getFailedMatchers()) {
            Character starterChar = failedMatcherPath.element.matcher.accept(getStarterCharVisitor);
            checkState(starterChar != null); // we should only have single character matchers
            if (starterChar == EOI) {
                continue; // we should never conjure up an EOI character (that would be cheating :)
            }
            buffer.insertChar(fixIndex, starterChar);
            buffer.insertChar(fixIndex, INS_ERROR);
            if (performLocatingRun(buffer)) {
                currentError.shiftIndexDeltaBy(2); // compensate for the inserted chars
                return null; // success, exit immediately
            }
            buffer.undoCharInsertion(fixIndex);
            buffer.undoCharInsertion(fixIndex);
            errorIndex = Math.max(errorIndex - 2, 0);

            if (bestNextErrorIndex < errorIndex) {
                bestNextErrorIndex = errorIndex;
                bestChar = starterChar;
            }
        }
        errorIndex = bestNextErrorIndex;
        return bestChar;
    }

    private Character findBestSingleCharReplacement(int fixIndex) {
        buffer.insertChar(fixIndex, DEL_ERROR);
        Character bestChar = findBestSingleCharInsertion(fixIndex + 2);
        if (bestChar == null) { // success, we found a fix that renders the complete input error free
            currentError
                    .shiftIndexDeltaBy(-1); // delta from DEL_ERROR char insertion and index shift by insertion method
        } else {
            buffer.undoCharInsertion(fixIndex);
            errorIndex = Math.max(errorIndex - 3, 0);
        }
        return bestChar;
    }

    /**
     * A {@link org.parboiled.MatchHandler} implementation that recognizes the special
     * {@link org.parboiled.support.Chars#RESYNC} character to overcome {@link InvalidInputError}s at the respective
     * error indices.
     */
    private class Handler implements MatchHandler {
        private final IsSingleCharMatcherVisitor isSingleCharMatcherVisitor = new IsSingleCharMatcherVisitor();
        private int fringeIndex;
        private MatcherPath lastMatchPath;

        public boolean match(MatcherContext<?> context) {
            Matcher matcher = context.getMatcher();
            if (matcher.accept(isSingleCharMatcherVisitor)) {
                if (prepareErrorLocation(context) && matcher.match(context)) {
                    if (fringeIndex < context.getCurrentIndex()) {
                        fringeIndex = context.getCurrentIndex();
                        lastMatchPath = context.getPath();
                    }
                    return true;
                }
                return false;
            }

            if (matcher.match(context)) {
                return true;
            }

            // if we didn't match we might have to resynchronize
            if (matcher instanceof SequenceMatcher) {
                switch(context.getCurrentChar()) {
                    case RESYNC:
                    case RESYNC_START:
                    case RESYNC_EOI:
                        // however we only resynchronize if we are at a RESYNC location and the matcher is a SequenceMatcher
                        // that has already matched at least one character and that is a parent of the last match
                        return qualifiesForResync(context) && resynchronize(context);
                }
                
                // check for timeout only on failures of sequences so as to not add too much overhead
                if (System.currentTimeMillis() - startTimeStamp > timeout) {
                    throw new TimeoutException(getRootMatcher(), buffer, lastParsingResult);
                }
            }
            return false;
        }

        private boolean qualifiesForResync(MatcherContext context) {
            if (context.getCurrentIndex() == context.getStartIndex() || !context.getPath().isPrefixOf(lastMatchPath)) {
                // if we have a sequence that hasn't match anything yet or is not a prefix we might still have to
                // resync on it if there is no other sequence parent anymore
                MatcherContext parent = context.getParent();
                while (parent != null) {
                    if (parent.getMatcher() instanceof SequenceMatcher) return false;
                    parent = parent.getParent();
                }
            }            
            return true;
        }

        private boolean prepareErrorLocation(MatcherContext context) {
            switch (context.getCurrentChar()) {
                case DEL_ERROR:
                    return willMatchDelError(context);
                case INS_ERROR:
                    return willMatchInsError(context);
                case RESYNC:
                case RESYNC_START:
                case RESYNC_EOI:
                    return false;
                default:
                    return true;
            }
        }

        private boolean willMatchDelError(MatcherContext context) {
            int preSkipIndex = context.getCurrentIndex();
            context.advanceIndex(2); // skip del marker char and illegal char
            if (!runTestMatch(context)) {
                // if we wouldn't succeed with the match do not swallow the ERROR char & Co
                context.setCurrentIndex(preSkipIndex);
                return false;
            }
            context.setStartIndex(context.getCurrentIndex());
            if (context.getParent() != null) context.getParent().markError();
            return true;
        }

        private boolean willMatchInsError(MatcherContext context) {
            int preSkipIndex = context.getCurrentIndex();
            context.advanceIndex(1); // skip ins marker char
            if (!runTestMatch(context)) {
                // if we wouldn't succeed with the match do not swallow the ERROR char
                context.setCurrentIndex(preSkipIndex);
                return false;
            }
            context.setStartIndex(context.getCurrentIndex());
            context.markError();
            return true;
        }

        private boolean runTestMatch(MatcherContext context) {
            TestMatcher testMatcher = new TestMatcher(context.getMatcher());
            MatcherContext testContext = testMatcher.getSubContext(context);
            return prepareErrorLocation(testContext) && testContext.runMatcher();
        }

        private boolean resynchronize(MatcherContext context) {
            context.markError();

            // create a node for the failed Sequence, taking ownership of all sub nodes created so far
            context.createNode();

            // by resyncing we flip an unmatched sequence to a matched one, so in order to keep the value stack
            // consistent we go into a special "error action mode" and execute the minimal set of actions underneath
            // the resync sequence
            rerunAndExecuteErrorActions(context);
            
            // skip over all characters that are not legal followers of the failed Sequence
            switch (context.getCurrentChar()) {
                case RESYNC:
                    // this RESYNC error is the last error, we establish the length of the bad sequence and
                    // change this RESYNC marker to a RESYNC_START / RESYNC_END block
                    context.advanceIndex(1); // gobble RESYNC marker
                    List<Matcher> followMatchers = new FollowMatchersVisitor().getFollowMatchers(context);
                    int endIndex = gobbleIllegalCharacters(context, followMatchers);
                    currentError.setEndIndex(endIndex);
                    buffer.replaceInsertedChar(currentError.getStartIndex() - 1, RESYNC_START);
                    buffer.insertChar(endIndex, RESYNC_END);
                    context.advanceIndex(1); // gobble RESYNC_END marker
                    break;

                case RESYNC_START:
                    // a RESYNC error we have already recovered from before
                    context.advanceIndex(1); // gobble RESYNC_START
                    while (context.getCurrentChar() != RESYNC_END) {
                        context.advanceIndex(1); // skip all characters up to the RESYNC_END
                        checkState(context.getCurrentChar() != EOI); // we MUST find a RESYNC_END before EOI
                    }
                    context.advanceIndex(1); // gobble RESYNC_END marker
                    break;
                
                case RESYNC_EOI:
                    // if we are resyncing on EOI we don't swallow anything
                    // we also do not have to update the currentError since we only hit this code here
                    // in the final run
                    break;

                default:
                    throw new IllegalStateException();
            }

            return true;
        }

        @SuppressWarnings( {"ConstantConditions"})
        private void rerunAndExecuteErrorActions(MatcherContext context) {
            // the context is for the resync action, which at this point has FAILED, i.e. ALL its sub actions haven't
            // had a chance to change the value stack, even the ones having run before the actual parse error matcher
            // so we need to rerun all sub matchers of the resync sequence up to the point of the parse error
            // and then run the minimal set of action in "error action mode"

            int savedCurrentIndex = context.getCurrentIndex();
            context.setCurrentIndex(context.getStartIndex()); // restart matching the resync sequence

            boolean preError = true;
            for (Matcher child : context.getMatcher().getChildren()) {
                if (preError && !child.getSubContext(context).runMatcher()) {
                    // run what will be the preceding matcher of all error actions
                    new EmptyMatcher().getSubContext(context).runMatcher();
                    context.setIntTag(1); // signal that at least one rule has run before the error actions
                    preError = false;
                }
                if (!preError) {
                    context.setInErrorRecovery(true);
                    List<ActionMatcher> errorActions = child.accept(new CollectResyncActionsVisitor());
                    checkState(errorActions != null);
                    for (ActionMatcher errorAction : errorActions) {
                        // execute the error actions without looking at their boolean results !!!
                        errorAction.getSubContext(context).runMatcher();
                    }
                    context.setInErrorRecovery(false);
                }
            }

            context.setCurrentIndex(savedCurrentIndex);
        }

        private int gobbleIllegalCharacters(MatcherContext context, List<Matcher> followMatchers) {
            while_loop:
            while (true) {
                char currentChar = context.getCurrentChar();
                if (currentChar == EOI) break;
                for (Matcher followMatcher : followMatchers) {
                    if (followMatcher.accept(new IsStarterCharVisitor(currentChar))) {
                        break while_loop;
                    }
                }
                context.advanceIndex(1);
            }
            return context.getCurrentIndex();
        }
    }

    /**
     * This MatcherVisitor collects the minimal set of actions that has to run underneath a resyncronization sequence
     * in order to maintain a consistent Value Stack state.
     */
    private static class CollectResyncActionsVisitor extends DefaultMatcherVisitor<List<ActionMatcher>> {
        private ImmutableLinkedList<SequenceMatcher> path = ImmutableLinkedList.nil();

        @Override
        public List<ActionMatcher> visit(ActionMatcher matcher) {
            return ImmutableList.of(matcher);
        }

        @Override
        public List<ActionMatcher> visit(FirstOfMatcher matcher) {
            for (Matcher child : matcher.getChildren()) {
                List<ActionMatcher> actions = child.accept(this);
                if (actions != null) return actions;
            }
            return null;
        }

        @Override
        public List<ActionMatcher> visit(OneOrMoreMatcher matcher) {
            return matcher.subMatcher.accept(this);
        }

        @Override
        public List<ActionMatcher> visit(SequenceMatcher matcher) {
            if (path.contains(matcher)) {
                return null;
            }

            ImmutableLinkedList<SequenceMatcher> previousPath = path;
            path = path.prepend(matcher);

            List<ActionMatcher> actions = new ArrayList<ActionMatcher>();
            for (Matcher sub : matcher.getChildren()) {
                List<ActionMatcher> subActions = sub.accept(this);
                if (subActions == null) return null;
                actions.addAll(subActions);
            }

            path = previousPath;
            return actions;
        }

        @Override
        public List<ActionMatcher> defaultValue(AbstractMatcher matcher) {
            return ImmutableList.of();
        }
    }
}
