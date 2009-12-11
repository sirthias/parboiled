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

package org.parboiled.trees;

import org.jetbrains.annotations.NotNull;
import org.parboiled.common.Formatter;
import org.parboiled.common.Function;

import java.util.Collection;
import java.util.HashSet;

/**
 * General utility methods for operating on directed graphs (consisting of GraphNodes).
 */
public class GraphUtils {

    private GraphUtils() {}

    /**
     * Returns true if this node is not null and has at least one child node.
     *
     * @param node a node
     * @return true if this node is not null and has at least one child node.
     */
    public static boolean hasChildren(GraphNode<?> node) {
        return node != null && !node.getChildren().isEmpty();
    }

    /**
     * Returns the first child node of the given node or null if node is null or does not have any children.
     *
     * @param node a node
     * @return the first child node of the given node or null if node is null or does not have any children
     */
    public static <T extends GraphNode<T>> T getFirstChild(T node) {
        return hasChildren(node) ? node.getChildren().get(0) : null;
    }

    /**
     * Returns the last child node of the given node or null if node is null or does not have any children.
     *
     * @param node a node
     * @return the last child node of the given node or null if node is null or does not have any children
     */
    public static <T extends GraphNode<T>> T getLastChild(T node) {
        return hasChildren(node) ? node.getChildren().get(node.getChildren().size() - 1) : null;
    }

    /**
     * Counts all distinct nodes in the graph reachable from the given node.
     * This method can properly deal with cycles in the graph.
     *
     * @param node the root node
     * @return the number of distinct nodes
     */
    public static <T extends GraphNode<T>> int countAllDistinct(T node) {
        if (node == null) return 0;
        return collectAllNodes(node, new HashSet<T>()).size();
    }

    /**
     * Collects all nodes from the graph reachable from the given node in the given collection.
     * This method can properly deal with cycles in the graph.
     *
     * @param node       the root node
     * @param collection the collection to collect into
     * @return the same collection passed as a parameter
     */
    @NotNull
    public static <T extends GraphNode<T>, C extends Collection<T>> C collectAllNodes(T node, @NotNull C collection) {
        // we don't recurse if the collecion already contains the node
        // this costs a bit of performance but prevents infinite recursion in the case of graph cycles
        if (node != null && !collection.contains(node)) {
            collection.add(node);
            for (T child : node.getChildren()) {
                collectAllNodes(child, collection);
            }
        }
        return collection;
    }

    /**
     * Creates a string representation of the graph reachable from the given node using the given formatter.
     *
     * @param node      the root node
     * @param formatter the node formatter
     * @return a new string
     */
    public static <T extends GraphNode<T>> String printTree(T node, @NotNull Formatter<T> formatter) {
        return printTree(node, formatter, null);
    }

    /**
     * Creates a string representation of the graph reachable from the given node using the given formatter.
     * If a non-null filter function is given its result is used to determine whether a particular node is
     * print and/or its subtree printed.
     *
     * @param node      the root node
     * @param formatter the node formatter
     * @param filter    optional node filter selecting the nodes to descend into for tree printing
     * @return a new string
     */
    public static <T extends GraphNode<T>> String printTree(T node, @NotNull Formatter<T> formatter,
                                                            Function<T, Printability> filter) {
        return node == null ? "" : printTree(node, formatter, "", new StringBuilder(), filter).toString();
    }

    // private recursion helper
    private static <T extends GraphNode<T>> StringBuilder printTree(T node, Formatter<T> formatter,
                                                                    String indent, StringBuilder sb,
                                                                    Function<T, Printability> filter) {
        Printability printability = filter != null ? filter.apply(node) : Printability.PrintAndDescend;
        if (printability == Printability.PrintAndDescend || printability == Printability.Print) {
            String line = formatter.format(node);
            if (line != null) {
                sb.append(indent).append(line).append("\n");
                indent += "    ";
            }
        }
        if ((printability == Printability.PrintAndDescend || printability == Printability.Descend) &&
                hasChildren(node)) {
            for (T sub : node.getChildren()) {
                printTree(sub, formatter, indent, sb, filter);
            }
        }
        return sb;
    }

}
