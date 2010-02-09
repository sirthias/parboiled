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

import org.jetbrains.annotations.NotNull;
import org.parboiled.common.ImmutableList;
import org.parboiled.common.Preconditions;
import org.parboiled.exceptions.ActionException;
import org.parboiled.exceptions.ParserRuntimeException;
import org.parboiled.matchers.*;
import org.parboiled.support.*;
import static org.parboiled.support.ParseTreeUtils.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The Context implementation orchestrating most of the matching process.</p>
 * <p>The parsing process works as following:</br>
 * After the rule tree (which is in fact a directed and potentially even cyclic graph of Matcher instances) has been
 * created a root MatcherContext is instantiated for the root rule (Matcher).
 * A subsequent call to {@link #runMatcher()} starts the parsing process.</p>
 * <p>The MatcherContext essentially calls {@link Matcher#match(MatcherContext)} passing itself to the Matcher
 * which executes its logic, potentially calling sub matchers. For each sub matcher the matcher calls
 * {@link #runMatcher()} on its Context, which creates a sub context of the
 * current MatcherContext and runs the given sub matcher in it.</p>
 * <p>This basically creates a stack of MatcherContexts, each corresponding to their rule matchers. The MatcherContext
 * instances serve as a kind of companion objects to the matchers, providing them with support for building the
 * parse tree nodes, keeping track of input locations and error recovery.</p>
 * <p>At each point during the parsing process the matchers and action expressions have access to the current
 * MatcherContext and all "open" parent MatcherContexts through the {@link #getParent()} chain.</p>
 *
 * @param <V> the node value type
 */
public class MatcherContext<V> implements Context<V> {

    public static class Globals<V> {
        public final List<ParseError<V>> parseErrors = new ArrayList<ParseError<V>>();
        public Node<V> lastNode;
        public ParseErrorMarker<V> currentErrorMarker;
        public ParsingState parsingState = ParsingState.Parsing;
    }

    // also global but kept in each MatcherContext instance for faster access
    private final InputBuffer inputBuffer;
    private final Globals<V> globals;
    private final int level;

    private MatcherContext<V> parent;
    private MatcherContext<V> subContext;
    private InputLocation startLocation;
    private InputLocation currentLocation;
    private Matcher<V> matcher;
    private Node<V> node;
    private List<Node<V>> subNodes;
    private V nodeValue;
    private int intTag;
    private boolean belowLeafLevel;
    private boolean recoveryCandidate;

    public MatcherContext(@NotNull InputBuffer inputBuffer, @NotNull InputLocation startLocation,
                          @NotNull Globals<V> globals, Matcher<V> matcher) {
        this(inputBuffer, globals, 0);
        setStartLocation(startLocation);
        this.matcher = matcher;
    }

    private MatcherContext(InputBuffer inputBuffer, Globals<V> globals, int level) {
        this.inputBuffer = inputBuffer;
        this.globals = globals;
        this.level = level;
    }

    @Override
    public String toString() {
        return getPath().toString();
    }

    //////////////////////////////// CONTEXT INTERFACE ////////////////////////////////////

    public MatcherContext<V> getParent() {
        return parent;
    }

    public MatcherContext<V> getSubContext() {
        return subContext != null && subContext.matcher != null ? subContext : null;
    }

    @NotNull
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    public InputLocation getStartLocation() {
        return startLocation;
    }

    public Matcher<V> getMatcher() {
        return matcher;
    }

    @NotNull
    public List<ParseError<V>> getParseErrors() {
        return globals.parseErrors;
    }

    public void addParseError(@NotNull ParseError<V> error) {
        globals.parseErrors.add(error);
    }

    public InputLocation getCurrentLocation() {
        return currentLocation;
    }

    public String getNodeText(Node<?> node) {
        return ParseTreeUtils.getNodeText(node, inputBuffer);
    }

    public Character getNodeChar(Node<?> node) {
        return ParseTreeUtils.getNodeChar(node, inputBuffer);
    }

    @NotNull
    public MatcherPath<V> getPath() {
        return new MatcherPath<V>(this);
    }

    public int getLevel() {
        return level;
    }

    public V getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(V value) {
        this.nodeValue = value;
    }

    public V getTreeValue() {
        V treeValue = nodeValue;
        if (subNodes != null) {
            int i = subNodes.size();
            while (treeValue == null && i-- > 0) {
                treeValue = subNodes.get(i).getValue();
            }
        }
        return treeValue;
    }

    public Node<V> getNodeByPath(String path) {
        return findNodeByPath(subNodes, path);
    }

    public Node<V> getNodeByLabel(String labelPrefix) {
        return subNodes != null ? findNode(subNodes, new LabelPrefixPredicate<V>(labelPrefix)) : null;
    }

    public Node<V> getLastNode() {
        return globals.lastNode;
    }

    public List<Node<V>> getSubNodes() {
        return subNodes != null ? ImmutableList.copyOf(subNodes) : ImmutableList.<Node<V>>of();
    }

    public boolean inPredicate() {
        return matcher instanceof TestMatcher || parent != null && parent.inPredicate();
    }

    public ParsingState getParsingState() {
        return globals.parsingState;
    }

    public boolean isBelowLeafLevel() {
        return belowLeafLevel;
    }

    public InputLocation getCurrentParseErrorLocation() {
        return globals.currentErrorMarker.getLocation();
    }

    public MatcherPath<V> getCurrentParseErrorPath() {
        return globals.currentErrorMarker.getPath();
    }

    public Context<V> getCurrentRecoveryContext() {
        MatcherContext<V> context = this;
        while (context != null) {
            if (context.recoveryCandidate) return context;
            context = context.getParent();
        }
        return null;
    }

    public Matcher<V> getFailedMatcher() {
        if (recoveryCandidate) {
            Matcher<V>[] errorMatchers = getCurrentParseErrorPath().getMatchers();
            Preconditions.checkState(errorMatchers[level] == matcher);
            if (level + 1 < errorMatchers.length) return errorMatchers[level + 1];
        }
        return null;
    }

    public void injectVirtualInput(char virtualInputChar) {
        currentLocation = currentLocation.insertVirtualInput(virtualInputChar);
    }

    public void injectVirtualInput(String virtualInputText) {
        currentLocation = currentLocation.insertVirtualInput(virtualInputText);
    }

    //////////////////////////////// PUBLIC ////////////////////////////////////

    public void setCurrentLocation(InputLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void setStartLocation(InputLocation location) {
        startLocation = currentLocation = location;
    }

    public void advanceInputLocation() {
        setCurrentLocation(currentLocation.advance(inputBuffer));
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

    public void createNode() {
        if (belowLeafLevel || matcher instanceof TestMatcher) {
            return;
        }
        if (matcher.isWithoutNode()) {
            if (parent != null) parent.addChildNodes(subNodes);
            return;
        }
        node = new NodeImpl<V>(matcher.getLabel(), subNodes, startLocation, currentLocation, getTreeValue());
        if (parent != null) parent.addChildNode(node);
        globals.lastNode = node;
    }

    public void addChildNode(Node<V> node) {
        if (subNodes == null) subNodes = new ArrayList<Node<V>>();
        subNodes.add(node);
    }

    public void addChildNodes(List<Node<V>> nodes) {
        if (subNodes == null) subNodes = new ArrayList<Node<V>>();
        subNodes.addAll(nodes);
    }

    public MatcherContext<V> getSubContext(Matcher<V> matcher) {
        if (subContext == null) {
            // we need to introduce a new level
            subContext = new MatcherContext<V>(inputBuffer, globals, level + 1);
            subContext.parent = this;
        }

        // normally we just reuse the existing subContext instance
        subContext.matcher = ProxyMatcher.unwrap(matcher);
        subContext.setStartLocation(currentLocation);
        subContext.node = null;
        subContext.subNodes = null;
        subContext.nodeValue = null;
        subContext.belowLeafLevel = belowLeafLevel || this.matcher.isLeaf();
        subContext.recoveryCandidate = false;
        return subContext;
    }

    /**
     * Runs the contexts matcher.
     *
     * @return true if matched
     */
    @SuppressWarnings({"unchecked", "fallthrough"})
    public boolean runMatcher() {
        boolean matched = false;
        try {
            matched = matcher.match(this);
            if (!matched) {
                switch (globals.parsingState) {
                    case Parsing:
                        globals.currentErrorMarker.mark(this);
                        break;

                    case SeekingToParseError:
                        if (!globals.currentErrorMarker.matchesState(this)) break;

                        // we just failed the matcher causing the previously recorded parse error
                        // so mark all contexts in the current stack as potential recovery candidates
                        MatcherContext<V> context = this;
                        while (context != null) {
                            context.recoveryCandidate = true;
                            context = context.getParent();
                        }
                        globals.parsingState = ParsingState.Recovering;
                        // fall-through

                    case Recovering:
                        if (recoveryCandidate) {
                            Matcher<V> recoveryRule = matcher.getRecoveryMatcher();
                            if (recoveryRule != null) {
                                if (getSubContext(recoveryRule).runMatcher()) {
                                    globals.currentErrorMarker = globals.currentErrorMarker.getNext();
                                    globals.parsingState = globals.currentErrorMarker.isValid() ?
                                            ParsingState.SeekingToParseError : ParsingState.Parsing;
                                    matched = true;
                                }
                            }
                        }
                        break;
                }
            }

        } catch (ActionException e) {
            addParseError(new ParseError<V>(currentLocation, getPath(), e.getMessage()));
        } catch (ParserRuntimeException e) {
            throw e; // don't wrap, just bubble up

        } catch (Throwable e) {
            throw new ParserRuntimeException(e,
                    printParseError(new ParseError<V>(currentLocation, getPath(),
                            String.format("Error during execution of parsing %s '%s' at input position",
                                    matcher instanceof ActionMatcher ? "action" : "rule", getPath())), inputBuffer));
        }

        if (matched && parent != null) {
            parent.setCurrentLocation(currentLocation);
        }
        matcher = null; // "retire" this context until is "activated" again by a getSubContext(...) on the parent
        return matched;
    }

    /**
     * Gets the Characters that can legally follow the currently running matcher in this context and/or any
     * ancestor contexts. Used during parse error recovery to determine the resynchronization characters.
     *
     * @return the Characters that can legally follow the currently running matcher according to the grammar
     */
    @SuppressWarnings({"unchecked"})
    public Characters getCurrentFollowerChars() {
        Characters chars = Characters.NONE;
        MatcherContext<V> parent = this;
        while (parent != null) {
            if (parent.getMatcher() instanceof FollowMatcher) {
                FollowMatcher<V> followMatcher = (FollowMatcher<V>) parent.getMatcher();
                chars = chars.add(followMatcher.getFollowerChars(parent));
                if (!chars.contains(Chars.EMPTY)) return chars;
            }
            parent = parent.parent;
        }
        return chars.remove(Chars.EMPTY).add(Chars.EOI);
    }

}
