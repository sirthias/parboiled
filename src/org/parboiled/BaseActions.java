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
import org.parboiled.common.StringUtils;
import org.parboiled.support.Checks;
import org.parboiled.support.LabelPrefixPredicate;
import org.parboiled.support.ParseTreeUtils;

import java.util.ArrayList;
import java.util.List;

import static org.parboiled.support.ParseTreeUtils.collectNodes;

/**
 * Convenience context aware base class defining a number of useful helper methods.
 *
 * @param <V> the type of the value field of a parse tree node
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class BaseActions<V> implements ContextAware<V> {

    private Context<V> context;

    /**
     * The current context for use with action methods. Updated immediately before action calls.
     *
     * @return the current context
     */
    public Context<V> getContext() {
        return context;
    }

    /**
     * ContextAware interface implementation.
     *
     * @param context the context
     */
    public void setContext(@NotNull Context<V> context) {
        this.context = context;
    }

    /**
     * Returns the first parse tree node found under the given prefix path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param path the path to search for
     * @return the parse tree node if found or null if not found
     */
    public Node<V> node(String path) {
        check();
        return context.getNodeByPath(path);
    }

    /**
     * Returns a list of all parse tree nodes found under the given prefix path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} )} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param path the path to search for
     * @return the list of parse tree nodes
     */
    @NotNull
    public List<Node<V>> nodes(String path) {
        check();
        return ParseTreeUtils.collectNodesByPath(context.getSubNodes(), path, new ArrayList<Node<V>>());
    }

    /**
     * Returns the first parse tree node found with the given label prefix.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} for a description of the path argument.
     * The search is performed in the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param labelPrefix the label prefix
     * @return the parse tree node if found or null if not found
     */
    public Node<V> nodeByLabel(String labelPrefix) {
        check();
        return context.getNodeByLabel(labelPrefix);
    }

    /**
     * Returns a list of all parse tree nodes found under the given prefix path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} )} for a description of the path argument.
     * The search is performed in the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param labelPrefix the label prefix
     * @return the list of parse tree nodes
     */
    @NotNull
    public List<Node<V>> nodesByLabel(String labelPrefix) {
        check();
        return collectNodes(context.getSubNodes(),
                new LabelPrefixPredicate<V>(labelPrefix),
                new ArrayList<Node<V>>()
        );
    }

    /**
     * Returns the last node created during this parsing run. This last node is independent of the current context
     * scope, i.e. {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)} have no influence on it.
     *
     * @return the last node created during this parsing run
     */
    public Node<V> lastNode() {
        check();
        return context.getLastNode();
    }

    /**
     * Returns the tree value of the current context scope, i.e., if there is an explicitly set value it is
     * returned. Otherwise the last non-null child value, or, if there is no such value, null.
     *
     * @return the tree value of the current context scope
     */
    public V value() {
        check();
        return context.getTreeValue();
    }

    /**
     * Returns the value of the given node.
     *
     * @param node the node the get the value from
     * @return the value of the node, or null if the node is null
     */
    public V value(Node<V> node) {
        return node == null ? null : node.getValue();
    }

    /**
     * Returns the value of the node found under the given prefix path.
     * Equivalent to <code>value(node(path))</code>.
     *
     * @param path the path to search for
     * @return the value of the node, or null if the node is not found
     */
    public V value(String path) {
        return value(node(path));
    }

    /**
     * Returns a list of the values of all given nodes.
     *
     * @param nodes the nodes to get the values from
     * @return the list of values
     */
    @NotNull
    public List<V> values(List<Node<V>> nodes) {
        List<V> values = new ArrayList<V>();
        for (Node<V> node : nodes) {
            values.add(node.getValue());
        }
        return values;
    }

    /**
     * Returns the list of the values of all nodes found under the given prefix path.
     * Equivalent to <code>values(nodes(path))</code>.
     *
     * @param path the path to search for
     * @return the list of values
     */
    @NotNull
    public List<V> values(String path) {
        return values(nodes(path));
    }

    /**
     * Returns the value of the last node created during this parsing run.
     * Equivalent to <code>value(lastNode())</code>.
     *
     * @return the value of the last node created
     */
    public V lastValue() {
        return value(lastNode());
    }

    /**
     * Returns the input text matched by the given parse tree node, with correctable errors corrected.
     *
     * @param node the parse tree node
     * @return the input text matched by the given node
     */
    public String text(Node<V> node) {
        check();
        return context.getNodeText(node);
    }

    /**
     * Returns the input text matched by the node found under the given prefix path.
     * Equivalent to <code>text(node(path))</code>.
     *
     * @param path the path to search for
     * @return the matched input text
     */
    public String text(String path) {
        return text(node(path));
    }

    /**
     * Returns a list of the input texts matched by all given nodes.
     *
     * @param nodes the nodes
     * @return the list of matched input texts
     */
    @NotNull
    public List<String> texts(List<Node<V>> nodes) {
        check();
        List<String> values = new ArrayList<String>();
        for (Node<V> node : nodes) {
            values.add(context.getNodeText(node));
        }
        return values;
    }

    /**
     * Returns a list of the input texts matched by of all nodes found under the given prefix path.
     * Equivalent to <code>texts(nodes(path))</code>.
     *
     * @param path the path to search for
     * @return the list of matched input texts
     */
    @NotNull
    public List<String> texts(String path) {
        return texts(nodes(path));
    }

    /**
     * Returns the input text matched by the last node created during this parsing run.
     * Equivalent to <code>text(lastNode())</code>.
     *
     * @return the input text matched by the last node created
     */
    public String lastText() {
        return text(lastNode());
    }

    /**
     * Returns the first character of the input text matched by the given parse tree node.
     *
     * @param node the parse tree node
     * @return the first matched character or null if the given node is null
     */
    public Character character(Node<V> node) {
        String text = text(node);
        return StringUtils.isEmpty(text) ? null : text.charAt(0);
    }

    /**
     * Returns the first character of the input text matched by the node found under the given prefix path.
     * Equivalent to <code>character(node(path))</code>.
     *
     * @param path the path to search for
     * @return the first matched character or null if the node is not found
     */
    public Character character(String path) {
        return character(node(path));
    }

    /**
     * Returns a list of the first characters of the input texts matched by all given nodes.
     *
     * @param nodes the nodes
     * @return the list of the first matched characters
     */
    @NotNull
    public List<Character> chars(List<Node<V>> nodes) {
        check();
        List<Character> values = new ArrayList<Character>();
        for (Node<V> node : nodes) {
            values.add(character(node));
        }
        return values;
    }

    /**
     * Returns a list of the first characters of the input texts matched by of all nodes found
     * under the given prefix path.
     * Equivalent to <code>chars(nodes(path))</code>.
     *
     * @param path the path to search for
     * @return the list of the first matched characters
     */
    @NotNull
    public List<Character> chars(String path) {
        return chars(nodes(path));
    }

    /**
     * Returns the first character of the input text matched by the last node created during this parsing run.
     * Equivalent to <code>character(lastNode())</code>.
     *
     * @return the first character of the input text matched by the last node created
     */
    public Character lastChar() {
        return character(lastNode());
    }

    /**
     * Sets the value of the parse tree node to be created for the current context
     * scope to the value of the last node created during the current parsing run.
     * Equivalent to <code>set(lastValue())</code>.
     *
     * @return true
     */
    public boolean set() {
        return set(lastValue());
    }

    /**
     * Sets the value of the parse tree node to be created for the current context scope to the given value.
     *
     * @param value the value to set
     * @return true
     */
    public boolean set(V value) {
        check();
        context.setNodeValue(value);
        return true;
    }

    /**
     * Returns the next input character about to be matched.
     *
     * @return the next input character about to be matched
     */
    public Character nextChar() {
        check();
        return context.getCurrentLocation().getChar();
    }

    /**
     * Returns true if the current rule is running somewhere underneath a Test/TestNot rule.
     * Useful for example for making sure actions are not run inside of a predicate evaluation:
     * <code>
     * return Sequence(
     * ...,
     * inPredicate() || actions.doSomething()
     * );
     * </code>
     *
     * @return true if in a predicate
     */
    public boolean inPredicate() {
        check();
        return context.inPredicate();
    }

    /**
     * Returns true if the current rule is running below a rule marked @Leaf.
     *
     * @return true if in a below a leaf rule
     */
    public boolean belowLeafLevel() {
        check();
        return context.isBelowLeafLevel();
    }

    /**
     * Determines whether the current rule or a sub rule has recorded a parse error.
     * Useful for example for making sure actions are not run on erroneous input:
     * <code>
     * return Sequence(
     * ...,
     * !hasError() && actions.doSomething()
     * );
     * </code>
     *
     * @return true if either the current rule or a sub rule has recorded a parse error
     */
    public boolean hasError() {
        check();
        return context.hasError();
    }

    private void check() {
        Checks.ensure(context != null && context.getMatcher() != null,
                "Illegal rule definition: Unwrapped action expression!");
    }

}
