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
import org.parboiled.common.Converter;
import org.parboiled.common.Utils;
import org.parboiled.support.LabelPrefixPredicate;
import org.parboiled.support.ParseTreeUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseActions<V> implements ContextAware<V> {

    /**
     * Current context for use by action methods.
     */
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
    public void setContext(Context<V> context) {
        this.context = context;
    }

    /**
     * Converts the given boolean value to an ActionResult, where <b>true</b> corresponds to ActionResult.CONTINUE.
     *
     * @param value the value to convert
     * @return ActionResult.CONTINUE for true, ActionResult.CANCEL_MATCH for false
     */
    public ActionResult toActionResult(boolean value) {
        return value ? ActionResult.CONTINUE : ActionResult.CANCEL_MATCH;
    }

    /**
     * Creates an action parameter that evaluates to the first parse tree node found under the given prefix path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Node<V> NODE(String path) {
        return context.getNodeByPath(path);
    }

    /**
     * Creates an action parameter that evaluates to a list of all parse tree nodes found under the given prefix path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} )} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<Node<V>> NODES(String path) {
        return ParseTreeUtils.collectNodesByPath(context.getSubNodes(), path, new ArrayList<Node<V>>());
    }

    /**
     * Creates an action parameter that evaluates to the first parse tree node found with the given label prefix.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param labelPrefix the label prefix
     * @return the action parameter
     */
    public Node<V> NODE_BY_LABEL(String labelPrefix) {
        return context.getNodeByLabel(labelPrefix);
    }

    /**
     * Creates an action parameter that evaluates to a list of all parse tree nodes found under the given prefix path.
     * See {@link org.parboiled.support.ParseTreeUtils#findNodeByPath(org.parboiled.Node, String)} )} for a description of the path argument.
     * The path is relative to the current context scope, which can be changed with {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)}.
     *
     * @param labelPrefix the label prefix
     * @return the action parameter
     */
    public List<Node<V>> NODES_BY_LABEL(String labelPrefix) {
        return ParseTreeUtils.collectNodes(context.getSubNodes(),
                new LabelPrefixPredicate<V>(labelPrefix),
                new ArrayList<Node<V>>()
        );
    }

    /**
     * Creates an action parameter that evaluates to the last node created during this parsing run. This last node
     * is independent of the current context scope, i.e. {@link BaseParser#UP(Object)} or {@link BaseParser#DOWN(Object)} have no influence
     * on it.
     *
     * @return the action parameter
     */
    public Node<V> LAST_NODE() {
        return context.getLastNode();
    }

    /**
     * Creates an action parameter that evaluates to the tree value of the current context scope level, i.e.,
     * if there is an explicitly set value it is returned. Otherwise the last non-null child value, or, if there
     * is no such value, null.
     *
     * @return the action parameter
     */
    public V VALUE() {
        return context.getTreeValue();
    }

    /**
     * Creates an action parameter that evaluates to the value of the given node.
     *
     * @param node the node the get the value from
     * @return the action parameter
     */
    public V VALUE(Node<V> node) {
        return node == null ? null : node.getValue();
    }

    /**
     * Creates an action parameter that evaluates to the value of the node found under the given prefix path.
     * Equivalent to <code>VALUE(NODE(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public V VALUE(String path) {
        return VALUE(NODE(path));
    }

    /**
     * Creates an action parameter that evaluates to a list of the values of all given nodes.
     *
     * @param nodes the nodes to get the values from
     * @return the action parameter
     */
    public List<V> VALUES(List<Node<V>> nodes) {
        List<V> values = new ArrayList<V>();
        for (Node<V> node : nodes) {
            values.add(node.getValue());
        }
        return values;
    }

    /**
     * Creates an action parameter that evaluates to a list of the values of all nodes found under the given prefix path.
     * Equivalent to <code>VALUES(NODES(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<V> VALUES(String path) {
        return VALUES(NODES(path));
    }

    /**
     * Creates an action parameter that evaluates to the value of the last node created during this parsing run.
     * Equivalent to <code>VALUE(LAST_NODE())</code>.
     *
     * @return the action parameter
     */
    public V LAST_VALUE() {
        return VALUE(LAST_NODE());
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the given parse tree node.
     *
     * @param node the parse tree node
     * @return the action parameter
     */
    public String TEXT(Node<V> node) {
        return context.getNodeText(node);
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the node found under the given prefix path.
     * Equivalent to <code>TEXT(NODE(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public String TEXT(String path) {
        return TEXT(NODE(path));
    }

    /**
     * Creates an action parameter that evaluates to a list of the input texts matched by all given nodes.
     *
     * @param nodes the nodes
     * @return the action parameter
     */
    public List<String> TEXTS(List<Node<V>> nodes) {
        List<String> values = new ArrayList<String>();
        for (Node<V> node : nodes) {
            values.add(context.getNodeText(node));
        }
        return values;
    }

    /**
     * Creates an action parameter that evaluates to a list of the input texts matched by of all nodes found
     * under the given prefix path.
     * Equivalent to <code>TEXTS(NODES(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<String> TEXTS(String path) {
        return TEXTS(NODES(path));
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the last node created during this parsing run.
     * Equivalent to <code>TEXT(LAST_NODE())</code>.
     *
     * @return the action parameter
     */
    public String LAST_TEXT() {
        return TEXT(LAST_NODE());
    }

    /**
     * Creates an action parameter that evaluates to the first character of the input text matched by the given parse tree node.
     *
     * @param node the parse tree node
     * @return the action parameter
     */
    public Character CHAR(Node<V> node) {
        return context.getNodeChar(node);
    }

    /**
     * Creates an action parameter that evaluates to the first character of the input text matched by the node found under the given prefix path.
     * Equivalent to <code>CHAR(NODE(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public Character CHAR(String path) {
        return CHAR(NODE(path));
    }

    /**
     * Creates an action parameter that evaluates to a list of the first characters of the input texts matched by all given nodes.
     *
     * @param nodes the nodes
     * @return the action parameter
     */
    public List<Character> CHARS(List<Node<V>> nodes) {
        List<Character> values = new ArrayList<Character>();
        for (Node<V> node : nodes) {
            values.add(context.getNodeChar(node));
        }
        return values;
    }

    /**
     * Creates an action parameter that evaluates to a list of the first characters of the input texts matched by of all nodes found
     * under the given prefix path.
     * Equivalent to <code>CHARS(NODES(path))</code>.
     *
     * @param path the path to search for
     * @return the action parameter
     */
    public List<Character> CHARS(String path) {
        return CHARS(NODES(path));
    }

    /**
     * Creates an action parameter that evaluates to the input text matched by the last node created during this parsing run.
     * Equivalent to <code>CHAR(LAST_NODE())</code>.
     *
     * @return the action parameter
     */
    public Character LAST_CHAR() {
        return CHAR(LAST_NODE());
    }

    /**
     * Creates a special action rule that sets the value of the parse tree node to be created for the current context
     * scope to the value of the last node created during the current parsing run.
     * Equivalent to <code>SET(LAST_VALUE())</code>.
     *
     * @return a new rule
     */
    public ActionResult SET() {
        return SET(LAST_VALUE());
    }

    /**
     * Creates a special action rule that sets the value of the parse tree node to be created for the current context
     * scope to the given value.
     *
     * @param value the value to set
     * @return a new rule
     */
    public ActionResult SET(V value) {
        context.setNodeValue(value);
        return ActionResult.CONTINUE;
    }

    /**
     * Creates a special predicate that evaluates to true if the two arguments evaluate to objects that are equal,
     * or more correctly if the following expression evaluates to true:
     * <code>a == b || (a != null && a.equals(b))</code>.
     *
     * @param a the first parameter
     * @param b the second parameter
     * @return a new rule
     */
    public ActionResult EQUALS(Object a, Object b) {
        return toActionResult(Utils.equals(a, b));
    }

    /**
     * Creates a wrapper for an action call that negates the result of the inner action,
     * i.e. flips {@link ActionResult#CONTINUE} to {@link ActionResult#CANCEL_MATCH} and vice versa.
     *
     * @param inner the inner action result to negate
     * @return a new rule
     */
    public ActionResult NOT(ActionResult inner) {
        return inner == ActionResult.CANCEL_MATCH ? ActionResult.CONTINUE : ActionResult.CANCEL_MATCH;
    }

    /**
     * Creates a wrapper for a number action calls that only returns {@link ActionResult#CONTINUE} if all sub actions
     * do not return {@link ActionResult#CANCEL_MATCH}.
     *
     * @param firstSubAction the first sub action
     * @param other          the other sub actions
     * @return a new rule
     */
    public ActionResult AND(ActionResult firstSubAction, ActionResult... other) {
        if (firstSubAction == ActionResult.CANCEL_MATCH) return ActionResult.CANCEL_MATCH;
        for (ActionResult result : other) {
            if (result == ActionResult.CANCEL_MATCH) return ActionResult.CANCEL_MATCH;
        }
        return ActionResult.CONTINUE;
    }

    /**
     * Creates a wrapper for a number action calls that return {@link ActionResult#CONTINUE} as soon as the first
     * sub action return anything but {@link ActionResult#CANCEL_MATCH}.
     *
     * @param firstSubAction the first sub action
     * @param other          the other sub actions
     * @return a new rule
     */
    public ActionResult OR(ActionResult firstSubAction, ActionResult... other) {
        if (firstSubAction == ActionResult.CONTINUE) return ActionResult.CONTINUE;
        for (ActionResult result : other) {
            if (result == ActionResult.CONTINUE) return ActionResult.CONTINUE;
        }
        return ActionResult.CANCEL_MATCH;
    }

    /**
     * Creates an action parameter that evaluates to the next input character about to be matched.
     *
     * @return the action parameter
     */
    public Character NEXT_CHAR() {
        return context.getCurrentLocation().currentChar;
    }

    /**
     * Creates an action parameter that converts the given text parameter to an object using the given converter.
     *
     * @param text      the text (parameter) to convert
     * @param converter the converter to use
     * @return the action parameter
     */
    public <T> T CONVERT(String text, @NotNull Converter<T> converter) {
        return converter.parse(text);
    }

    /**
     * Creates an action parameter that converts the given text parameter to an Integer.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Integer CONVERT_TO_INTEGER(String text) {
        return CONVERT(text, new Converter<Integer>() {
            public Integer parse(String string) {
                return Integer.parseInt(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Long.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Long CONVERT_TO_LONG(String text) {
        return CONVERT(text, new Converter<Long>() {
            public Long parse(String string) {
                return Long.parseLong(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Float.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Float CONVERT_TO_FLOAT(String text) {
        return CONVERT(text, new Converter<Float>() {
            public Float parse(String string) {
                return Float.parseFloat(string);
            }
        });
    }

    /**
     * Creates an action parameter that converts the given text parameter to a Double.
     *
     * @param text the text (parameter) to convert
     * @return the action parameter
     */
    public Double CONVERT_TO_DOUBLE(String text) {
        return CONVERT(text, new Converter<Double>() {
            public Double parse(String string) {
                return Double.parseDouble(string);
            }
        });
    }

}
