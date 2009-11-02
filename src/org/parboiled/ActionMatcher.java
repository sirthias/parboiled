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

import org.jetbrains.annotations.NotNull;
import org.parboiled.actionparameters.ActionCallParameter;

/**
 * A Matcher that not actually matches input but rather invoking parser actions.
 */
class ActionMatcher<V> extends SpecialMatcher<V> implements ActionResult {

    private final ActionCallParameter actionCall;

    public ActionMatcher(ActionCallParameter actionCall) {
        this.actionCall = actionCall;
    }

    public String getLabel() {
        return "action '" + actionCall.getMethodName() + '\'';
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        context = context.getParent(); // actions want to operate in the parent scope
        Object result = actionCall.resolve(context);
        return result == ActionResult.CONTINUE;
    }

}
