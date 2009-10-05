package org.parboiled;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.LazyLoader;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Method;
import java.util.Map;

class StagingInterceptor implements MethodInterceptor {

    private final Map<Method, Rule> rules = Maps.newHashMap();

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Preconditions.checkState(obj instanceof BaseParser);
        Preconditions.checkState(args.length == 0);

        // create/get the underlying AbstractRule or proxy
        Rule innerRule = getRule(obj, method, proxy);

        // every call to the method receives its own StagingRule to set properties on (if required)
        return new StagingRule(innerRule, (BaseParser<?>) obj);
    }

    private Rule getRule(Object obj, Method method, MethodProxy methodProxy) throws Throwable {
        Rule rule = rules.get(method);
        if (rule != null) return rule;

        // first call to the Rule, create a proxy and put it into the rule cache so any potential
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
        realRule[0] = (AbstractRule) methodProxy.invokeSuper(obj, ArrayUtils.EMPTY_OBJECT_ARRAY);
        realRule[0].label(method.getName());
        realRule[0].lock();

        // finally we put the actual rule into the cache overwriting the previously put proxy
        // so the proxy is only used by rules that recursed into the current method during their execution
        rules.put(method, realRule[0]);

        return realRule[0];
    }

}
