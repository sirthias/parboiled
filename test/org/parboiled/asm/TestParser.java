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

import org.parboiled.Rule;
import org.parboiled.examples.calculator3.CalculatorParser;

@SuppressWarnings({"UnusedDeclaration"})
class TestActions {
    public int action1() {
        return 5;
    }

    public boolean action2(char c) {
        return true;
    }
}

class TestParser extends CalculatorParser {
    private final TestActions testActions = new TestActions();

    /*public Rule localVarRule() {
        char next_char = nextChar();
        return sequence(
                ch(next_char),
                actions.action2(next_char)
        );
    }*/

    /*public Rule conditionActionRule() {
        return sequence(
                rule(),
                nextChar() == 'y' ? ActionResult.CONTINUE : ActionResult.CANCEL_MATCH
        );
    }*/

    public Rule intComparison() {
        return sequence(
                atom(), "some Text",
                testActions.action1() == 26
        );
    }

    public Rule skipInPredicate() {
        return sequence(
                atom(), "some Text",
                IN_PREDICATE() || testActions.action2(LAST_CHAR())
        );
    }

    public Rule simpleTernary() {
        return sequence(
                atom(), "some Text",
                testActions.action1() == 5 ? IN_PREDICATE() : testActions.action2(LAST_CHAR())
        );
    }

    public Rule upDownAction() {
        return sequence(
                atom(), "some Text",
                UP(testActions.action2(DOWN(LAST_CHAR())))
        );
    }

    public Rule twoActionsRule() {
        return sequence(
                atom(), "some Text",
                SET(actions.createAst(1.0)),
                testActions.action2(NEXT_CHAR())
        );
    }

}
