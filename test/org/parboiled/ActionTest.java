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

import org.testng.annotations.Test;

public class ActionTest extends AbstractTest {

    public static class ActionTestActions extends BaseActions<Object> {
        public Integer timesTwo(Integer i) {
            return i != null ? i * 2 : null;
        }
    }

    public static class ActionTestParser extends BaseParser<Object, ActionTestActions> {

        public ActionTestParser(ActionTestActions actions) {
            super(actions);
        }

        public Rule number() {
            return sequence(
                    oneOrMore(digit()),
                    SET(actions.timesTwo(CONVERT_TO_INTEGER(LAST_TEXT())))
            );
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    @Test
    public void test() {
        ActionTestActions actions = Parboiled.createActions(ActionTestActions.class);
        ActionTestParser parser = Parboiled.createParser(ActionTestParser.class, actions);
        test(parser, parser.number(), "123", "" +
                "[number, {246}] '123'\n" +
                "    [oneOrMore] '123'\n" +
                "        [digit] '1'\n" +
                "        [digit] '2'\n" +
                "        [digit] '3'\n");
    }

}