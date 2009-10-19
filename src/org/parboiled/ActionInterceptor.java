package org.parboiled;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.parboiled.support.Checks;
import org.parboiled.utils.Preconditions;

import java.lang.reflect.Method;

/**
 * This interceptor intercepts all action method calls to the parser action object during the parser construction
 * phase. It creates the respective ActionMatcher for the call and returns it.
 */
class ActionInterceptor implements MethodInterceptor {

    private BaseParser parser;

    protected void setParser(BaseParser parser) {
        this.parser = parser;
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Preconditions.checkState(obj instanceof Actions);
        Checks.ensure(parser != null,
                "Illegal action method call, action methods can only be invoked during parser construction");

        // build real arguments by replacing null values with respective parameter objects from the parser
        Class<?>[] parameterTypes = method.getParameterTypes();
        ActionParameter[] params = parser.retrieveAndClearActionParameters();
        Object[] realArgs = new Object[args.length];
        int j = 0;
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                Checks.ensure(params.length > j, "Illegal argument list for action '%': " +
                        "null values are not allowed! (Please use BaseParser.NULL)", proxy.getSignature().getName());
                ActionParameter param = params[j++];
                param.setExpectedType(parameterTypes[i]);
                arg = param;
            }
            realArgs[i] = arg;
        }
        Preconditions.checkState(j == params.length);

        return new ActionMatcher((Actions) obj, proxy, realArgs);
    }

}
