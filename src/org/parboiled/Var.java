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

package org.parboiled;

import org.parboiled.common.Reference;

import java.util.LinkedList;

public class Var<T> extends Reference<T> {

    private LinkedList<T> stack;
    private int level;
    private String name;

    /**
     * Initializes a new Var with a null value.
     */
    public Var() {
    }

    /**
     * Initializes a new Var with the given value.
     *
     * @param value the value
     */
    public Var(T value) {
        super(value);
    }

    /**
     * Gets the name of this Var.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Var.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the current frame level of this variable, the very first level corresponding to zero.
     *
     * @return the current level
     */
    public int getLevel() {
        return level;
    }

    /**
     * Provides a new frame for the variable.
     * Potentially existing previous frames are saved.
     * Normally you do not have to call this method manually as parboiled provides for automatic Var frame management.
     */
    public void enterFrame() {
        if (level++ > 0) {
            if (stack == null) stack = new LinkedList<T>();
            stack.add(getAndClear());
        }
    }

    /**
     * Exits a frame previously entered with {@link #enterFrame()}.
     * Normally you do not have to call this method manually as parboiled provides for automatic Var frame management.
     */
    public void exitFrame() {
        if (--level > 0) {
            set(stack.removeLast());
        }
    }

    @Override
    public String toString() {
        return name != null ? name : super.toString();
    }
    
}
