/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled;

import org.jetbrains.annotations.NotNull;

/**
 * Interface implemented by objects returned by {@link BaseParser#CAPTURE(Object)} constructs.
 * Captures act like closures, they wrap an expression including its context and allow for its evaluation in another
 * place and at another time. The captures expression can be evaluated by calling one of its get methods.
 *
 * @param <T> the type of the captured expression
 */
public interface Capture<T> {

    /**
     * Evaluates the expression wrapped by this capture. This overload can only be used in action expressions.
     * If you want to evaluate the capture outside of an action expression you have to use {@link #get(Context)}.
     *
     * @return the value of the captured expression
     */
    T get();

    /**
     * Evaluates the expression wrapped by this capture. The capture uses the given context to find its own context
     * (i.e. the one it was created in). The given context passed to this method as a parameter is the one of the
     * caller of this method, e.g. the one of the action method this call is a part of.
     * If you are evaluating this Capture inside of an action expression you can use the {@link #get()} overload.
     *
     * @param context the current context
     * @return the value of the captured expression
     */
    T get(@NotNull Context<?> context);

}
