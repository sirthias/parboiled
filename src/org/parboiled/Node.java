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

package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.trees.TreeNode;
import org.parboiled.support.InputLocation;

/**
 * A Node represents a node in the parse tree created during a parsing run.
 */
public interface Node<V> extends TreeNode<Node<V>> {

    /**
     * @return the label of this node, usually set to the name of the matcher that created this node
     */
    String getLabel();

    /**
     * @return the start location of this nodes text in the underlying input buffer.
     */
    @NotNull
    InputLocation getStartLocation();

    /**
     * @return the end location of this nodes text in the underlying input buffer.
     */
    @NotNull
    InputLocation getEndLocation();

    /**
     * @return the value object attached to this node
     */
    V getValue();

}
