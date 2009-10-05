package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputLocation;
import org.parboiled.utils.ImmutableTreeNode;

import java.util.List;

class NodeImpl extends ImmutableTreeNode<Node> implements Node {

    private final String label;
    private final InputLocation startLocation;
    private final InputLocation endLocation;
    private final Object value;

    public NodeImpl(String label, List<Node> children, @NotNull InputLocation startLocation,
                    @NotNull InputLocation endLocation, Object value) {
        super(children);
        this.label = label;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    @NotNull
    public InputLocation getStartLocation() {
        return startLocation;
    }

    @NotNull
    public InputLocation getEndLocation() {
        return endLocation;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(label);
        if (value != null) {
            sb.append(", {").append(value).append('}');
        }
        sb.append(']');
        return sb.toString();
    }

}
