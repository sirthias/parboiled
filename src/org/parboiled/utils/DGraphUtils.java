package org.parboiled.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/**
 * General utility methods for operating on directed graphs (consisting of DGraphNodes).
 */
public class DGraphUtils {

    private DGraphUtils() {}

    /**
     * @param node a node
     * @return true if this node is not null and has at least one child node.
     */
    public static boolean hasChildren(DGraphNode<?> node) {
        return node != null && !node.getChildren().isEmpty();
    }

    /**
     * @param node a node
     * @return the first child node of the given node or null if node is null or does not have any children
     */
    public static <T extends DGraphNode<T>> T getFirstChild(T node) {
        return hasChildren(node) ? node.getChildren().get(0) : null;
    }

    /**
     * @param node a node
     * @return the last child node of the given node or null if node is null or does not have any children
     */
    public static <T extends DGraphNode<T>> T getLastChild(T node) {
        return hasChildren(node) ? node.getChildren().get(node.getChildren().size() - 1) : null;
    }

    /**
     * Counts all distinct nodes in the graph reachable from the given node.
     * This method can properly deal with cycles in the graph.
     * @param node the root node
     * @return the number of distinct nodes
     */
    public static <T extends DGraphNode<T>> int countAllDistinct(T node) {
        if (node == null) return 0;
        return collectAllNodes(node, new HashSet<T>()).size();
    }

    /**
     * Collects all nodes from the graph reachable from the given node in the given collection.
     * This method can properly deal with cycles in the graph.
     * @param node the root node
     * @param collection the collection to collect into
     * @return the same collection passed as a parameter
     */
    @NotNull
    public static <T extends DGraphNode<T>, C extends Collection<T>> C collectAllNodes(T node, @NotNull C collection) {
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
     * @param node the root node
     * @param formatter the node formatter
     * @return a new string
     */
    public static <T extends DGraphNode<T>> String printTree(T node, @NotNull Formatter<T> formatter) {
        return node == null ? "" : printTree(node, formatter, "", new StringBuilder()).toString();
    }

    // private recursion helper
    private static <T extends DGraphNode<T>> StringBuilder printTree(T node, Formatter<T> formatter,
                                                                  String indent, StringBuilder sb) {
        String line = formatter.format(node);
        if (line != null) {
            sb.append(indent).append(line).append("\n");
            indent += "    ";
        }
        if (hasChildren(node)) {
            for (T sub : node.getChildren()) {
                printTree(sub, formatter, indent, sb);
            }
        }
        return sb;
    }

}
