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

package org.parboiled.trees;

import org.parboiled.Node;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;

import java.util.HashSet;
import java.util.Set;

import static org.parboiled.trees.GraphUtils.hasChildren;

public class Filters {

    public static  Filter<Node> skipEmptyOptionalsAndZeroOrMores() {
        return new Filter<Node>() {
            private final Filter<Node> skipEmptyOptionals = skipEmptyOptionals();
            private final Filter<Node> skipEmptyZeroOrMores = skipEmptyZeroOrMores();

            public Printability apply(Node node) {
                return skipEmptyOptionals.apply(node) == Printability.Skip ||
                        skipEmptyZeroOrMores.apply(node) == Printability.Skip ?
                        Printability.Skip : Printability.PrintAndDescend;
            }
        };
    }

    public static  Filter<Node> skipEmptyOptionals() {
        return new Filter<Node>() {
            public Printability apply(Node node) {
                return hasChildren(node) || node.getEndIndex() != node.getStartIndex() ||
                        !"Optional".equals(node.getLabel()) ?
                        Printability.PrintAndDescend : Printability.Skip;
            }
        };
    }

    public static  Filter<Node> skipEmptyZeroOrMores() {
        return new Filter<Node>() {
            public Printability apply(Node node) {
                return hasChildren(node) || node.getEndIndex() != node.getStartIndex() || !"ZeroOrMore"
                        .equals(node.getLabel()) ?
                        Printability.PrintAndDescend : Printability.Skip;
            }
        };
    }

    public static  Filter<Matcher> preventLoops() {
        return new Filter<Matcher>() {
            private final Set<Matcher> visited = new HashSet<Matcher>();

            public Printability apply(Matcher node) {
                node = ProxyMatcher.unwrap(node);
                if (visited.contains(node)) {
                    return Printability.Print;
                }
                visited.add(node);
                return Printability.PrintAndDescend;
            }
        };
    }

}
