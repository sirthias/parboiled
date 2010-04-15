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

package org.parboiled;

import org.parboiled.test.AbstractTest;
import org.testng.annotations.Test;

public class PutGetTest extends AbstractTest {

    static class Parser extends BaseParser<Integer> {

        public Rule Clause() {
            return Sequence(Digit(), put(), Digit(), set(get().getValue() + lastValue()), Eoi());
        }

        public Rule Digit() {
            return Sequence(CharRange('0', '9'), set(Integer.parseInt(lastText())));
        }

    }

    @Test
    public void testPutGet() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Clause();
        test(rule, "27", "" +
                "[Clause, {9}] '27'\n" +
                "    [Digit, {2}] '2'\n" +
                "        [0..9] '2'\n" +
                "    [Digit, {7}] '7'\n" +
                "        [0..9] '7'\n" +
                "    [EOI]\n");
    }

}