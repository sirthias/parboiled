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
import org.parboiled.MatcherContext;
import org.parboiled.common.Preconditions;
import org.parboiled.support.Checks;

import java.util.Stack;

public class ActionParameterUtils {

    public static Object[] mixInParameters(Stack<ActionParameter> actionParameters, Object... arguments) {
        Object[] params = new Object[arguments.length];
        for (int i = params.length - 1; i >= 0; i--) {
            params[i] = mixInParameter(actionParameters, arguments[i]);
        }
        return params;
    }

    public static Object mixInParameter(Stack<ActionParameter> actionParameters, Object arg) {
        if (arg == null) {
            Checks.ensure(!actionParameters.isEmpty(),
                    "Illegal action argument: null values are not allowed! (Please use special NULL value)");
            arg = actionParameters.pop();
        }
        return arg;
    }

    /**
     * Creates and returns a new Object array with the same elements except for all ActionParameters.
     * These are resolved to their values.
     *
     * @param arguments the original arguments
     * @param context   the context in which to resolve all ActionParameters
     * @return a new Object array with all ActionParameters resolved
     */
    public static Object[] resolve(@NotNull Object[] arguments, @NotNull MatcherContext<?> context) {
        Object[] args = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            args[i] = resolve(arguments[i], context);
        }
        return args;
    }

    /**
     * If the given argument is an ActionParameter it is resolved to its value and the value returned.
     * If the given argumetn is BaseParser.NULL it is resolved to null.
     * All other arguments are returned unchanged.
     *
     * @param argument the original argument
     * @param context  the context in which to resolve a potential ActionParameter
     * @return the resolved Object
     */
    @SuppressWarnings({"unchecked"})
    public static Object resolve(Object argument, @NotNull MatcherContext<?> context) {
        if (argument instanceof ActionParameter) {
            return ((ActionParameter) argument).resolve(context);
        }
        return argument;
    }

    public static void verifyArgumentType(Object argument, Class<?> requiredArgumentType) {
        if (argument instanceof ActionParameter) {
            ((ActionParameter) argument).verifyReturnType(requiredArgumentType);
        } else {
            Preconditions.checkState(requiredArgumentType.isAssignableFrom(argument.getClass()));
        }
    }

}
