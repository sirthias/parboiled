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

import org.parboiled.BaseActions;
import org.parboiled.BaseParser;
import org.parboiled.Rule;

class SimpleActions {
    public char lastTest;

    public int return5() {
        return 5;
    }

    public boolean testX(char c) {
        lastTest = c;
        return c == 'X';
    }
}

class ContextActions extends BaseActions<Integer> {

    public int action1() {
        return 5;
    }

    public boolean action2(int row) {
        return row == 26 && getContext().getMatcher().getLabel().equals("specialSeq");
    }
}

class TestParser extends BaseParser<Integer> {
    private final SimpleActions actions = new SimpleActions();
    private final ContextActions contextActions = new ContextActions();

    public Rule noActionRule() {
        return sequence(
                'a',
                oneOrMore(
                        sequence(
                                'b',
                                optional('c')
                        )
                ).label("bsAndCs")
        );
    }

    public Rule simpleActionRule() {
        return sequence(
                'a',
                actions.testX(LAST_CHAR())
        );
    }

    public Rule upSetActionRule() {
        return sequence(
                'a',
                oneOrMore(
                        sequence(
                                'b',
                                UP(UP(SET(actions.return5()))),
                                optional('c')
                        )
                )
        );
    }

    public Rule booleanExpressionActionRule() {
        return sequence(
                'a',
                oneOrMore(
                        sequence(
                                'b',
                                optional('c')
                        )
                ),
                !IN_PREDICATE() && LAST_CHAR() == 'b'
        );
    }

    public Rule complexActionsRule() {
        return sequence(
                'a',
                oneOrMore(
                        sequence(
                                ch('b').label("b"),
                                optional('c'),
                                UP(DOWN(contextActions.action2(NODE("b").getEndLocation().row + 26))),
                                UP(SET(actions.return5()))
                        ).label("specialSeq")
                ),
                SET()
        );
    }

}
