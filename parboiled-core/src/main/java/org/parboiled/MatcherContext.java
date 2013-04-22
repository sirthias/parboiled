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

package org.parboiled;

import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.ImmutableLinkedList;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.BasicParseError;
import org.parboiled.errors.GrammarException;
import org.parboiled.errors.ParseError;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.matchers.*;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.support.*;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import static org.parboiled.errors.ErrorUtils.printParseError;
import static org.parboiled.common.Preconditions.*;
import static org.parboiled.matchers.MatcherUtils.unwrap;

/**
 * <p>The Context implementation orchestrating most of the matching process.</p>
 * <p>The parsing process works as following:</br>
 * After the rule tree (which is in fact a directed and potentially even cyclic graph of {@link Matcher} instances)
 * has been created a root MatcherContext is instantiated for the root rule (Matcher).
 * A subsequent call to {@link #runMatcher()} starts the parsing process.</p>
 * <p>The MatcherContext delegates to a given {@link MatchHandler} to call {@link Matcher#match(MatcherContext)},
 * passing itself to the Matcher which executes its logic, potentially calling sub matchers.
 * For each sub matcher the matcher creates/initializes a subcontext with {@link Matcher#getSubContext(MatcherContext)}
 * and then calls {@link #runMatcher()} on it.</p>
 * <p>This basically creates a stack of MatcherContexts, each corresponding to their rule matchers. The MatcherContext
 * instances serve as companion objects to the matchers, providing them with support for building the
 * parse tree nodes, keeping track of input locations and error recovery.</p>
 * <p>At each point during the parsing process the matchers and action expressions have access to the current
 * MatcherContext and all "open" parent MatcherContexts through the {@link #getParent()} chain.</p>
 * <p>For performance reasons subcontext instances are reused instead of being recreated. If a MatcherContext instance
 * returns null on a {@link #getMatcher()} call it has been retired (is invalid) and is waiting to be reinitialized
 * with a new Matcher by its parent</p>
 */
public class MatcherContext<V> implements Context<V> {

    private final InputBuffer inputBuffer;
    private final ValueStack<V> valueStack;
    private final List<ParseError> parseErrors;
    private final MatchHandler matchHandler;
    private final MatcherContext<V> parent;
    private final int level;
    private final boolean fastStringMatching;
    private final Set<MatcherPosition> memoizedMismatches;

    private MatcherContext<V> subContext;
    private int startIndex;
    private int currentIndex;
    private char currentChar;
    private Matcher matcher;
    private Node<V> node;
    private ImmutableLinkedList<Node<V>> subNodes = ImmutableLinkedList.nil();
    private MatcherPath path;
    private int intTag;
    private boolean hasError;
    private boolean nodeSuppressed;
    private boolean inErrorRecovery;

    /**
     * Initializes a new root MatcherContext.
     *
     * @param inputBuffer        the InputBuffer for the parsing run
     * @param valueStack         the ValueStack instance to use for the parsing run
     * @param parseErrors        the parse error list to create ParseError objects in
     * @param matchHandler       the MatcherHandler to use for the parsing run
     * @param matcher            the root matcher
     * @param fastStringMatching <p>Fast string matching "short-circuits" the default practice of treating string rules
     *                           as simple Sequence of character rules. When fast string matching is enabled strings are
     *                           matched at once, without relying on inner CharacterMatchers. Even though this can lead
     *                           to significant increases of parsing performance it does not play well with error
     *                           reporting and recovery, which relies on character level matches.
     *                           Therefore the {@link org.parboiled.parserunners.ReportingParseRunner} and {@link org.parboiled.parserunners.RecoveringParseRunner}
     *                           implementations only enable fast string matching during their basic first parsing run
     *                           and disable it once the input has proven to contain errors.</p>
     */
    public MatcherContext(InputBuffer inputBuffer, ValueStack<V> valueStack, List<ParseError> parseErrors,
                          MatchHandler matchHandler, Matcher matcher, boolean fastStringMatching) {
        this(checkArgNotNull(inputBuffer, "inputBuffer"), checkArgNotNull(valueStack, "valueStack"),
                checkArgNotNull(parseErrors, "parseErrors"), checkArgNotNull(matchHandler, "matchHandler"),
                null, 0, fastStringMatching,  new HashSet<MatcherPosition>());
        this.currentChar = inputBuffer.charAt(0);
        this.matcher = ProxyMatcher.unwrap(checkArgNotNull(matcher, "matcher"));
        this.nodeSuppressed = matcher.isNodeSuppressed();
    }

