/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled.transform;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.Capture;
import org.parboiled.Context;
import org.parboiled.ContextAware;
import org.parboiled.Rule;
import org.parboiled.errors.ParserRuntimeException;

/**
 * Base class of generated classes wrapping capture expressions.
 *
 * @param <V> the type of the value field of a parse tree node
 */
@SuppressWarnings({"UnusedDeclaration"})
public abstract class BaseCapture<V> extends BaseGroupClass<V> implements Capture, ContextAware {

    public Rule contextRule;

    protected BaseCapture(@NotNull String name) {
        super(name);
    }

    @SuppressWarnings({"unchecked"})
    public void setContext(@NotNull Context c) {
        // find the context of the captured expression somewhere up in the current context stack
        Preconditions.checkState(contextRule != null);
        context = c;
        while (context.getMatcher() != contextRule) {
            context = context.getParent();
            if (context == null) {
                throw new ParserRuntimeException("Illegal capture evaluation in '%s': " +
                        "Captured context could not be found", this);
            }
        }
    }

    @SuppressWarnings({"unchecked"})
    public Object get(@NotNull Context context) {
        setContext(context);
        return get();
    }

    protected void checkContext() {
        if (context == null) {
            throw new ParserRuntimeException("Illegal capture evaluation in '%s': " +
                    "Outside of action expressions you have to use the get(Context) overload", this);
        }
    }
}