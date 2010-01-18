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

package org.parboiled.asm;

import org.parboiled.ActionResult;
import org.parboiled.Rule;
import org.parboiled.examples.calculator3.CalculatorParser;

@SuppressWarnings({"UnusedDeclaration"})
class TestActions {
    public ActionResult action1() {
        return ActionResult.CONTINUE;
    }

    public ActionResult action2(char c) {
        return ActionResult.CONTINUE;
    }
}

class TestParser extends CalculatorParser {
    private final TestActions testActions = new TestActions();

    /*public Rule localVarRule() {
        char next_char = NEXT_CHAR();
        return sequence(
                ch(next_char),
                actions.action2(next_char)
        );
    }*/

    /*public Rule conditionActionRule() {
        return sequence(
                rule(),
                NEXT_CHAR() == 'y' ? ActionResult.CONTINUE : ActionResult.CANCEL_MATCH
        );
    }*/

    public Rule twoActionsRule() {
        return sequence(
                atom(), "some Text",
                SET(actions.createAst(1.0)),
                testActions.action1()
        );
    }

}
