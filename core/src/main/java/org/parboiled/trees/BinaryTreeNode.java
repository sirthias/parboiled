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
 * A {@link TreeNode} specialization with only two child nodes, left and right.
 *
 * @param <T> the actual implementation type of this tree node
 */
public interface BinaryTreeNode<T extends BinaryTreeNode<T>> extends TreeNode<T> {

    /**
     * Returns the left sub node.
     *
     * @return the left sub node
     */
    T left();

    /**
     * Returns the right sub node.
     *
     * @return the right sub node
     */
    T right();

}