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

package org.parboiled.common;

import java.util.AbstractList;
import java.util.List;

import static org.parboiled.common.Preconditions.checkArgNotNull;
import static org.parboiled.common.Preconditions.checkElementIndex;
import static org.parboiled.common.Preconditions.checkState;
import static org.parboiled.common.Utils.arrayOf;

/**
 * A simple, immutable List implementation wrapping an array.
 *
 * @param <T>
 */
@SuppressWarnings( {"unchecked"})
public abstract class ImmutableList<T> extends AbstractList<T> {

    private final static ImmutableList<?> EMPTY_LIST = new ImmutableList<Object>() {
        @Override
        public Object get(int index) {
            throw new IndexOutOfBoundsException("Empty list has no element with index " + index);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public ImmutableList<Object> append(Object element) {
            return of(element);
        }
    };

    private static class SingleElementList<T> extends ImmutableList<T> {
        private final T element;

        public SingleElementList(T element) {
            this.element = element;
        }

        @Override
        public T get(int index) {
            checkElementIndex(index, 1);
            return element;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ImmutableList<T> append(T element) {
            return of(this.element, element);
        }
    }
    
    private static class TwoElementList<T> extends ImmutableList<T> {
        private final T element0;
        private final T element1;

        private TwoElementList(T element0, T element1) {
            this.element0 = element0;
            this.element1 = element1;
        }

        @Override
        public T get(int index) {
            checkElementIndex(index, 2);
            return index == 0 ? element0 : element1;
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public ImmutableList<T> append(T element) {
            return of(element0, element1, element);
        }
    }
    
    private static class RegularList extends ImmutableList<Object> {
        private final Object[] elements;

        private RegularList(Object[] elements) {
            this.elements = elements;
        }

        @Override
        public Object get(int index) {
            return elements[index];
        }

        @Override
        public int size() {
            return elements.length;
        }

        @Override
        public ImmutableList<Object> append(Object element) {
            Object[] newElements = new Object[elements.length + 1];
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            newElements[elements.length] = element;
            return new RegularList(newElements);
        }
    }
    
    public abstract ImmutableList<T> append(T element);

    public static <T> ImmutableList<T> copyOf(List<T> other) {
        checkArgNotNull(other, "other");
        return (ImmutableList<T>) (other instanceof ImmutableList ? other : new RegularList(other.toArray()));
    }

    public static <T> ImmutableList<T> of() {
        return (ImmutableList<T>) EMPTY_LIST;
    }

    public static <T> ImmutableList<T> of(T a) {
        return new SingleElementList<T>(a);
    }

    public static <T> ImmutableList<T> of(T a, T b) {
        return new TwoElementList<T>(a, b);
    }

    public static <T> ImmutableList<T> of(T a, T b, T c) {
        return (ImmutableList<T>) new RegularList(new Object[] {a, b, c});
    }

    public static <T> ImmutableList<T> of(T... elements) {
        checkArgNotNull(elements, "elements");
        return (ImmutableList<T>) new RegularList(elements.clone());
    }

    public static <T> ImmutableList<T> of(T first, T[] more) {
        checkArgNotNull(more, "more");
        return (ImmutableList<T>) new RegularList(arrayOf(first, more.clone()));
    }

    public static <T> ImmutableList<T> of(T[] first, T last) {
        checkArgNotNull(first, "first");
        return (ImmutableList<T>) new RegularList(arrayOf(first.clone(), last));
    }

    public static <T> ImmutableList<T> of(T first, ImmutableList<T> more) {
        checkArgNotNull(more, "more");
        if (more instanceof SingleElementList) {
            return of(first, (T) ((SingleElementList) more).element);
        } else if (more instanceof TwoElementList) {
            TwoElementList list = (TwoElementList) more;
            return (ImmutableList<T>) new RegularList(new Object[] {first, list.element0, list.element1});
        } else if (more instanceof RegularList) {
            RegularList list = (RegularList) more;
            return (ImmutableList<T>) new RegularList(arrayOf(first, list.elements));
        } else {
            checkState(more == EMPTY_LIST);
            return of(first);
        }
    }

    public static <T> ImmutableList<T> of(ImmutableList<T> first, T last) {
        checkArgNotNull(first, "more");
        if (first instanceof SingleElementList) {
            return of((T) ((SingleElementList) first).element, last);
        } else if (first instanceof TwoElementList) {
            TwoElementList list = (TwoElementList) first;
            return (ImmutableList<T>) new RegularList(new Object[] {list.element0, list.element1, last});
        } else if (first instanceof RegularList) {
            RegularList list = (RegularList) first;
            return (ImmutableList<T>) new RegularList(arrayOf(list.elements, last));
        } else {
            checkState(first == EMPTY_LIST);
            return of(last);
        }
    }
}
