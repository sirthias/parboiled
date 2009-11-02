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

package org.parboiled.actionparameters;

import net.sf.cglib.core.Signature;
import net.sf.cglib.proxy.MethodProxy;
import org.jetbrains.annotations.NotNull;
import org.parboiled.Actions;
import org.parboiled.MatcherContext;
import static org.parboiled.actionparameters.ActionParameterUtils.maskNull;
import static org.parboiled.actionparameters.ActionParameterUtils.verifyArgumentType;
import org.parboiled.support.ParsingException;

import java.lang.reflect.Method;

public class ActionCallParameter extends BaseActionParameter {
    private final Actions actionsObject;
    private final Method method;
    private final MethodProxy proxy;
    private final Object[] args;

    public ActionCallParameter(@NotNull Actions actions, @NotNull Method method, @NotNull Object[] params,
                               @NotNull MethodProxy proxy) {
        super(method.getReturnType());
        this.actionsObject = actions;
        this.method = method;
        this.args = params;
        this.proxy = proxy;
    }

    public void verifyReturnType(Class returnType) {
        for (int i = 0; i < args.length; i++) {
            Class argType = method.getParameterTypes()[i];
            verifyArgumentType(args[i], argType);
        }
        super.verifyReturnType(returnType);
    }

    @SuppressWarnings({"unchecked"})
    public Object resolve(@NotNull MatcherContext<?> context) {
        try {
            actionsObject.setContext(context);
            Object[] resolvedArgs = ActionParameterUtils.resolve(args, context);
            Object result = proxy.invokeSuper(actionsObject, resolvedArgs);
            return maskNull(result);
        } catch (ParsingException pex) {
            context.addError(pex.getMessage());
            return null;
        } catch (Throwable e) {
            throw new RuntimeException("Error during execution of " + context.getPath(), e);
        }
    }

    public String getMethodName() {
        return method.getName();
    }

}
