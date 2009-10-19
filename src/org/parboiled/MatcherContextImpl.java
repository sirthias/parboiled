package org.parboiled;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;
import org.parboiled.support.ParseTreeUtils;
import static org.parboiled.support.ParseTreeUtils.findNodeByPath;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public boolean runMatcher(@NotNull Matcher matcher, boolean enforced) {
        MatcherContextImpl innerContext = matcher instanceof ActionMatcher ? this :
                new MatcherContextImpl(this, currentLocation, matcher, actions, parseErrors);
        boolean matched = matcher.match(innerContext, enforced);
        if (matched) {
            currentLocation = innerContext.getCurrentLocation();
        } else {
            Preconditions.checkState(!enforced);
        }
        ParseError error = innerContext.getError(matcher);
        if (error != null) parseErrors.add(error);
        return matched;
    }

    private ParseError getError(Matcher matcher) {
        return errorMessage == null ? null :
                new ParseError(this, startLocation, currentLocation, matcher, node, errorMessage);
    }

    public void addUnexpectedInputError(@NotNull String expected) {
        this.errorMessage = new StringBuilder()
                .append("Invalid input, expected ")
                .append(expected)
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
        if (subNodes != null && StringUtils.isNotEmpty(path)) {
            for (Node node : subNodes) {
                if (StringUtils.equals(node.getLabel(), path)) return node;
                if (!path.startsWith(node.getLabel())) continue;
                Node found = findNodeByPath(node, path.substring(path.indexOf('/') + 1));
                if (found != null) return found;
            }
        }
        return null;
    }

}
