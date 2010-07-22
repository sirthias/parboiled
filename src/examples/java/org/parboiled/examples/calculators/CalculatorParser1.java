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

import org.parboiled.Rule;
import org.parboiled.annotations.SuppressNode;

/**
 * A calculator parser keeping calculation results directly in the value field of the parse tree nodes.
 * All calculations are implemented directly in action expressions.
 */
public class CalculatorParser1 extends CalculatorParser<Integer> {

    @Override
    public Rule InputLine() {
        return Sequence(Expression(), Eoi());
    }

    public Rule Expression() {
        return Sequence(
                Term(),
                ZeroOrMore(
                        FirstOf(
                                // the action that is run after the '+' and the "Term" have been matched
                                // sets the value of the enclosing "Expression" to the sum of the old value and the
                                // value of the node that was constructed last, in this case the preceding "Term"
                                Sequence('+', Term(), push(pop() + pop())),

                                // dito for the '-' operator
                                Sequence('-', Term(), push(pop(1) - pop()))
                        )
                )
        );
    }

    public Rule Term() {
        return Sequence(
                Factor(),
                ZeroOrMore(
                        FirstOf(
                                // the action that is run after the '*' and the "Factor" have been matched
                                // sets the value of the enclosing "Term" to the product of the old value and the
                                // value of the node that was constructed last, in this case the preceding "Factor"
                                Sequence('*', Factor(), push(pop() * pop())),

                                // dito for the '/' operator
                                Sequence('/', Factor(), push(pop(1) / pop()))
                        )
                )
        );
    }

    public Rule Factor() {
        return FirstOf(Number(), Parens());
    }

    public Rule Parens() {
        return Sequence('(', Expression(), ')');
    }

    public Rule Number() {
        return Sequence(
                Digits(),

                // parse the input text matched by the preceding "Digits" rule, convert it into an Integer and set this
                // Integer as the value of the parse tree node of this rule (the Sequence rule labelled "Number")
                push(Integer.parseInt(match()))
        );
    }
    
    public Rule Digits() {
        return OneOrMore(Digit());
    }

    @SuppressNode
    public Rule Digit() {
        return CharRange('0', '9');
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser1.class);
    }

}