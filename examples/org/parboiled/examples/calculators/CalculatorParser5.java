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

package org.parboiled.examples.calculators;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Rule;
import org.parboiled.examples.calculators.CalculatorParser4.CalcNode;

/**
 * A calculator parser defining the same language as the CalculatorParser4 but using a rule building helper methods
 * to factor out common constructs.
 */
public class CalculatorParser5 extends CalculatorParser<CalcNode> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), eoi());
    }

    public Rule expression() {
        return operatorRule(term(), firstOf('+', '-'));
    }

    public Rule term() {
        return operatorRule(factor(), firstOf('*', '/'));
    }

    public Rule factor() {
        // by using toRule('^') instead of ch('^') we make use of the fromCharLiteral(...) transformation below
        return operatorRule(atom(), toRule('^'));
    }

    public Rule operatorRule(Rule subRule, Rule operatorRule) {
        return sequence(
                subRule, SET(),
                zeroOrMore(
                        sequence(
                                operatorRule.label("op"),
                                subRule,
                                UP2(SET(createAst(DOWN2(CHAR("op")), VALUE(), LAST_VALUE())))
                        )
                )
        );
    }

    public Rule atom() {
        return firstOf(number(), squareRoot(), parens());
    }

    public Rule squareRoot() {
        return sequence("SQRT", parens(), SET(createAst('R', LAST_VALUE(), null)));
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
        return zeroOrMore(charSet(" \t\f"));
    }

    @Override
    protected Rule fromCharLiteral(char c) {
        return sequence(ch(c), whiteSpace());
    }

    @Override
    protected Rule fromStringLiteral(@NotNull String string) {
        return sequence(string(string), whiteSpace());
    }

    //**************** ACTIONS ****************

    public CalcNode createAst(double value) {
        return new CalcNode(value);
    }

    public CalcNode createAst(Character type, CalcNode left, CalcNode right) {
        return new CalcNode(type, left, right);
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser5.class);
    }
}