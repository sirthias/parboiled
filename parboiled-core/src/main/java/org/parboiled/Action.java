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
 * Instances of classes implementing this interface can be used directly in a rule definition to define a parser action.
 * If the class also implements the {@link org.parboiled.ContextAware} interface it will be used to inform the object of the
 * current parsing {@link org.parboiled.Context} immediately before the invocation of the {@link #run} method.
 * Additionally, if the class implementing this interface is an inner class (anonymous or not) and its outer class(es)
 * implement(s) {@link org.parboiled.ContextAware} its outer class(es) will also be informed object of the current parsing {@link org.parboiled.Context}
 * immediately before the invocation of the actions {@link #run} method.
 * This allows simple anonymous action class implementations directly in the parser rule definitions, even when
 * they access context-sensitive methods defined in the BaseActions or BaseParser classes.
 */
public interface Action<V> {

    /**
     * Runs the parser action.
     *
     * @param context the current parsing context
     * @return true if the parsing process is to proceed, false if the current rule is to fail
     */
    boolean run(Context<V> context);

}
