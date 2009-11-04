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

/**
 * An ActionParameter defines just that, a late-bound parameter to an action call.
 */
public interface ActionParameter {

    /**
     * Checks whether the return type of this ActionParameter is assignable to the given expected type.
     * If not an exception is thrown.
     * @param returnType the type that this action parameter is supposed to be assignable to
     */
    void verifyReturnType(Class<?> returnType);

    /**
     * Resolves this parameter in this given context.
     * @param context the context
     * @return the value of this parameter in the given context
     */
    Object resolve(@NotNull MatcherContext<?> context);

}
