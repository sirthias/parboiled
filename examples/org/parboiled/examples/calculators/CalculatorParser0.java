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

/**
 * A basic calculator parser without any actions.
 */
public class CalculatorParser0 extends CalculatorParser<Integer> {

    @Override
    public Rule inputLine() {
        return sequence(expression(), eoi());
    }

    public Rule expression() {
        return sequence(
                term(),
                zeroOrMore(sequence(charSet("+-"), term()))
        );
    }

    public Rule term() {
        return sequence(
                factor(),
                zeroOrMore(sequence(charSet("*/"), factor()))
        );
    }

    public Rule factor() {
        return firstOf(number(), parens());
    }

    public Rule parens() {
        return sequence('(', expression(), ')');
    }

    public Rule number() {
        return oneOrMore(digit());
    }

    public Rule digit() {
        return charRange('0', '9');
    }

    //**************** MAIN ****************

    public static void main(String[] args) {
        main(CalculatorParser0.class);
    }

}