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

import org.parboiled.ActionResult;
import org.parboiled.ast.AstActions;
import static org.parboiled.ast.AstUtils.toLeftAssociativity;

public class CalculatorActions extends AstActions<Character, CalcNode> {

    public ActionResult createAstNode(Integer value) {
        return setAstNode(new CalcNode(value));
    }

    public ActionResult createAstNode(Character type, CalcNode left, CalcNode right, String convertOps) {
        if (left == null) return setAstNode(right);
        if (right == null) return setAstNode(left);

        CalcNode node = new CalcNode(type, left, right);
        if (right.isOneOf(convertOps)) {
            node = toLeftAssociativity(node);
        }
        return setValue(node);
    }

}