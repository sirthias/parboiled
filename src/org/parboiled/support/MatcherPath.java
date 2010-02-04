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
import org.parboiled.Context;
import org.parboiled.MatcherContext;
import org.parboiled.matchers.Matcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a path of rule matchers as they are nested within each other at a certain point during the parsing process.
 */
public class MatcherPath<V> {

    private final List<Matcher<V>> matchers = new ArrayList<Matcher<V>>();

    public MatcherPath(@NotNull Matcher<V> matcher, Context<V> context) {
        matchers.add(matcher);
        while (context != null) {
            matchers.add(context.getMatcher());
            context = context.getParent();
        }
    }

    public MatcherPath(MatcherContext<V> context) {
        while (context != null) {
            matchers.add(context.getMatcher());
            context = context.getParent();
        }
    }

    /**
     * Returns the matcher along the path starting with the "deepest". The last matcher in the returned list is
     * the root matcher.
     *
     * @return the matchers
     */
    public List<Matcher<V>> getMatchers() {
        return matchers;
    }

    /**
     * @return the deepest matcher of the path
     */
    public Matcher<V> getHead() {
        return matchers.get(0);
    }

    /**
     * Determines whether this path matches the given constellation of matcher plus context.
     *
     * @param matcher the current matcher
     * @param context the current matchers context
     * @return true if this path matches
     */
    public boolean matches(Matcher<V> matcher, MatcherContext<V> context) {
        if (matchers.get(0) != matcher) return false;
        for (int i = 1; i < matchers.size(); i++) {
            if (context == null || context.getMatcher() != matchers.get(i)) return false;
            context = context.getParent();
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(matchers.get(matchers.size()-1).toString());
        for (int i = matchers.size()-2; i >= 0; i--) {
            sb.append('/');
            sb.append(matchers.get(i));
        }
        return sb.toString();
    }

}
