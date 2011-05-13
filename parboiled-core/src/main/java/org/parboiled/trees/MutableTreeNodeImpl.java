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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.parboiled.common.Preconditions.*;

/**
 * A base implementation of the {@link MutableTreeNode}.
 *
 * @param <T> the actual implementation type of this MutableTreeNodeImpl
 */
public class MutableTreeNodeImpl<T extends MutableTreeNode<T>> implements MutableTreeNode<T> {

    private final List<T> children = new ArrayList<T>();
    private final List<T> childrenView = Collections.unmodifiableList(children);
    private T parent;

    public T getParent() {
        return parent;
    }

    public List<T> getChildren() {
        return childrenView;
    }

    public void addChild(int index, T child) {
        checkElementIndex(index, children.size() + 1);

        // detach new child from old parent
        if (child != null) {
            if (child.getParent() == this) return;
            if (child.getParent() != null) {
                TreeUtils.removeChild(child.getParent(), child);
            }
        }

        // attach new child
        children.add(index, child);
        setParent(child, this);
    }

    public void setChild(int index, T child) {
        checkElementIndex(index, children.size());

        // detach old child
        T old = children.get(index);
        if (old == child) return;
        setParent(old, null);

        // detach new child from old parent
        if (child != null && child.getParent() != this) {
            TreeUtils.removeChild(child.getParent(), child);
        }

        // attach new child
        children.set(index, child);
        setParent(child, this);
    }

    public T removeChild(int index) {
        checkElementIndex(index, children.size());
        T removed = children.remove(index);
        setParent(removed, null);
        return removed;
    }

    @SuppressWarnings("unchecked")
    private static <T extends MutableTreeNode<T>> void setParent(T node, MutableTreeNodeImpl<T> parent) {
        if (node != null) {
            ((MutableTreeNodeImpl) node).parent = parent;
        }
    }

}