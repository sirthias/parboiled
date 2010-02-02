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

public class Filters {

    public static final Function<Node, Printability> SkipEmptyOptionalsAndZeroOrMores = new Function<Node, Printability>() {
        public Printability apply(Node node) {
            return SkipEmptyOptionals.apply(node) == Printability.Skip ||
                    SkipEmptyZeroOrMores.apply(node) == Printability.Skip ?
                    Printability.Skip : Printability.PrintAndDescend;
        }
    };

    public static final Function<Node, Printability> SkipEmptyOptionals = new Function<Node, Printability>() {
        public Printability apply(Node node) {
            return hasChildren(node) || node.getEndLocation() != node.getStartLocation() || !"optional"
                    .equals(node.getLabel()) ?
                    Printability.PrintAndDescend : Printability.Skip;
        }
    };

    public static final Function<Node, Printability> SkipEmptyZeroOrMores = new Function<Node, Printability>() {
        public Printability apply(Node node) {
            return hasChildren(node) || node.getEndLocation() != node.getStartLocation() || !"zeroOrMore"
                    .equals(node.getLabel()) ?
                    Printability.PrintAndDescend : Printability.Skip;
        }
    };

}
