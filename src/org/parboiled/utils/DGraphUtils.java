package org.parboiled.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;

/*
 * Copyright 2008-2009 Mathias Doenitz, http://lis.to/
 *
 * This file is part of the listo java desktop client. The listo java desktop client is free software: you can
 * redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * The listo java desktop client is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with the listo java desktop client.
 * If not, see http://www.gnu.org/licenses/
 */

public class DGraphUtils {

    private DGraphUtils() {}

    public static boolean hasChildren(DGraphNode<?> node) {
        return node != null && !node.getChildren().isEmpty();
    }

    public static <T extends DGraphNode<T>> T getFirstChild(T node) {
        return hasChildren(node) ? node.getChildren().get(0) : null;
    }

    public static <T extends DGraphNode<T>> T getLastChild(T node) {
        return hasChildren(node) ? node.getChildren().get(node.getChildren().size() - 1) : null;
    }

    public static int countAll(DGraphNode<?> node) {
        if (node == null) return 0;
        int count = 1;
        for (DGraphNode<?> child : node.getChildren()) {
            count += countAll(child);
        }
        return count;
    }

    public static <T extends DGraphNode<T>> int countAllDistinct(T node) {
        if (node == null) return 0;
        return collectAllNodes(node, new HashSet<T>()).size();
    }

    public static <T extends DGraphNode<T>> T findEqual(T searchTreeRoot, T nodeToSearchFor) {
        if (searchTreeRoot != null && nodeToSearchFor != null) {
            if (searchTreeRoot.equals(nodeToSearchFor)) return searchTreeRoot;
            if (hasChildren(searchTreeRoot)) {
                for (T child : searchTreeRoot.getChildren()) {
                    T found = findEqual(child, nodeToSearchFor);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    public static <T extends DGraphNode<T>> boolean hasEqual(T searchTreeRoot, T nodeToSearchFor) {
        return findEqual(searchTreeRoot, nodeToSearchFor) != null;
    }

    @NotNull
    public static <T extends DGraphNode<T>, C extends Collection<T>> C collectAllNodes(T node, @NotNull C collection) {
        // we don't recurse if the collecion already contains the node
        // this costs a bit of performance but prevents infinite recursion in the case of graph loops
        if (node != null && !collection.contains(node)) {
            collection.add(node);
            for (T child : node.getChildren()) {
                collectAllNodes(child, collection);
            }
        }
        return collection;
    }

    public static <T extends DGraphNode<T>> String printTree(T root, Formatter<T> formatter) {
        return root == null ? "" : printTree(root, formatter, "", new StringBuilder()).toString();
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
