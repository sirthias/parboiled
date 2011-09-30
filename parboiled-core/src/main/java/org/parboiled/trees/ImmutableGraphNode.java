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

import org.parboiled.common.ImmutableLinkedList;
import org.parboiled.common.ImmutableList;

import java.util.List;

/**
 * A simple, immutable {@link GraphNode} implementation.
 *
 * @param <T> the actual implementation type of this ImmutableGraphNode
 */
public class ImmutableGraphNode<T extends GraphNode<T>> implements GraphNode<T> {

    private final List<T> children;

    public ImmutableGraphNode() {
        this(null);
    }

    public ImmutableGraphNode(List<T> children) {
        this.children = children ==
                null ? ImmutableList.<T>of() :
                children instanceof ImmutableList ? children :
                children instanceof ImmutableLinkedList ? children :
                ImmutableList.copyOf(children);
    }

    public List<T> getChildren() {
        return children;
    }
}
