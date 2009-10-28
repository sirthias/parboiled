package org.parboiled.support;

import org.parboiled.Node;
import org.parboiled.utils.Formatter;
import org.parboiled.utils.StringUtils;

/**
 * A simple Formatter<Node> that provides String representation for parse tree nodes.
 */
public class NodeFormatter implements Formatter<Node> {

    private final InputBuffer inputBuffer;

    /**
     * Creates a new NodeFormatter.
     * @param inputBuffer the input buffer underlying the parse tree whose nodes are to be formatted.
     */
    public NodeFormatter(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public String format(Node node) {
        String text = ParseTreeUtils.getNodeText(node, inputBuffer);
        if (StringUtils.isEmpty(text)) return node.toString();
        return node + " '" + StringUtils.escapeNLs(text) + '\'';
    }

}
