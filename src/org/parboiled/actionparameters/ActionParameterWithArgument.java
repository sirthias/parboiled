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

import org.parboiled.MatcherContext;
import static org.parboiled.actionparameters.ActionParameterUtils.verifyArgumentType;

/**
 * Specialized base type for ActionParameters that themselves take an argument.
 * @param <A> the type of the value this ActionParameter resolves to.
 */
abstract class ActionParameterWithArgument<A> extends BaseActionParameter {

    protected final Object argument;
    protected final Class argumentType;

    protected ActionParameterWithArgument(Class returnType, Object argument, Class argType) {
        super(returnType);
        this.argument = argument;
        argumentType = argType;
    }

    public void verifyReturnType(Class returnType) {
        verifyArgumentType(argument, argumentType);
        super.verifyReturnType(returnType);
    }

    @SuppressWarnings({"unchecked"})
    protected A resolveArgument(MatcherContext<?> context) {
        return (A) ActionParameterUtils.resolve(argument, context);
    }

}
