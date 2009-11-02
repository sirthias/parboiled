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
import static org.parboiled.actionparameters.ActionParameterUtils.verifyArgumentType;
import static org.parboiled.actionparameters.ActionParameterUtils.maskNull;
import org.parboiled.support.Checks;

public class UpParameter implements ActionParameter {

    protected final Object argument;

    public UpParameter(Object argument) {
        this.argument = argument;
    }

    public void verifyReturnType(Class returnType) {
        verifyArgumentType(argument, returnType);
    }

    @SuppressWarnings({"ConstantConditions"})
    public Object resolve(@NotNull MatcherContext<?> context) {
        MatcherContext<?> parentContext = context.getParent();
        Checks.ensure(parentContext != null, "Illegal up() call, already at root level");
        return maskNull(ActionParameterUtils.resolve(argument, parentContext));
    }

}