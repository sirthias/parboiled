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
            Preconditions.checkState(errorMatchers[level] == this);
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
        if (belowLeafLevel) {
            return;
        }
        node = new NodeImpl<V>(matcher.getLabel(), subNodes, startLocation, currentLocation, getTreeValue());
        if (!(matcher instanceof TestMatcher)) { // special case: TestMatchers do not add nodes
            if (parent != null) {
                parent.addChildNode(node);
            }
            globals.lastNode = node;
        }
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
                            Matcher<V> recoveryRule = (Matcher<V>) matcher.getRecoveryRule();
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

    //////////////////////////////// PRIVATE ////////////////////////////////////

    private void setStartLocation(InputLocation location) {
        startLocation = currentLocation = location;
    }

    private void addChildNode(Node<V> node) {
        if (subNodes == null) subNodes = new ArrayList<Node<V>>();
        subNodes.add(node);
    }

    private void recover() throws Throwable {
        if (trySingleSymbolDeletion()) return;

        Characters followerChars = getFollowerChars();
        if (trySingleSymbolInsertion(followerChars)) return;
        resynchronize(followerChars);
    }

    @SuppressWarnings({"unchecked"})
    private Characters getFollowerChars() {
        Characters chars = Characters.NONE;
        MatcherContext<V> parent = this.parent;
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

    // check whether the current char is a junk char that we can simply discard to continue with the next char
    private boolean trySingleSymbolDeletion() throws Throwable {
        Characters starterChars = matcher.getStarterChars();
        Preconditions.checkState(!starterChars.contains(Chars.EMPTY));
        InputLocation locationBeforeError = currentLocation;
        char lookAheadOne = locationBeforeError.lookAhead(inputBuffer, 1);
        if (!starterChars.contains(lookAheadOne)) {
            return false;
        }

        // success, we have to skip only one char in order to be able to start the match
        // match the illegal char and create a node for it
        advanceInputLocation();
        (parent != null ? parent : this).addChildNode(
                new NodeImpl<V>("ILLEGAL", null, locationBeforeError, currentLocation, null)
        );

        startLocation = currentLocation;
        // retry the original match
        return matcher.match(this);
    }

    // check whether the current char is a legally following next char in the follower set
    // if so, just virtually "insert" the missing expected character and continue
    private boolean trySingleSymbolInsertion(Characters followerChars) {
        char currentChar = currentLocation.currentChar;
        if (!followerChars.contains(currentChar)) return false;

        // success, the current mismatching character is a legal follower,
        // so add a ParseError and still "match" (empty)
        createNode();
        return true;
    }

    // consume all characters until we see a legal follower
    private void resynchronize(Characters followerChars) {
        createNode(); // create an empty match node

        InputLocation locationBeforeError = currentLocation;

        // consume all illegal characters up until a char that we can continue parsing with
        do {
            advanceInputLocation();
        } while (!followerChars.contains(currentLocation.currentChar) && currentLocation.currentChar != Chars.EOI);

        (parent != null ? parent : this).addChildNode(
                new NodeImpl<V>("ILLEGAL", null, locationBeforeError, currentLocation, null)
        );
    }

}
