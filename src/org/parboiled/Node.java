package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.TreeNode;
import org.parboiled.support.InputLocation;

/**
 * A Node represents a node in the parse tree created during a parsing run.
 */
public interface Node extends TreeNode<Node> {

    /**
     * @return the label of this node, usually set to the name of the matcher that created this node
     */
    String getLabel();

    /**
     * @return the start location of this nodes text in the underlying input buffer.
     */
    @NotNull
    InputLocation getStartLocation();

    /**
     * @return the end location of this nodes text in the underlying input buffer.
     */
    @NotNull
    InputLocation getEndLocation();

    /**
     * @return the value object attached to this node
     */
    Object getValue();

}
