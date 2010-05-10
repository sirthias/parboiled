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

import com.google.common.base.Predicate;
import org.jetbrains.annotations.NotNull;
import org.parboiled.Node;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.Matcher;
import org.parboiled.trees.ImmutableTreeNode;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.parboiled.support.ParseTreeUtils.*;
import static org.testng.Assert.assertEquals;

public class ParseTreeUtilsTest {

    private static class TestNode extends ImmutableTreeNode<Node<String>> implements Node<String> {

        private final String label;

        private TestNode(String label, TestNode... children) {
            super(Arrays.<Node<String>>asList(children));
            this.label = label;
        }

        @NotNull
        public Matcher<String> getMatcher() {
            throw new UnsupportedOperationException();
        }

        public String getLabel() {
            return label;
        }

        @NotNull
        public InputLocation getStartLocation() {
            throw new UnsupportedOperationException();
        }

        @NotNull
        public InputLocation getEndLocation() {
            throw new UnsupportedOperationException();
        }

        public String getValue() {
            throw new UnsupportedOperationException();
        }

        public boolean hasError() {
            return false;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private final TestNode tree = new TestNode("root",
            new TestNode("A",
                    new TestNode("AA"),
                    new TestNode("AB"),
                    new TestNode("AC")
            ),
            new TestNode("B",
                    new TestNode("BA",
                            new TestNode("BAA"),
                            new TestNode("BAB")
                    ),
                    new TestNode("BB",
                            new TestNode("BBA")
                    ),
                    new TestNode("BC",
                            new TestNode("BCA"),
                            new TestNode("BCB"),
                            new TestNode("BCC")
                    )
            ),
            new TestNode("C")
    );

    private final Predicate<Node<String>> predicate = new Predicate<Node<String>>() {
        public boolean apply(Node<String> input) {
            return input.getLabel().length() == 3;
        }
    };

    @Test
    public void testFindNodeByPath() {
        assertEquals(findNodeByPath(tree, "A/A").toString(), "AA");
        assertEquals(findNodeByPath(tree, "A/last:A").toString(), "AC");
        assertEquals(findNodeByPath(tree, "B/B/last:B").toString(), "BAB");
        assertEquals(findNodeByPath(tree, "B/last:B/B").toString(), "BCA");
        assertEquals(findNodeByPath(tree, "B/last:B/Z"), null);
    }

    @Test
    public void testCollectNodesByPath() {
        List<Node<String>> list = collectNodesByPath(tree, "B/B", new ArrayList<Node<String>>());
        assertEquals(StringUtils.join(list, ";"), "BA;BB;BC");
    }

    @Test
    public void testFindNode() {
        assertEquals(findNode(tree, predicate).toString(), "BAA");
        assertEquals(findLastNode(tree, predicate).toString(), "BCC");
    }

    @Test
    public void testCollectNodes() {
        List<Node<String>> list = collectNodes(tree, predicate, new ArrayList<Node<String>>());
        assertEquals(StringUtils.join(list, ";"), "BAA;BAB;BBA;BCA;BCB;BCC");
    }

}
