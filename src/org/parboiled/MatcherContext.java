package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.*;
import static org.parboiled.support.ParseTreeUtils.findNodeByPath;
import org.parboiled.utils.Preconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class MatcherContext implements Context {

    private final MatcherContext parent;
    private final InputLocation startLocation;
    private final Matcher matcher;
    private final Actions actions;
    private final List<ParseError> parseErrors;

    private InputLocation currentLocation;
    private Node node;
    private List<Node> subNodes;
    private String errorMessage;
    private Object nodeValue;
    private Object tag;

    public MatcherContext(MatcherContext parent, @NotNull InputLocation startLocation, @NotNull Matcher matcher,
                          Actions actions, @NotNull List<ParseError> parseErrors) {

        this.parent = parent;
        this.startLocation = currentLocation = startLocation;
        this.matcher = matcher;
        this.actions = actions;
        this.parseErrors = parseErrors;
    }

    //////////////////////////////// CONTEXT INTERFACE ////////////////////////////////////

    public MatcherContext getParent() {
        return parent;
    }

    @NotNull
    public InputLocation getStartLocation() {
        return startLocation;
    }

    @NotNull
    public Matcher getMatcher() {
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

    public String getNodeText(Node node) {
        return ParseTreeUtils.getNodeText(node, startLocation.inputBuffer);
    }

    public Character getNodeChar(Node node) {
        return ParseTreeUtils.getNodeChar(node, startLocation.inputBuffer);
    }

    @NotNull
    public String getPath() {
        return parent == null ? "" : parent.getPath() + '/' + matcher.getLabel();
    }

    public Object getNodeValue() {
        return nodeValue;
    }

    public void setNodeValue(Object value) {
        this.nodeValue = value;
    }

    public Node getNodeByPath(String path) {
        return findNodeByPath(subNodes, path);
    }

    //////////////////////////////// PUBLIC ////////////////////////////////////

    public MatcherContext createCopy(MatcherContext parent, Matcher matcher) {
        return new MatcherContext(parent, currentLocation, matcher, actions, parseErrors);
    }

    public void setCurrentLocation(InputLocation currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void advanceInputLocation() {
        setCurrentLocation(getCurrentLocation().advance());
    }

    public Node getNode() {
        return node;
    }

    public List<Node> getSubNodes() {
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
                .append("Invalid input ").append(illegalChar != Chars.EOF ? "\'" + illegalChar + '\'' : "EOF")
                .append(", expected ").append(expected)
                .append(ParseError.createMessageSuffix(startLocation, currentLocation))
                .toString());
    }

    public void addError(@NotNull String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void createNode() {
        node = new NodeImpl(matcher.getLabel(), subNodes, startLocation, currentLocation, nodeValue);
        if (parent != null) parent.addChildNode(node);
    }

    public void addChildNode(@NotNull Node node) {
        if (subNodes == null) subNodes = new ArrayList<Node>();
        subNodes.add(node);
    }

    public boolean runMatcher(@NotNull Matcher matcher, boolean enforced) {
        MatcherContext innerContext = createCopy(this, matcher);
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

    public Characters getFollowerChars() {
        Characters chars = Characters.NONE;
        MatcherContext parent = this.parent;
        while (parent != null) {
            if (parent.getMatcher() instanceof FollowMatcher) {
                FollowMatcher followMatcher = (FollowMatcher) parent.getMatcher();
                chars = chars.add(followMatcher.getFollowerChars(parent));
                if (!chars.contains(Chars.EMPTY)) return chars;
            }
            parent = parent.parent;
        }
        return chars.remove(Chars.EMPTY).add(Chars.EOF);
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
        MatcherContext parentContext = parent != null ? parent : this;

        // success, we have to skip only one char in order to be able to start the match
        // match the illegal char and create a node for it
        IllegalCharactersMatcher illegalCharsMatcher = new IllegalCharactersMatcher(matcher.getExpectedString(),
                Characters.of(lookAheadOne));
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
        createNode(); // create an empty match node

        // normally, we need to run the IllegalCharactersMatcher in our parent context so the created node
        // appears on the same tree level, however if we are the root ourselves we run in this context
        MatcherContext parentContext = parent != null ? parent : this;

        // create a node for the illegal chars
        parentContext.runMatcher(new IllegalCharactersMatcher(matcher.getExpectedString(), followerChars), true);

        // catch up with the advanced location
        setCurrentLocation(parentContext.getCurrentLocation());
    }

}
