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

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

public class NotEqualTest extends AbstractTest {

    public static class NotEqualTestParser extends BaseParser<Object, Actions<Object>> {

        public Rule equalClause() {
            return sequence(
                    digit().label("a"),
                    digit().label("b"),
                    EQUALS(TEXT("a"), TEXT("b")),
                    eoi());
        }

        public Rule notEqualClause() {
            return sequence(
                    digit().label("a"),
                    digit().label("b"),
                    NOT(EQUALS(TEXT("a"), TEXT("b"))),
                    eoi());
        }

        public Rule digit() {
            return charRange('0', '9');
        }

    }

    @Test
    public void test() {
        NotEqualTestParser parser = Parboiled.createParser(NotEqualTestParser.class);
        assertFalse(parser.parse(parser.equalClause(), "22").hasErrors());
        assertTrue(parser.parse(parser.equalClause(), "23").hasErrors());
        assertFalse(parser.parse(parser.notEqualClause(), "23").hasErrors());
        assertTrue(parser.parse(parser.notEqualClause(), "22").hasErrors());
    }

}
