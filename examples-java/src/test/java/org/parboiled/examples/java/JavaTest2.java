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

package org.parboiled.examples.java;

import org.parboiled.Node;
import org.parboiled.Parboiled;
import org.parboiled.ParserStatistics;
import org.parboiled.Rule;
import org.parboiled.common.FileUtils;
import org.parboiled.common.Formatter;
import org.parboiled.common.Predicates;
import org.parboiled.common.StringUtils;
import org.parboiled.errors.ErrorUtils;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.Filters;
import org.parboiled.support.ParsingResult;
import org.parboiled.support.ToStringFormatter;
import org.testng.annotations.Test;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;
import static org.parboiled.trees.GraphUtils.printTree;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public class JavaTest2 {

    @Test
    public void javaTest2() {
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Rule compilationUnit = parser.CompilationUnit();

        ParsingResult<Object> parsingResult = new ReportingParseRunner<Object>(compilationUnit).run(
                "class test\n" +
                "{\n" +
                "  double d = 0xff;\n" +
                "}"
        );
        if (parsingResult.hasErrors()) {
            fail(ErrorUtils.printParseErrors(parsingResult));
        }
        assertEquals(
                printTree(parsingResult.parseTreeRoot, new ToStringFormatter<Node<Object>>()),
                "[CompilationUnit]\n" +
                "  [Optional]\n" +
                "  [ZeroOrMore]\n" +
                "  [ZeroOrMore]\n" +
                "    [TypeDeclaration]\n" +
                "      [Sequence]\n" +
                "        [ZeroOrMore]\n" +
                "        [FirstOf]\n" +
                "          [ClassDeclaration]\n" +
                "            [Identifier]\n" +
                "            [Optional]\n" +
                "            [Optional]\n" +
                "            [Optional]\n" +
                "            [ClassBody]\n" +
                "              [ZeroOrMore]\n" +
                "                [ClassBodyDeclaration]\n" +
                "                  [Sequence]\n" +
                "                    [ZeroOrMore]\n" +
                "                    [MemberDecl]\n" +
                "                      [Sequence]\n" +
                "                        [Type]\n" +
                "                          [FirstOf]\n" +
                "                            [BasicType]\n" +
                "                              [FirstOf]\n" +
                "                          [ZeroOrMore]\n" +
                "                        [VariableDeclarators]\n" +
                "                          [VariableDeclarator]\n" +
                "                            [Identifier]\n" +
                "                            [ZeroOrMore]\n" +
                "                            [Optional]\n" +
                "                              [Sequence]\n" +
                "                                [VariableInitializer]\n" +
                "                                  [Expression]\n" +
                "                                    [ConditionalExpression]\n" +
                "                                      [ConditionalOrExpression]\n" +
                "                                        [ConditionalAndExpression]\n" +
                "                                          [InclusiveOrExpression]\n" +
                "                                            [ExclusiveOrExpression]\n" +
                "                                              [AndExpression]\n" +
                "                                                [EqualityExpression]\n" +
                "                                                  [RelationalExpression]\n" +
                "                                                    [ShiftExpression]\n" +
                "                                                      [AdditiveExpression]\n" +
                "                                                        [MultiplicativeExpression]\n" +
                "                                                          [UnaryExpression]\n" +
                "                                                            [Sequence]\n" +
                "                                                              [Primary]\n" +
                "                                                                [Literal]\n" +
                "                                                                  [FirstOf]\n" +
                "                                                                    [IntegerLiteral]\n" +
                "                                                              [ZeroOrMore]\n" +
                "                                                              [ZeroOrMore]\n" +
                "                                                          [ZeroOrMore]\n" +
                "                                                        [ZeroOrMore]\n" +
                "                                                      [ZeroOrMore]\n" +
                "                                                    [ZeroOrMore]\n" +
                "                                                  [ZeroOrMore]\n" +
                "                                                [ZeroOrMore]\n" +
                "                                              [ZeroOrMore]\n" +
                "                                            [ZeroOrMore]\n" +
                "                                          [ZeroOrMore]\n" +
                "                                        [ZeroOrMore]\n" +
                "                                      [ZeroOrMore]\n" +
                "                                    [ZeroOrMore]\n" +
                "                          [ZeroOrMore]\n" +
                "  [EOI]\n"
        );
    }

}
