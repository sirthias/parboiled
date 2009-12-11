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

package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Node;
import org.parboiled.common.Function;
import org.parboiled.common.Predicate;
import org.parboiled.common.StringUtils;
import org.parboiled.common.Utils;
import static org.parboiled.trees.GraphUtils.hasChildren;
import static org.parboiled.trees.GraphUtils.printTree;
import org.parboiled.trees.Printability;

import java.util.Collection;
import java.util.List;

/**
 * General utility methods for operating on parse trees.
 */
public class ParseTreeUtils {

    private ParseTreeUtils() {}

    /**
     * <p>Returns the parse tree node underneath the given parent that matches the given path.</p>
     * <p>The path is a '/' separated list of node label prefixes describing the ancestor chain of the node to look for
     * relative to the given parent node. If there are several nodes that match the given path the method
     * returns the first one unless the respective path segments has the special prefix "last:". In this case the
     * method will return the last matching node.</p>
     * <p><b>Example:</b> "per/last:so/fix" will return the first node, whose label starts with "fix" under the last
     * node, whose label starts with "so" under the first node, whose label starts with "per".</p>
     * If parent is null or no node is found the method returns null.
     *
     * @param parent the parent Node
     * @param path   the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByPath(Node<V> parent, @NotNull String path) {
        return parent != null && hasChildren(parent) ? findNodeByPath(parent.getChildren(), path) : null;
    }

    /**
     * Returns the node underneath the given parents that matches the given path.
     * See {@link #findNodeByPath(org.parboiled.Node, String)} )} for a description of the path argument.
     * If the given collections of parents is null or empty or no node is found the method returns null.
     *
     * @param parents the parent Nodes to look through
     * @param path    the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByPath(List<Node<V>> parents, @NotNull String path) {
        if (parents != null && !parents.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String prefix = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            Iterable<Node<V>> iterable = parents;
            if (prefix.startsWith("last:")) {
                prefix = prefix.substring(5);
                iterable = Utils.reverse(parents);
            }
            for (Node<V> child : iterable) {
                if (StringUtils.startsWith(child.getLabel(), prefix)) {
                    return separatorIndex == -1 ? child : findNodeByPath(child, path.substring(separatorIndex + 1));
                }
            }
        }
        return null;
    }

    /**
     * Collects all nodes underneath the given parent that match the given path.
     * The path is a '/' separated list of node label prefixes describing the ancestor chain of the node to look for
     * relative to the given parent node.
     *
     * @param parent     the parent Node
     * @param path       the path to the Nodes being searched for
     * @param collection the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodesByPath(Node<V> parent,
                                                                          @NotNull String path,
                                                                          @NotNull C collection) {
        return parent != null && hasChildren(parent) ?
                collectNodesByPath(parent.getChildren(), path, collection) : collection;
    }

    /**
     * Collects all nodes underneath the given parents that match the given path.
     * The path is a '/' separated list of node label prefixes describing the ancestor chain of the node to look for
     * relative to the given parent nodes.
     *
     * @param parents    the parent Nodes to look through
     * @param path       the path to the Nodes being searched for
     * @param collection the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodesByPath(List<Node<V>> parents,
                                                                          @NotNull String path,
                                                                          @NotNull C collection) {
        if (parents != null && !parents.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String prefix = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            Iterable<Node<V>> iterable = parents;
            if (prefix.startsWith("last:")) {
                prefix = prefix.substring(5);
                iterable = Utils.reverse(parents);
            }
            for (Node<V> child : iterable) {
                if (StringUtils.startsWith(child.getLabel(), prefix)) {
                    if (separatorIndex == -1) {
                        collection.add(child);
                    } else {
                        collectNodesByPath(child, path.substring(separatorIndex + 1), collection);
                    }
                }
            }
        }
        return collection;
    }

    /**
     * Returns the first node underneath the given parent for which the given predicate evaluates to true.
     * If parent is null or no node is found the method returns null.
     *
     * @param parent    the parent Node
     * @param predicate the predicate
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNode(Node<V> parent, @NotNull Predicate<Node<V>> predicate) {
        if (parent != null) {
            if (predicate.apply(parent)) return parent;
            if (hasChildren(parent)) {
                Node<V> found = findNode(parent.getChildren(), predicate);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Returns the first node underneath the given parents for which the given predicate evaluates to true.
     * If parents is null or empty or no node is found the method returns null.
     *
     * @param parents   the parent Nodes to look through
     * @param predicate the predicate
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNode(List<Node<V>> parents, @NotNull Predicate<Node<V>> predicate) {
        if (parents != null && !parents.isEmpty()) {
            for (Node<V> child : parents) {
                Node<V> found = findNode(child, predicate);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Returns the last node underneath the given parent for which the given predicate evaluates to true.
     * If parent is null or no node is found the method returns null.
     *
     * @param parent    the parent Node
     * @param predicate the predicate
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findLastNode(Node<V> parent, @NotNull Predicate<Node<V>> predicate) {
        if (parent != null) {
            if (predicate.apply(parent)) return parent;
            if (hasChildren(parent)) {
                Node<V> found = findLastNode(parent.getChildren(), predicate);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Returns the last node underneath the given parents for which the given predicate evaluates to true.
     * If parents is null or empty or no node is found the method returns null.
     *
     * @param parents   the parent Nodes to look through
     * @param predicate the predicate
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findLastNode(List<Node<V>> parents, @NotNull Predicate<Node<V>> predicate) {
        if (parents != null && !parents.isEmpty()) {
            for (Node<V> child : Utils.reverse(parents)) {
                Node<V> found = findLastNode(child, predicate);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Collects all nodes underneath the given parent for which the given predicate evaluates to true.
     *
     * @param parent     the parent Node
     * @param predicate  the predicate
     * @param collection the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodes(Node<V> parent,
                                                                    @NotNull Predicate<Node<V>> predicate,
                                                                    @NotNull C collection) {
        return parent != null && hasChildren(parent) ?
                collectNodes(parent.getChildren(), predicate, collection) : collection;
    }

    /**
     * Collects all nodes underneath the given parents for which the given predicate evaluates to true.
     *
     * @param parents    the parent Nodes to look through
     * @param predicate  the predicate
     * @param collection the collection to collect the found Nodes into
     * @return the same collection instance passed as a parameter
     */
    public static <V, C extends Collection<Node<V>>> C collectNodes(List<Node<V>> parents,
                                                                    @NotNull Predicate<Node<V>> predicate,
                                                                    @NotNull C collection) {
        if (parents != null && !parents.isEmpty()) {
            for (Node<V> child : parents) {
                if (predicate.apply(child)) {
                    collection.add(child);
                }
                collectNodes(child, predicate, collection);
            }
        }
        return collection;
    }

