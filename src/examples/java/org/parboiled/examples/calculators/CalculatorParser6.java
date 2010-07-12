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
import org.parboiled.annotations.SuppressSubnodes;
import org.parboiled.examples.calculators.CalculatorParser4.CalcNode;
import org.parboiled.support.Var;

/**
 * A calculator parser defining the same language as the CalculatorParser5 but relying completely on parse-tree-less
 * parsing and action variables for maximized performance.
 */
public class CalculatorParser6 extends CalculatorParser<CalcNode> {

    @Override
    @SuppressSubnodes // putting this annotation on the root rule of the grammar "turns on" parse-tree-less parsing
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
        Var<Character> operator = new Var<Character>();
        return Sequence(
                subRule, set(), // run the subRule and set its value on the Sequence
                ZeroOrMore(
                        Sequence(
                                // match the operator and save it in the action variable
                                operatorRule, operator.set(prevChar()),
                                
                                // match the second subrule and update the value of the outer sequence with a
                                // newly created AST node for the operation
                                subRule, UP2(set(new CalcNode(operator.get(), value(), prevValue())))
                        )
                )
        );
    }

    public Rule Atom() {
        return FirstOf(Number(), SquareRoot(), Parens());
    }

    public Rule SquareRoot() {
        return Sequence("SQRT", Parens(), set(new CalcNode('R', prevValue(), null)));
    }

    public Rule Parens() {
        return Sequence('(', Expression(), ')');
    }

    public Rule Number() {
        return Sequence( 
                Sequence(
                        // we use another Sequence to group the inner rules so we can access the input text matched
                        // by all of them in one go
                        Optional(Ch('-')),
                        OneOrMore(Digit()),
                        Optional(Sequence(Ch('.'), OneOrMore(Digit())))
                ),
                set(new CalcNode(Double.parseDouble(prevText()))),
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
        main(CalculatorParser6.class);
    }
}