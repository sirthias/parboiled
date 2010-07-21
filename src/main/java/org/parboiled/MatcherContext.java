/*
 * Copyright (C) 2009 Mathias Doenitz
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
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.parboiled.common.Reference;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.BasicParseError;
import org.parboiled.errors.ParseError;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.matchers.*;
import org.parboiled.support.*;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.parboiled.errors.ErrorUtils.printParseError;

/**
 * <p>The Context implementation orchestrating most of the matching process.</p>
 * <p>The parsing process works as following:</br>
 * After the rule tree (which is in fact a directed and potentially even cyclic graph of {@link Matcher} instances)
 * has been created a root MatcherContext is instantiated for the root rule (Matcher).
 * A subsequent call to {@link #runMatcher()} starts the parsing process.</p>
 * <p>The MatcherContext delegates to a given {@link MatchHandler} to call {@link Matcher#match(MatcherContext)},
 * passing itself to the Matcher which executes its logic, potentially calling sub matchers.
 * For each sub matcher the matcher creates/initializes a sub context with {@link Matcher#getSubContext(MatcherContext)}
 * and then calls {@link #runMatcher()} on it.</p>
 * <p>This basically creates a stack of MatcherContexts, each corresponding to their rule matchers. The MatcherContext
 * instances serve as companion objects to the matchers, providing them with support for building the
 * parse tree nodes, keeping track of input locations and error recovery.</p>
 * <p>At each point during the parsing process the matchers and action expressions have access to the current
 * MatcherContext and all "open" parent MatcherContexts through the {@link #getParent()} chain.</p>
 * <p>For performance reasons sub context instances are reused instead of being recreated. If a MatcherContext instance
 * returns null on a {@link #getMatcher()} call it has been retired (is invalid) and is waiting to be reinitialized
 * with a new Matcher by its parent</p>
 */
public class MatcherContext implements Context {

    static class StackElement {
        private Object value;
        private StackElement next;

        private StackElement(Object value, StackElement next) {
            this.value = value;
            this.next = next;
        }
    }

    private final InputBuffer inputBuffer;
    private final List<ParseError> parseErrors;
    private final MatchHandler matchHandler;
    private final MatcherContext parent;
    private final int level;
    private final boolean fastStringMatching;

    private MatcherContext subContext;
    private int startIndex;
    int currentIndex; // package private because of direct access from ActionMatcher
    private char currentChar;
    private Matcher matcher;
    private Node node;
    StackElement valueStack; // package private because of direct access from ActionMatcher
    private List<Node> subNodes = ImmutableList.of();
    private int intTag;
    private boolean hasError;
    private boolean nodeSuppressed;

    /**
     * Initializes a new root MatcherContext.
     *
     * @param inputBuffer        the InputBuffer for the parsing run
     * @param parseErrors        the parse error list to create ParseError objects in
     * @param matchHandler       the MatcherHandler to use for the parsing run
     * @param matcher            the root matcher
     * @param fastStringMatching <p>Fast string matching "short-circuits" the default practice of treating string rules
     *                           as simple Sequence of character rules. When fast string matching is enabled strings are
     *                           matched at once, without relying on inner CharacterMatchers. Even though this can lead
     *                           to significant increases of parsing performance it does not play well with error
     *                           reporting and recovery, which relies on character level matches.
     *                           Therefore the {@link ReportingParseRunner} and {@link RecoveringParseRunner}
     *                           implementations only enable fast string matching during their basic first parsing run
     *                           and disable it once the input has proven to contain errors.</p>
     */
    public MatcherContext(@NotNull InputBuffer inputBuffer, @NotNull List<ParseError> parseErrors,
                          @NotNull MatchHandler matchHandler, @NotNull Matcher matcher,
                          boolean fastStringMatching) {
        this(inputBuffer, parseErrors, matchHandler, null, 0, fastStringMatching);
        this.currentChar = inputBuffer.charAt(0);
        this.matcher = ProxyMatcher.unwrap(matcher);
        this.nodeSuppressed = matcher.isNodeSuppressed();
    }

