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

import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;
import org.parboiled.test.AbstractTest;

public class IndentDedentTest extends AbstractTest {

    public static class IndentDedentActions extends BaseActions<Object> {

        public int currentIndent;
        public int indents;
        public int dedents;

        public boolean countIndentOrDedent(String lineStartWhiteSpace) {
            if (lineStartWhiteSpace.length() > currentIndent) indents++;
            if (lineStartWhiteSpace.length() < currentIndent) dedents++;
            currentIndent = lineStartWhiteSpace.length();
            return true;
        }
    }

    public static class IndentDedentParser extends BaseParser<Object> {

        protected final IndentDedentActions actions = new IndentDedentActions();

        public Rule file() {
            return zeroOrMore(line());
        }

        public Rule line() {
            return sequence(
                    zeroOrMore(' '),
                    actions.countIndentOrDedent(TEXT(LAST_NODE())),
                    zeroOrMore(lineChar()),
                    newline()
            );
        }

        public Rule lineChar() {
            return sequence(testNot(newline()), any());
        }

        public Rule newline() {
            return firstOf("\r\n", '\r', '\n');
        }

    }

    @Test
    public void test() {
        IndentDedentParser parser = Parboiled.createParser(IndentDedentParser.class);
        parser.parse(parser.file(), "" +
                "a file containing\n" +
                "  some\n" +
                "     indents\n" +
                "     some lines\n" +
                "     that do not indent\n" +
                "  they might dedent\n" +
                "  stay there\n" +
                "          go back big time\n" +
                " and return"
        );
        assertEquals(parser.actions.indents, 3);
        assertEquals(parser.actions.dedents, 2);
        assertEquals(parser.actions.currentIndent, 1);
    }

}