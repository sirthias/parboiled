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

package org.parboiled.examples;

import org.parboiled.test.AbstractTest;
import org.parboiled.Parboiled;
import org.parboiled.examples.calculator.CalculatorParser;
import org.testng.annotations.Test;

public class CalculatorTest extends AbstractTest {

    @Test
    public void test() {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);
        test(parser.inputLine(), "1+5", "" +
                "[inputLine, {6}] '1+5'\n" +
                "    [expression, {6}] '1+5'\n" +
                "        [term, {1}] '1'\n" +
                "            [factor, {1}] '1'\n" +
                "                [number, {1}] '1'\n" +
                "                    [oneOrMore] '1'\n" +
                "            [zeroOrMore]\n" +
                "        [zeroOrMore, {5}] '+5'\n" +
                "            [sequence, {5}] '+5'\n" +
                "                [[+,-]] '+'\n" +
                "                [term, {5}] '5'\n" +
                "                    [factor, {5}] '5'\n" +
                "                        [number, {5}] '5'\n" +
                "                            [oneOrMore] '5'\n" +
                "                    [zeroOrMore]\n" +
                "    [EOI]\n");
    }

}