package org.parboiled.utils;

/**
 * A specialization of a DGraphNode that contains a reference to its parent. 
 * @param <T>
 */
public interface TreeNode<T extends TreeNode<T>> extends DGraphNode<T> {

    /**
     * @return the parent node or null if this node is the root
     */
    T getParent();

}
