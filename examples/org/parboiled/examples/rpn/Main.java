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

package org.parboiled.examples.rpn;

import org.parboiled.Parboiled;
import org.parboiled.common.StringUtils;
import org.parboiled.support.Filters;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        RpnParser parser = Parboiled.createParser(RpnParser.class);
        
        while (true) {
            System.out.print("Enter an RPN expression, separate operands by a blank (single RETURN to exit)!\n");
            String input = new Scanner(System.in).nextLine();
            if (StringUtils.isEmpty(input)) break;

            ParsingResult<Node> result = Parboiled.parse(parser, parser.operation(), input);

            System.out.println(ParseTreeUtils.printNodeTree(result, Filters.SkipEmptyOptionalsAndZeroOrMores));

            List<BigDecimal> output = result.parseTreeRoot.getValue().getResult();
            if (!result.matched) {
                System.out.println(StringUtils.join(result.parseErrors, "---\n"));
            }
            System.out.println(result.parseTreeRoot.getValue().getWarnings());
            System.out.println("Result: " + (output.size() == 1 ? output.get(0) : output.toString()));
        }
    }

}