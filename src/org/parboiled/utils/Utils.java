package org.parboiled.utils;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;

public final class Utils {

    private Utils() {}

    /**
     * Returns the smallest of the given integers.
     *
     * @param integers the integers
     * @return the minimum
     */
    public static int min(int... integers) {
        if (integers.length == 0) throw new IllegalArgumentException();
        int min = integers[0];
        for (int i = 1; i < integers.length; i++) {
            min = Math.min(min, integers[i]);
        }
        return min;
    }

    /**
     * Returns the largest of the given integers.
     *
     * @param integers the integers
     * @return the maximum
     */
    public static int max(int... integers) {
        if (integers.length == 0) throw new IllegalArgumentException();
        int min = integers[0];
        for (int i = 1; i < integers.length; i++) {
            min = Math.max(min, integers[i]);
        }
        return min;
    }

    /**
     * Returns i if lo < i < hi, otherwise lo or hi.
     *
     * @param i  the value
     * @param lo the lower bound
     * @param hi the higher bound
     * @return the maximum
     */
    public static int applyBounds(int i, int lo, int hi) {
        Preconditions.checkArgument(lo <= hi);
        return i < lo ? lo : i > hi ? hi : i;
    }

    /**
     * Small helper for static imports, emulates the C# ?? operator.
     *
     * @param object       an object
     * @param defaultValue the default value to return if object is null
     * @param <T>          the type
     * @return object (if not null) or defaultValue (if object is null)
     */
    public static <T> T ifNull(T object, T defaultValue) {
        return object != null ? object : defaultValue;
    }

    /**
     * Gets the enum constant with the given name (case insensitive comparison) or null
     *
     * @param enumClass the enum class
     * @param name      the name of the constant to get
     * @return the constant or null
     */
    public static <E extends Enum<E>> E getEnumNull(@NotNull Class<E> enumClass, String name) {
        for (E value : enumClass.getEnumConstants()) {
            if (StringUtils.equalsIgnoreCase(value.toString(), name)) return value;
        }
        return null;
    }

    /**
     * Gets the enum constant with the given name (case insensitive comparison).
     * Throws an IllegalArgumentException if the name is not a valid enum constant name.
     *
     * @param enumClass the enum class
     * @param name      the name of the constant to get
     * @return the constant or null
     */
    public static <E extends Enum<E>> E getEnum(@NotNull Class<E> enumClass, String name) {
        for (E value : enumClass.getEnumConstants()) {
            if (StringUtils.equalsIgnoreCase(value.toString(), name)) return value;
        }
        throw new IllegalArgumentException();
    }

    /**
     * Gets the enum constant with the given ordinal value or null.
     *
     * @param enumClass the enum class
     * @param value     the oridinal value of the constant to get
     * @return the constant or null
     */
    public static <E extends Enum<E>> E getEnum(@NotNull Class<E> enumClass, int value) {
        if (value < 0) return null;
        E[] values = enumClass.getEnumConstants();
        if (value >= values.length) return null;
        return values[value];
    }

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
     * Joins the given arguments into one array.
     *
     * @param firstElement the first element
     * @param secondElement the second element
     * @param moreElements more elements (optional)
     * @return a new array containing all arguments.
     */
    @SuppressWarnings({"unchecked"})
    public static <T> T[] arrayOf(T firstElement, T secondElement, T... moreElements) {
        Class elementType = moreElements.getClass().getComponentType();
        T[] array = (T[]) Array.newInstance(elementType, moreElements.length + 2);
        array[0] = firstElement;
        array[1] = secondElement;
        System.arraycopy(moreElements, 0, array, 2, moreElements.length);
        return array;
    }

}

