/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import org.parboiled.Node;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Predicate;
import org.parboiled.common.Predicates;
import org.parboiled.common.StringUtils;

import java.util.Collection;
import java.util.List;

import static org.parboiled.common.Preconditions.checkArgNotNull;
import static org.parboiled.trees.GraphUtils.hasChildren;
import static org.parboiled.trees.GraphUtils.printTree;

/**
 * General utility methods for operating on parse trees.
 */
public final class ParseTreeUtils {

    private ParseTreeUtils() {}

    /**
     * <p>Returns the parse tree node underneath the given parent that matches the given path.</p>
     * <p>The path is a '/' separated list of node label prefixes describing the ancestor chain of the node to look for
     * relative to the given parent node. If there are several nodes that match the given path the method
     * returns the first one unless the respective path segments has the special prefix "last:". In this case the
     * last matching node is returned.
     * <p><b>Example:</b> "per/last:so/fix" will return the first node, whose label starts with "fix" under the last
     * node, whose label starts with "so" under the first node, whose label starts with "per".</p>
     * If parent is null or no node is found the method returns null.
     *
     * @param parent the parent Node
     * @param path   the path to the Node being searched for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByPath(Node<V> parent, String path) {
        checkArgNotNull(path, "path");
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
    public static <V> Node<V> findNodeByPath(List<Node<V>> parents, String path) {
        checkArgNotNull(path, "path");
        if (parents != null && !parents.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String prefix = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            int start = 0, step = 1;
            if (prefix.startsWith("last:")) {
                prefix = prefix.substring(5);
                start = parents.size() - 1;
                step = -1;
            }
            for (int i = start; 0 <= i && i < parents.size(); i += step) {
                Node<V> child = parents.get(i);
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
    public static <V, C extends Collection<Node<V>>> C collectNodesByPath(Node<V> parent, String path, C collection) {
        checkArgNotNull(path, "path");
        checkArgNotNull(collection, "collection");
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
    public static <V, C extends Collection<Node<V>>> C collectNodesByPath(List<Node<V>> parents, String path,
                                                                          C collection) {
        checkArgNotNull(path, "path");
        checkArgNotNull(collection, "collection");
        if (parents != null && !parents.isEmpty()) {
            int separatorIndex = path.indexOf('/');
            String prefix = separatorIndex != -1 ? path.substring(0, separatorIndex) : path;
            for (Node<V> child : parents) {
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
    public static <V> Node<V> findNode(Node<V> parent, Predicate<Node<V>> predicate) {
        checkArgNotNull(predicate, "predicate");
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
    public static <V> Node<V> findNode(List<Node<V>> parents, Predicate<Node<V>> predicate) {
        checkArgNotNull(predicate, "predicate");
        if (parents != null && !parents.isEmpty()) {
            for (Node<V> child : parents) {
                Node<V> found = findNode(child, predicate);
                if (found != null) return found;
            }
        }
        return null;
    }

    /**
     * Returns the first node underneath the given parent for which matches the given label prefix.
     * If parents is null or empty or no node is found the method returns null.
     *
     * @param parent      the parent node
     * @param labelPrefix the label prefix to look for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByLabel(Node<V> parent, String labelPrefix) {
        return findNode(parent, new LabelPrefixPredicate<V>(labelPrefix));
    }

    /**
     * Returns the first node underneath the given parents which matches the given label prefix.
     * If parents is null or empty or no node is found the method returns null.
     *
     * @param parents     the parent Nodes to look through
     * @param labelPrefix the label prefix to look for
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findNodeByLabel(List<Node<V>> parents, String labelPrefix) {
        return findNode(parents, new LabelPrefixPredicate<V>(labelPrefix));
    }

    /**
     * Returns the last node underneath the given parent for which the given predicate evaluates to true.
     * If parent is null or no node is found the method returns null.
     *
     * @param parent    the parent Node
     * @param predicate the predicate
     * @return the Node if found or null if not found
     */
    public static <V> Node<V> findLastNode(Node<V> parent, Predicate<Node<V>> predicate) {
        checkArgNotNull(predicate, "predicate");
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
    public static <V> Node<V> findLastNode(List<Node<V>> parents, Predicate<Node<V>> predicate) {
        checkArgNotNull(predicate, "predicate");
        if (parents != null && !parents.isEmpty()) {
            int parentsSize = parents.size();
            for (int i = parentsSize - 1; i >= 0; i--) {
                Node<V> found = findLastNode(parents.get(i), predicate);
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
                                                                    Predicate<Node<V>> predicate,
                                                                    C collection) {
        checkArgNotNull(predicate, "predicate");
        checkArgNotNull(collection, "collection");
        return parent != null && hasChildren(parent) ?
                collectNodes(parent.getChildren(), predicate, collection) : collection;
    }

    /**
     * Returns the input text matched by the given node, with error correction.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    public static String getNodeText(Node<?> node, InputBuffer inputBuffer) {
        checkArgNotNull(node, "node");
        checkArgNotNull(inputBuffer, "inputBuffer");
        if (node.hasError()) {
            // if the node has a parse error we cannot simply cut a string out of the underlying input buffer, since we
            // would also include illegal characters, so we need to build it constructively
            StringBuilder sb = new StringBuilder();
            for (int i = node.getStartIndex(); i < node.getEndIndex(); i++) {
                char c = inputBuffer.charAt(i);
                switch (c) {
                    case Chars.DEL_ERROR:
                        i++;
                        break;
                    case Chars.INS_ERROR:
                    case Chars.EOI:
                        break;
                    case Chars.RESYNC_START:
                        i++;
                        while (inputBuffer.charAt(i) != Chars.RESYNC_END) i++;
                        break;
                    case Chars.RESYNC_END:
                    case Chars.RESYNC_EOI:
                    case Chars.RESYNC:
                        // we should only see proper RESYNC_START / RESYNC_END blocks
                        throw new IllegalStateException();
                    default:
                        sb.append(c);
                }
            }
            return sb.toString();
        }        
        return inputBuffer.extract(node.getStartIndex(), node.getEndIndex());
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
                                                                    Predicate<Node<V>> predicate,
                                                                    C collection) {
        checkArgNotNull(predicate, "predicate");
        checkArgNotNull(collection, "collection");
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
     * Creates a readable string represenation of the parse tree in the given {@link ParsingResult} object.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @return a new String
     */
    public static <V> String printNodeTree(ParsingResult<V> parsingResult) {
        checkArgNotNull(parsingResult, "parsingResult");
        return printNodeTree(parsingResult, Predicates.<Node<V>>alwaysTrue(), Predicates.<Node<V>>alwaysTrue());
    }

    /**
     * Creates a readable string represenation of the parse tree in thee given {@link ParsingResult} object.
     * The given filter predicate determines whether a particular node (incl. its subtree) is printed or not.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @param nodeFilter    the predicate selecting the nodes to print
     * @param subTreeFilter the predicate determining whether to descend into a given nodes subtree or not
     * @return a new String
     */
    public static <V> String printNodeTree(ParsingResult<V> parsingResult, Predicate<Node<V>> nodeFilter,
                                           Predicate<Node<V>> subTreeFilter) {
        checkArgNotNull(parsingResult, "parsingResult");
        checkArgNotNull(nodeFilter, "nodeFilter");
        checkArgNotNull(subTreeFilter, "subTreeFilter");
        return printTree(parsingResult.parseTreeRoot, new NodeFormatter<V>(parsingResult.inputBuffer), nodeFilter,
                subTreeFilter);
    }

}

