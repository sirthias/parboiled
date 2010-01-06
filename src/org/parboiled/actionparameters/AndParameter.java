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
 * A special ActionParameter that only returns ActionResult.CONTINUE if all inner actions do not return ActionResult.CANCEL_MATCH,
 * otherwise ActionResult.CANCEL_MATCH is returned. Note that the result is short-circuited, i.e. the evaluation of
 * sub actions will stop as soon as the first ActionResult.CANCEL_MATCH is encountered.
 */
public class AndParameter implements ActionParameter {

    private final Object[] arguments;

    public AndParameter(Object[] arguments) {
        this.arguments = arguments;
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        for (Object argument : arguments) {
            if (ActionParameterUtils.resolve(argument, context, ActionResult.class) == ActionResult.CANCEL_MATCH) {
                return ActionResult.CANCEL_MATCH;
            }
        }
        return ActionResult.CONTINUE;
    }

    @Override
    public String toString() {
        return "AND(" + StringUtils.join(arguments, ", ") + ')';
    }

}