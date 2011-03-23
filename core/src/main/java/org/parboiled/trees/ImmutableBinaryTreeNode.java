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

import org.parboiled.common.ImmutableList;

/**
 * A simple immutable implementation of the {@link BinaryTreeNode} interface.
 *
 * @param <T> the actual implementation type of this ImmutableBinaryTreeNode
 */
public class ImmutableBinaryTreeNode<T extends BinaryTreeNode<T>> extends ImmutableTreeNode<T>
        implements BinaryTreeNode<T> {

    private final T left;
    private final T right;

    @SuppressWarnings({"unchecked"})
    public ImmutableBinaryTreeNode(T left, T right) {
        super(left == null ?
                right == null ? ImmutableList.<T>of() : ImmutableList.of(right) :
                right == null ? ImmutableList.of(left) : ImmutableList.of(left, right));
        this.left = left;
        this.right = right;
    }

    public T left() {
        return left;
    }

    public T right() {
        return right;
    }

}
