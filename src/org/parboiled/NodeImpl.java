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
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;

import java.util.List;

/**
 * An immutable implementation of the Node interface.
 *
 * @param <V> the type of the value field of a parse tree node
 */
class NodeImpl<V> extends ImmutableTreeNode<Node<V>> implements Node<V> {

    private final Matcher<V> matcher;
    private final InputLocation startLocation;
    private final InputLocation endLocation;
    private final V value;
    private final boolean hasError;

    public NodeImpl(@NotNull Matcher<V> matcher, List<Node<V>> children, @NotNull InputLocation startLocation,
                    @NotNull InputLocation endLocation, V value, boolean hasError) {
        super(children);
        this.matcher = matcher;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.value = value;
        this.hasError = hasError;
    }

    @NotNull
    public Matcher<V> getMatcher() {
        return matcher;
    }

    public String getLabel() {
        return matcher.getLabel();
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

    public boolean hasError() {
        return hasError;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(getLabel());
        if (value != null) {
            sb.append(", {").append(value).append('}');
        }
        sb.append(']');
        if (hasError) sb.append('E'); 
        return StringUtils.escape(sb.toString());
    }

}
