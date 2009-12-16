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

package org.parboiled.actionparameters;

import org.jetbrains.annotations.NotNull;
import org.parboiled.ActionResult;
import org.parboiled.MatcherContext;
import org.parboiled.common.Utils;

/**
 * A special ActionParameter that sets the value passed in as argument on the node to be created for the rule
 * corresponding to the current Context scope.
 */
public class EqualsParameter extends BaseActionParameter {

    private final Object a;
    private final Object b;

    public EqualsParameter(Object a, Object b) {
        super(ActionResult.class);
        this.a = a;
        this.b = b;
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        Object resolvedA = ActionParameterUtils.resolve(a, context);
        Object resolvedB = ActionParameterUtils.resolve(b, context);
        return Utils.equals(resolvedA, resolvedB) ? ActionResult.CONTINUE : ActionResult.CANCEL_MATCH;
    }

    @Override
    public String toString() {
        return "EQUALS(" + a + ',' + b + ')';
    }

}