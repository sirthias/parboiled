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

package org.parboiled;

import org.parboiled.matchers.Matcher;
import org.parboiled.support.Label;
import org.parboiled.support.ToStringFormatter;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import static org.parboiled.trees.GraphUtils.countAllDistinct;
import static org.parboiled.trees.GraphUtils.printTree;
import static org.testng.Assert.assertEquals;

public class LabelTest extends AbstractTest {

    public static class LabellingParser extends BaseParser<Object> {

        public Rule AOpB() {
            return Sequence(
                    Number().label("A"),
                    Operator().label("FirstOp"),
                    Number().label("B"),
                    Operator().label("SecondOp"),
                    Number()
            );
        }

        public Rule Operator() {
            return FirstOf('+', '-');
        }

        @Label("NUmBER")
        public Rule Number() {
            return OneOrMore(Digit());
        }

        public Rule Digit() {
            return CharRange('0', '9');
        }

        @SuppressWarnings({"InfiniteRecursion"})
        public Rule RecursiveLabel() {
            return FirstOf('a', Sequence('b', RecursiveLabel().label("First"), RecursiveLabel().label("Second")));
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testLabellingParser() {
        LabellingParser parser = Parboiled.createParser(LabellingParser.class);
        Rule rule = parser.AOpB();

        assertEqualsMultiline(printTree((Matcher) rule, new ToStringFormatter<Matcher>()), "" +
                "AOpB\n" +
                "    A\n" +
                "        Digit\n" +
                "    FirstOp\n" +
                "        '+'\n" +
                "        '-'\n" +
                "    B\n" +
                "        Digit\n" +
                "    SecondOp\n" +
                "        '+'\n" +
                "        '-'\n" +
                "    NUmBER\n" +
                "        Digit\n");

        // verify that there is each only one Digit matcher, '+' matcher and '-' matcher
        assertEquals(countAllDistinct((Matcher) rule), 9);

        test(rule, "123-54+9", "" +
                "[AOpB] '123-54+9'\n" +
                "    [A] '123'\n" +
                "        [Digit] '1'\n" +
                "        [Digit] '2'\n" +
                "        [Digit] '3'\n" +
                "    [FirstOp] '-'\n" +
                "        ['-'] '-'\n" +
                "    [B] '54'\n" +
                "        [Digit] '5'\n" +
                "        [Digit] '4'\n" +
                "    [SecondOp] '+'\n" +
                "        ['+'] '+'\n" +
                "    [NUmBER] '9'\n" +
                "        [Digit] '9'\n");
    }

    @Test
    public void testRecursiveLabelling() {
        LabellingParser parser = Parboiled.createParser(LabellingParser.class);
        Rule rule = parser.RecursiveLabel();

        test(rule, "bbaaaa", "" +
                "[RecursiveLabel] 'bbaaa'\n" +
                "    [Sequence] 'bbaaa'\n" +
                "        ['b'] 'b'\n" +
                "        [First] 'baa'\n" +
                "            [Sequence] 'baa'\n" +
                "                ['b'] 'b'\n" +
                "                [First] 'a'\n" +
                "                    ['a'] 'a'\n" +
                "                [Second] 'a'\n" +
                "                    ['a'] 'a'\n" +
                "        [Second] 'a'\n" +
                "            ['a'] 'a'\n");
    }

}
