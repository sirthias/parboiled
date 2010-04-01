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

import org.parboiled.support.Label;
import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

public class ActionTest extends AbstractTest {

    public static class Actions extends BaseActions<Object> {

        public boolean addOne() {
            Integer i = (Integer) getContext().getNodeValue();
            getContext().setNodeValue(i + 1);
            return true;
        }
    }

    public static class Parser extends BaseParser<Integer> {

        final Actions actions = new Actions();

        public Rule A() {
            return sequence(
                    'a',
                    SET(42),
                    B(18)
            );
        }

        @Label
        public Rule B(int i) {
            int j = i + 1;
            return sequence(
                    'b',
                    SET(timesTwo(i + j)),
                    C()
            );
        }

        public Rule C() {
            return sequence(
                    'c',
                    new Action() {
                        public boolean run(Context context) {
                            return getContext() == context;
                        }
                    },
                    D(1)
            );
        }

        @Label
        public Rule D(int i) {
            return sequence(
                    'd', SET(UP3(VALUE())),
                    UP(SET(i)),
                    actions.addOne()
            );
        }

        // ************* ACTIONS **************

        public int timesTwo(int i) {
            return i * 2;
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        test(parser.A(), "abcd", "" +
                "[A, {42}] 'abcd'\n" +
                "    ['a'] 'a'\n" +
                "    [B, {74}] 'bcd'\n" +
                "        ['b'] 'b'\n" +
                "        [C, {1}] 'cd'\n" +
                "            ['c'] 'c'\n" +
                "            [D, {43}] 'd'\n" +
                "                ['d'] 'd'\n");
    }

}