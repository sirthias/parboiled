package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Chars;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;
import org.parboiled.support.ParseTreeUtils;
import static org.parboiled.support.ParseTreeUtils.findNodeByPath;
import org.parboiled.utils.StringUtils2;
import org.parboiled.utils.Utils;

import java.util.*;

class MatcherContextImpl implements MatcherContext {

    private final MatcherContextImpl parent;
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

    public MatcherContextImpl(MatcherContextImpl parent, @NotNull InputLocation startLocation, @NotNull Matcher matcher,
                              Actions actions, @NotNull List<ParseError> parseErrors) {

        this.parent = parent;
        this.startLocation = currentLocation = startLocation;
        this.matcher = matcher;
        this.actions = actions;
        this.parseErrors = parseErrors;
    }

    public MatcherContextImpl getParent() {
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

    public Actions getActions() {
        return actions;
    }

    @NotNull
    public List<ParseError> getParseErrors() {
        return Collections.unmodifiableList(parseErrors);
    }

    @NotNull
    public InputLocation getCurrentLocation() {
        return currentLocation;
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

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }

    public void addUnexpectedInputError(char illegalChar, @NotNull String expected) {
        this.errorMessage = new StringBuilder()
                .append("Invalid input ").append(illegalChar != Chars.EOF ? "\'" + illegalChar + '\'' : "EOF")
                .append(", expected ").append(expected)
                .append(ParseError.createMessageSuffix(startLocation, currentLocation))
                .toString();
    }

    public void addActionError(@NotNull String errorMessage) {
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

    public Node getNodeByPath(String path) {
        if (subNodes != null && StringUtils2.isNotEmpty(path)) {
            for (Node node : subNodes) {
                if (Utils.equals(node.getLabel(), path)) return node;
                if (!path.startsWith(node.getLabel())) continue;
                Node found = findNodeByPath(node, path.substring(path.indexOf('/') + 1));
                if (found != null) return found;
            }
        }
        return null;
    }

    public boolean runMatcher(@NotNull Matcher matcher, boolean enforced) {
        MatcherContextImpl innerContext = new MatcherContextImpl(this, currentLocation, matcher, actions, parseErrors);
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

    private void recover() {
        Set<Character> starterSet = new HashSet<Character>();
        matcher.collectFirstCharSet(starterSet);
        if (trySingleSymbolDeletion(starterSet)) return;

        Set<Character> followerSet = getFollowerSet();
        if (trySingleSymbolInsertion(followerSet)) return;
        resynchronize(followerSet);
    }

    // check whether the current char is a junk char that we can simply discard to continue with the next char
    private boolean trySingleSymbolDeletion(Set<Character> starterSet) {
        char lookAheadOne = getCurrentLocation().lookAhead(1);
        if (!starterSet.contains(lookAheadOne)) {
            return false;
        }

        // normally, we need to run the IllegalCharactersMatcher in our parent context so the created node
        // appears on the same tree level, however if we are the root ourselves we run in this context
        MatcherContext parentContext = parent != null ? parent : this;

        // success, we have to skip only one char in order to be able to start the match
        // match the illegal char and create a node for it
        parentContext.runMatcher(new IllegalCharactersMatcher(matcher.getExpectedString()), true);

        // retry the original match
        parentContext.runMatcher(matcher, true);

        // catch up with the advanced location
        setCurrentLocation(parentContext.getCurrentLocation());
        
        return true;
    }

    // check whether the current char is a legally following next char in the follower set
    // if so, just virtually "insert" the missing expected token and continue
    private boolean trySingleSymbolInsertion(Set<Character> followerSet) {
        char currentChar = getCurrentLocation().currentChar;
        if (!followerSet.contains(currentChar)) return false;

        // success, the current mismatching token is a legal follower,
        // so add a ParseError and still "match" (empty)
        addUnexpectedInputError(currentChar, matcher.getExpectedString());
        createNode();
        return true;
    }

    // consume all characters until we see a legal follower
    private void resynchronize(Set<Character> followerSet) {
        createNode(); // create an empty match node

        // normally, we need to run the IllegalCharactersMatcher in our parent context so the created node
        // appears on the same tree level, however if we are the root ourselves we run in this context
        MatcherContext parentContext = parent != null ? parent : this;

        // create a node for the illegal chars
        parentContext.runMatcher(new IllegalCharactersMatcher(matcher.getExpectedString(), followerSet), true);

        // catch up with the advanced location
        setCurrentLocation(parentContext.getCurrentLocation());
    }

    public Set<Character> getFollowerSet() {
        Set<Character> set = new HashSet<Character>();
        MatcherContextImpl parent = this.parent;
        while (parent != null) {
            if (parent.getMatcher() instanceof FollowMatcher) {
                FollowMatcher followMatcher = (FollowMatcher) parent.getMatcher();
                if (followMatcher.collectCurrentFollowerSet(parent, set)) {
                    return set;
                }
            }
            parent = parent.parent;
        }
        set.add(Chars.EOF);
        return set;
    }

}
