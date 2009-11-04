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

package org.parboiled.trees;

/**
 * A simple MutableBinaryTreeNode implementation based on the MutableTreeNodeImpl.
 * @param <N>
 */
public class MutableBinaryTreeNodeImpl<N extends MutableBinaryTreeNode<N>> extends MutableTreeNodeImpl<N>
        implements BinaryTreeNode<N> {

    public MutableBinaryTreeNodeImpl() {
        super.addChild(0, null); // left
        super.addChild(1, null); // right
    }

    public N left() {
        return getChildren().get(0);
    }

    public void setLeft(N node) {
        setChild(0, node);
    }

    public N right() {
        return getChildren().get(1);
    }

    public void setRight(N node) {
        setChild(1, node);
    }

    @Override
    public void addChild(int index, N child) {
        throw new UnsupportedOperationException();
    }

    @Override
    public N removeChild(int index) {
        throw new UnsupportedOperationException();
    }

}