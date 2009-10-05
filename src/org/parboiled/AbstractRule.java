package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.utils.DGraphNode;
import org.parboiled.utils.ImmutableDGraphNode;

import java.util.List;

abstract class AbstractRule<T extends DGraphNode<T>> extends ImmutableDGraphNode<T> implements Rule {

    private String label;
    private boolean enforced;
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

    //************************************ Rule interface ********************************************

    public Rule label(String label) {
        checkNotLocked();
        this.label = label;
        return this;
    }

    public Rule enforce() {
        checkNotLocked();
        this.enforced = true;
        return this;
    }

    //************************************ GETTERS ********************************************

    public String getLabel() {
        return label;
    }

    public boolean isEnforced() {
        return enforced;
    }

    public boolean isLocked() {
        return locked;
    }

}
