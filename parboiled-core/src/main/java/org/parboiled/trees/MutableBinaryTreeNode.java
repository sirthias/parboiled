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

package org.parboiled.trees;

/**
 * A {@link MutableTreeNode} specialization also satisfying the {@link BinaryTreeNode} interface
 * and providing mutability methods.
 *
 * @param <T> the actual implementation type of this MutableBinaryTreeNode
 */
public interface MutableBinaryTreeNode<T extends MutableBinaryTreeNode<T>>
        extends BinaryTreeNode<T>, MutableTreeNode<T> {

    /**
     * Sets the left child node to the given node.
     *
     * @param node the node to set as left child
     */
    void setLeft(T node);

    /**
     * Sets the right child node to the given node.
     *
     * @param node the node to set as right child
     */
    void setRight(T node);

}