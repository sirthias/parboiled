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

package org.parboiled.support;

import org.parboiled.Node;
import org.parboiled.common.Function;
import static org.parboiled.trees.GraphUtils.hasChildren;
import org.parboiled.trees.Printability;

import java.util.Arrays;

/**
 * A simple node filter for use with {@link ParseTreeUtils#printNodeTree(ParsingResult, org.parboiled.common.Function)}.
 * It marks certain nodes as leaf nodes of the printed tree, i.e. their sub trees are not printed.
 * This can improve the readability of large parse trees substantially.
 */
public class LeafNodeFilter<V> implements Function<Node<V>, Printability> {

    private final String[] leafNodeLabels;
    private boolean markStrings;
    private boolean skipEmptyOptionals;
    private boolean skipEmptyZeroOrMores;

    /**
     * Creates a new LeafNodeFilter. The given labels will mark the nodes to act as leaf nodes in the tree printout,
     * i.e. the printer will not descend into them and therefore not print any potentially existing substructures.
     *
     * @param leafNodeLabels the labels of the nodes to mark as print tree leaves
     */
    public LeafNodeFilter(String[] leafNodeLabels) {
        this.leafNodeLabels = leafNodeLabels.clone();
        Arrays.sort(this.leafNodeLabels);
    }

    public Printability apply(Node<V> node) {
        String label = node.getLabel();
        if (Arrays.binarySearch(leafNodeLabels, label) >= 0) return Printability.Print;
        if (markStrings && label.charAt(0) == '"' && label.charAt(label.length() - 1) == '"') return Printability.Print;
        if (skipEmptyOptionals && "optional".equals(label) && !hasChildren(node)) return Printability.Skip;
        if (skipEmptyZeroOrMores && "zeroOrMore".equals(label) && !hasChildren(node)) return Printability.Skip;
        return Printability.PrintAndDescend;
    }

    /**
     * Enables reporting of string matchers as print tree leafs. String matches are recognized by their labels
     * beginning and ending with a double quote character '"'.
     *
     * @return this filter
     */
    public LeafNodeFilter<V> markStrings() {
        markStrings = true;
        return this;
    }

    /**
     * Enables skipping of empty optional matches.
     *
     * @return this filter
     */
    public LeafNodeFilter<V> skipEmptyOptionals() {
        skipEmptyOptionals = true;
        return this;
    }

    /**
     * Enables skipping of empty zeroOrMore matches.
     *
     * @return this filter
     */
    public LeafNodeFilter<V> markEmptyZeroOrMores() {
        skipEmptyZeroOrMores = true;
        return this;
    }

    /**
     * Static helper for convenient LeafNodeFilter creation without having to specify the type parameters (due to
     * compiler type parameter inference).
     *
     * @param leafNodeLabels the labels of the nodes to mark as print tree leaves
     * @return a new LeafNodeFilter
     */
    public static <V> LeafNodeFilter<V> create(String[] leafNodeLabels) {
        return new LeafNodeFilter<V>(leafNodeLabels);
    }

}
