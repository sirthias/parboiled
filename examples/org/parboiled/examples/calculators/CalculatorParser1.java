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
    public Rule inputLine() {
        return sequence(expression(), eoi());
    }

    public Rule expression() {
        return sequence(
                term(),
                zeroOrMore(
                        sequence(
                                charSet("+-").label("op"),
                                term()
                        )
                ),

                // "z/s/..." is short for "zeroOrMore/sequence/..."
                compute(VALUE("term"), CHARS("z/s/op"), VALUES("z/s/term"))
        );
    }

    public Rule term() {
        return sequence(
                factor(),
                zeroOrMore(
                        sequence(
                                charSet("*/").label("op"),
                                factor()
                        )
                ),

                // "z/s/..." is short for "zeroOrMore/sequence/..."
                compute(VALUE("factor"), CHARS("z/s/op"), VALUES("z/s/factor"))
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

    public Rule digits() {
        return oneOrMore(digit());
    }

    public Rule digit() {
        return charRange('0', '9');
    }

    //**************** ACTIONS ****************

    public boolean compute(Integer firstValue, List<Character> operators, List<Integer> values) {
        int value = firstValue;
        for (Map.Entry<Character, Integer> entry : zip(operators, values)) {
            value = performOperation(entry.getKey(), value, entry.getValue());
        }
        return SET(value);
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
