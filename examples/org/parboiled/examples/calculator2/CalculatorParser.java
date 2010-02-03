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

package org.parboiled.examples.calculator2;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

public class CalculatorParser extends BaseParser<CalcNode> {

    final CalculatorActions actions = new CalculatorActions();

    public Rule inputLine() {
        return sequence(
                expression(),
                eoi()
        );
    }

    public Rule expression() {
        return sequence(
                term(), SET(), // the SET() sets the value of the "expression" to the value of the "term"
                zeroOrMore(
                        sequence(
                                firstOf('+', '-'),
                                term(),
                                UP(UP(SET(actions.createAst(DOWN(DOWN(CHAR("firstOf"))), VALUE(), LAST_VALUE()))))
                                // this creates a new AST node and sets it as the value of the "expression"
                                // the node contains the operator ('+' or '-'), the old "expression" value as left
                                // child and the value of the "term" following the operator as right child
                        )
                )
        );
    }

    public Rule term() {
        return sequence(
                factor(), SET(),
                zeroOrMore(
                        sequence(
                                firstOf('*', '/'),
                                factor(),
                                UP(UP(SET(actions.createAst(DOWN(DOWN(CHAR("firstOf"))), VALUE(), LAST_VALUE()))))
                        )
                )
        );
    }

    public Rule factor() {
        return firstOf(
                number(),
                parens()
        );
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return sequence(
                oneOrMore(digit()),
                SET(actions.createAst(Integer.parseInt(LAST_TEXT())))
        );
    }

    public Rule digit() {
        return charRange('0', '9');
    }

}