/*
 * Copyright (C) 2009 Mathias Doenitz
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

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;

import java.util.List;

/**
 * A {@link Matcher} that delegates all {@link Rule} and {@link Matcher} interface methods to another {@link Matcher}.
 * It can also hold a label and a leaf marker and lazily apply these to the underlying {@link Matcher} once it is available.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class ProxyMatcher<V> implements Matcher<V>, Cloneable {

    private Matcher<V> target;
    private String label;
    private boolean nodeSuppressed;
    private boolean subnodesSuppressed;

    @NotNull
    public List<Matcher<V>> getChildren() {
        apply();
        return target.getChildren();
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        apply();
        return target.match(context);
    }

    public String getLabel() {
        apply();
        return target.getLabel();
    }

    public boolean isNodeSuppressed() {
        apply();
        return target.isNodeSuppressed();
    }

    public boolean areSubnodesSuppressed() {
        apply();
        return target.areSubnodesSuppressed();
    }

    public <R> R accept(@NotNull MatcherVisitor<V, R> visitor) {
        apply();
        return target.accept(visitor);
    }

    @Override
    public String toString() {
        if (target == null) return super.toString();
        apply();
        return target.toString();
    }

    private void apply() {
        Preconditions.checkState(target != null);
        if (label != null) label(label);
        if (nodeSuppressed) suppressNode();
        if (subnodesSuppressed) suppressSubnodes();
    }

    @SuppressWarnings({"unchecked"})
    public Rule label(String label) {
        if (target == null) {
            // if we have no target yet we need to save the label and "apply" it later
            if (this.label == null) {
                this.label = label;
                return this;
            }

            // this proxy matcher is already waiting for its label application opportunity,
            // so we need to create another proxy level
            ProxyMatcher<V> anotherProxy = createClone();
            anotherProxy.label = label;
            anotherProxy.arm(this);
            return anotherProxy;
        }

        // we already have a target to which we can directly apply the label
        Rule inner = unwrap(target);
        target = (Matcher<V>) inner.label(label); // since relabelling might change the instance we have to update it
        this.label = null;
        return target;
    }

    @SuppressWarnings({"unchecked"})
    public Rule suppressNode() {
        if (target == null) {
            // if we have no target yet we need to save the marker and "apply" it later
            nodeSuppressed = true;
            return this;
        }

        // we already have a target to which we can directly apply the marker
        Rule inner = unwrap(target);
        target = (Matcher<V>) inner.suppressNode(); // since this might change the instance we have to update it
        nodeSuppressed = false;
        return target;
    }

    @SuppressWarnings({"unchecked"})
    public Rule suppressSubnodes() {
        if (target == null) {
            // if we have no target yet we need to save the marker and "apply" it later
            subnodesSuppressed = true;
            return this;
        }

        // we already have a target to which we can directly apply the marker
        Rule inner = unwrap(target);
        target = (Matcher<V>) inner.suppressSubnodes(); // since this might change the instance we have to update it
        subnodesSuppressed = false;
        return target;
    }

    /**
     * Supplies this ProxyMatcher with its underlying delegate.
     *
     * @param target the Matcher to delegate to
     */
    public void arm(Matcher<V> target) {
        this.target = target;
    }

    /**
     * Retrieves the innermost Matcher that is not a ProxyMatcher.
     *
     * @param matcher the matcher to unwrap
     * @param <V>     the type of the value field of a parse tree node
     * @return the given instance if it is not a ProxyMatcher, otherwise the innermost non-proxy Matcher
     */
    @SuppressWarnings({"unchecked"})
    public static <V> Matcher<V> unwrap(Matcher<V> matcher) {
        if (matcher instanceof ProxyMatcher) {
            ProxyMatcher<V> proxyMatcher = (ProxyMatcher<V>) matcher;
            proxyMatcher.apply();
            return proxyMatcher.target;
        }
        return matcher;
    }

    // creates a shallow copy
    @SuppressWarnings({"unchecked"})
    private ProxyMatcher<V> createClone() {
        try {
            return (ProxyMatcher<V>) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }

}
