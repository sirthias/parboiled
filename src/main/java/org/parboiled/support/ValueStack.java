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

import java.util.Iterator;

/**
 * An implementation of a stack of value objects providing an efficient snapshot capability and a number of convenience
 * methods. The current state of the stack can be saved and restored in small constant time with the methods
 * {@link #takeSnapshot()} and {@link #restoreSnapshot(Object)} ()}. The implementation also serves as an Iterable
 * over the current stack values (the values are being provided with the last value (on top of the stack) first).
 *
 * @param <V> the type of the value objects
 */
@SuppressWarnings({"ConstantConditions"})
public class ValueStack<V> implements Iterable<V> {

    private static class Element {
        private final Object value;
        private final Element tail;

        private Element(Object value, Element tail) {
            this.value = value;
            this.tail = tail;
        }
    }

    private Element head;
    private V tempValue;

    /**
     * Initializes an empty value stack.
     */
    public ValueStack() {
    }

    /**
     * Initializes a value stack containing the given values with the last value being at the top of the stack.
     *
     * @param values the initial stack values
     */
    public ValueStack(Iterable<V> values) {
        pushAll(values);
    }

    /**
     * Determines whether the stack is empty.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return head == null;
    }

    /**
     * Returns the number of elements currently on the stack.
     *
     * @return the number of elements
     */
    public int size() {
        return size(head);
    }

    private static int size(Element head) {
        return head == null ? 0 : size(head.tail) + 1;
    }

    /**
     * Clears all values.
     */
    public void clear() {
        head = null;
    }

    /**
     * Returns an object representing the current state of the stack.
     * This cost of running this operation is negligible and independent from the size of the stack.
     *
     * @return an object representing the current state of the stack
     */
    public Object takeSnapshot() {
        return head;
    }

    /**
     * Restores the stack state as previously returned by {@link #takeSnapshot()}.
     * This cost of running this operation is negligible and independent from the size of the stack.
     *
     * @param snapshot a snapshot object previously returned by {@link #takeSnapshot()}
     */
    public void restoreSnapshot(Object snapshot) {
        try {
            head = (Element) snapshot;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Given argument '" + snapshot + "' is not a valid snapshot element");
        }
    }

    /**
     * Pushes the given value onto the stack. Equivalent to push(0, value).
     *
     * @param value the value
     */
    public void push(V value) {
        push(0, value);
    }

    /**
     * Inserts the given value a given number of elements below the current top of the stack.
     *
     * @param down  the number of elements to skip before inserting the value (0 being equivalent to push(value))
     * @param value the value
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public void push(int down, V value) {
        head = push(down, value, head);
    }

    private static Element push(int down, Object value, Element head) {
        if (down == 0) return new Element(value, head);
        Preconditions.checkArgument(head != null, "Cannot push beyond the bottom of the stack");
        if (down > 0) return new Element(head.value, push(down - 1, value, head.tail));
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    /**
     * Pushes all given elements onto the stack (in the order as given).
     *
     * @param firstValue the first value
     * @param moreValues the other values
     */
    public void pushAll(V firstValue, V... moreValues) {
        push(firstValue);
        for (V value : moreValues) push(value);
    }

    /**
     * Pushes all given elements onto the stack (in the order as given).
     *
     * @param values the values
     */
    public void pushAll(Iterable<V> values) {
        head = null;
        for (V value : values) push(value);
    }

    /**
     * Removes the value at the top of the stack and returns it.
     *
     * @return the current top value
     * @throws IllegalArgumentException if the stack is empty
     */
    public V pop() {
        return pop(0);
    }

    /**
     * Removes the value the given number of elements below the top of the stack.
     *
     * @param down the number of elements to skip before removing the value (0 being equivalent to pop())
     * @return the value
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public V pop(int down) {
        head = pop(down, head);
        return tempValue;
    }

    @SuppressWarnings("unchecked")
    private Element pop(int down, Element head) {
        Preconditions.checkArgument(head != null, "Cannot pop from beyond the bottom of the stack");
        if (down == 0) {
            tempValue = (V) head.value;
            return head.tail;
        }
        if (down > 0) return new Element(head.value, pop(down - 1, head.tail));
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    /**
     * Returns the value at the top of the stack without removing it.
     *
     * @return the current top value
     * @throws IllegalArgumentException if the stack is empty
     */
    public V peek() {
        return peek(0);
    }

