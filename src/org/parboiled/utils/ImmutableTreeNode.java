package org.parboiled.utils;

import java.util.List;

public class ImmutableTreeNode<T extends TreeNode<T>> extends ImmutableDGraphNode<T> implements TreeNode<T> {

    // we cannot make the parent field final since otherwise we can't create a tree hierarchy with parents linking to
    // their children and vice versa. So we design this for a bottom up tree construction strategy were children
    // are created first and then "acquired" by their parents
    private T parent;

    public ImmutableTreeNode() {
    }

    public ImmutableTreeNode(List<T> children) {
        super(children);
        acquireChildren();
    }

    public T getParent() {
        return parent;
    }

    // this method is private to assure immutability to the outside
    private void setParent(T parent) {
        this.parent = parent;
    }

    @SuppressWarnings({"unchecked"})
    protected void acquireChildren() {
        for (T child : getChildren()) {
            ((ImmutableTreeNode)child).setParent(this);
        }
    }

}
