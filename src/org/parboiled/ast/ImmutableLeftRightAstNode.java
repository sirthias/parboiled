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

package org.parboiled.ast;

import org.parboiled.utils.ImmutableList;

public class ImmutableLeftRightAstNode<T, N extends ImmutableLeftRightAstNode<T, N>> extends ImmutableAstNode<T, N>
        implements LeftRightAstNode<T, N> {

    public ImmutableLeftRightAstNode() {
        this(null, null, null);
    }

    public ImmutableLeftRightAstNode(T type) {
        this(type, null, null);
    }

    public ImmutableLeftRightAstNode(T type, N left) {
        this(type, left, null);
    }

    public ImmutableLeftRightAstNode(T type, N left, N right) {
        super(type, ImmutableList.of(left, right));
    }

    public N left() {
        return getChildren().get(0);
    }

    public N right() {
        return getChildren().get(1);
    }

}
