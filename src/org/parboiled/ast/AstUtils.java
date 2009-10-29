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

import org.jetbrains.annotations.NotNull;

/**
 * General utility methods for operating on abstract syntax trees.
 */
public class AstUtils {

    private AstUtils() {}

    /**
     * Performs the following transformation on the given AST node:
     * <pre>
     *        o1                    o2
     *       / \                   / \
     *      A   o2     ====>     o1   C
     *         / \              / \
     *        B   C            A   B
     * </pre>
     *
     * @param node the node to transform
     * @return the new root after the transformation
     */
    public static <T, N extends MutableLeftRightAstNode<T, N>> N toLeftAssociativity(@NotNull N node) {
        N right = node.right();
        if (right == null) return node;

        node.setRight(right.left());
        right.setLeft(node);
        return right;
    }

}