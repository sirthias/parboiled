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

package org.parboiled.examples.calculator;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

public class CalculatorParser extends BaseParser<Integer, CalculatorActions> {

    public CalculatorParser(CalculatorActions actions) {
        super(actions);
    }

    public Rule inputLine() {
        return enforcedSequence(
                expression(),
                eoi()
        );
    }

    public Rule expression() {
        return sequence(
                term(),
                zeroOrMore(enforcedSequence(firstOf('+', '-'), term())),
                actions.compute(VALUE("term"), CHARS("z/e/firstOf"), VALUES("z/e/term"))
        );
    }

    public Rule term() {
        return sequence(
                factor(),
                zeroOrMore(enforcedSequence(firstOf('*', '/'), factor())),
                actions.compute(VALUE("factor"), CHARS("z/e/firstOf"), VALUES("z/e/factor"))
        );
    }

    public Rule factor() {
        return firstOf(
                number(),
                enforcedSequence('(', expression(), ')').label("parens")
        );
    }

    public Rule number() {
        return sequence(
                oneOrMore(digit()),
                SET(CONVERT_TO_INTEGER(LAST_TEXT()))
        );
    }

    public Rule digit() {
        return charRange('0', '9');
    }

}
