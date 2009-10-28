package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;

import java.util.List;

/**
 * A Context object is available to parser actions methods during their runtime and provides various support functionality.
 */
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
     * @return the list of parse errors so far generated during the entire parsing run
     */
    @NotNull
    List<ParseError> getParseErrors();

    /**
     * Returns the input text matched by the given Node.
     *
     * @param node the node
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    String getNodeText(Node node);

    /**
     * Returns the first input character matched by the given Node.
     *
     * @param node the node
     * @return null if node is null or did not match at least one character otherwise the first matched input char
     */
    Character getNodeChar(Node node);

    /**
     * Returns the '/' separated full path name of the currently running Matcher.
     *
     * @return the path
     */
    @NotNull
    String getPath();

    /**
     * Returns the first Node underneath the given parents that matches the given path.
     * The path is a '/' separated list of Node label prefixes describing the ancestor chain of the sought for Node
     * relative to each of the given parent nodes.
     * If the given collections of parents is null or empty or no node is found the method returns null.
     *
     * @param path the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    Node getNodeByPath(String path);

    /**
     * Sets the value object of the node to be created for the current matcher.
     *
     * @param value the object
     */
    void setNodeValue(Object value);

    /**
     * @return the previously set value of the node to be created for the current matcher
     */
    Object getNodeValue();

}

