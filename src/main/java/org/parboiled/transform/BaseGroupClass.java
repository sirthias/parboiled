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

import org.jetbrains.annotations.NotNull;
import org.parboiled.Context;
import org.parboiled.support.Checks;

@SuppressWarnings({"UnusedDeclaration"})
public abstract class BaseGroupClass {

    public final String name;

    protected BaseGroupClass(@NotNull String name) {
        this.name = name;
    }

    protected final Context UP(Context context) {
        Context parentContext = context.getParent();
        Checks.ensure(parentContext != null, "Illegal UP() call in '%s', already at root level", this);
        return parentContext;
    }

    protected final Context UP2(Context context) {
        return UP(UP(context));
    }

    protected final Context UP3(Context context) {
        return UP(UP(UP(context)));
    }

    protected final Context UP4(Context context) {
        return UP(UP(UP(UP(context))));
    }

    protected final Context UP5(Context context) {
        return UP(UP(UP(UP(UP(context)))));
    }

    protected final Context UP6(Context context) {
        return UP(UP(UP(UP(UP(UP(context))))));
    }

    protected final Context DOWN(Context context) {
        Context subContext = context.getSubContext();
        Checks.ensure(subContext != null, "Illegal DOWN() call in '%s', already at leaf level", this);
        return subContext;
    }

    protected final Context DOWN2(Context context) {
        return DOWN(DOWN(context));
    }

    protected final Context DOWN3(Context context) {
        return DOWN(DOWN(DOWN(context)));
    }

    protected final Context DOWN4(Context context) {
        return DOWN(DOWN(DOWN(DOWN(context))));
    }

    protected final Context DOWN5(Context context) {
        return DOWN(DOWN(DOWN(DOWN(DOWN(context)))));
    }

    protected final Context DOWN6(Context context) {
        return DOWN(DOWN(DOWN(DOWN(DOWN(DOWN(context))))));
    }

    @Override
    public String toString() {
        return name;
    }

}