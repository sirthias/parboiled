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
public abstract class BaseGroupClass<V> {

    public final String name;
    protected Context<V> context;

    protected BaseGroupClass(@NotNull String name) {
        this.name = name;
    }

    protected final void UP() {
        Context<V> parentContext = context.getParent();
        Checks.ensure(parentContext != null, "Illegal UP() call in '%s', already at root level", this);
        this.context = parentContext;
    }

    protected final void UP2() {
        UP();
        UP();
    }

    protected final void UP3() {
        UP();
        UP();
        UP();
    }

    protected final void UP4() {
        UP();
        UP();
        UP();
        UP();
    }

    protected final void UP5() {
        UP();
        UP();
        UP();
        UP();
        UP();
    }

    protected final void UP6() {
        UP();
        UP();
        UP();
        UP();
        UP();
        UP();
    }

    protected final void DOWN() {
        Context<V> subContext = context.getSubContext();
        Checks.ensure(subContext != null, "Illegal DOWN() call in '%s', already at leaf level", this);
        this.context = subContext;
    }

    protected final void DOWN2() {
        DOWN();
        DOWN();
    }

    protected final void DOWN3() {
        DOWN();
        DOWN();
        DOWN();
    }

    protected final void DOWN4() {
        DOWN();
        DOWN();
        DOWN();
        DOWN();
    }

    protected final void DOWN5() {
        DOWN();
        DOWN();
        DOWN();
        DOWN();
        DOWN();
    }

    protected final void DOWN6() {
        DOWN();
        DOWN();
        DOWN();
        DOWN();
        DOWN();
        DOWN();
    }

    @Override
    public String toString() {
        return name;
    }

}