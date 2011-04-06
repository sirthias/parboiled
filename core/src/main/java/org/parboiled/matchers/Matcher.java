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

import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.matchervisitors.MatcherVisitor;
import org.parboiled.trees.GraphNode;

/**
 * A Matcher instance is responsible for "executing" a specific Rule instance, i.e. it implements the actual
 * rule type specific matching logic.
 * Since it extends the {@link GraphNode} interface it can have submatchers.
 */
public interface Matcher extends Rule, GraphNode<Matcher> {

    /**
     * @return the label of the matcher (which is identical to the label of the Rule this matcher matches)
     */
    String getLabel();

    /**
     * @return true if this matcher has been assigned a custom label
     */
    boolean hasCustomLabel();

    /**
     * @return true if this matcher has been marked with @SuppressNode
     */
    boolean isNodeSuppressed();

    /**
     * @return true if this matcher has been marked with @SuppressSubnodes
     */
    boolean areSubnodesSuppressed();

    /**
     * @return true if this matcher has been marked with @SkipNode
     */
    boolean isNodeSkipped();

    /**
     * @return true if this matcher has been marked with @MemoMismatches
     */
    boolean areMismatchesMemoed();

    /**
     * Creates a context for the matching of this matcher using the given parent context.
     *
     * @param context the parent context
     * @return the context this matcher is to be run in
     */
    MatcherContext getSubContext(MatcherContext context);

    /**
     * Tries a match on the given MatcherContext.
     *
     * @param context the MatcherContext
     * @return true if the match was successful
     */
    <V> boolean match(MatcherContext<V> context);

    /**
     * Associates an arbitrary object with this matcher. Used for example during profiling and packrat parsing.
     * The matcher implementations themselves completely ignore the contents of this property. It purely serves as a
     * performance optimization for ParseRunners and/or MatchHandlers and saves these from the need to use
     * Map&lt;Matcher, XYZ&gt; structures for associating internal objects with matchers.
     *
     * @param tagObject the tag object
     */
    void setTag(Object tagObject);

    /**
     * Retrieves a previously set tag object.
     *
     * @return the tag object or null if none set
     */
    Object getTag();

    /**
     * Accepts the given matcher visitor.
     *
     * @param visitor the visitor
     * @return the value returned by the given visitor
     */
    <R> R accept(MatcherVisitor<R> visitor);
}