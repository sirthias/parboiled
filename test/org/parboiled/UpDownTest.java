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

public class UpDownTest extends AbstractTest {

    static class Parser extends BaseParser<Integer> {

        public Rule Clause() {
            return Sequence(
                    set(42),
                    A()
            );
        }

        public Rule A() {
            return Sequence(
                    set(UP(value())),
                    'a',
                    UP(set(value() + DOWN(value())))
            );
        }

    }

    @Test
    public void testPutGet() {
        Parser parser = Parboiled.createParser(Parser.class);
        Rule rule = parser.Clause();
        test(rule, "a", "" +
                "[Clause, {84}] 'a'\n" +
                "    [A, {42}] 'a'\n" +
                "        ['a'] 'a'\n");
    }

}