/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.MatcherContext;
import org.parboiled.common.Preconditions;
import org.parboiled.common.Utils;
import org.parboiled.matchers.Matcher;

import java.util.Arrays;

/**
 * Describes a path of rule matchers as they are nested within each other at a certain point during the parsing process.
 */
public class MatcherPath<V> {

    private final Matcher[] matchers;

    @SuppressWarnings({"ConstantConditions"})
    public MatcherPath(@NotNull MatcherContext<V> context) {
        this(context.getLevel() + 1);
        while (context != null) {
            matchers[context.getLevel()] = context.getMatcher();
            context = context.getParent();
        }
    }

    private MatcherPath(int length) {
        matchers = new Matcher[length];
    }

    /**
     * @return the length of this path
     */
    public int length() {
        return matchers.length;
    }

    /**
     * Returns the matcher with the given index.
     *
     * @param i the index to get
     * @return the matcher at the given index
     */
    @SuppressWarnings({"unchecked"})
    public Matcher<V> get(int i) {
        Preconditions.checkElementIndex(i, matchers.length);
        return matchers[i];
    }

    /**
     * Returns the matcher along the path starting with the root matcher.
     * The last matcher in the returned list is the "deepest" matcher.
     *
     * @return the matchers
     */
    @SuppressWarnings({"unchecked"})
    public Matcher<V>[] getMatchers() {
        return matchers;
    }

    /**
     * @return the deepest matcher of the path
     */
    @SuppressWarnings({"unchecked"})
    public Matcher<V> getHead() {
        return matchers[matchers.length - 1];
    }

    /**
     * Determines the length of the longest common path prefix of this path and the given other path.
     *
     * @param other the other path
     * @return the length of the longest common path prefix of this path and the given other path
     */
    public int getCommonPrefixLength(MatcherPath<V> other) {
        if (other == null) return 0;
        for (int i = 0; i < matchers.length; i++) {
            if (other.length() == i || matchers[i] != other.get(i)) return i;
        }
        return matchers.length;
    }

    /**
     * Determines whether the given matcher is contained in this path.
     *
     * @param matcher the matcher
     * @return true if contained
     */
    public boolean contains(Matcher<V> matcher) {
        return indexOf(matcher) != -1;
    }

    /**
     * Finds the index of this matcher in this path, with 0 being the root and getMatchers().length -1 being the head.
     *
     * @param matcher the matcher to find
     * @return the index if found, -1 if not found
     */
    public int indexOf(Matcher<V> matcher) {
        for (int i = 0; i < matchers.length; i++) {
            if (matchers[i] == matcher) return i;
        }
        return -1;
    }

    /**
     * Appends the given matcher in a new instance.
     *
     * @param matcher the matcher to append
     * @return a new MatcherPath 
     */
    public MatcherPath<V> append(@NotNull Matcher<V> matcher) {
        MatcherPath<V> newPath = new MatcherPath<V>(length() + 1);
        System.arraycopy(matchers, 0, newPath.matchers, 0, length());
        newPath.matchers[length()] = matcher;
        return newPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatcherPath)) return false;
        MatcherPath that = (MatcherPath) o;
        return Arrays.equals(matchers, that.matchers);

    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(matchers);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(Utils.toString(matchers[0]));
        for (int i = 1; i < matchers.length; i++) {
            sb.append('/');
            sb.append(matchers[i]);
        }
        return sb.toString();
    }
}
