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

import org.parboiled.ActionResult;
import org.parboiled.BaseActions;

import java.util.List;

public class CalculatorActions extends BaseActions<Integer> {

    public ActionResult compute(Integer firstValue, List<Character> operators, List<Integer> values) {
        int value = firstValue != null ? firstValue : 0;
        for (int i = 0; i < operators.size(); i++) {
            value = performOperation(value, operators.get(i), values.get(i));
        }
        getContext().setNodeValue(value);
        return ActionResult.CONTINUE;
    }

    private int performOperation(int value1, Character operator, Integer value2) {
        if (operator == null || value2 == null) return value1;
        switch (operator) {
            case '+':
                return value1 + value2;
            case '-':
                return value1 - value2;
            case '*':
                return value1 * value2;
            case '/':
                return value1 / value2;
        }
        throw new IllegalStateException();
    }

}
