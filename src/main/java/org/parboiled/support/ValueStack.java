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

package org.parboiled.support;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.parboiled.common.Reference;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ValueStack<V> {

    private static final int INITIAL_SIZE = 16;

    private Object[] array;
    private int index; // points always to the element after the last one pushed

    public ValueStack() {
        reset();
    }

    public ValueStack(V[] values) {
        reset(values);
    }

    public boolean isEmpty() {
        return index == 0;
    }

    public int size() {
        return index;
    }

    public void reset() {
        index = 0;
        array = new Object[INITIAL_SIZE];
    }

    public void reset(@NotNull V[] values) {
        array = new Object[values.length];
        System.arraycopy(values, 0, array, 0, values.length);
        index = values.length;
    }

    public int savePointer() {
        return index;
    }

    public void restorePointer(int pointer) {
        Preconditions.checkArgument(pointer <= array.length);
        index = pointer;
    }

    public void push(V value) {
        if (index == array.length) growArray();
        array[index++] = value;
    }

    public void push(int down, V value) {
        Preconditions.checkArgument(down >= 0, "Argument 'down' must not be negative");
        Preconditions.checkArgument(index > down, "Cannot push under bottom of the stack");
        if (index == array.length) growArray();
        int ix = index - down;
        if (down > 0) System.arraycopy(array, ix, array, ix + 1, index - ix);
        array[ix] = value;
        index++;
    }

    public void pushAll(V firstValue, V... values) {
        push(firstValue);
        for (V value : values) {
            push(value);
        }
    }

    private void growArray() {
        Object[] newArray = new Object[array.length * 2];
        System.arraycopy(array, 0, newArray, 0, array.length);
        array = newArray;
    }

    @SuppressWarnings({"unchecked"})
    public V pop() {
        Checks.ensure(index > 0, "Cannot pop from an empty value stack");
        return (V) array[--index];
    }

    @SuppressWarnings({"unchecked"})
    public V pop(int down) {
        Preconditions.checkArgument(down >= 0, "Argument 'down' must not be negative");
        Preconditions.checkArgument(index > down, "Cannot pop from beyond the bottom of the stack");
        int ix = --index - down;
        V value = (V) array[ix];
        if (down > 0) System.arraycopy(array, ix + 1, array, ix, index - ix);
        return value;
    }

    @SuppressWarnings({"unchecked"})
    public V peek() {
        Checks.ensure(index > 0, "Cannot peek beyond bottom of the stack ");
        return (V) array[index - 1];
    }

    @SuppressWarnings({"unchecked"})
    public V peek(int down) {
        Preconditions.checkArgument(down >= 0, "Argument 'down' must not be negative");
        Preconditions.checkArgument(index > down, "Cannot peek beyond bottom of the stack ");
        return (V) array[index - down - 1];
    }

    public void poke(V value) {
        Checks.ensure(index > 0, "Cannot poke into an empty value stack");
        array[index - 1] = value;
    }

    public void poke(int down, V value) {
        Preconditions.checkArgument(down >= 0, "Argument 'down' must not be negative");
        Preconditions.checkArgument(index > down, "Cannot poke beyond the bottom of the stack");
        array[index - down - 1] = value;
    }

    public void dup() {
        push(peek());
    }

    public void swap() {
        Checks.ensure(index >= 2, "Swap not allowed on stack with less than two elements");
        swap(1, 2);
    }

    public void swap3() {
        Checks.ensure(index >= 3, "Swap3 not allowed on stack with less than 3 elements");
        swap(1, 3);
    }

    public void swap4() {
        Checks.ensure(index >= 4, "Swap4 not allowed on stack with less than 4 elements");
        swap(1, 4);
        swap(2, 3);
    }

    public void swap5() {
        Checks.ensure(index >= 5, "Swap5 not allowed on stack with less than 5 elements");
        swap(1, 5);
        swap(2, 4);
    }

    public void swap6() {
        Checks.ensure(index >= 6, "Swap6 not allowed on stack with less than 6 elements");
        swap(1, 6);
        swap(2, 5);
        swap(3, 4);
    }

    private void swap(int a, int b) {
        Object temp = array[index - a];
        array[index - a] = array[index - b];
        array[index - b] = temp;
    }

    /**
     * Returns all stack values in an immutable list, with the deepest object at the first position.
     * @return the list of stack values
     */
    @SuppressWarnings({"unchecked"})
    public List<V> getValues() {
        return new AbstractList<V>() {
            @Override
            public V get(int ix) {
                if (0<= ix && ix < index) return (V) array[ix];
                throw new IndexOutOfBoundsException("Illegal index: " + ix);
            }
            @Override
            public int size() {
                return index;
            }
        };
    }

}