    /**
     * Returns the value the given number of elements below the top of the stack without removing it.
     *
     * @param down the number of elements to skip (0 being equivalent to peek())
     * @return the value
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    @SuppressWarnings({"unchecked"})
    public V peek(int down) {
        return (V) peek(down, head);
    }

    @SuppressWarnings({"ConstantConditions"})
    private static Object peek(int down, Element head) {
        Preconditions.checkArgument(head != null, "Cannot peek beyond the bottom of the stack");
        if (down == 0) return head.value;
        if (down > 0) return peek(down - 1, head.tail);
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    /**
     * Replaces the current top value with the given value. Equivalent to poke(0, value).
     *
     * @param value the value
     * @throws IllegalArgumentException if the stack is empty
     */
    public void poke(V value) {
        poke(0, value);
    }

    /**
     * Replaces the element the given number of elements below the current top of the stack.
     *
     * @param down  the number of elements to skip before replacing the value (0 being equivalent to poke(value))
     * @param value the value to replace with
     * @throws IllegalArgumentException if the stack does not contain enough elements to perform this operation
     */
    public void poke(int down, V value) {
        head = poke(down, value, head);
    }

    private static Element poke(int down, Object value, Element head) {
        Preconditions.checkArgument(head != null, "Cannot poke beyond the bottom of the stack");
        if (down == 0) return new Element(value, head.tail);
        if (down > 0) return new Element(head.value, poke(down - 1, value, head.tail));
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    /**
     * Duplicates the top value. Equivalent to push(peek()).
     *
     * @throws IllegalArgumentException if the stack is empty
     */
    public void dup() {
        push(peek());
    }

    /**
     * Swaps the top two stack values.
     *
     * @throws org.parboiled.errors.GrammarException if the stack does not contain at least two elements
     */
    public void swap() {
        Checks.ensure(isSizeGTE(2, head), "Swap not allowed on stack with less than two elements");
        Element down1 = head.tail;
        head = new Element(down1.value, new Element(head.value, down1.tail));
    }

    /**
     * Reverses the order of the top 3 stack values.
     *
     * @throws org.parboiled.errors.GrammarException if the stack does not contain at least 3 elements
     */
    public void swap3() {
        Checks.ensure(isSizeGTE(3, head), "Swap3 not allowed on stack with less than 3 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        head = new Element(down2.value, new Element(down1.value, new Element(head.value, down2.tail)));
    }

    /**
     * Reverses the order of the top 4 stack values.
     *
     * @throws org.parboiled.errors.GrammarException if the stack does not contain at least 4 elements
     */
    public void swap4() {
        Checks.ensure(isSizeGTE(4, head), "Swap4 not allowed on stack with less than 4 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        Element down3 = down2.tail;
        head = new Element(down3.value, new Element(down2.value, new Element(down1.value, new Element(head.value,
                down3.tail))));
    }

    /**
     * Reverses the order of the top 5 stack values.
     *
     * @throws org.parboiled.errors.GrammarException if the stack does not contain at least 5 elements
     */
    public void swap5() {
        Checks.ensure(isSizeGTE(5, head), "Swap5 not allowed on stack with less than 5 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        Element down3 = down2.tail;
        Element down4 = down3.tail;
        head = new Element(down4.value, new Element(down3.value, new Element(down2.value, new Element(down1.value,
                new Element(head.value, down4.tail)))));
    }

    /**
     * Reverses the order of the top 5 stack values.
     *
     * @throws org.parboiled.errors.GrammarException if the stack does not contain at least 5 elements
     */
    public void swap6() {
        Checks.ensure(isSizeGTE(6, head), "Swap6 not allowed on stack with less than 6 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        Element down3 = down2.tail;
        Element down4 = down3.tail;
        Element down5 = down4.tail;
        head = new Element(down5.value, new Element(down4.value, new Element(down3.value, new Element(down2.value,
                new Element(down1.value, new Element(head.value, down5.tail))))));
    }

    private static boolean isSizeGTE(int minSize, Element head) {
        return minSize == 1 ? head != null : isSizeGTE(minSize - 1, head.tail);
    }

    public Iterator<V> iterator() {
        return new Iterator<V>() {
            private Element next = head;
            public boolean hasNext() {
                return next != null;
            }
            @SuppressWarnings({"unchecked"})
            public V next() {
                V value = (V) next.value;
                next = next.tail;
                return value;
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
