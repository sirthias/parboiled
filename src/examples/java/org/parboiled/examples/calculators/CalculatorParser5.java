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
 * to Factor out common constructs.
 */
public class CalculatorParser5 extends CalculatorParser<CalcNode> {

    @Override
    public Rule InputLine() {
        return Sequence(Expression(), Eoi());
    }

    public Rule Expression() {
        return OperatorRule(Term(), FirstOf('+', '-'));
    }

    public Rule Term() {
        return OperatorRule(Factor(), FirstOf('*', '/'));
    }

    public Rule Factor() {
        // by using ToRule('^') instead of Ch('^') we make use of the FromCharLiteral(...) transformation below
        return OperatorRule(Atom(), ToRule('^'));
    }

    public Rule OperatorRule(Rule subRule, Rule operatorRule) {
        return Sequence(
                subRule, set(), // run the subRule and set its value on the Sequence
                ZeroOrMore(
                        Sequence(
                                operatorRule.label("Op"),
                                subRule,
                                UP2(set(new CalcNode(DOWN2(character("Op")), value(), lastValue())))
                        )
                )
        );
    }

    public Rule Atom() {
        return FirstOf(Number(), SquareRoot(), Parens());
    }

    public Rule SquareRoot() {
        return Sequence("SQRT", Parens(), set(new CalcNode('R', lastValue(), null)));
    }

    public Rule Parens() {
        return Sequence('(', Expression(), ')');
    }

    public Rule Number() {
        return Sequence(
                Sequence(
                        Optional(Ch('-')),
                        OneOrMore(Digit()),
                        Optional(Sequence(Ch('.'), OneOrMore(Digit())))
                ),
                set(new CalcNode(Double.parseDouble(lastText()))),
                WhiteSpace()
        );
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    public Rule WhiteSpace() {
        return ZeroOrMore(CharSet(" \t\f"));
    }

    @Override
    protected Rule FromCharLiteral(char c) {
        return Sequence(Ch(c), WhiteSpace());
    }

    @Override
    protected Rule FromStringLiteral(@NotNull String string) {
        return Sequence(String(string), WhiteSpace());
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser5.class);
    }
}