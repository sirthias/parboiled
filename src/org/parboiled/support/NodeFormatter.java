package org.parboiled.support;

import org.parboiled.Node;
import org.parboiled.utils.Formatter;
import org.parboiled.utils.StringUtils2;

public class NodeFormatter implements Formatter<Node> {

    private final InputBuffer inputBuffer;

    public NodeFormatter(InputBuffer inputBuffer) {
        this.inputBuffer = inputBuffer;
    }

    public String format(Node node) {
        String text = ParseTreeUtils.getNodeText(node, inputBuffer);
        if (StringUtils2.isEmpty(text)) return node.toString();
        return node + " '" + org.parboiled.utils.StringUtils2.escapeNLs(text) + '\'';
    }

}
