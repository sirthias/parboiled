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
import org.parboiled.errors.ParseError;
import org.parboiled.matchers.Matcher;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.MatcherPath;

import java.util.List;

/**
 * A Context object is available to parser actions methods during their runtime and provides various support functionalities.
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
    @NotNull
    InputBuffer getInputBuffer();

    /**
     * Returns the Matcher of this context or null, if this context is not valid anymore.
     *
     * @return the matcher
     */
    Matcher<V> getMatcher();

    /**
     * Returns the input location where the matcher of this context started its match.
     *
     * @return the start location
     */
    InputLocation getStartLocation();

    /**
     * Returns the current location in the input buffer.
     *
     * @return the current location
     */
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
     * Returns the MatcherPath of the currently running matcher.
     *
     * @return the path
     */
    @NotNull
    MatcherPath<V> getPath();

    /**
     * Returns the current matcher level, with 0 being the root level, 1 being one level below the root and so on.
     *
     * @return the current matcher level
     */
    int getLevel();

    /**
     * <p>Returns the first node that matches the given path.
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
     * <p>Returns the first node that matches the given labelPrefix.
     * <p><b>Caution</b>: Be aware of the current state of the parse tree when calling this method.
     * You can only reference nodes that have already been created. Since parse tree nodes are immutable they are
     * only created when their complete subtrees are fully determined. This means that all parent nodes have not yet
     * been created when you call this method. Note that you can still access the value objects of future parent
     * nodes through the getParent() chain.</p>
     *
     * @param labelPrefix the label prefix to search for
     * @return the node if found or null if not found
     */
    Node<V> getNodeByLabel(String labelPrefix);

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
    @NotNull
    List<Node<V>> getSubNodes();

    /**
     * Returns true if the current rule is running somewhere underneath a test/testNot rule.
     *
     * @return true if the current context has a parent which corresponds to a test/testNot rule
     */
    boolean inPredicate();

    /**
     * Returns true if the current context is running below a context with a matcher marked @Leaf
     *
     * @return true if the current context is running below a context with a matcher marked @Leaf
     */
    boolean isBelowLeafLevel();

    /**
     * Returns true if this context or any sub node recorded a parse error.
     *
     * @return true if this context or any sub node recorded a parse error
     */
    boolean hasError();

}

