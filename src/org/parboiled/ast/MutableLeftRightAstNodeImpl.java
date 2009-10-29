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

public class MutableLeftRightAstNodeImpl<T, N extends MutableLeftRightAstNode<T, N>>
        extends MutableAstNodeImpl<T, N>
        implements MutableLeftRightAstNode<T, N> {

    public N left() {
        return !getChildren().isEmpty() ? getChildren().get(0) : null;
    }

    public void setLeft(N node) {
        if (getChildren().isEmpty()) {
            addChild(node);
        } else {
            setChild(0, node);
        }
    }

    public N right() {
        return getChildren().size() > 1 ? getChildren().get(1) : null;
    }

    @SuppressWarnings("fallthrough")
    public void setRight(N node) {
        switch (getChildren().size()) {
            case 0:
                addChild(null); // fall through
            case 1:
                addChild(node);
                break;
            default:
                setChild(1, node);
        }
    }
}