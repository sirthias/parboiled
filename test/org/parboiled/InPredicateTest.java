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

import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class InPredicateTest extends AbstractTest {

    public static class Parser extends BaseParser<Object> {
        public int count = 0;

        public Rule Number() {
            return OneOrMore(Digit());
        }

        public Rule Digit() {
            return Sequence(Test(FirstOf(Five(), Six())), CharRange('0', '9'));
        }

        public Rule Five() {
            return Sequence('5', inPredicate() || count1());
        }

        public Rule Six() {
            return Sequence('6', count2());
        }

        // ********* ACTION *******

        public boolean count1() {
            count++;
            return true;
        }

        public boolean count2() {
            count++;
            return true;
        }
    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.Number(), "565", "" +
                "[Number] '565'\n" +
                "    [Digit] '5'\n" +
                "        [0..9] '5'\n" +
                "    [Digit] '6'\n" +
                "        [0..9] '6'\n" +
                "    [Digit] '5'\n" +
                "        [0..9] '5'\n");
        assertEquals(parser.count, 1);
    }

}