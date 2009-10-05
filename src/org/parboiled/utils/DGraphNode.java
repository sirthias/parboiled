package org.parboiled.utils;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A node in a directed graph (that may have cycles).
 * @param <T>
 */
public interface DGraphNode<T extends DGraphNode<T>> {

    /**
     * @return the sub nodes of this node
     */
    @NotNull
    List<T> getChildren();

}
