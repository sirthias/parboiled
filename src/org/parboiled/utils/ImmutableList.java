package org.parboiled.utils;

import org.jetbrains.annotations.NotNull;
import static org.parboiled.utils.Utils.arrayOf;

import java.util.AbstractList;
import java.util.List;

@SuppressWarnings({"unchecked"})
public class ImmutableList<T> extends AbstractList<T> {

    public final static ImmutableList<?> EMPTY_LIST = new ImmutableList<Object>(new Object[0]);

    private final T[] array;

    private ImmutableList(@NotNull T[] array) {
        this.array = array;
    }

    public int size() {
        return array.length;
    }

    public T get(int index) {
        Preconditions.checkElementIndex(index, size());
        return array[index];
    }

    public static <T> List<T> copyOf(@NotNull List<T> other) {
        return new ImmutableList<T>((T[]) other.toArray());
    }

    public static <T> List<T> of() {
        return (List<T>) EMPTY_LIST;
    }

    public static <T> List<T> of(T a) {
        return new ImmutableList<T>(arrayOf(a));
    }

    public static <T> List<T> of(T a, T b) {
        return new ImmutableList<T>(arrayOf(a, b));
    }

    public static <T> List<T> of(T a, T b, T c) {
        return new ImmutableList<T>(arrayOf(a, b, c));
    }

    public static <T> List<T> of(T... elements) {
        return new ImmutableList<T>(elements);
    }

}
