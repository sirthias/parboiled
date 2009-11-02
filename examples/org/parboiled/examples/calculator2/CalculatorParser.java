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

package org.parboiled.examples.calculator2;

import org.parboiled.Rule;
import org.parboiled.BaseParser;

public class CalculatorParser extends BaseParser<CalcNode, CalculatorActions> {

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
                term(), set(),
                zeroOrMore(
                        enforcedSequence(
                                firstOf('+', '-'),
                                term(), up(up(set(actions.createAst(ch("z/last:e/firstOf"), value(), lastValue()))))
                        )
                )
        );
    }

    public Rule term() {
        return sequence(
                factor(), set(),
                zeroOrMore(
                        enforcedSequence(
                                firstOf('*', '/'),
                                factor(), up(up(set(actions.createAst(ch("z/last:e/firstOf"), value(), lastValue()))))
                        )
                )
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
                set(actions.createAst(convertToInteger(lastText())))
        );
    }

    public Rule digit() {
        return charRange('0', '9');
    }

}