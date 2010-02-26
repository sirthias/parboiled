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

import org.parboiled.Parboiled;
import org.parboiled.RecoveringParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.common.StringUtils;
import org.parboiled.common.ToStringFormatter;
import static org.parboiled.trees.GraphUtils.printTree;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        CalculatorParser parser = Parboiled.createParser(CalculatorParser.class);

        while (true) {
            System.out.print("Enter a calculator expression (single RETURN to exit)!\n");
            String input = new Scanner(System.in).nextLine();
            if (StringUtils.isEmpty(input)) break;

            ParsingResult<CalcNode> result = RecoveringParseRunner.run(parser.inputLine(), input);

            CalcNode astRoot = result.parseTreeRoot.getValue();
            System.out.println(input + " = " + astRoot.getValue() + '\n');

            // System.out.println("Parse Tree:\n" + printNodeTree(result) + '\n');

            System.out.println("Abstract Syntax Tree:\n" +
                    printTree(result.parseTreeRoot.getValue(), new ToStringFormatter<CalcNode>(null)) + '\n');

            if (!result.matched) {
                System.out.println(StringUtils.join(result.parseErrors, "---\n"));
            }
        }
    }

}