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

import org.parboiled.support.SkipInPredicates;
import org.parboiled.test.AbstractTest;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

public class SkipInPredicatesTest extends AbstractTest {

    public static class ActionTestActions extends BaseActions<Object> {
        public int count = 0;

        @SkipInPredicates
        public ActionResult count1() {
            count++;
            return ActionResult.CONTINUE;
        }

        public ActionResult count2() {
            count++;
            return ActionResult.CONTINUE;
        }
    }

    public static class ActionTestParser extends BaseParser<Object> {

        private final ActionTestActions actions = new ActionTestActions();

        public Rule number() {
            return oneOrMore(digit());
        }

        public Rule digit() {
            return sequence(test(firstOf(five(), six())), charRange('0', '9'));
        }

        public Rule five() {
            return sequence('5', actions.count1());
        }

        public Rule six() {
            return sequence('6', actions.count2());
        }

    }

    @Test
    public void test() {
        ActionTestParser parser = Parboiled.createParser(ActionTestParser.class);
        test(parser, parser.number(), "565", "" +
                "[number] '565'\n" +
                "    [digit] '5'\n" +
                "        [0..9] '5'\n" +
                "    [digit] '6'\n" +
                "        [0..9] '6'\n" +
                "    [digit] '5'\n" +
                "        [0..9] '5'\n");
        assertEquals(parser.actions.count, 1);
    }

}