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

package org.parboiled.common;

/**
 * A simple container holding a reference to another object.
 *
 * @param <T>
 */
public class Reference<T> {

    private T target;

    /**
     * Create a new Reference with a null target.
     */
    public Reference() {
    }

    /**
     * Create a new Reference to the given target.
     *
     * @param target the target object
     */
    public Reference(T target) {
        this.target = target;
    }

    /**
     * Sets the target to null.
     *
     * @return true
     */
    public boolean clear() {
        return set(null);
    }

    /**
     * Retrieves the previously set target.
     *
     * @return the target
     */
    public T get() {
        return target;
    }

    /**
     * Retrieves the previously set target instance and clears this reference.
     *
     * @return the target
     */
    public T getAndClear() {
        T t = target;
        clear();
        return t;
    }

    /**
     * Sets the references target object to the given instance.
     *
     * @param target the target
     * @return true
     */
    public boolean set(T target) {
        this.target = target;
        return true;
    }

    /**
     * @return true if this Reference holds a non-null target
     */
    public boolean isSet() {
        return target != null;
    }

    /**
     * @return true if this Reference is cleared
     */
    public boolean isNotSet() {
        return target != null;
    }

}
