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
import org.parboiled.actionparameters.ActionCallParameter;
import static org.parboiled.actionparameters.ActionParameterUtils.mixInParameters;
import org.parboiled.common.Preconditions;

import java.lang.reflect.Method;

/**
 * This interceptor intercepts all action method calls to the parser action object.
 * In the rule creation phase it creates an ActionCallParameter for the call. Later, during the parsing run, it
 * simply delegates to the underlying method on the parser action object.
 */
class ActionInterceptor implements MethodInterceptor {

    private BaseParser parser;

    protected void setParser(BaseParser parser) {
        this.parser = parser;
    }

    public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Preconditions.checkState(obj instanceof Actions<?>);

        // if we have a non-null parser object set we are in the rule construction phase and do not actually invoke
        // the action method but construct an ActionMatcher
        // however, actions might also be called by other actions during the parsing run (i.e. after rule construction)
        // in this case the parser object will have been set to null and we should directly invoke the action method
        if (parser != null) {
            return newActionCallParameter(obj, method, args, proxy);
        }
        return proxy.invokeSuper(obj, args);
    }

    @SuppressWarnings({"unchecked"})
    private Object newActionCallParameter(Object obj, Method method, Object[] args, MethodProxy proxy) {
        Object[] params = mixInParameters(parser.actionParameters, args);
        ActionCallParameter actionCall = new ActionCallParameter((Actions) obj, method, params, proxy);
        parser.actionParameters.add(actionCall);
        return null;
    }

}
