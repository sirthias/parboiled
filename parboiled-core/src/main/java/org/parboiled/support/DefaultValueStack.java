/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import static org.parboiled.common.Preconditions.*;
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
public class DefaultValueStack<V> implements ValueStack<V> {

    protected static class Element {
        protected final Object value;
        protected final Element tail;

        protected Element(Object value, Element tail) {
            this.value = value;
            this.tail = tail;
        }
    }

    protected Element head;
    protected V tempValue;

    /**
     * Initializes an empty value stack.
     */
    public DefaultValueStack() {
    }

    /**
     * Initializes a value stack containing the given values with the last value being at the top of the stack.
     *
     * @param values the initial stack values
     */
    public DefaultValueStack(Iterable<V> values) {
        pushAll(values);
    }

    public boolean isEmpty() {
        return head == null;
    }

    public int size() {
        Element cursor = head;
        int size = 0;
        while (cursor != null) {
            size++;
            cursor = cursor.tail;
        }
        return size;
    }

    public void clear() {
        head = null;
    }

    public Object takeSnapshot() {
        return head;
    }

    public void restoreSnapshot(Object snapshot) {
        try {
            head = (Element) snapshot;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Given argument '" + snapshot + "' is not a valid snapshot element");
        }
    }

    public void push(V value) {
        head = new Element(value, head);
    }

    public void push(int down, V value) {
        head = push(down, value, head);
    }

    private static Element push(int down, Object value, Element head) {
        if (down == 0) return new Element(value, head);
        checkArgument(head != null, "Cannot push beyond the bottom of the stack");
        if (down > 0) return new Element(head.value, push(down - 1, value, head.tail));
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    public void pushAll(V firstValue, V... moreValues) {
        push(firstValue);
        for (V value : moreValues) push(value);
    }

    public void pushAll(Iterable<V> values) {
        head = null;
        for (V value : values) push(value);
    }

    public V pop() {
        return pop(0);
    }

    public V pop(int down) {
        head = pop(down, head);
        V result = tempValue;
        tempValue = null; // avoid memory leak
        return result;
    }

    @SuppressWarnings("unchecked")
    private Element pop(int down, Element head) {
        checkArgument(head != null, "Cannot pop from beyond the bottom of the stack");
        if (down == 0) {
            tempValue = (V) head.value;
            return head.tail;
        }
        if (down > 0) return new Element(head.value, pop(down - 1, head.tail));
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    public V peek() {
        return peek(0);
    }

    @SuppressWarnings({"unchecked"})
    public V peek(int down) {
        return (V) peek(down, head);
    }

    @SuppressWarnings({"ConstantConditions"})
    private static Object peek(int down, Element head) {
        checkArgument(head != null, "Cannot peek beyond the bottom of the stack");
        if (down == 0) return head.value;
        if (down > 0) return peek(down - 1, head.tail);
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    public void poke(V value) {
        poke(0, value);
    }

    public void poke(int down, V value) {
        head = poke(down, value, head);
    }

    private static Element poke(int down, Object value, Element head) {
        checkArgument(head != null, "Cannot poke beyond the bottom of the stack");
        if (down == 0) return new Element(value, head.tail);
        if (down > 0) return new Element(head.value, poke(down - 1, value, head.tail));
        throw new IllegalArgumentException("Argument 'down' must not be negative");
    }

    public void dup() {
        push(peek());
    }

    public void swap() {
        Checks.ensure(isSizeGTE(2, head), "Swap not allowed on stack with less than two elements");
        Element down1 = head.tail;
        head = new Element(down1.value, new Element(head.value, down1.tail));
    }

    public void swap3() {
        Checks.ensure(isSizeGTE(3, head), "Swap3 not allowed on stack with less than 3 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        head = new Element(down2.value, new Element(down1.value, new Element(head.value, down2.tail)));
    }

    public void swap4() {
        Checks.ensure(isSizeGTE(4, head), "Swap4 not allowed on stack with less than 4 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        Element down3 = down2.tail;
        head = new Element(down3.value, new Element(down2.value, new Element(down1.value, new Element(head.value,
                down3.tail))));
    }

    public void swap5() {
        Checks.ensure(isSizeGTE(5, head), "Swap5 not allowed on stack with less than 5 elements");
        Element down1 = head.tail;
        Element down2 = down1.tail;
        Element down3 = down2.tail;
        Element down4 = down3.tail;
        head = new Element(down4.value, new Element(down3.value, new Element(down2.value, new Element(down1.value,
                new Element(head.value, down4.tail)))));
    }

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
