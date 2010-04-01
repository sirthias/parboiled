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
import org.parboiled.ContextAware;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.errors.ActionError;
import org.parboiled.errors.ActionException;
import org.parboiled.errors.GrammarException;
import org.parboiled.transform.BaseAction;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Matcher} that not actually matches input but runs a given parser {@link Action}.
 *
 * @param <V> the type of the value field of a parse tree node
 */
public class ActionMatcher<V> extends AbstractMatcher<V> {

    public final Action<V> action;
    public final List<ContextAware<V>> contextAwares = new ArrayList<ContextAware<V>>();

    @SuppressWarnings({"unchecked"})
    public ActionMatcher(@NotNull Action<V> action) {
        this.action = action;

        // Base Actions take care of their context setting need themselves, so we do not need to analyze fields, etc.
        if (!(action instanceof BaseAction)) {
            if (action instanceof ContextAware) {
                contextAwares.add((ContextAware<V>) action);
            }
            // in order to make anonymous inner classes and other member classes work seamlessly
            // we collect the synthetic references to the outer parent classes and inform them of
            // the current parsing context if they implement ContextAware
            for (Field field : action.getClass().getDeclaredFields()) {
                if (field.isSynthetic() && ContextAware.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    try {
                        ContextAware<V> contextAware = (ContextAware<V>) field.get(action);
                        if (contextAware != null) contextAwares.add(contextAware);
                    } catch (IllegalAccessException e) {
                        // ignore
                    } finally {
                        field.setAccessible(false);
                    }
                }
            }
        }
    }

    public boolean match(@NotNull MatcherContext<V> context) {
        // actions need to run in the parent context
        context = context.getParent();
        if (!contextAwares.isEmpty()) {
            for (ContextAware<V> contextAware : contextAwares) {
                contextAware.setContext(context);
            }
        }

        try {
            return action.run(context);
        } catch (ActionException e) {
            context.getParseErrors().add(
                    new ActionError<V>(context.getCurrentLocation(), e.getMessage(), context.getPath(), e));
            return false;
        }
    }

    @Override
    public Rule asLeaf() {
        throw new GrammarException("Actions cannot be leaf rules");
    }

    public <R> R accept(@NotNull MatcherVisitor<V, R> visitor) {
        return visitor.visit(this);
    }

}
