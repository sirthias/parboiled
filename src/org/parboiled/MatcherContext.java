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
import org.parboiled.support.*;
import static org.parboiled.support.ParseTreeUtils.findNodeByPath;
import static org.parboiled.support.ParseTreeUtils.findNodeByLabel;
import org.parboiled.utils.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MatcherContext<V> implements Context<V> {

    private final MatcherContext<V> parent;
    private final InputLocation startLocation;
    private final Matcher<V> matcher;
    private final Actions<V> actions;
    private final List<ParseError> parseErrors;

    private InputLocation currentLocation;
    private Node<V> node;
    private List<Node<V>> subNodes;
    private String errorMessage;
    private V nodeValue;
    private Object tag;

    public MatcherContext(MatcherContext<V> parent, @NotNull InputLocation startLocation, @NotNull Matcher<V> matcher,
                          Actions<V> actions, @NotNull List<ParseError> parseErrors) {

        this.parent = parent;
        this.startLocation = currentLocation = startLocation;
        this.matcher = matcher;
        this.actions = actions;
        this.parseErrors = parseErrors;
    }

    //////////////////////////////// CONTEXT INTERFACE ////////////////////////////////////

    public MatcherContext<V> getParent() {
        return parent;
    }

    @NotNull
    public InputLocation getStartLocation() {
        return startLocation;
    }

    @NotNull
    public Matcher<V> getMatcher() {
        return matcher;
    }

    @NotNull
    public List<ParseError> getParseErrors() {
        return Collections.unmodifiableList(parseErrors);
    }

    @NotNull
    public InputLocation getCurrentLocation() {
        return currentLocation;
    }

    public String getNodeText(Node<V> node) {
        return ParseTreeUtils.getNodeText(node, startLocation.inputBuffer);
    }

    public Character getNodeChar(Node<V> node) {
        return ParseTreeUtils.getNodeChar(node, startLocation.inputBuffer);
    }

    @NotNull
    public String getPath() {
        return parent == null ? "" : parent.getPath() + '/' + matcher.getLabel();
    }

    public V getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(V value) {
        this.nodeValue = value;
    }

    public Node<V> getNodeByPath(String path) {
        return findNodeByPath(subNodes, path);
    }

    public Node<V> getNodeByLabel(String path) {
        return findNodeByLabel(subNodes, path);
    }

    //////////////////////////////// PUBLIC ////////////////////////////////////

    public MatcherContext<V> createCopy(MatcherContext<V> parent, Matcher<V> matcher) {
        return new MatcherContext<V>(parent, currentLocation, matcher, actions, parseErrors);
    }

    public void setCurrentLocation(InputLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void advanceInputLocation() {
        setCurrentLocation(getCurrentLocation().advance());
    }

    public Node<V> getNode() {
        return node;
    }

    public List<Node<V>> getSubNodes() {
        return subNodes;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public void addUnexpectedInputError(char illegalChar, @NotNull String expected) {
        addError(new StringBuilder()
                .append("Invalid input ").append(illegalChar != Chars.EOI ? "\'" + illegalChar + '\'' : "EOI")
                .append(", expected ").append(expected)
                .append(ParseError.createMessageSuffix(startLocation, currentLocation))
                .toString());
    }

    public void addError(@NotNull String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void createNode() {
        node = new NodeImpl<V>(matcher.getLabel(), subNodes, startLocation, currentLocation, nodeValue);
        if (parent != null) parent.addChildNode(node);
    }

    public void addChildNode(@NotNull Node<V> node) {
        if (subNodes == null) subNodes = new ArrayList<Node<V>>();
        subNodes.add(node);
    }

    public boolean runMatcher(@NotNull Matcher<V> matcher, boolean enforced) {
        MatcherContext<V> innerContext = createCopy(this, matcher);
        boolean matched = innerContext.runMatcher(enforced);
        if (matched) {
            setCurrentLocation(innerContext.getCurrentLocation());
        }
        return matched;
    }

    public boolean runMatcher(boolean enforced) {
        boolean matched = matcher.match(this, enforced);
        if (!matched && enforced) {
            recover();
            matched = true;
        }
        if (errorMessage != null) {
            parseErrors.add(new ParseError(this, startLocation, currentLocation, matcher, node, errorMessage));
        }
        return matched;
    }

    @SuppressWarnings({"unchecked"})
    public Characters getFollowerChars() {
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

    //////////////////////////////// PRIVATE ////////////////////////////////////

    private void recover() {
        if (trySingleSymbolDeletion()) return;

        Characters followerChars = getFollowerChars();
        if (trySingleSymbolInsertion(followerChars)) return;
        resynchronize(followerChars);
    }

    // check whether the current char is a junk char that we can simply discard to continue with the next char
    private boolean trySingleSymbolDeletion() {
        Characters starterChars = matcher.getStarterChars();
        Preconditions.checkState(!starterChars.contains(Chars.EMPTY));
        char lookAheadOne = getCurrentLocation().lookAhead(1);
        if (!starterChars.contains(lookAheadOne)) {
            return false;
        }

        // normally, we need to run the IllegalCharactersMatcher in our parent context so the created node
        // appears on the same tree level, however if we are the root ourselves we run in this context
        MatcherContext<V> parentContext = parent != null ? parent : this;

        // success, we have to skip only one char in order to be able to start the match
        // match the illegal char and createActions a node for it
        IllegalCharactersMatcher<V> illegalCharsMatcher =
                new IllegalCharactersMatcher<V>(matcher.getExpectedString(), Characters.of(lookAheadOne));
        parentContext.runMatcher(illegalCharsMatcher, true);

        // retry the original match
        parentContext.runMatcher(matcher, true);

        // catch up with the advanced location
        setCurrentLocation(parentContext.getCurrentLocation());

        return true;
    }

    // check whether the current char is a legally following next char in the follower set
    // if so, just virtually "insert" the missing expected token and continue
    private boolean trySingleSymbolInsertion(Characters followerChars) {
        char currentChar = getCurrentLocation().currentChar;
        if (!followerChars.contains(currentChar)) return false;

        // success, the current mismatching token is a legal follower,
        // so add a ParseError and still "match" (empty)
        addUnexpectedInputError(currentChar, matcher.getExpectedString());
        createNode();
        return true;
    }

    // consume all characters until we see a legal follower
    private void resynchronize(Characters followerChars) {
        createNode(); // createActions an empty match node

        // normally, we need to run the IllegalCharactersMatcher in our parent context so the created node
        // appears on the same tree level, however if we are the root ourselves we run in this context
        MatcherContext<V> parentContext = parent != null ? parent : this;

        // createActions a node for the illegal chars
        parentContext.runMatcher(new IllegalCharactersMatcher<V>(matcher.getExpectedString(), followerChars), true);

        // catch up with the advanced location
        setCurrentLocation(parentContext.getCurrentLocation());
    }

}
