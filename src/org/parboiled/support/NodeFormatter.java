package org.parboiled.support;

import org.apache.commons.lang.StringUtils;
import org.parboiled.Node;
import org.parboiled.utils.Formatter;

public class NodeFormatter implements Formatter<Node> {

    private final InputBuffer inputBuffer;
    private final boolean includeShadowNodes;

    public NodeFormatter(InputBuffer inputBuffer) {
        this(inputBuffer, false);
    }

    public NodeFormatter(InputBuffer inputBuffer, boolean includeShadowNodes) {
        this.inputBuffer = inputBuffer;
        this.includeShadowNodes = includeShadowNodes;
    }

    public String format(Node node) {
        if (includeShadowNodes || node.getLabel() == null) return null;
        String text = ParseTreeUtils.getNodeText(node, inputBuffer);
        if (StringUtils.isEmpty(text)) return node.toString();
        return node + " '" + org.parboiled.utils.StringUtils2.escapeNLs(text) + '\'';
    }

}
