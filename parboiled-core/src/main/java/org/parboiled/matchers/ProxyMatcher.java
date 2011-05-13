/*
 * Copyright (C) 2009-2011 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.matchers;

import static org.parboiled.common.Preconditions.*;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.matchervisitors.MatcherVisitor;

import java.util.List;

/**
 * A {@link Matcher} that delegates all {@link Rule} and {@link Matcher} interface methods to another {@link Matcher}.
 * It can also hold a label and a leaf marker and lazily apply these to the underlying {@link Matcher} once it is available.
 */
public class ProxyMatcher implements Matcher, Cloneable {
    private Matcher target;
    private String label;
    private boolean nodeSuppressed;
    private boolean subnodesSuppressed;
    private boolean nodeSkipped;
    private boolean memoMismatches;
    private boolean dirty;

    public List<Matcher> getChildren() {
        if (dirty) apply();
        return target.getChildren();
    }

    public void setLabel(String label) {
        this.label = label;
        updateDirtyFlag();
    }

    private void setNodeSuppressed(boolean nodeSuppressed) {
        this.nodeSuppressed = nodeSuppressed;
        updateDirtyFlag();
    }

    private void setSubnodesSuppressed(boolean subnodesSuppressed) {
        this.subnodesSuppressed = subnodesSuppressed;
        updateDirtyFlag();
    }

    private void setNodeSkipped(boolean nodeSkipped) {
        this.nodeSkipped = nodeSkipped;
        updateDirtyFlag();
    }

    private void setMemoMismatches(boolean memoMismatches) {
        this.memoMismatches = memoMismatches;
        updateDirtyFlag();
    }

    private void updateDirtyFlag() {
        dirty = label != null || nodeSuppressed || subnodesSuppressed || nodeSkipped || memoMismatches;
    }

    public <V> boolean match(MatcherContext<V> context) {
        if (dirty) apply();
        return target.match(context);
    }

    public String getLabel() {
        if (dirty) apply();
        return target.getLabel();
    }

    public boolean hasCustomLabel() {
        if (dirty) apply();
        return target.hasCustomLabel();
    }

    public boolean isNodeSuppressed() {
        if (dirty) apply();
        return target.isNodeSuppressed();
    }

    public boolean areSubnodesSuppressed() {
        if (dirty) apply();
        return target.areSubnodesSuppressed();
    }

    public boolean isNodeSkipped() {
        if (dirty) apply();
        return target.isNodeSkipped();
    }

    public boolean areMismatchesMemoed() {
        if (dirty) apply();
        return target.areMismatchesMemoed();
    }

    public void setTag(Object tagObject) {
        if (dirty) apply();
        target.setTag(tagObject);
    }

    public Object getTag() {
        if (dirty) apply();
        return target.getTag();
    }

    public <R> R accept(MatcherVisitor<R> visitor) {
        checkArgNotNull(visitor, "visitor");
        if (dirty) apply();
        return target.accept(visitor);
    }

    @Override
    public String toString() {
        if (target == null) return super.toString();
        if (dirty) apply();
        return target.toString();
    }

    private void apply() {
        if (label != null) label(label);
        if (nodeSuppressed) suppressNode();
        if (subnodesSuppressed) suppressSubnodes();
        if (nodeSkipped) skipNode();
    }

    public Rule label(String label) {
        if (target == null) {
            // if we have no target yet we need to save the label and "apply" it later
            if (this.label == null) {
                setLabel(label);
                return this;
            }

            // this proxy matcher is already waiting for its label application opportunity,
            // so we need to create another proxy level
            ProxyMatcher anotherProxy = createClone();
            anotherProxy.setLabel(label);
            anotherProxy.arm(this);
            return anotherProxy;
        }

        // we already have a target to which we can directly apply the label
        Rule inner = unwrap(target);
        target = (Matcher) inner.label(label); // since relabelling might change the instance we have to update it
        setLabel(null);
        return target;
    }

    public Rule suppressNode() {
        if (target == null) {
            // if we have no target yet we need to save the marker and "apply" it later
            setNodeSuppressed(true);
            return this;
        }

        // we already have a target to which we can directly apply the marker
        Rule inner = unwrap(target);
        target = (Matcher) inner.suppressNode(); // since this might change the instance we have to update it
        setNodeSuppressed(false);
        return target;
    }

    public Rule suppressSubnodes() {
        if (target == null) {
            // if we have no target yet we need to save the marker and "apply" it later
            setSubnodesSuppressed(true);
            return this;
        }

        // we already have a target to which we can directly apply the marker
        Rule inner = unwrap(target);
        target = (Matcher) inner.suppressSubnodes(); // since this might change the instance we have to update it
        setSubnodesSuppressed(false);
        return target;
    }

    public Rule skipNode() {
        if (target == null) {
            // if we have no target yet we need to save the marker and "apply" it later
            setNodeSkipped(true);
            return this;
        }

        // we already have a target to which we can directly apply the marker
        Rule inner = unwrap(target);
        target = (Matcher) inner.skipNode(); // since this might change the instance we have to update it
        setNodeSkipped(false);
        return target;
    }

    public Rule memoMismatches() {
        if (target == null) {
            // if we have no target yet we need to save the marker and "apply" it later
            setMemoMismatches(true);
            return this;
        }

        // we already have a target to which we can directly apply the marker
        Rule inner = unwrap(target);
        target = (Matcher) inner.memoMismatches(); // since this might change the instance we have to update it
        setMemoMismatches(false);
        return target;
    }

    /**
     * Supplies this ProxyMatcher with its underlying delegate.
     *
     * @param target the Matcher to delegate to
     */
    public void arm(Matcher target) {
        this.target = checkArgNotNull(target, "target");
    }

    /**
     * Retrieves the innermost Matcher that is not a ProxyMatcher.
     *
     * @param matcher the matcher to unwrap
     * @return the given instance if it is not a ProxyMatcher, otherwise the innermost non-proxy Matcher
     */
    public static Matcher unwrap(Matcher matcher) {
        if (matcher instanceof ProxyMatcher) {
            ProxyMatcher proxyMatcher = (ProxyMatcher) matcher;
            if (proxyMatcher.dirty) proxyMatcher.apply();
            return proxyMatcher.target;
        }
        return matcher;
    }

    public MatcherContext getSubContext(MatcherContext context) {
        if (dirty) apply();
        return target.getSubContext(context);
    }

    // creates a shallow copy
    private ProxyMatcher createClone() {
        try {
            return (ProxyMatcher) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }

}
