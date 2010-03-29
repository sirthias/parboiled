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

package org.parboiled.examples.calculator4;

import org.jetbrains.annotations.NotNull;
import org.parboiled.BaseParser;
import org.parboiled.Rule;

public class CalculatorParser extends BaseParser<CalcNode> {

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
                                UP2(SET(createAst(DOWN2(CHAR("firstOf")), VALUE(), LAST_VALUE())))
                                // this creates a new AST node and sets it as the value of the "expression",
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
                                UP2(SET(createAst(DOWN2(CHAR("firstOf")), VALUE(), LAST_VALUE())))
                        )
                )
        );
    }

    public Rule factor() {
        return sequence(
                atom(), SET(),
                zeroOrMore(
                        sequence(
                                '^',
                                atom(),
                                UP2(SET(createAst('^', VALUE(), LAST_VALUE())))
                        )
                )
        );
    }

    public Rule atom() {
        return firstOf(
                number(),
                squareRoot(),
                parens()
        );
    }

    public Rule squareRoot() {
        return sequence(
                "SQRT",
                parens(),
                SET(createAst('R', VALUE(), VALUE()))
        );
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return sequence(
                sequence(
                        optional(ch('-')),
                        oneOrMore(digit()),
                        optional(sequence(ch('.'), oneOrMore(digit())))
                ),
                SET(createAst(Double.parseDouble(LAST_TEXT()))),
                whiteSpace()
        );
    }

    public Rule digit() {
        return charRange('0', '9');
    }

    public Rule whiteSpace() {
        return zeroOrMore(firstOf(charSet(" \t\f"), newline()));
    }

    public Rule newline() {
        return firstOf(string("\r\n"), charSet("\r\n"));
    }

    @Override
    protected Rule fromCharLiteral(char c) {
        // we redefine the rule creation for character literals to also match trailing whitespace
        // this way we don't have to insert extra whitespace() rules after each character literal
        // however, we now have to wrap character matching rules we don't want to be "space swallowing"
        // with the ch(...) rule creator
        return sequence(ch(c), whiteSpace());
    }

    @Override
    protected Rule fromStringLiteral(@NotNull String string) {
        // same thing for string literals
        return sequence(string(string), whiteSpace());
    }

    // ACTIONS

    public CalcNode createAst(Double value) {
        return new CalcNode(value);
    }

    public CalcNode createAst(Character type, CalcNode left, CalcNode right) {
        return new CalcNode(type, left, right);
    }

}