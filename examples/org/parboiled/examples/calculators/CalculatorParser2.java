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
import org.parboiled.support.Leaf;

/**
 * A calculator parser keeping calculation results directly in the value field of the parse tree nodes.
 * All calculations are implemented directly in action expressions.
 */
public class CalculatorParser2 extends CalculatorParser<Integer> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), eoi());
    }

    public Rule expression() {
        return sequence(
                term(), SET(), // the SET() sets the value of the "expression" to the value of the preceding "term"
                zeroOrMore(
                        firstOf(
                                // the action that is run after the '+' and the "term" have been matched
                                // sets the value of the enclosing "expression" to the sum of the old value and the
                                // value of the node that was constructed last, in this case the preceding "term"
                                sequence('+', term(), UP3(SET(VALUE() + LAST_VALUE()))),

                                // dito for the '-' operator
                                sequence('-', term(), UP3(SET(VALUE() - LAST_VALUE())))
                        )
                )
        );
    }

    public Rule term() {
        return sequence(
                factor(), SET(), // the SET() sets the value of the "term" to the value of the preceding "factor"
                zeroOrMore(
                        firstOf(
                                // the action that is run after the '*' and the "factor" have been matched
                                // sets the value of the enclosing "term" to the product of the old value and the
                                // value of the node that was constructed last, in this case the preceding "factor"
                                sequence('*', factor(), UP3(SET(VALUE() * LAST_VALUE()))),

                                // dito for the '/' operator
                                sequence('/', factor(), UP3(SET(VALUE() / LAST_VALUE())))
                        )
                )
        );
    }

    public Rule factor() {
        return firstOf(number(), parens());
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return sequence(
                digits(),

                // parse the input text matched by the preceding "digits" rule, convert it into an Integer and set this
                // Integer as the value of the parse tree node of this rule (the sequence rule labelled "number")
                SET(Integer.parseInt(LAST_TEXT()))
        );
    }
    
    @Leaf
    public Rule digits() {
        return oneOrMore(digit());
    }

    public Rule digit() {
        return charRange('0', '9');
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser2.class);
    }

}