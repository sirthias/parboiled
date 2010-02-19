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

package org.parboiled.trees;

import com.google.common.base.Function;
import org.parboiled.Node;
import static org.parboiled.trees.GraphUtils.hasChildren;

public interface Filter<T extends GraphNode<T>> extends Function<T, Printability> {

    static final Filter<Node<Object>> SkipEmptyOptionalsAndZeroOrMores = new Filter<Node<Object>>() {
        public Printability apply(Node<Object> node) {
            return SkipEmptyOptionals.apply(node) == Printability.Skip ||
                    SkipEmptyZeroOrMores.apply(node) == Printability.Skip ?
                    Printability.Skip : Printability.PrintAndDescend;
        }
    };

    static final Filter<Node<Object>> SkipEmptyOptionals = new Filter<Node<Object>>() {
        public Printability apply(Node<Object> node) {
            return hasChildren(node) || node.getEndLocation() != node.getStartLocation() || !"optional"
                    .equals(node.getLabel()) ?
                    Printability.PrintAndDescend : Printability.Skip;
        }
    };

    static final Filter<Node<Object>> SkipEmptyZeroOrMores = new Filter<Node<Object>>() {
        public Printability apply(Node<Object> node) {
            return hasChildren(node) || node.getEndLocation() != node.getStartLocation() || !"zeroOrMore"
                    .equals(node.getLabel()) ?
                    Printability.PrintAndDescend : Printability.Skip;
        }
    };

}
