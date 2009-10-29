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

import net.sf.cglib.proxy.MethodProxy;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Characters;
import org.parboiled.support.ParsingException;
import org.parboiled.utils.ImmutableList;
import org.parboiled.utils.Preconditions;

import java.util.List;

/**
 * A Matcher that not actually matches input but rather invoking parser actions.
 */
class ActionMatcher<V> extends AbstractRule<Matcher<V>> implements Matcher<V>, ActionResult {

    private final Actions<V> actionsObject;
    private final MethodProxy methodProxy;
    private final Object[] methodArguments;

    public ActionMatcher(Actions<V> actionsObject, MethodProxy methodProxy, Object[] methodArguments) {
        super(ImmutableList.<Matcher<V>>of());
        this.actionsObject = actionsObject;
        this.methodProxy = methodProxy;
        this.methodArguments = methodArguments;
    }

    public Matcher toMatcher() {
        return this;
    }

    public String getLabel() {
        return "action '" + methodProxy.getSignature().getName() + '\'';
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        context = context.getParent(); // actions want to operate in the parent scope
        Object result;
        try {
            actionsObject.setContext(context);
            result = methodProxy.invokeSuper(actionsObject, buildArguments(context));
        } catch (ParsingException pex) {
            context.addError(pex.getMessage());
            return false;
        } catch (Throwable e) {
            throw new RuntimeException("Error during execution of " + context.getPath(), e);
        }
        Preconditions.checkState(result instanceof ActionResult);
        return result == ActionResult.CONTINUE;
    }

    private Object[] buildArguments(@NotNull MatcherContext context) {
        Object[] args = new Object[methodArguments.length];
        for (int i = 0; i < methodArguments.length; i++) {
            Object methodArgument = methodArguments[i];
            if (methodArgument instanceof ActionParameter) {
                methodArgument = ((ActionParameter) methodArgument).getValue(context);
            } else if (methodArgument == BaseParser.NULL) {
                methodArgument = null;
            }
            args[i] = methodArgument;
        }
        return args;
    }

    @NotNull
    public List<Matcher<V>> getChildren() {
        return ImmutableList.of();
    }

    public Characters getStarterChars() {
        return Characters.ONLY_EMPTY;
    }

    public String getExpectedString() {
        return "successful execution of " + getLabel();
    }

}
