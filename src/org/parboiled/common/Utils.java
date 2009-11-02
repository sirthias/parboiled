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

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * General utility methods.
 */
public final class Utils {

    private Utils() {}

    /**
     * Joins the given arguments into one array.
     *
     * @param firstElement the first element
     * @param moreElements more elements (optional)
     * @return a new array containing all arguments.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T[] arrayOf(T firstElement, T... moreElements) {
        Class elementType = moreElements.getClass().getComponentType();
        T[] array = (T[]) Array.newInstance(elementType, moreElements.length + 1);
        array[0] = firstElement;
        System.arraycopy(moreElements, 0, array, 1, moreElements.length);
        return array;
    }

    /**
     * Provides a null enabled equals().
     *
     * @param a the first object
     * @param b the second object
     * @return true if a and b are either both null or a.equals(b), false otherwise
     */
    public static boolean equals(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /**
     * Null enabled toString()
     *
     * @param obj the object
     * @return the empty string of obj is null, otherwise obj.toString()
     */
    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * Tests whether all elements of the given array are null.
     *
     * @param elements the array
     * @return true if all elements are null
     */
    public static <T> boolean allElementsNull(@NotNull T[] elements) {
        for (T element : elements) {
            if (element != null) return false;
        }
        return true;
    }

    // Subarrays
    //-----------------------------------------------------------------------
    /**
     * <p>Produces a new array containing the elements between
     * the start and end indices.</p>
     * <p/>
     * <p>The start index is inclusive, the end index exclusive.
     * Null array input produces null output.</p>
     * <p/>
     * <p>The component type of the subarray is always the same as
     * that of the input array. Thus, if the input is an array of type
     * <code>Date</code>, the following usage is envisaged:</p>
     * <p/>
     * <pre>
     * Date[] someDates = (Date[])ArrayUtils.subarray(allDates, 2, 5);
     * </pre>
     *
     * @param array               the array
     * @param startIndexInclusive the starting index. Undervalue (&lt;0)
     *                            is promoted to 0, overvalue (&gt;array.length) results
     *                            in an empty array.
     * @param endIndexExclusive   elements up to endIndex-1 are present in the
     *                            returned subarray. Undervalue (&lt; startIndex) produces
     *                            empty array, overvalue (&gt;array.length) is demoted to
     *                            array length.
     * @return a new array containing the elements between
     *         the start and end indices.
     */
    public static Object[] subarray(Object[] array, int startIndexInclusive, int endIndexExclusive) {
        if (array == null) {
            return null;
        }
        if (startIndexInclusive < 0) {
            startIndexInclusive = 0;
        }
        if (endIndexExclusive > array.length) {
            endIndexExclusive = array.length;
        }
        int newSize = endIndexExclusive - startIndexInclusive;
        Class type = array.getClass().getComponentType();
        if (newSize <= 0) {
            return (Object[]) Array.newInstance(type, 0);
        }
        Object[] subarray = (Object[]) Array.newInstance(type, newSize);
        System.arraycopy(array, startIndexInclusive, subarray, 0, newSize);
        return subarray;
    }

    /**
     * Adapts a list to an iterable with reversed iteration order. It's especially useful in foreach-style loops:
     * <pre>
     * List<String> mylist = ...
     * for (String str : Iterables.reverse(mylist)) {
     *   ...
     * } </pre>
     *
     * @param list the list to reverse
     * @return an iterable with the same elements as the list, in reverse.
     */
    public static <T> Iterable<T> reverse(@NotNull final List<T> list) {
        return new Iterable<T>() {
            public Iterator<T> iterator() {
                final ListIterator<T> listIterator = list.listIterator(list.size());
                return new Iterator<T>() {
                    public boolean hasNext() {
                        return listIterator.hasPrevious();
                    }

                    public T next() {
                        return listIterator.previous();
                    }

                    public void remove() {
                        listIterator.remove();
                    }
                };
            }
        };
    }

}

