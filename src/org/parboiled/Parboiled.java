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

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.ParserConstructionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Main class providing the high-level entrypoints into the parboiled library.
 */
public class Parboiled {

    private Parboiled() {}

    /**
     * Creates a parser object whose rule creation methods can then be used with the
     * {@link BaseParser#parse(Rule, String)} method.
     * Since parboiled needs to extend your parser with certain extra logic (e.g. to prevent infinite recursions
     * in recursive rule definitions) you cannot create your parser object yourself, but have to go through this method.
     * Also your parser class has to be derived from BaseParser. If you want to use a non-default constructor you also
     * have to provide its arguments to this method.
     *
     * @param parserType      the type of the parser to create
     * @param constructorArgs optional arguments to the parser class constructor
     * @return the ready to use parser instance
     */
    public static <V, A extends Actions<V>, P extends BaseParser<V, A>> P createParser(@NotNull Class<P> parserType,
                                                                                       Object... constructorArgs) {
        // intercept all no-argument rule creation methods with a RuleInterceptor
        return create(parserType, new RuleInterceptor(), new CallbackFilter() {
            public int accept(Method method) {
                boolean isRuleCreatingMethod = method.getReturnType() == Rule.class;
                boolean hasNoParameters = method.getParameterTypes().length == 0;
                return isRuleCreatingMethod && hasNoParameters ? 1 : 0;
            }
        }, constructorArgs);
    }

    /**
     * Creates an action object whose methods can then be used directly in your rule definitions.
     * Since parboiled needs to extends your parser actions object with certain extra logic you cannot create your
     * actions object yourself, but have to go through this method. Still your actions object can be of any type (as
     * long as it implements the {@link org.parboiled.Actions} interface) and can define arbitrary constructors.
     * If you want to use a non-default constructor you also have to provide its arguments to this method.
     *
     * @param actionsType     the type of the action object to create
     * @param constructorArgs optional arguments to the class constructor
     * @return the actions object to be used for the parser creation
     */
    public static <A extends Actions<?>> A createActions(@NotNull Class<A> actionsType, Object... constructorArgs) {
        return create(actionsType, new ActionInterceptor(), new CallbackFilter() {
            public int accept(Method method) {
                // we intercept all methods we can properly wrap in an ActionCallParameter
                // since we do not have a common placeholder for primitives we cannot intercept methods returning
                // primitives
                return method.getReturnType().isPrimitive() ? 0 : 1;
            }
        }, constructorArgs);
    }

    @SuppressWarnings({"unchecked"})
    private static <T> T create(@NotNull Class<T> type, Callback interceptor, CallbackFilter filter,
                                Object... constructorArguments) {
        Enhancer e = new Enhancer();
        e.setSuperclass(type);
        e.setCallbackFilter(filter);
        e.setCallbacks(new Callback[] {
                NoOp.INSTANCE, // index 0: just call the underlying method directly
                interceptor    // index 1: use the Interceptor
        });
        Constructor constructor = findConstructor(type, constructorArguments);
        return (T) e.create(constructor.getParameterTypes(), constructorArguments);
    }

    private static Constructor findConstructor(Class<?> type, Object[] args) {
        outer:
        for (Constructor constructor : type.getConstructors()) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != args.length) continue;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null && !paramTypes[i].isAssignableFrom(arg.getClass())) continue outer;
                if (arg == null && paramTypes[i].isPrimitive()) continue outer;
            }
            return constructor;
        }
        throw new ParserConstructionException("No constructor found for the given arguments");
    }

}
