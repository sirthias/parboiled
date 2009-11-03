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

import static org.parboiled.TestUtils.assertEqualsMultiline;
import static org.parboiled.trees.GraphUtils.countAllDistinct;
import static org.parboiled.trees.GraphUtils.printTree;
import org.parboiled.common.ToStringFormatter;
import org.parboiled.matchers.Matcher;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class WrappingTest extends AbstractTest {

    public static class WrappingParser extends BaseParser<Object, Actions<Object>> {

        public WrappingParser(Actions<Object> actions) {
            super(actions);
        }

        public Rule aOpB() {
            return sequence(
                    number().label("a"),
                    operator(),
                    number().label("b")
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

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRecursion() {
        WrappingParser parser = Parboiled.createParser(WrappingParser.class, null);
        Rule rule = parser.aOpB();

        Matcher matcher = rule.toMatcher();
        assertEqualsMultiline(printTree(matcher, new ToStringFormatter<Matcher>()), "" +
                "aOpB\n" +
                "    wrapper:a\n" +
                "        number\n" +
                "            digit\n" +
                "    operator\n" +
                "        '+'\n" +
                "        '-'\n" +
                "    wrapper:b\n" +
                "        number\n" +
                "            digit\n");

        // verify that number and digit matchers only exist once
        assertEquals(countAllDistinct(matcher), 8);

        test(parser, rule, "123-54", "" +
                "[aOpB] '123-54'\n" +
                "    [a] '123'\n" +
                "        [digit] '1'\n" +
                "        [digit] '2'\n" +
                "        [digit] '3'\n" +
                "    [operator] '-'\n" +
                "        ['-'] '-'\n" +
                "    [b] '54'\n" +
                "        [digit] '5'\n" +
                "        [digit] '4'\n");
    }

}