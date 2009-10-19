package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;

import java.util.List;

public interface Context {

    /**
     * @return the parent context, i.e. the context for the currently running parent matcher
     */
    Context getParent();

    /**
     * @return the matcher this context was constructed for
     */
    @NotNull
    Matcher getMatcher();

    /**
     * @return the start location of the currently running rule match attempt
     */
    @NotNull
    InputLocation getStartLocation();

    /**
     * @return the current location in the input buffer
     */
    @NotNull
    InputLocation getCurrentLocation();

    /**
     * @return the list of parse errors so far generated
     */
    @NotNull
    List<ParseError> getParseErrors();

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
     * @return the full path name of the currently running Matcher.
     */
    @NotNull
    String getPath();

    /**
     * Tries to find an already created parse tree node by its relative path.
     *
     * @param path the path of the node to find relative to the current level
     * @return the Node or null
     */
    Node getNodeByPath(String path);

    /**
     * Sets the value object of the node to be created for the current matcher
     *
     * @param value the object
     */
    void setNodeValue(Object value);

    /**
     * @return the previously set value of the node to be created for the current matcher
     */
    Object getNodeValue();

}

