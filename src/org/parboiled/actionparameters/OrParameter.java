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
import org.parboiled.common.StringUtils;

/**
 * A special ActionParameter that returns ActionResult.CONTINUE as soon as the first inner action returns
 * anything but ActionResult.CANCEL_MATCH.
 */
public class OrParameter implements ActionParameter {

    private final Object[] arguments;

    public OrParameter(Object[] arguments) {
        this.arguments = arguments;
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        for (Object argument : arguments) {
            if (ActionParameterUtils.resolve(argument, context, ActionResult.class) != ActionResult.CANCEL_MATCH) {
                return ActionResult.CONTINUE;
            }
        }
        return ActionResult.CANCEL_MATCH;
    }

    @Override
    public String toString() {
        return "OR(" + StringUtils.join(arguments, ", ") + ')';
    }

}