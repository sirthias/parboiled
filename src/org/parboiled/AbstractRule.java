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
import org.parboiled.trees.GraphNode;
import org.parboiled.trees.ImmutableGraphNode;

import java.util.List;

/**
 * Abstract base class of all Rules.
 * @param <T>
 */
abstract class AbstractRule<T extends GraphNode<T>> extends ImmutableGraphNode<T> implements Rule {

    private String label;
    private boolean locked;

    protected AbstractRule(@NotNull List<T> children) {
        super(children);
    }

    private void checkNotLocked() {
        if (locked) {
            throw new UnsupportedOperationException("Rule has been locked, no further change allowed");
        }
    }

    public Rule lock() {
        locked = true;
        return this;
    }

    public Rule label(String label) {
        checkNotLocked();
        this.label = label;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLocked() {
        return locked;
    }

}
