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
 * A {@link TreeNode} specialiation that allow for mutability of the tree structure.
 * The three defined methods are all expected to properly uphold the trees "linking back" contract, where children
 * have their parent fields point to the node actually holding them in their children list at all times.
 * The three defined methods are the basic ones required, other convenience methods (like a simple addChild(child)
 * without index) are defined as static methods of the {@link TreeUtils} class.
 *
 * @param <T> the actual implementation type of this TreeNode
 */
public interface MutableTreeNode<T extends MutableTreeNode<T>> extends TreeNode<T> {

    /**
     * Adds the given child to this nodes children list and setting the childs parent field to this node.
     * If the child is currently attached to another node it is first removed.
     *
     * @param index the index under which to insert this child into the children list
     * @param child the child node to add
     */
    void addChild(int index, T child);

    /**
     * Sets the child node at the given index to the given node. The node previously existing at the given child index
     * is first properly removed by setting its parent field to null. If the child is currently attached to another
     * node it is first removed from its old parent.
     *
     * @param index the index under which to set this child into the children list
     * @param child the child node to set
     */
    void setChild(int index, T child);

    /**
     * Removes the child with the given index.
     *
     * @param index the index of the child to remove.
     * @return the removed child
     */
    T removeChild(int index);

}