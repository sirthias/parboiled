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

import org.jetbrains.annotations.NotNull;
import static org.parboiled.common.Utils.arrayOf;

import java.util.AbstractList;
import java.util.List;

/**
 * A simple, immutable List implementation wrapping an array.
 *
 * @param <T>
 */
@SuppressWarnings({"unchecked"})
public class ImmutableList<T> extends AbstractList<T> {

    public final static ImmutableList<?> EMPTY_LIST = new ImmutableList<Object>(Utils.EMPTY_OBJECT_ARRAY);

    private final T[] array;

    private ImmutableList(@NotNull T[] array) {
        this.array = array;
    }

    public int size() {
        return array.length;
    }

    public T get(int index) {
        return array[index];
    }

    public static <T> ImmutableList<T> copyOf(@NotNull List<T> other) {
        return (ImmutableList<T>) (other instanceof ImmutableList ? other : create((T[]) other.toArray()));
    }

    public static <T> ImmutableList<T> of() {
        return (ImmutableList<T>) EMPTY_LIST;
    }

    public static <T> ImmutableList<T> of(T a) {
        return create(a);
    }

    public static <T> ImmutableList<T> of(T a, T b) {
        return create(a, b);
    }

    public static <T> ImmutableList<T> of(T a, T b, T c) {
        return create(a, b, c);
    }

    public static <T> ImmutableList<T> of(T... elements) {
        return create(elements.clone());
    }

    public static <T> ImmutableList<T> of(T first, T[] more) {
        return create(arrayOf(first, more.clone()));
    }

    public static <T> ImmutableList<T> of(@NotNull T[] first, T last) {
        return create(arrayOf(first.clone(), last));
    }

    public static <T> ImmutableList<T> of(T first, @NotNull ImmutableList<T> more) {
        return create(arrayOf(first, more.array));
    }

    public static <T> ImmutableList<T> of(@NotNull ImmutableList<T> first, T last) {
        return create(arrayOf(first.array, last));
    }

    private static <T> ImmutableList<T> create(T... elements) {
        return new ImmutableList<T>(elements);
    }

}
