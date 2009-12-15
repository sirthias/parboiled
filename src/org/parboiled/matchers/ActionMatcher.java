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

package org.parboiled.matchers;

import org.jetbrains.annotations.NotNull;
import org.parboiled.actionparameters.ActionParameter;
import org.parboiled.ActionResult;
import org.parboiled.MatcherContext;
import org.parboiled.support.Characters;

/**
 * A Matcher that not actually matches input but rather resolves an ActionParameter in the current rule context.
 */
public class ActionMatcher<V> extends AbstractMatcher<V> implements ActionResult {

    private final ActionParameter action;

    public ActionMatcher(ActionParameter action) {
        action.verifyReturnType(ActionResult.class);
        this.action = action;
    }

    public String getLabel() {
        return "run " + action;
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        Object result = action.resolve(context);
        return result != ActionResult.CANCEL_MATCH;
    }

    public Characters getStarterChars() {
        return Characters.ONLY_EMPTY;
    }
    
}
