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

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.parboiled.common.Preconditions;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.AbstractRule;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for the Rule creation methods of the parser rule object.
 * Provides the following functionality to the Rule-creating, no-arg instance methods of the parser object:
 * - Automatically locks all created Rules (which prevents further changes to their properties)
 * - Automatically labels all created Rule objects with the name of the respective creation method
 * - Caches Rules created by such methods, so subsequent calls return the same Rule instance
 * - Prevents infinite recursions during rule construction by inserting proxy objects where required
 * - Automatically injects WrapMatcher to allow for setting of custom properties for cached, locked Rules
 */
class StagingInterceptor implements MethodInterceptor {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private final Map<Method, Rule> rules = new HashMap<Method, Rule>();

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Preconditions.checkState(obj instanceof BaseParser);
        Preconditions.checkState(args.length == 0);

        // createActions/get the underlying AbstractRule or proxy
        Rule innerRule = getRule(obj, method, proxy);

        // every call to the method receives its own StagingRule to set properties on (if required)
        return new StagingRule(innerRule, (BaseParser<?,?>) obj);
    }

    private Rule getRule(Object obj, Method method, MethodProxy methodProxy) throws Throwable {
        Rule rule = rules.get(method);
        if (rule != null) return rule;

        // first call to the Rule, createActions a proxy and put it into the rule cache so any potential
        // recursive calls don't recurse infinitely but rather immediately return the proxy
        final AbstractRule[] realRule = new AbstractRule[] {null};
        Rule proxy = (Rule) Enhancer.create(Object.class, new Class[] {Rule.class, Matcher.class}, new LazyLoader() {
            public Object loadObject() throws Exception {
                Preconditions.checkState(realRule[0] != null);
                return realRule[0];
            }
        });
        rules.put(method, proxy);

        // call the actual rule creation method (which might recurse into itself or any ancestor)
        // and arm the proxy with the actual rule, so all Rules that have so far received the proxy object
        // from now on "call through" to the actual Rule
        realRule[0] = (AbstractRule) methodProxy.invokeSuper(obj, EMPTY_OBJECT_ARRAY);
        realRule[0].label(method.getName());
        realRule[0].lock();

        // finally we put the actual rule into the cache overwriting the previously put proxy
        // so the proxy is only used by rules that recursed into the current method during their execution
        rules.put(method, realRule[0]);

        return realRule[0];
    }

}