    private MatcherContext(@NotNull InputBuffer inputBuffer, @NotNull List<ParseError> parseErrors,
                           @NotNull MatchHandler matchHandler, MatcherContext parent, int level,
                           boolean fastStringMatching) {
        this.inputBuffer = inputBuffer;
        this.parseErrors = parseErrors;
        this.matchHandler = matchHandler;
        this.parent = parent;
        this.level = level;
        this.fastStringMatching = fastStringMatching;
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

    //////////////////////////////// CONTEXT INTERFACE ////////////////////////////////////

    public MatcherContext getParent() {
        return parent;
    }

    public MatcherContext getSubContext() {
        // if the subContext has a null matcher it has been retired and is invalid
        return subContext != null && subContext.matcher != null ? subContext : null;
    }

    @NotNull
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

    @NotNull
    public List<ParseError> getParseErrors() {
        return parseErrors;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @NotNull
    public MatcherPath getPath() {
        return new MatcherPath(this);
    }

    public int getLevel() {
        return level;
    }

    public boolean fastStringMatching() {
        return fastStringMatching;
    }

    @NotNull
    public List<Node> getSubNodes() {
        return subNodes;
    }

    public boolean inPredicate() {
        return matcher instanceof TestMatcher || matcher instanceof TestNotMatcher ||
                parent != null && parent.inPredicate();
    }

    public boolean isNodeSuppressed() {
        return nodeSuppressed;
    }

    public boolean hasError() {
        return hasError;
    }

    public String getMatch() {
        MatcherContext sequenceContext = getPrevSequenceContext();
        MatcherContext prevContext = sequenceContext.subContext;
        return sequenceContext.hasError ? ParseTreeUtils.getNodeText(prevContext.node, inputBuffer) :
                inputBuffer.extract(prevContext.startIndex, prevContext.currentIndex);
    }

    public int getMatchStartIndex() {
        MatcherContext sequenceContext = getPrevSequenceContext();
        return sequenceContext.subContext.startIndex;
    }

    public int getMatchEndIndex() {
        MatcherContext sequenceContext = getPrevSequenceContext();
        return sequenceContext.subContext.currentIndex;
    }

    public List<Object> getValueStack() {
        return new AbstractList<Object>() {
            @Override
            public Object get(int index) { return get(valueStack, index).value; }

            private StackElement get(StackElement head, int index) {
                if (head == null) throw new IndexOutOfBoundsException();
                return index == 0 ? head : get(head.next, index - 1);
            }

            @Override
            public int size() { return size(valueStack); }

            private int size(StackElement head) {
                return head == null ? 0 : size(head.next) + 1;
            }

            @Override
            public Iterator<Object> iterator() {
                final Reference<StackElement> cursor = new Reference<StackElement>(valueStack);
                return new Iterator<Object>() {
                    public boolean hasNext() {
                        return cursor.isSet();
                    }
                    public Object next() {
                        StackElement head = cursor.get();
                        Object value = head.value;
                        cursor.set(head.next);
                        return value;
                    }
                    public void remove() { throw new UnsupportedOperationException(); }
                };
            }
        };
    }

    public void push(Object value) {
        valueStack = new StackElement(value, valueStack);
    }

    public void push(Object... values) {
        for (Object value : values) push(value);
    }

    public Object pop() {
        Checks.ensure(valueStack != null, "Cannot pop from an empty value stack");
        Object value = valueStack.value;
        valueStack = valueStack.next;
        return value;
    }

    public Object peek() {
        Checks.ensure(valueStack != null, "Cannot peek into an empty value stack");
        return valueStack.value;
    }

    public void poke(Object value) {
        Checks.ensure(valueStack != null, "Cannot poke into an empty value stack");
        valueStack.value = value;
    }

    public void swap() {
        Checks.ensure(valueStack != null && valueStack.next != null,
                "Swap not allowed on value stack with less than two elements");
        Object temp = valueStack.value;
        StackElement down1 = valueStack.next;
        valueStack.value = down1.value;
        down1.value = temp;
    }

    public void swap3() {
        Checks.ensure(valueStack != null && valueStack.next != null && valueStack.next.next != null,
                "Swap3 not allowed on value stack with less than 3 elements");
        StackElement down2 = valueStack.next.next;
        Object temp = valueStack.value;
        valueStack.value = down2.value;
        down2.value = temp;
    }

    public void swap4() {
        Checks.ensure(valueStack != null && valueStack.next != null && valueStack.next.next != null &&
                valueStack.next.next.next != null,
                "Swap4 not allowed on value stack with less than 4 elements");
        StackElement down1 = valueStack.next;
        StackElement down2 = down1.next;
        StackElement down3 = down2.next;
        Object temp = valueStack.value;
        valueStack.value = down3.value;
        down3.value = temp;
        temp = down1.value;
        down1.value = down2.value;
        down2.value = temp;
    }

    public void swap5() {
        Checks.ensure(valueStack != null && valueStack.next != null && valueStack.next.next != null &&
                valueStack.next.next.next != null && valueStack.next.next.next.next != null,
                "Swap5 not allowed on value stack with less than 5 elements");
        StackElement down1 = valueStack.next;
        StackElement down3 = down1.next.next;
        StackElement down4 = down3.next;
        Object temp = valueStack.value;
        valueStack.value = down4.value;
        down4.value = temp;
        temp = down1.value;
        down1.value = down3.value;
        down3.value = temp;
    }

    public void swap6() {
        Checks.ensure(valueStack != null && valueStack.next != null && valueStack.next.next != null &&
                valueStack.next.next.next != null && valueStack.next.next.next.next != null &&
                valueStack.next.next.next.next.next != null,
                "Swap6 not allowed on value stack with less than 6 elements");
        StackElement down1 = valueStack.next;
        StackElement down2 = down1.next;
        StackElement down3 = down2.next;
        StackElement down4 = down3.next;
        StackElement down5 = down4.next;
        Object temp = valueStack.value;
        valueStack.value = down5.value;
        down5.value = temp;
        temp = down1.value;
        down1.value = down4.value;
        down4.value = temp;
        temp = down2.value;
        down2.value = down3.value;
        down3.value = temp;
    }

    private MatcherContext getPrevSequenceContext() {
        MatcherContext actionContext = this;

        // we need to find the deepest currently active context
        while (actionContext.subContext != null && actionContext.subContext.matcher != null) {
            actionContext = actionContext.subContext;
        }
        MatcherContext sequenceContext = actionContext.getParent();

        // make sure all the constraints are met
        Checks.ensure(
                ProxyMatcher.unwrap(VarFramingMatcher.unwrap(sequenceContext.matcher)) instanceof SequenceMatcher &&
                        sequenceContext.intTag > 0 &&
                        actionContext.matcher instanceof ActionMatcher,
                "Illegal getPrevValue() call, only valid in Sequence rule actions that are not in first position");
        return sequenceContext;
    }

    //////////////////////////////// PUBLIC ////////////////////////////////////

    public void setMatcher(Matcher matcher) {
        this.matcher = matcher;
    }

    public void setStartIndex(int startIndex) {
        Preconditions.checkArgument(startIndex >= 0);
        this.startIndex = startIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        Preconditions.checkArgument(currentIndex >= 0);
        this.currentIndex = currentIndex;
        currentChar = inputBuffer.charAt(currentIndex);
    }

    public void advanceIndex(int delta) {
        if (currentIndex != Characters.EOI) currentIndex += delta;
        currentChar = inputBuffer.charAt(currentIndex);
    }

    public Node getNode() {
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

    public void clearNodeSuppression() {
        if (nodeSuppressed) {
            nodeSuppressed = false;
            if (parent != null) parent.clearNodeSuppression();
        }
    }

    @SuppressWarnings({"ConstantConditions"})
    public void createNode() {
        if (!nodeSuppressed && !matcher.isNodeSkipped()) {
            node = new NodeImpl(matcher, subNodes, startIndex, currentIndex,
                    valueStack != null ? valueStack.value : null, hasError);

            MatcherContext nodeParentContext = parent;
            if (nodeParentContext != null) {
                while (nodeParentContext.getMatcher().isNodeSkipped()) {
                    nodeParentContext = nodeParentContext.getParent();
                    Checks.ensure(nodeParentContext != null, "Root rule must not be marked @SkipNode");
                }
                nodeParentContext.addChildNode(node);
            }
        }
    }

    public final MatcherContext getBasicSubContext() {
        return subContext == null ?

                // init new level
                subContext = new MatcherContext(inputBuffer, parseErrors, matchHandler, this, level + 1,
                        fastStringMatching) :

                // reuse existing instance
                subContext;
    }

    public final MatcherContext getSubContext(Matcher matcher) {
        MatcherContext sc = getBasicSubContext();
        sc.matcher = matcher;
        sc.startIndex = sc.currentIndex = currentIndex;
        sc.currentChar = currentChar;
        sc.node = null;
        sc.subNodes = ImmutableList.of();
        sc.valueStack = valueStack;
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
                    parent.valueStack = valueStack;
                }
                matcher = null; // "retire" this context
                return true;
            }
            matcher = null; // "retire" this context until is "activated" again by a getSubContext(...) on the parent
            return false;
        } catch (ParserRuntimeException e) {
            throw e; // don't wrap, just bubble up
        } catch (Throwable e) {
            throw new ParserRuntimeException(e,
                    printParseError(new BasicParseError(inputBuffer, currentIndex,
                            StringUtils.escape(String.format("Error while parsing %s '%s' at input position",
                                    matcher instanceof ActionMatcher ? "action" : "rule", getPath()))), inputBuffer));
        }
    }

    //////////////////////////////// PRIVATE ////////////////////////////////////

    @SuppressWarnings({"fallthrough"})
    private void addChildNode(@NotNull Node node) {
        int size = subNodes.size();
        if (size == 0) {
            subNodes = ImmutableList.of(node);
            return;
        }
        if (size == 1) {
            Node node0 = subNodes.get(0);
            subNodes = new ArrayList<Node>(4);
            subNodes.add(node0);
        }
        subNodes.add(node);
    }
}
