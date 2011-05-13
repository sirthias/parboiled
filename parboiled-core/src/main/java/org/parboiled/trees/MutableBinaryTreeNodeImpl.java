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
 * A simple {@link MutableBinaryTreeNode} implementation based on the {@link MutableTreeNodeImpl}.
 *
 * @param <T> the actual implementation type of this MutableBinaryTreeNodeImpl
 */
public class MutableBinaryTreeNodeImpl<T extends MutableBinaryTreeNode<T>> extends MutableTreeNodeImpl<T>
        implements BinaryTreeNode<T> {

    public MutableBinaryTreeNodeImpl() {
        super.addChild(0, null); // left
        super.addChild(1, null); // right
    }

    public T left() {
        return getChildren().get(0);
    }

    public void setLeft(T node) {
        setChild(0, node);
    }

    public T right() {
        return getChildren().get(1);
    }

    public void setRight(T node) {
        setChild(1, node);
    }

    @Override
    public void addChild(int index, T child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T removeChild(int index) {
        throw new UnsupportedOperationException();
    }

}