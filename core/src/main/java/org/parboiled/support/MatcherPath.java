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

package org.parboiled.support;

import static org.parboiled.common.Preconditions.*;
import org.parboiled.matchers.Matcher;

/**
 * Holds a snapshot of the current {@link Matcher} stack at a certain point during the parsing process.
 * Implemented as a specialized, immutable single-linked list of Element objects with the deepest stack Element
 * in the first position and the root at the end.
 */
public class MatcherPath {

    public static class Element {
        public final Matcher matcher;
        public final int startIndex;
        public final int level;

        public Element(Matcher matcher, int startIndex, int level) {
            this.matcher = matcher;
            this.startIndex = startIndex;
            this.level = level;
        }
    }

    public final Element element;
    public final MatcherPath parent;

    /**
     * Constructs a new MatcherPath wrapping the given elements.
     * Normally you don't construct a MatcherPath directly but rather call {@link org.parboiled.Context#getPath()} to
     * get one.
     *
     * @param element the last element of this path
     * @param parent  the parent path
     */
    public MatcherPath(Element element, MatcherPath parent) {
        this.element = checkArgNotNull(element, "element");
        this.parent = parent;
    }

    /**
     * @return the length of this path, i.e. the number of matchers contained in it
     */
    public int length() {
        return element.level + 1;
    }

    /**
     * Determines whether this path is a prefix of the given other path.
     *
     * @param that the other path
     * @return true if this path is a prefix of the given other path
     */
    public boolean isPrefixOf(MatcherPath that) {
        checkArgNotNull(that, "that");
        return element.level <= that.element.level &&
                (this == that || (that.parent != null && isPrefixOf(that.parent)));
    }

    /**
     * Returns the Element at the given level.
     * @param level the level to get the element from
     * @return the element
     */
    public Element getElementAtLevel(int level) {
        checkArgument(level >= 0);
        if (level > element.level) return null;
        if (level < element.level) return parent.getElementAtLevel(level);
        return element;
    }

    /**
     * Returns the common prefix of this MatcherPath and the given other one.
     *
     * @param that the other path
     * @return the common prefix or null
     */
    public MatcherPath commonPrefix(MatcherPath that) {
        checkArgNotNull(that, "that");
        if (element.level > that.element.level) return parent.commonPrefix(that);
        if (element.level < that.element.level) return commonPrefix(that.parent);
        if (this == that) return this;
        return (parent != null && that.parent != null) ? parent.commonPrefix(that.parent) : null;
    }

    /**
     * Determines whether the given matcher is contained in this path.
     *
     * @param matcher the matcher
     * @return true if contained
     */
    public boolean contains(Matcher matcher) {
        return element.matcher == matcher || (parent != null && parent.contains(matcher));
    }

    @Override
    public String toString() {
        return toString(null);
    }

    public String toString(MatcherPath skipPrefix) {
        return print(new StringBuilder(), skipPrefix).toString();
    }

    private StringBuilder print(StringBuilder sb, MatcherPath skipPrefix) {
        return (parent == skipPrefix ? sb : parent.print(sb, skipPrefix).append('/')).append(element.matcher);
    }

}
