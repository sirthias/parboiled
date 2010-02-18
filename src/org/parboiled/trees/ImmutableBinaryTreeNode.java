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

import com.google.common.collect.ImmutableList;

/**
 * A simple immutable implementation of the BinaryTreeNode interface.
 *
 * @param <N>
 */
@SuppressWarnings({"unchecked"})
public class ImmutableBinaryTreeNode<N extends BinaryTreeNode<N>> extends ImmutableTreeNode<N>
        implements BinaryTreeNode<N> {

    private final N left;
    private final N right;

    @SuppressWarnings({"unchecked"})
    public ImmutableBinaryTreeNode(N left, N right) {
        super(left == null ?
                right == null ? ImmutableList.<N>of() : ImmutableList.of(right) :
                right == null ? ImmutableList.of(left) : ImmutableList.of(left, right));
        this.left = left;
        this.right = right;
    }

    public N left() {
        return left;
    }

    public N right() {
        return right;
    }

}
