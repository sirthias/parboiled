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

import java.util.List;

/**
 * An ImmutableDGraphNode specialization representing a tree node with a parent field linking back to the nodes
 * (only) parent.
 */
public class ImmutableTreeNode<T extends TreeNode<T>> extends ImmutableDGraphNode<T> implements TreeNode<T> {

    // we cannot make the parent field final since otherwise we can't createActions a tree hierarchy with parents linking to
    // their children and vice versa. So we design this for a bottom up tree construction strategy were children
    // are created first and then "acquired" by their parents
    private T parent;

    public ImmutableTreeNode() {
    }

    public ImmutableTreeNode(List<T> children) {
        super(children);
        acquireChildren();
    }

    public T getParent() {
        return parent;
    }

    // this method is private to assure immutability to the outside
    private void setParent(T parent) {
        this.parent = parent;
    }

    @SuppressWarnings({"unchecked"})
    protected void acquireChildren() {
        for (T child : getChildren()) {
            ((ImmutableTreeNode)child).setParent(this);
        }
    }

}
