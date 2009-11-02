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
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;

import java.util.List;

/**
 * A Context object is available to parser actions methods during their runtime and provides various support functionality.
 */
public interface Context<V> {

    /**
     * @return the parent context, i.e. the context for the currently running parent matcher
     */
    Context<V> getParent();

    /**
     * @return the matcher this context was constructed for
     */
    @NotNull
    Matcher<V> getMatcher();

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
    String getNodeText(Node<?> node);

    /**
     * Returns the first input character matched by the given Node.
     *
     * @param node the node
     * @return null if node is null or did not match at least one character otherwise the first matched input char
     */
    Character getNodeChar(Node<?> node);

    /**
     * Returns the '/' separated full path name of the currently running Matcher.
     *
     * @return the path
     */
    @NotNull
    String getPath();

    /*Returns the node underneath the given parents that matches the given path.
     * The path is a '/' separated list of Node label prefixes describing the ancestor chain of the sought for Node
     * relative to each of the given parent nodes. If there are several nodes that match the given path the method
     * returns the first one unless the respective path segments has the special prefix "last:". In this case the
     * method will return the last matching node.
     * Example: "per/last:so/fix" will return the first node, whose label starts with "fix" under the last node,
     * whose label starts with "so" under the first node, whose label starts with "per".
     * If the given collections of parents is null or empty or no node is found the method returns null. */

    /**
     * <p>Returns the node that matches the given path. The path is a '/' separated list of node label prefixes
     * describing the ancestor chain of the sought for node relative to the current rule.
     * If there are several nodes that match the given path the method returns the first one unless the respective
     * path segments has the special prefix "last:". In this case the method will return the last matching node.</p>
     * <p>Example: "per/last:so/fix" will return the first node, whose label starts with "fix" under the last node,
     * whose label starts with "so" under the first node, whose label starts with "per".</p>
     * <p><b>Caution</b>: Be aware of the current state of the parse tree when calling this method.
     * You can only reference nodes that have already been created. Since parse tree nodes are immutable they are
     * only created when their complete subtrees are fully determined. This means that all parent nodes have not yet
     * been created when you call this method. Note that you can still access the AST nodes/value objects of parent
     * nodes through the getParent() chain.</p>
     *
     * @param path the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    Node<V> getNodeByPath(String path);

    /**
     * <p>Returns the first Node underneath the current rule that matches the given label prefix.
     * If no node is found the method returns null.</p>
     *<p><b>Caution:</b> This method traverses all parse tree hierarchy level in order to find a matching node.
     * This can cause confusing results with recursive methods since you won't know from which recursion the returned
     * node stems.</p>
     * @param label the label prefix to look for.
     * @return the found node or null
     */
    Node<V> getNodeByLabel(final String label);

    /**
     * @return the last node created during this parsing run.
     */
    Node<V> getLastNode();

    /**
     * Sets the value object of the node to be created for the current matcher.
     *
     * @param value the object
     */
    void setNodeValue(V value);

    /**
     * @return the previously set value of the node to be created for the current matcher
     */
    V getNodeValue();

    /**
     * If this context has an explicitly set node value (see setNodeValue()) it is returned.
     * Otherwise the method return the last non-null value of this levels subnodes.
     * @return a node value or null
     */
    V getTreeValue();

    /**
     * @return the already created parse tree subnodes underneath this rule
     */
    List<Node<V>> getSubNodes();
}

