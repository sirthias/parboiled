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

import org.parboiled.common.Preconditions;

/**
 * Base implementation of an ActionParameter that checks for compatibility with the given return type.
 */
abstract class BaseActionParameter implements ActionParameter {

    protected final Class<?> returnType;

    public BaseActionParameter(Class returnType) {
        this.returnType = returnType;
    }

    public void verifyReturnType(Class<?> returnType) {
        Preconditions.checkState(returnType.isAssignableFrom(this.returnType), "Illegal action parameter type");
    }

}