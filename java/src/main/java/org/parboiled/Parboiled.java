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

import static org.parboiled.common.Preconditions.*;
import org.parboiled.transform.ParserTransformer;

import java.lang.reflect.Constructor;

import static org.parboiled.common.Utils.findConstructor;

/**
 * Main class providing the high-level entry point into the parboiled library.
 */
public class Parboiled {

    protected Parboiled() {}

    /**
     * <p>Creates a parser object whose rule creation methods can then be used with one of the {@link org.parboiled.parserunners.ParseRunner} implementations.</p>
     * <p>Since parboiled needs to extend your parser with certain extra logic (e.g. to prevent infinite recursions
     * in recursive rule definitions) you cannot create your parser object yourself, but have to go through this method.
     * Also your parser class has to be derived from {@link BaseParser}. If you want to use a non-default constructor
     * you can provide its arguments to this method. Make sure your non-default constructor does not use primitive
     * type parameters (like "int") but rather their boxed counterparts (like "Integer"), otherwise the constructor
     * will not be found.</p>
     * <p>Performing the rule analysis and extending the parser class is an expensive process (time-wise) and can
     * take up to several hundred milliseconds for large grammars. However, this cost is only incurred once per
     * parser class and class loader. Subsequent calls to this method are therefore fast once the initial extension
     * has been performed.</p>
     *
     * @param parserClass     the type of the parser to create
     * @param constructorArgs optional arguments to the parser class constructor
     * @return the ready to use parser instance
     */
    @SuppressWarnings({"unchecked"})
    public static <P extends BaseParser<V>, V> P createParser(Class<P> parserClass, Object... constructorArgs) {
        checkArgNotNull(parserClass, "parserClass");
        try {
            Class<?> extendedClass = ParserTransformer.transformParser(parserClass);
            Constructor constructor = findConstructor(extendedClass, constructorArgs);
            return (P) constructor.newInstance(constructorArgs);
        } catch (Exception e) {
            throw new RuntimeException("Error creating extended parser class: " + e.getMessage(), e);
        }
    }

}
