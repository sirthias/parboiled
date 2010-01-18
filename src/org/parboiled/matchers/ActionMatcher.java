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
import org.parboiled.Action;
import org.parboiled.ActionResult;
import org.parboiled.ContextAware;
import org.parboiled.MatcherContext;
import org.parboiled.exceptions.ActionException;
import org.parboiled.support.Characters;

/**
 * A Matcher that not actually matches input but rather resolves an ActionParameter in the current rule context.
 */
public class ActionMatcher<V> extends AbstractMatcher<V> {

    private final Action action;
    private final ContextAware<V> contextAwareAction;

    @SuppressWarnings({"unchecked"})
    public ActionMatcher(@NotNull Action action) {
        this.action = action;
        this.contextAwareAction = action instanceof ContextAware ? (ContextAware<V>) action : null;
    }

    public String getLabel() {
        return "run " + action.toString();
    }

    public boolean match(@NotNull MatcherContext<V> context, boolean enforced) {
        if (contextAwareAction != null) {
            contextAwareAction.setContext(context);
        }
        try {
            return action.run() != ActionResult.CANCEL_MATCH;
        } catch (ActionException e) {
            context.addError(e.getMessage());
            return false;
        }
    }

    public Characters getStarterChars() {
        return Characters.ONLY_EMPTY;
    }

}
