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
import org.parboiled.support.InputLocation;
import org.parboiled.trees.ImmutableTreeNode;

import java.util.List;

/**
 * An immutable implementation of the Node interface.
 * @param <V>
 */
class NodeImpl<V> extends ImmutableTreeNode<Node<V>> implements Node<V> {

    private final String label;
    private final InputLocation startLocation;
    private final InputLocation endLocation;
    private final V value;

    public NodeImpl(String label, List<Node<V>> children, @NotNull InputLocation startLocation,
                    @NotNull InputLocation endLocation, V value) {
        super(children);
        this.label = label;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    @NotNull
    public InputLocation getStartLocation() {
        return startLocation;
    }

    @NotNull
    public InputLocation getEndLocation() {
        return endLocation;
    }

    public V getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(label);
        if (value != null) {
            sb.append(", {").append(value).append('}');
        }
        sb.append(']');
        return sb.toString();
    }

}