    private MatcherContext(InputBuffer inputBuffer, ValueStack<V> valueStack, List<ParseError> parseErrors,
                           MatchHandler matchHandler, MatcherContext<V> parent, int level, boolean fastStringMatching,
                           Set<MatcherPosition> memoizedMismatches) {
        this.inputBuffer = inputBuffer;
        this.valueStack = valueStack;
        this.parseErrors = parseErrors;
        this.matchHandler = matchHandler;
        this.parent = parent;
        this.level = level;
        this.fastStringMatching = fastStringMatching;
        this.memoizedMismatches = memoizedMismatches;
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

    //////////////////////////////// CONTEXT INTERFACE ////////////////////////////////////

    public MatcherContext<V> getParent() {
        return parent;
    }

    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public Matcher getMatcher() {
        return matcher;
    }

    public char getCurrentChar() {
        return currentChar;
    }

    public List<ParseError> getParseErrors() {
        return parseErrors;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public MatcherPath getPath() {
        if (path == null) {
            path = new MatcherPath(new MatcherPath.Element(matcher, startIndex, level),
                    parent != null ? parent.getPath() : null);
        }
        return path;
    }

    public int getLevel() {
        return level;
    }

    public boolean fastStringMatching() {
        return fastStringMatching;
    }

    public ImmutableLinkedList<Node<V>> getSubNodes() {
        return matcher.isNodeSkipped() ? subNodes : getSubNodes(subNodes, ImmutableLinkedList.<Node<V>>nil());
    }

    private static <V> ImmutableLinkedList<Node<V>> getSubNodes(ImmutableLinkedList<Node<V>> remaining,
                                                                ImmutableLinkedList<Node<V>> tail) {
        while (!remaining.isEmpty()) {
            Node<V> head = remaining.head();
            if (head.getMatcher().isNodeSkipped()) {
                tail = getSubNodes(((ImmutableLinkedList<Node<V>>)head.getChildren()), tail);
            } else {
                tail = tail.prepend(head);
            }
            remaining = remaining.tail();
        }
        return tail;
    }

    public boolean inPredicate() {
        return matcher instanceof TestMatcher || matcher instanceof TestNotMatcher ||
                parent != null && parent.inPredicate();
    }

    public boolean inErrorRecovery() {
        return inErrorRecovery;
    }
    
    public boolean isNodeSuppressed() {
        return nodeSuppressed;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getMatch() {
        checkActionContext();
        MatcherContext prevContext = subContext;
        if (hasError) {
            Node prevNode = prevContext.node;
            return prevNode != null ? ParseTreeUtils.getNodeText(prevNode, inputBuffer) : "";
        }
        return inputBuffer.extract(prevContext.startIndex, prevContext.currentIndex);
    }

    public char getFirstMatchChar() {
        checkActionContext();
        int ix = subContext.startIndex;
        if (subContext.currentIndex <= ix) {
            throw new GrammarException("getFirstMatchChar called but previous rule did not match anything");
        }
        return inputBuffer.charAt(ix);
    }

    public int getMatchStartIndex() {
        checkActionContext();
        return subContext.startIndex;
    }

    public int getMatchEndIndex() {
        checkActionContext();
        return subContext.currentIndex;
    }

    public int getMatchLength() {
        checkActionContext();
        return subContext.currentIndex - subContext.getStartIndex();
    }

    public Position getPosition() {
        return inputBuffer.getPosition(currentIndex);
    }

    public IndexRange getMatchRange() {
        checkActionContext();
        return new IndexRange(subContext.startIndex, subContext.currentIndex);
    }

    private void checkActionContext() {
        // make sure all the constraints are met
        Checks.ensure(unwrap(matcher) instanceof SequenceMatcher && intTag > 0 &&
                subContext.matcher instanceof ActionMatcher,
                "Illegal call to getMatch(), getMatchStartIndex(), getMatchEndIndex() or getMatchRange(), " +
                        "only valid in Sequence rule actions that are not in first position");
    }

    public ValueStack<V> getValueStack() {
        return valueStack;
    }

    //////////////////////////////// PUBLIC ////////////////////////////////////

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public void setStartIndex(int startIndex) {
        checkArgument(startIndex >= 0);
        this.startIndex = startIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        checkArgument(currentIndex >= 0);
        this.currentIndex = currentIndex;
        currentChar = inputBuffer.charAt(currentIndex);
    }
    
    public void setInErrorRecovery(boolean flag) {
        inErrorRecovery = flag;
    }

    public void advanceIndex(int delta) {
        currentIndex += delta;
        currentChar = inputBuffer.charAt(currentIndex);
    }

    public Node<V> getNode() {
        return node;
    }

    public int getIntTag() {
        return intTag;
    }

    public void setIntTag(int intTag) {
        this.intTag = intTag;
    }

    public void markError() {
        if (!hasError) {
            hasError = true;
            if (parent != null) parent.markError();
        }
    }

    public Boolean hasMismatched() {
        return memoizedMismatches.contains(MatcherPosition.at(matcher, currentIndex));
    }

    public void memoizeMismatch() {
    	memoizedMismatches.add(MatcherPosition.at(matcher, currentIndex));
    }

    @SuppressWarnings({"ConstantConditions"})
    public void createNode() {
        if (!nodeSuppressed) {
            node = new NodeImpl<V>(matcher, getSubNodes(), startIndex, currentIndex,
                    valueStack.isEmpty() ? null : valueStack.peek(), hasError);
            if (parent != null) {
                parent.subNodes = parent.subNodes.prepend(node);
            }
        }
    }

    public final MatcherContext<V> getBasicSubContext() {
        if (subContext == null) {
            // init new level
            subContext = new MatcherContext<V>(inputBuffer, valueStack, parseErrors, matchHandler, this, level + 1,
                        fastStringMatching, memoizedMismatches);
        } else {
            subContext.path = null; // we always need to reset the MatcherPath, even for actions
        }
        return subContext;
    }

    public final MatcherContext<V> getSubContext(Matcher matcher) {
        MatcherContext<V> sc = getBasicSubContext();
        sc.matcher = matcher;
        sc.startIndex = sc.currentIndex = currentIndex;
        sc.currentChar = currentChar;
        sc.node = null;
        sc.subNodes = ImmutableLinkedList.nil();
        sc.nodeSuppressed = nodeSuppressed || this.matcher.areSubnodesSuppressed() || matcher.isNodeSuppressed();
        sc.hasError = false;
        return sc;
    }

    public boolean runMatcher() {
        try {
            if (matchHandler.match(this)) {
                if (parent != null) {
                    parent.currentIndex = currentIndex;
                    parent.currentChar = currentChar;
                }
                matcher = null; // "retire" this context
                return true;
            }
            matcher = null; // "retire" this context until is "activated" again by a getSubContext(...) on the parent
            return false;
        } catch (ParserRuntimeException e) {
            throw e; // don't wrap, just bubble up
        } catch (RecoveringParseRunner.TimeoutException e) {
            throw e; // don't wrap, just bubble up
        } catch (Throwable e) {
            throw new ParserRuntimeException(e,
                    printParseError(new BasicParseError(inputBuffer, currentIndex,
                            StringUtils.escape(String.format("Error while parsing %s '%s' at input position",
                                    matcher instanceof ActionMatcher ? "action" : "rule", getPath())))) + '\n' + e);
        }
    }
}
