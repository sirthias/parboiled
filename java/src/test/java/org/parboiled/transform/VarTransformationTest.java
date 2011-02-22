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

package org.parboiled.transform;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.Var;
import org.parboiled.parserunners.BasicParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.test.TestNgParboiledTest;
import org.testng.annotations.Test;

public class VarTransformationTest extends TestNgParboiledTest<Integer> {

    @BuildParseTree
    static class ParserA extends BaseParser<Object> {
        public Rule RuleA(@Var int actionParam, int constant) {
            int local;
            return Sequence(local = constant, CharRange('0', '9'), local = actionParam + Integer.valueOf(match()), actionParam = local,
                    push(actionParam));
        }
    }

    static class ComposedParser extends BaseParser<Object> {
        ParserA parserA = Parboiled.createParser(ParserA.class);

        public Rule RuleB(@Var int constant) {
            return getParserA().RuleA(constant, 2);
        }

        public ParserA getParserA() {
            return parserA;
        }
    }

    static class UnusedParamParser extends BaseParser<Object> {
        public Rule Rule1(@Var int param) {
            return fromStringLiteral("");
        }

        public Rule Rule2(@Var int param, int constant) {
            return Rule1(constant);
        }
    }

    @Test
    public void testComposition() {
        ComposedParser parser = Parboiled.createParser(ComposedParser.class);
        ParsingResult<Object> result = new BasicParseRunner<Object>(parser.RuleB(3)).run("5");
        assertEquals(result.resultValue, 8);
    }

    @Test
    public void testUnusedParam() {
        Parboiled.createParser(UnusedParamParser.class);
    }
}