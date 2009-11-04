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
import org.parboiled.support.Checks;

/**
 * An ActionParameter that simply evaluates to null.
 */
public class NullParameter implements ActionParameter {

    public void verifyReturnType(Class<?> returnType) {
        Checks.ensure(!returnType.isPrimitive(), "Illegal argument type, cannot use NULL() for primitive types");
    }

    public Object resolve(@NotNull MatcherContext<?> context) {
        return null;
    }

    @Override
    public String toString() {
        return "null";
    }

}