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

package org.parboiled.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A simple, immutable DGraphNode implementation.
 * @param <T>
 */
public class ImmutableDGraphNode<T extends DGraphNode<T>> implements DGraphNode<T> {

    @NotNull private final List<T> children;

    public ImmutableDGraphNode() {
        this(null);
    }

    public ImmutableDGraphNode(List<T> children) {
        this.children = children != null ? ImmutableList.copyOf(children) : ImmutableList.<T>of();
    }

    @NotNull
    public List<T> getChildren() {
        return children;
    }

}
