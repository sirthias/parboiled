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

import java.util.List;

/**
 * A node in a directed graph (that may have cycles).
 * The children list must not contain null entries.
 *
 * @param <T> the actual implementation type of this graph node
 */
public interface GraphNode<T extends GraphNode<T>> {

    /**
     * Returns the sub nodes of this node.
     *
     * @return the sub nodes
     */
    List<T> getChildren();

}
