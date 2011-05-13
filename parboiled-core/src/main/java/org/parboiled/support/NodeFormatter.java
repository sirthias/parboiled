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

package org.parboiled.support;

import static org.parboiled.common.Preconditions.*;
import org.parboiled.Node;
import org.parboiled.buffers.InputBuffer;
import org.parboiled.common.Formatter;
import org.parboiled.common.StringUtils;

/**
 * A simple Formatter<Node> that provides String representation for parse tree nodes.
 */
public class NodeFormatter<V> implements Formatter<Node<V>> {

    private final InputBuffer inputBuffer;

    /**
     * Creates a new NodeFormatter.
     *
     * @param inputBuffer the input buffer underlying the parse tree whose nodes are to be formatted.
     */
    public NodeFormatter(InputBuffer inputBuffer) {
        this.inputBuffer = checkArgNotNull(inputBuffer, "inputBuffer");
    }

    public String format(Node<V> node) {
        String nodeLabel = node.toString();
        String nodeText = StringUtils.escape(ParseTreeUtils.getNodeText(node, inputBuffer));
        return StringUtils.isEmpty(nodeText) ? nodeLabel : nodeLabel + " '" + nodeText + '\'';
    }

}
