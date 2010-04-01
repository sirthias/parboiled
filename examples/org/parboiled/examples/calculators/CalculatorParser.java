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

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.RecoveringParseRunner;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;
import org.parboiled.support.ParsingResult;

import java.util.Scanner;

import static org.parboiled.support.ParseTreeUtils.printNodeTree;

/**
 * Base class of all calculator parsers in the org.parboiled.examples.calculators package.
 * Simply add the public static main entry point.
 *
 * @param <V>
 */
public abstract class CalculatorParser<V> extends BaseParser<V> {

    public abstract Rule InputLine();

    public static <V, P extends CalculatorParser<V>> void main(Class<P> parserClass) {
        CalculatorParser<V> parser = Parboiled.createParser(parserClass);

        while (true) {
            System.out.print("Enter a calculators expression (single RETURN to exit)!\n");
            String input = new Scanner(System.in).nextLine();
            if (StringUtils.isEmpty(input)) break;

            ParsingResult<?> result = RecoveringParseRunner.run(parser.InputLine(), input);

            System.out.println(input + " = " + result.parseTreeRoot.getValue() + '\n');
            System.out.println("Parse Tree:\n" + printNodeTree(result) + '\n');

            if (!result.matched) {
                System.out.println(StringUtils.join(result.parseErrors, "---\n"));
            }
        }
    }

}
