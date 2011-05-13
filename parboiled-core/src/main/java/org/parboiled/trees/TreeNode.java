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
 * A specialization of a {@link GraphNode} that contains a reference to its parent, thereby making the graph a tree
 * (since each node can now have only one parent node).
 *
 * @param <T> the actual implementation type of this TreeNode
 */
public interface TreeNode<T extends TreeNode<T>> extends GraphNode<T> {

    /**
     * Returns the parent node or null if this node is the root.
     *
     * @return the parent node
     */
    T getParent();

}
