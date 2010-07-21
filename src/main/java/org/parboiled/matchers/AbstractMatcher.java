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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.trees.ImmutableGraphNode;

/**
 * Abstract base class of most regular {@link Matcher}s.
 */
public abstract class AbstractMatcher extends ImmutableGraphNode<Matcher> implements Matcher, Cloneable {

    private String label;
    private boolean nodeSuppressed;
    private boolean subnodesSuppressed;
    private boolean nodeSkipped;

    public AbstractMatcher() {
        this(new Rule[0]);
    }

    AbstractMatcher(@NotNull Rule subRule) {
        this(new Rule[] {subRule});
    }

    AbstractMatcher(@NotNull Rule[] subRules) {
        super(ImmutableList.<Matcher>of(toMatchers(subRules)));
    }

    private static Matcher[] toMatchers(@NotNull Rule[] subRules) {
        Matcher[] matchers = new Matcher[subRules.length];
        for (int i = 0; i < subRules.length; i++) {
            matchers[i] = (Matcher) subRules[i];
        }
        return matchers;
    }

    public boolean isNodeSuppressed() {
        return nodeSuppressed;
    }

    public boolean areSubnodesSuppressed() {
        return subnodesSuppressed;
    }

    public boolean isNodeSkipped() {
        return nodeSkipped;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return getLabel();
    }

    public AbstractMatcher label(@NotNull String label) {
        if (label.equals(this.label)) return this;
        AbstractMatcher clone = createClone();
        clone.label = label;
        return clone;
    }

    public Rule suppressNode() {
        if (nodeSuppressed) return this;
        AbstractMatcher clone = createClone();
        clone.nodeSuppressed = true;
        return clone;
    }

    public Rule suppressSubnodes() {
        if (subnodesSuppressed) return this;
        AbstractMatcher clone = createClone();
        clone.subnodesSuppressed = true;
        return clone;
    }

    public Rule skipNode() {
        if (nodeSkipped) return this;
        AbstractMatcher clone = createClone();
        clone.nodeSkipped = true;
        return clone;
    }

    // default implementation is to simply delegate to the context
    public MatcherContext getSubContext(MatcherContext context) {
        return context.getSubContext(this);
    }

    // creates a shallow copy
    private AbstractMatcher createClone() {
        try {
            return (AbstractMatcher) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }

}

