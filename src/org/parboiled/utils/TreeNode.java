package org.parboiled.utils;

public interface TreeNode<T extends TreeNode<T>> extends DGraphNode<T> {

    /**
     * @return the parent node or null if this node is the root
     */
    T getParent();

}
