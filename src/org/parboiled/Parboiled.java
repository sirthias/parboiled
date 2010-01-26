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

import org.jetbrains.annotations.NotNull;
import org.parboiled.asm.ParserTransformer;
import static org.parboiled.common.Utils.findConstructor;

import java.lang.reflect.Constructor;

/**
 * Main class providing the high-level entrypoints into the parboiled library.
 */
public class Parboiled {

    protected Parboiled() {}

    /**
     * Creates a parser object whose rule creation methods can then be used with the
     * {@link BaseParser#parse(Rule, String)} method.
     * Since parboiled needs to extend your parser with certain extra logic (e.g. to prevent infinite recursions
     * in recursive rule definitions) you cannot create your parser object yourself, but have to go through this method.
     * Also your parser class has to be derived from BaseParser. If you want to use a non-default constructor you also
     * have to provide its arguments to this method.
     *
     * @param parserClass     the type of the parser to create
     * @param constructorArgs optional arguments to the parser class constructor
     * @return the ready to use parser instance
     */
    @SuppressWarnings({"unchecked"})
    public static <P extends BaseParser<V>, V> P createParser(@NotNull Class<P> parserClass,
                                                              Object... constructorArgs) {
        try {
            Class<?> extendedClass = ParserTransformer.transformParser(parserClass);
            Constructor constructor = findConstructor(extendedClass, constructorArgs);
            return (P) constructor.newInstance(constructorArgs);
        } catch (Exception e) {
            throw new RuntimeException("Error creating extended parser class: " + e.getMessage(), e);
        }
    }

}
