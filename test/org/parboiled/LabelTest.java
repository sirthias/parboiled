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

import static org.parboiled.test.TestUtils.assertEqualsMultiline;
import org.parboiled.test.AbstractTest;
import org.parboiled.common.ToStringFormatter;
import org.parboiled.matchers.Matcher;
import static org.parboiled.trees.GraphUtils.countAllDistinct;
import static org.parboiled.trees.GraphUtils.printTree;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class LabelTest extends AbstractTest {

    public static class LabellingParser extends BaseParser<Object> {

        public Rule aOpB() {
            return sequence(
                    number().label("a"),
                    operator().label("firstOp"),
                    number().label("b"),
                    operator().label("secondOp"),
                    number()
            );
        }

        public Rule operator() {
            return firstOf('+', '-');
        }

        public Rule number() {
            return oneOrMore(digit());
        }

        public Rule digit() {
            return charRange('0', '9');
        }

        @SuppressWarnings({"InfiniteRecursion"})
        public Rule recursiveLabel() {
            return firstOf('a', sequence('b', recursiveLabel().label("first"), recursiveLabel().label("second")));
        }

    }

    @SuppressWarnings("unchecked")
    //@Test
    public void testLabellingParser() {
        LabellingParser parser = Parboiled.createParser(LabellingParser.class);
        Rule rule = parser.aOpB();

        assertEqualsMultiline(printTree((Matcher) rule, new ToStringFormatter<Matcher>()), "" +
                "aOpB\n" +
                "    a\n" +
                "        digit\n" +
                "    firstOp\n" +
                "        '+'\n" +
                "        '-'\n" +
                "    b\n" +
                "        digit\n" +
                "    secondOp\n" +
                "        '+'\n" +
                "        '-'\n" +
                "    number\n" +
                "        digit\n");

        // verify that there is each only one digit matcher, '+' matcher and '-' matcher
        assertEquals(countAllDistinct((Matcher) rule), 9);

        test(parser, rule, "123-54+9", "" +
                "[aOpB] '123-54+9'\n" +
                "    [a] '123'\n" +
                "        [digit] '1'\n" +
                "        [digit] '2'\n" +
                "        [digit] '3'\n" +
                "    [firstOp] '-'\n" +
                "        ['-'] '-'\n" +
                "    [b] '54'\n" +
                "        [digit] '5'\n" +
                "        [digit] '4'\n" +
                "    [secondOp] '+'\n" +
                "        ['+'] '+'\n" +
                "    [number] '9'\n" +
                "        [digit] '9'\n");
    }

    @Test
    public void testRecursiveLabelling() {
        LabellingParser parser = Parboiled.createParser(LabellingParser.class);
        Rule rule = parser.recursiveLabel();

        test(parser, rule, "bbaaaa", "" +
                "[recursiveLabel] 'bbaaa'\n" +
                "    [sequence] 'bbaaa'\n" +
                "        ['b'] 'b'\n" +
                "        [first] 'baa'\n" +
                "            [sequence] 'baa'\n" +
                "                ['b'] 'b'\n" +
                "                [first] 'a'\n" +
                "                    ['a'] 'a'\n" +
                "                [second] 'a'\n" +
                "                    ['a'] 'a'\n" +
                "        [second] 'a'\n" +
                "            ['a'] 'a'\n");
    }

}