    /**
     * Returns the input text matched by the given node.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    public static String getNodeText(Node<?> node, @NotNull InputBuffer inputBuffer) {
        return node != null ? inputBuffer.extract(node.getStartLocation().index, node.getEndLocation().index) : null;
    }

    /**
     * Returns the first input character matched by the given node.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null or did not match at least one character otherwise the first matched input char
     */
    public static Character getNodeChar(Node<?> node, InputBuffer inputBuffer) {
        return node != null && node.getEndLocation().index > node.getStartLocation().index ?
                inputBuffer.charAt(node.getStartLocation().index) : null;
    }

    /**
     * Creates a readable string represenation of the parse tree in thee given ParsingResult object.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @return a new String
     */
    public static <V> String printNodeTree(@NotNull ParsingResult<V> parsingResult) {
        return printNodeTree(parsingResult, null);
    }

    /**
     * Creates a readable string represenation of the parse tree in thee given ParsingResult object.
     * If a non-null filter function is given its result is used to determine whether a particular node is
     * print and/or its subtree printed.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @param filter        optional node filter selecting the nodes to print and/or descend into for tree printing
     * @return a new String
     */
    public static <V> String printNodeTree(@NotNull ParsingResult<V> parsingResult,
                                           Function<Node<V>, Printability> filter) {
        return printTree(parsingResult.parseTreeRoot, new NodeFormatter<V>(parsingResult.inputBuffer), filter);
    }

}

