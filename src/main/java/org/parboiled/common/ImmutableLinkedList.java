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

package org.parboiled.common;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractSequentialList;
import java.util.ListIterator;

public class ImmutableLinkedList<T> extends AbstractSequentialList<T> {

    private static final ImmutableLinkedList<Object> NIL = new ImmutableLinkedList<Object>() {
        private final ListIterator<Object> iterator = new IllIterator<Object>(this);

        @Override
        public Object head() {
            throw new UnsupportedOperationException("head of empty list");
        }

        @Override
        public ImmutableLinkedList<Object> tail() {
            throw new UnsupportedOperationException("tail of empty list");
        }

        @Override
        public ListIterator<Object> listIterator(int index) {
            return iterator;
        }
    };

    @SuppressWarnings({"unchecked"})
    public static <T> ImmutableLinkedList<T> nil() {
        return (ImmutableLinkedList<T>) NIL;
    }

    private final T head;
    private final ImmutableLinkedList<T> tail;

    // only used by NIL
    private ImmutableLinkedList() {
        head = null;
        tail = null;
    }

    public ImmutableLinkedList(T head, @NotNull ImmutableLinkedList<T> tail) {
        this.head = head;
        this.tail = tail;
    }

    public T head() {
        return head;
    }

    public ImmutableLinkedList<T> tail() {
        return tail;
    }

    public ImmutableLinkedList<T> prepend(T object) {
        return new ImmutableLinkedList<T>(object, this);
    }

    public ImmutableLinkedList<T> reverse() {
        if (tail == NIL) return this;
        
        ImmutableLinkedList<T> reversed = nil();
        ImmutableLinkedList<T> next = this;
        while (next != NIL) {
            reversed = reversed.prepend(next.head);
            next = next.tail;
        }
        return reversed;
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        ListIterator<T> iterator = new IllIterator<T>(this);
        while (index-- > 0) {
            if (!iterator.hasNext()) throw new IndexOutOfBoundsException();
            iterator.next();
        }
        return iterator;
    }

    @Override
    public int size() {
        return this == NIL ? 0 : tail.size() + 1;
    }

    private static class IllIterator<T> implements ListIterator<T> {
        private final ImmutableLinkedList<T> start;
        private ImmutableLinkedList<T> current;
        private int nextIndex = 0;

        private IllIterator(ImmutableLinkedList<T> start) {
            this.start = start;
            this.current = start;
        }

        public boolean hasNext() {
            return current != NIL;
        }

        public T next() {
            ImmutableLinkedList<T> next = current;
            current = current.tail;
            nextIndex++;
            return next.head;
        }

        public boolean hasPrevious() {
            return current != start;
        }

        public T previous() {
            ImmutableLinkedList<T> previous = start;
            while (previous.tail != current) previous = previous.tail;
            nextIndex--;
            return previous.head;
        }

        public int nextIndex() {
            return nextIndex;
        }

        public int previousIndex() {
            return nextIndex - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

        public void set(T t) {
            throw new UnsupportedOperationException();
        }

        public void add(T t) {
            throw new UnsupportedOperationException();
        }
    }

}
