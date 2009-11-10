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

public class TreeUtils {

    private TreeUtils() {}

    public static <T extends TreeNode<T>> T getRoot(T node) {
        if (node == null) return null;
        if (node.getParent() != null) return getRoot(node.getParent());
        return node;
    }

    public static <T extends MutableTreeNode<T>> void addChild(T parent, T child) {
        if (parent != null) {
            parent.addChild(parent.getChildren().size(), child);
        }
    }

    public static <T extends MutableTreeNode<T>> void removeChild(T parent, @NotNull T child) {
        if (parent != null) {
            parent.removeChild(parent.getChildren().indexOf(child));
        }
    }

    /**
     * Performs the following transformation on the given MutableBinaryTreeNode:
     * <pre>
     *        o1                    o2
     *       / \                   / \
     *      A   o2     ====>     o1   C
     *         / \              / \
     *        B   C            A   B
     * </pre>
     *
     * @param node the node to transform
     * @return the new root after the transformation, which is either the right sub node of the original root
     *         or the original root, if the right sub node is null
     */
    public static <N extends MutableBinaryTreeNode<N>> N toLeftAssociativity(@NotNull N node) {
        N right = node.right();
        if (right == null) return node;

        node.setRight(right.left());
        right.setLeft(node);
        return right;
    }

}
