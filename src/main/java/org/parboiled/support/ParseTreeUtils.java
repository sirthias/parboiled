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

package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Node;
import org.parboiled.trees.Filter;

import static org.parboiled.trees.GraphUtils.printTree;

/**
 * General utility methods for operating on parse trees.
 */
public final class ParseTreeUtils {

    private ParseTreeUtils() {}

    /**
     * Returns the input text matched by the given node, with error correction.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    public static String getNodeText(Node<?> node, @NotNull InputBuffer inputBuffer) {
        if (node == null) return null;
        if (!node.hasError()) {
            return getRawNodeText(node, inputBuffer);
        }
        // if the node has a parse error we cannot simpy cut a string out of the underlying input buffer, since we
        // would also include illegal characters, so we need to build it bottom up
        if (node.getMatcher().accept(new IsSingleCharMatcherVisitor())) {
            return String.valueOf(inputBuffer.charAt(node.getStartIndex()));
        } else {
            StringBuilder sb = new StringBuilder();
            int index = node.getStartIndex();
            for (Node<?> child : node.getChildren()) {
                addInputLocations(inputBuffer, sb, index, child.getStartIndex());
                sb.append(getNodeText(child, inputBuffer));
                index = child.getEndIndex();
            }
            addInputLocations(inputBuffer, sb, index, node.getEndIndex());
            return sb.toString();
        }
    }

    private static void addInputLocations(InputBuffer inputBuffer, StringBuilder sb, int start, int end) {
        for (int i = start; i < end; i++) {
            char c = inputBuffer.charAt(i);
            switch (c) {
                case Characters.DEL_ERROR:
                    i++;
                    break;
                case Characters.INS_ERROR:
                    break;
                case Characters.RESYNC:
                    return;
                default:
                    sb.append(c);
            }
        }
    }

    /**
     * Returns the raw input text matched by the given node, without error correction.
     *
     * @param node        the node
     * @param inputBuffer the underlying inputBuffer
     * @return null if node is null otherwise a string with the matched input text (which can be empty)
     */
    public static String getRawNodeText(Node<?> node, @NotNull InputBuffer inputBuffer) {
        return node == null ? null : inputBuffer.extract(node.getStartIndex(), node.getEndIndex());
    }

    /**
     * Creates a readable string represenation of the parse tree in the given {@link ParsingResult} object.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @return a new String
     */
    public static String printNodeTree(@NotNull ParsingResult<?> parsingResult) {
        return printNodeTree(parsingResult, null);
    }

    /**
     * Creates a readable string represenation of the parse tree in thee given {@link ParsingResult} object.
     * If a non-null filter function is given its result is used to determine whether a particular node is
     * printed and/or its subtree printed.
     *
     * @param parsingResult the parsing result containing the parse tree
     * @param filter        optional node filter selecting the nodes to print and/or descend into for tree printing
     * @return a new String
     */
    public static <V> String printNodeTree(@NotNull ParsingResult<V> parsingResult, Filter<Node<V>> filter) {
        return printTree(parsingResult.parseTreeRoot, new NodeFormatter<V>(parsingResult.inputBuffer), filter);
    }

}

