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
 * Instances of classes implementing this interface can be used directly in a rule definition to define a parser action.
 * If the class also implements the {@link ContextAware} interface this will be used to inform the object of the
 * current parsing {@link Context} immediately before the invocation of the {@link #run} method.
 */
public interface Action {

    /**
     * Runs the parser action.
     *
     * @return true if the parsing process is to proceed, false if the current rule is to fail
     */
    boolean run();

}
