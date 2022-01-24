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

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class CreateParserTest {

    public static class Parser extends BaseParser<Object> {
        private String stringParam;
        private boolean booleanParam;
        private float floatParam;

        public Parser(String stringParam, boolean booleanParam, float floatParam) {
            this.stringParam = stringParam;
            this.booleanParam = booleanParam;
            this.floatParam = floatParam;
        }

        public String getStringParam() {
            return stringParam;
        }

        public boolean getBooleanParam() {
            return booleanParam;
        }

        public float getFloatParam() {
            return floatParam;
        }
    }

    @Test
    public void testCreateParser() {
        Parser parser = Parboiled.createParser(Parser.class, "Hello, world", false, 1.056f);
        assertEquals(parser.getStringParam(), "Hello, world");
        assertFalse(parser.getBooleanParam());
        assertEquals(parser.getFloatParam(), 1.056f);
    }
}
