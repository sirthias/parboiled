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

package org.parboiled;

/**
 * Convenience base class for parser actions. Provides a base implementation of the Actions interface as well as
 * some commonly used convenience methods.
 */
public class ActionsImpl<V> implements Actions<V> {

    private Context<V> context;

    public Context<V> getContext() {
        return context;
    }

    public void setContext(Context<V> context) {
        this.context = context;
    }

    /**
     * Sets the value of the parse tree node to be created for the current rule to the given object.
     *
     * @param value the object to be set as value object
     * @return ActionResult.CONTINUE
     */
    public ActionResult setValue(V value) {
        getContext().setNodeValue(value);
        return ActionResult.CONTINUE;
    }

}
