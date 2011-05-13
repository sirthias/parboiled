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

import org.parboiled.parserunners.RecoveringParseRunner;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CurrentCharTest {

    public static class Parser extends BaseParser<Object> {

        public Rule Clause() {
            return Sequence(currentChar() == 'a', ANY, EOI);
        }

    }

    @Test
    public void test() {
        Parser parser = Parboiled.createParser(Parser.class);
        assertFalse(new RecoveringParseRunner(parser.Clause()).run("a").hasErrors());
        assertTrue(new RecoveringParseRunner(parser.Clause()).run("b").hasErrors());
    }

}