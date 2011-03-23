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
import org.parboiled.support.ParsingResult;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class IndentDedentTest {

    public static class IndentDedentParser extends BaseParser<Object> {
        public int currentIndent;
        public int indents;
        public int dedents;

        public Rule File() {
            return ZeroOrMore(Line());
        }

        public Rule Line() {
            return Sequence(
                    ZeroOrMore(' '),
                    countIndentOrDedent(match()),
                    ZeroOrMore(LineChar()),
                    Newline()
            );
        }

        public Rule LineChar() {
            return Sequence(TestNot(Newline()), ANY);
        }

        public Rule Newline() {
            return FirstOf("\r\n", '\r', '\n');
        }

        public boolean countIndentOrDedent(String lineStartWhiteSpace) {
            if (lineStartWhiteSpace.length() > currentIndent) indents++;
            if (lineStartWhiteSpace.length() < currentIndent) dedents++;
            currentIndent = lineStartWhiteSpace.length();
            return true;
        }
    }

    @Test
    public void test() {
        IndentDedentParser parser = Parboiled.createParser(IndentDedentParser.class);
        Rule rule = parser.File();
        String source = "" +
                "a file containing\n" +
                "  some\n" +
                "     indents\n" +
                "     some lines\n" +
                "     that do not indent\n" +
                "  they might dedent\n" +
                "  stay there\n" +
                "          go back big time\n" +
                " and return";
        ParsingResult result = new RecoveringParseRunner(rule).run(source);
        assertEquals(parser.indents, 3);
        assertEquals(parser.dedents, 2);
        assertEquals(parser.currentIndent, 1);
        assertEquals(result.inputBuffer.getPosition(source.length() - 1).line, 9);
    }

}