package org.parboiled;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;
import org.parboiled.support.ParseTreeUtils;
import static org.parboiled.support.ParseTreeUtils.findNodeByPath;

import java.util.Collections;
import java.util.List;

class MatcherContextImpl implements MatcherContext {

    private final InputBuffer inputBuffer;
    private final List<ParseError> parseErrors = Lists.newArrayList();
    private Node lastNodeCreated;

    private String currentPath = "";
    private Node currentNode;
    private Matcher currentMatcher;
    private List<Node> currentNodes;
    private List<Node> currentParentNodes;
    private InputLocation currentStart;
    private String currentErrorMessage;
    private Object currentValue;

    public MatcherContextImpl(@NotNull InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    @NotNull
    public InputBuffer getInputBuffer() {
        return inputBuffer;
    }

    @NotNull
    public List<ParseError> getParseErrors() {
        return Collections.unmodifiableList(parseErrors);
    }

    public Node getLastNodeCreated() {
        return lastNodeCreated;
    }

    public boolean runMatcher(@NotNull Matcher matcher, boolean enforced) {
        if (matcher instanceof ActionMatcher) {
            // do not create a new frame for ActionMatcher, they want to operate on the level of the current Matcher
            return doRunMatcher(matcher, enforced);
        }

        // save current "frame"
        String previousPath = currentPath;
        Node previousNode = currentNode;
        Matcher previousMatcher = currentMatcher;
        List<Node> previousParentNodes = currentParentNodes;
        InputLocation previousStart = currentStart;
        String previousErrorMessage = currentErrorMessage;
        Object previousValue = currentValue;

        // create new "frame"
        currentPath += currentPath.length() > 0 ? '/' + matcher.getLabel() : matcher.getLabel();
        currentNode = null;
        currentMatcher = matcher;
        currentParentNodes = currentNodes;
        currentNodes = null;
        currentStart = inputBuffer.getCurrentLocation();
        currentErrorMessage = null;
        currentValue = null;

        boolean matched = doRunMatcher(matcher, enforced);

        // discard current "frame" and restore to previous one
        currentPath = previousPath;
        currentNode = previousNode;
        currentMatcher = previousMatcher;
        currentNodes = currentParentNodes;
        currentParentNodes = previousParentNodes;
        currentStart = previousStart;
        currentErrorMessage = previousErrorMessage;
        currentValue = previousValue;
        return matched;
    }

    private boolean doRunMatcher(Matcher matcher, boolean enforced) {
        boolean matched = matcher.match(this, enforced);
        if (!matched) {
            Preconditions.checkState(!enforced);
            inputBuffer.rewind(currentStart);
        }
        if (currentErrorMessage != null) {
            parseErrors.add(new ParseError(this, currentStart, inputBuffer.getCurrentLocation(), currentMatcher,
                    currentNode, currentErrorMessage));
        }
        return matched;
    }

    public void addUnexpectedInputError(@NotNull String expected) {
        currentErrorMessage = new StringBuilder()
                .append("Invalid input, expected ")
                .append(expected)
                .append(ParseError.createMessageSuffix(inputBuffer, currentStart,
                        inputBuffer.getCurrentLocation()))
                .toString();
    }

    public void addActionError(@NotNull String errorMessage) {
        currentErrorMessage = errorMessage;
    }

    public void createNode() {
        if (currentParentNodes == null) currentParentNodes = Lists.newArrayList();
        currentNode = new NodeImpl(currentMatcher.getLabel(), currentNodes, currentStart,
                inputBuffer.getCurrentLocation(), currentValue);
        currentParentNodes.add(currentNode);
        lastNodeCreated = currentNode;
    }

    public String getNodeText(Node node) {
        return ParseTreeUtils.getNodeText(node, inputBuffer);
    }

    public Character getNodeChar(Node node) {
        return ParseTreeUtils.getNodeChar(node, inputBuffer);
    }

    @NotNull
    public List<Node> getCurrentNodes() {
        return currentNodes != null ? ImmutableList.copyOf(currentNodes) : ImmutableList.<Node>of();
    }

    @NotNull
    public List<Node> getCurrentParentNodes() {
        return currentParentNodes != null ? ImmutableList.copyOf(currentParentNodes) : ImmutableList.<Node>of();
    }

    @NotNull
    public String getCurrentPath() {
        return currentPath;
    }

    public Node getNodeByPath(String path) {
        if (currentNodes != null && StringUtils.isNotEmpty(path)) {
            for (Node node : currentNodes) {
                if (StringUtils.equals(node.getLabel(), path)) return node;
                if (!path.startsWith(node.getLabel())) continue;
                Node found = findNodeByPath(node, path.substring(path.indexOf('/') + 1));
                if (found != null) return found;
            }
        }
        return null;
    }

    public Object getNodeValue() {
        return currentValue;
    }

    public void setNodeValue(Object value) {
        this.currentValue = value;
    }

}
