package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.DGraphNode;
import org.parboiled.utils.ImmutableDGraphNode;

import java.util.List;

/**
 * Abstract base class of all Rules.
 * @param <T>
 */
abstract class AbstractRule<T extends DGraphNode<T>> extends ImmutableDGraphNode<T> implements Rule {

    private String label;
    private boolean locked;

    protected AbstractRule(@NotNull List<T> children) {
        super(children);
    }

    private void checkNotLocked() {
        if (locked) {
            throw new UnsupportedOperationException("Rule has been locked, no further change allowed");
        }
    }

    public Rule lock() {
        locked = true;
        return this;
    }

    public Rule label(String label) {
        checkNotLocked();
        this.label = label;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public boolean isLocked() {
        return locked;
    }

}
