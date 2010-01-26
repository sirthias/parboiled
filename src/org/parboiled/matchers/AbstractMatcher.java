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

import org.jetbrains.annotations.NotNull;
import org.parboiled.Rule;
import org.parboiled.common.ImmutableList;
import org.parboiled.trees.ImmutableGraphNode;

/**
 * Abstract base class of most regular Matchers.
 */
public abstract class AbstractMatcher<V> extends ImmutableGraphNode<Matcher<V>> implements Rule, Matcher<V>, Cloneable {

    public static final ThreadLocal<Integer> indexCounter = new ThreadLocal<Integer>();

    private final int index;
    private boolean locked;
    private String label;

    protected AbstractMatcher() {
        this(new Rule[0]);
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractMatcher(@NotNull Rule subRule) {
        this(new Rule[] {subRule});
    }

    @SuppressWarnings({"unchecked"})
    protected AbstractMatcher(@NotNull Rule[] subRules) {
        super(ImmutableList.<Matcher<V>>of(toMatchers(subRules)));
        index = indexCounter.get();
        indexCounter.set(index + 1);
    }

    private static Matcher[] toMatchers(@NotNull Rule[] subRules) {
        Matcher[] matchers = new Matcher[subRules.length];
        for (int i = 0; i < subRules.length; i++) {
            matchers[i] = (Matcher) subRules[i];
        }
        return matchers;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public String getLabel() {
        return label;
    }

    public int getIndex() {
        return index;
    }

    @SuppressWarnings({"unchecked"})
    public AbstractMatcher<V> label(String label) {
        if (!isLocked()) {
            this.label = label;
            return this;
        }

        // if we are locked we are not allowed to change the label anymore
        // therefore we need to create a shallow copy, apply the label to it and return the copy
        try {
            AbstractMatcher<V> clone = (AbstractMatcher<V>) clone();
            clone.label = label;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException();
        }
    }

    public boolean hasLabel() {
        return label != null;
    }

    public String getExpectedString() {
        return getLabel(); // default implementation
    }

    @Override
    public String toString() {
        return getLabel();
    }

}

