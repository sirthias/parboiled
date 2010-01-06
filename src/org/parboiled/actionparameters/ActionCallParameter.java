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

import net.sf.cglib.proxy.MethodProxy;
import org.jetbrains.annotations.NotNull;
import org.parboiled.Actions;
import org.parboiled.MatcherContext;
import org.parboiled.ActionResult;
import org.parboiled.common.StringUtils;
import org.parboiled.support.ParsingException;
import org.parboiled.support.SkipInPredicates;

import java.lang.reflect.Method;

/**
 * An ActionParameter that wraps a method call on the parser actions object.
 */
public class ActionCallParameter implements ActionParameter {
    private final Actions actionsObject;
    private final Method method;
    private final MethodProxy proxy;
    private final Object[] args;
    private final Class<?>[] parameterTypes;
    private final boolean skipInPredicates;

    public ActionCallParameter(@NotNull Actions actions, @NotNull Method method, @NotNull Object[] params,
                               @NotNull MethodProxy proxy) {
        this.actionsObject = actions;
        this.method = method;
        this.args = params;
        this.proxy = proxy;
        this.parameterTypes = method.getParameterTypes();
        this.skipInPredicates = method.getAnnotation(SkipInPredicates.class) != null;
    }

    @SuppressWarnings({"unchecked"})
    public Object resolve(@NotNull MatcherContext<?> context) {
        try {
            if (skipInPredicates && context.inPredicate()) return ActionResult.CONTINUE;
            actionsObject.setContext(context);
            Object[] resolvedArgs = ActionParameterUtils.resolve(args, context, parameterTypes);
            return proxy.invokeSuper(actionsObject, resolvedArgs);
        } catch (ParsingException pex) {
            context.addError(pex.getMessage());
            return null;
        } catch (Throwable e) {
            throw new RuntimeException("Error during execution of " + context.getPath(), e);
        }
    }

    @Override
    public String toString() {
        return new StringBuilder("action ")
                .append(method.getName())
                .append('(')
                .append(StringUtils.join(args, ", "))
                .append(')')
                .toString();
    }

}
