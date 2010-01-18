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

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.parboiled.common.Preconditions;
import org.parboiled.common.Utils;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.ProxyMatcher;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor for the rule creation methods of the parser rule object.
 * Provides the following functionality to the rule-creating, no-arg instance methods of the parser object:
 * - Caches rules created by such methods, so subsequent calls return the same rule instance
 * - Automatically labels all created rule objects with the name of the respective creation method
 * - Prevents infinite recursions during rule construction by inserting proxy objects where required
 */
class RuleInterceptor implements MethodInterceptor {

    private final Map<Method, Rule> rules = new HashMap<Method, Rule>();

    @SuppressWarnings({"unchecked"})
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Preconditions.checkState(obj instanceof BaseParser);
        Preconditions.checkState(args.length == 0);

        Rule rule = rules.get(method);
        if (rule != null) return rule;

        // first call to the Rule, create a proxy and put it into the rule cache so any potential
        // recursive calls don't recurse infinitely but rather immediately return the proxy
        ProxyMatcher proxyMatcher = new ProxyMatcher();
        rules.put(method, proxyMatcher);

        // call the actual rule creation method (which might recurse into itself or any ancestor)
        AbstractMatcher matcher = (AbstractMatcher) proxy.invokeSuper(obj, Utils.EMPTY_OBJECT_ARRAY);

        // label if not yet locked
        if (!matcher.isLocked()) {
            matcher.label(method.getName());
            matcher.lock(); // lock the rule to prevent any further change of this particular instance
        }

        // arm the proxy with the actual rule, so all Rules that have so far received the proxy object
        // from now on "call through" to the actual Rule
        proxyMatcher.arm(matcher);

        // finally we put the actual rule into the cache overwriting the previously put proxy
        // so the proxy is only used by rules that recursed into the current method during their execution
        rules.put(method, matcher);

        return matcher;
    }

}
