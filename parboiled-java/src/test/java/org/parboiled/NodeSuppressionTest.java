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

package org.parboiled;

import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class NodeSuppressionTest extends TestNgParboiledTest<Object> {

    @BuildParseTree
    public static class Parser extends BaseParser<Object> {

        public Rule ABCDEFGH() {
            return Sequence(ABCD(), EFGH());
        }

        public Rule ABCD() {
            return Sequence(AB(), CD());
        }

        public Rule EFGH() {
            return Sequence(EF(), GH());
        }

        public Rule AB() {
            return Sequence(A(), B());
        }

        @SuppressSubnodes
        public Rule CD() {
            return Sequence(C(), D());
        }

        public Rule EF() {
            return Sequence(E(), F());
        }

        public Rule GH() {
            return Sequence(G(), H()).suppressNode();
        }

        public Rule A() {
            return Ch('a');
        }

        @SuppressNode
        public Rule B() {
            return Ch('b');
        }

        public Rule C() {
            return Ch('c');
        }

        public Rule D() {
            return Ch('d');
        }

        public Rule E() {
            return Ch('e').suppressSubnodes();
        }

        public Rule F() {
            return Ch('f').suppressNode();
        }

        public Rule G() {
            return Ch('g');
        }

        public Rule H() {
            return Ch('h');
        }
    }

    @Test
    public void testNodeSuppression() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.ABCDEFGH(), "abcdefgh")
                .hasNoErrors()
                .hasParseTree("" +
                        "[ABCDEFGH] 'abcdefgh'\n" +
                        "  [ABCD] 'abcd'\n" +
                        "    [AB] 'ab'\n" +
                        "      [A] 'a'\n" +
                        "    [CD] 'cd'\n" +
                        "  [EFGH] 'efgh'\n" +
                        "    [EF] 'ef'\n" +
                        "      [E] 'e'\n");
    }

}