/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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
 * Interface that can be implemented by classes containing action methods.
 * If the class containing action methods implements this interface parboiled will use it to inform the
 * instance of the current context, immediately before an action call.
 */
public interface ContextAware<V> {

    /**
     * Called immediately before any parser action method invocation. Informs the object containing the
     * action about the context to be used for the coming action call.
     *
     * @param context the context
     */
    void setContext(Context<V> context);

}
