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
import org.parboiled.matchers.Matcher;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;

import java.util.List;

/**
 * A Context object is available to parser actions methods during their runtime and provides various support functionality.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public interface Context<V> {

    /**
     * Returns the parent context, i.e. the context for the currently running parent matcher.
     *
     * @return the parent context
     */
    Context<V> getParent();

    /**
     * Returns the current sub context, which will only be non-null if the action call leading to this method was
     * wrapped with one or more {@link BaseParser#UP(Object)} calls.
     *
     * @return the current sub context.
     */
    Context<V> getSubContext();

    /**
     * Returns the InputBuffer the parser is currently running against
     *
     * @return the InputBuffer
     */
    InputBuffer getInputBuffer();

    /**
     * Returns the Matcher this context was constructed for.
     *
     * @return the matcher
     */
    @NotNull
    Matcher<V> getMatcher();

    /**
     * Returns the start location of the currently running rule match attempt.
     *
     * @return the start location
     */
    @NotNull
    InputLocation getStartLocation();

    /**
     * Returns the current location in the input buffer.
     *
     * @return the current location
     */
    @NotNull
    InputLocation getCurrentLocation();

    /**
     * Returns the list of parse errors so far generated during the entire parsing run.
     *
     * @return the list of parse errors
     */
    @NotNull
    List<ParseError> getParseErrors();

    /**
     * Returns the input text matched by the given node.
     *
     * @param node the node
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    String getNodeText(Node<?> node);

    /**
     * Returns the first input character matched by the given node.
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

    /**
     * <p>Returns the node that matches the given path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} )} for a
     * description of the path argument.</p>
     * <p><b>Caution</b>: Be aware of the current state of the parse tree when calling this method.
     * You can only reference nodes that have already been created. Since parse tree nodes are immutable they are
     * only created when their complete subtrees are fully determined. This means that all parent nodes have not yet
     * been created when you call this method. Note that you can still access the value objects of future parent
     * nodes through the getParent() chain.</p>
     *
     * @param path the path to the node being searched for
     * @return the node if found or null if not found
     */
    Node<V> getNodeByPath(String path);

    /**
     * Returns the last node created during this parsing run. The last node is independent from the current context
     * scope, i.e. all context along the context chain would return the same object at any given point in the
     * parsing process.
     *
     * @return the last node created during this parsing run.
     */
    Node<V> getLastNode();

    /**
     * Sets the value object of the node to be created at the current context scope.
     *
     * @param value the object
     */
    void setNodeValue(V value);

    /**
     * Returns the previously set value of the node to be created at the current context scope.
     *
     * @return the previously set value
     */
    V getNodeValue();

    /**
     * If this context has an explicitly set node value (see {@link #setNodeValue(Object)}) it is returned.
     * Otherwise the method return the last non-null value of the already created subnodes.
     *
     * @return a node value or null
     */
    V getTreeValue();

    /**
     * Returns the parse tree subnodes already created in the current context scope.
     *
     * @return the parse tree subnodes already created in the current context scope
     */
    List<Node<V>> getSubNodes();
}

