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

import org.jetbrains.annotations.NotNull;
import org.parboiled.Action;
import org.parboiled.ActionResult;
import org.parboiled.Actions;
import org.parboiled.MatcherContext;
import org.parboiled.support.ParsingException;
import org.parboiled.support.SkipInPredicates;

/**
 * An ActionParameter that wraps an Action object.
 */
public class ActionObjectParameter implements ActionParameter {
    private final Action actionObject;
    private final Actions actionsObject;
    private final boolean skipInPredicates;

    public ActionObjectParameter(@NotNull Action action) {
        this.actionObject = action;
        this.actionsObject = action instanceof Actions ? (Actions) action : null;
        try {
            Class<? extends Action> clazz = action.getClass();
            skipInPredicates = clazz.getAnnotation(SkipInPredicates.class) != null ||
                    clazz.getMethod("run").getAnnotation(SkipInPredicates.class) != null;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException();
        }
    }

    @SuppressWarnings({"unchecked"})
    public Object resolve(@NotNull MatcherContext<?> context) {
        try {
            if (skipInPredicates && context.inPredicate()) return ActionResult.CONTINUE;
            if (actionsObject != null) actionsObject.setContext(context);
            return actionObject.run();
        } catch (ParsingException pex) {
            context.addError(pex.getMessage());
            return null;
        } catch (Throwable e) {
            throw new RuntimeException("Error during execution of " + context.getPath(), e);
        }
    }

    @Override
    public String toString() {
        return "action " + actionObject.getClass().getName();
    }

}