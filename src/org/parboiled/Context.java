package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.ParseError;

import java.util.List;

public interface Context {

    /**
     * @return the input buffer this parser is running on
     */
    @NotNull
    InputBuffer getInputBuffer();

    /**
     * @return the parse errors detected during the parser run
     */
    @NotNull
    List<ParseError> getParseErrors();

    /**
     * @return the last Node that was created during the parser run
     */
    Node getLastNodeCreated();

    /**
     * @param node the node to get the input text for
     * @return the input text matched by the given node
     */
    String getNodeText(Node node);

    /**
     * @param node the node to get the input text for
     * @return the input char matched by the given node or null, if the node is null or doesn't match exactly one char.
     */
    Character getNodeChar(Node node);

    /**
     * @return the list of nodes already matched in the current sequence
     */
    @NotNull
    List<Node> getCurrentNodes();

    /**
     * @return the list of nodes already matched in the current parent sequence
     */
    @NotNull
    List<Node> getCurrentParentNodes();

    /**
     * @return the full path name of the currently running Matcher.
     */
    @NotNull
    String getCurrentPath();

    /**
     * Tries to find an already created parse tree node by its relative path.
     * @param path the path of the node to find relative to the current level
     * @return the Node or null
     */
    Node getNodeByPath(String path);

    /**
     * Sets the value object of the node to be created for the current matcher
     * @param value the object
     */
    void setNodeValue(Object value);

    /**
     * @return the previously set value of the node to be created for the current matcher
     */
    Object getNodeValue();
}

