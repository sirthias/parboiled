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

package org.parboiled.examples.calculators;

import org.parboiled.Rule;

import java.util.List;
import java.util.Map;

import static org.parboiled.common.Utils.zip;

/**
 * A simple calculator parser using an action approach demonstrating node selecting queries and keeping calculation
 * results directly in the value field of the parse tree nodes.
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
                        Sequence(
                                CharSet("+-").label("Op"),
                                Term()
                        )
                ),

                // "Z/S/..." is short for "ZeroOrMore/Sequence/..."
                compute(value("Term"), chars("Z/S/Op"), values("Z/S/Term"))
        );
    }

    public Rule Term() {
        return Sequence(
                Factor(),
                ZeroOrMore(
                        Sequence(
                                CharSet("*/").label("Op"),
                                Factor()
                        )
                ),

                // "Z/S/..." is short for "ZeroOrMore/Sequence/..."
                compute(value("Factor"), chars("Z/S/Op"), values("Z/S/Factor"))
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
                // Integer as the value of the parse tree node of this rule (the Sequence rule labeled "Number")
                set(Integer.parseInt(lastText()))
        );
    }

    public Rule Digits() {
        return OneOrMore(Digit());
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    //**************** ACTIONS ****************

    public boolean compute(Integer firstValue, List<Character> operators, List<Integer> values) {
        int value = firstValue;
        for (Map.Entry<Character, Integer> entry : zip(operators, values)) {
            value = performOperation(entry.getKey(), value, entry.getValue());
        }
        return set(value);
    }

    private int performOperation(char operator, int value1, int value2) {
        switch (operator) {
            case '+':
                return value1 + value2;
            case '-':
                return value1 - value2;
            case '*':
                return value1 * value2;
            case '/':
                return value1 / value2;
        }
        throw new IllegalStateException();
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser1.class);
    }

}
