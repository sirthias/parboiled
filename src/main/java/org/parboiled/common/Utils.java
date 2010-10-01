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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.parboiled.errors.GrammarException;

import java.lang.reflect.*;
import java.util.*;

/**
 * General utility methods.
 */
public final class Utils {

    public static final Character[] EMPTY_CHARACTER_OBJECT_ARRAY = new Character[0];
    public static final Integer[] EMPTY_INTEGER_OBJECT_ARRAY = new Integer[0];
    public static final Long[] EMPTY_LONG_OBJECT_ARRAY = new Long[0];
    public static final Short[] EMPTY_SHORT_OBJECT_ARRAY = new Short[0];
    public static final Byte[] EMPTY_BYTE_OBJECT_ARRAY = new Byte[0];
    public static final Float[] EMPTY_FLOAT_OBJECT_ARRAY = new Float[0];
    public static final Double[] EMPTY_DOUBLE_OBJECT_ARRAY = new Double[0];
    public static final Boolean[] EMPTY_BOOLEAN_OBJECT_ARRAY = new Boolean[0];

    private Utils() {}

    public static Character[] toObjectArray(char[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_CHARACTER_OBJECT_ARRAY;
        Character[] result = new Character[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Integer[] toObjectArray(int[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_INTEGER_OBJECT_ARRAY;
        Integer[] result = new Integer[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Long[] toObjectArray(long[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_LONG_OBJECT_ARRAY;
        Long[] result = new Long[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Short[] toObjectArray(short[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_SHORT_OBJECT_ARRAY;
        Short[] result = new Short[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Byte[] toObjectArray(byte[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_BYTE_OBJECT_ARRAY;
        Byte[] result = new Byte[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Float[] toObjectArray(float[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_FLOAT_OBJECT_ARRAY;
        Float[] result = new Float[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Double[] toObjectArray(double[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_DOUBLE_OBJECT_ARRAY;
        Double[] result = new Double[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    public static Boolean[] toObjectArray(boolean[] array) {
        if (array == null) return null;
        if (array.length == 0) return EMPTY_BOOLEAN_OBJECT_ARRAY;
        Boolean[] result = new Boolean[array.length];
        for (int i = 0; i < array.length; i++) result[i] = array[i];
        return result;
    }

    /**
     * Null enabled toString().
     *
     * @param obj the object
     * @return the empty string of obj is null, otherwise obj.toString()
     */
    public static String toString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * Null enabled equals().
     *
     * @param a the first object
     * @param b the second object
     * @return true if both are null or both are equal
     */
    public static <T> boolean equal(T a, T b) {
        return a != null ? a.equals(b) : b == null;
    }

    /**
     * Gets the actual type arguments that are used in a given implementation of a given generic base class or interface.
     * (Based on code copyright 2007 by Ian Robertson).
     *
     * @param base           the generic base class or interface
     * @param implementation the type (potentially) implementing the given base class or interface
     * @return a list of the raw classes for the actual type arguments.
     */
    @NotNull
    public static List<Class<?>> getTypeArguments(@NotNull Class<?> base, @NotNull Class<?> implementation) {
        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();

        // first we need to resolve all supertypes up to the required base class or interface
        // and find the right Type for it
        Type type;

        Queue<Type> toCheck = new LinkedList<Type>();
        toCheck.add(implementation);
        while (true) {
            // if we have checked everything and not found the base class we return an empty list
            if (toCheck.isEmpty()) return ImmutableList.of();

            type = toCheck.remove();
            Class<?> clazz;

            if (type instanceof Class) {
                // there is no useful information for us in raw types, so just keep going up the inheritance chain
                clazz = (Class) type;
                if (base.isInterface()) {
                    // if we are actually looking for the type parameters to an interface we also need to
                    // look at all the ones implemented by the given current one
                    toCheck.addAll(Arrays.asList(clazz.getGenericInterfaces()));
                }
            } else if (type instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                clazz = (Class) parameterizedType.getRawType();

                // for instances of ParameterizedType we extract and remember all type arguments
                TypeVariable<?>[] typeParameters = clazz.getTypeParameters();
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
                }
            } else {
                return ImmutableList.of();
            }

            // we can stop if we have reached the sought for base type
            if (base.equals(getClass(type))) break;

            toCheck.add(clazz.getGenericSuperclass());
        }

        // finally, for each actual type argument provided to baseClass,
        // determine (if possible) the raw class for that type argument.
        Type[] actualTypeArguments;
        if (type instanceof Class) {
            actualTypeArguments = ((Class) type).getTypeParameters();
        } else {
            actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
        }
        List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(getClass(baseType));
        }
        return typeArgumentsAsClasses;
    }

    /**
     * Get the underlying class for a type, or null if the type is a variable type.
     * (Copyright 2007 by Ian Robertson).
     *
     * @param type the type
     * @return the underlying class
     */

    public static Class<?> getClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return getClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = getClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        }
        return null;
    }

    /**
     * Finds the constructor of the given class that is compatible with the given arguments.
     *
     * @param type the class to find the constructor of
     * @param args the arguments
     * @return the constructor
     */
    public static Constructor findConstructor(Class<?> type, Object[] args) {
        outer:
        for (Constructor constructor : type.getConstructors()) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != args.length) continue;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null && !paramTypes[i].isAssignableFrom(arg.getClass())) continue outer;
                if (arg == null && paramTypes[i].isPrimitive()) continue outer;
            }
            return constructor;
        }
        throw new GrammarException("No constructor found for %s and the given %s arguments", type, args.length);
    }

    /**
     * Formats the given long value into a human readable notation using the Kilo, Mega, Giga, etc. abbreviations.
     *
     * @param value the value to format
     * @return the string representation
     */
    public static String humanize(long value) {
        if (value < 0) {
            return '-' + humanize(-value);
        } else if (value > 1000000000000000000L) {
            return Double.toString(
                    (value + 500000000000000L) / 1000000000000000L * 1000000000000000L / 1000000000000000000.0) + 'E';
        } else if (value > 100000000000000000L) {
            return Double.toString(
                    (value + 50000000000000L) / 100000000000000L * 100000000000000L / 1000000000000000.0) + 'P';
        } else if (value > 10000000000000000L) {
            return Double
                    .toString((value + 5000000000000L) / 10000000000000L * 10000000000000L / 1000000000000000.0) + 'P';
        } else if (value > 1000000000000000L) {
            return Double
                    .toString((value + 500000000000L) / 1000000000000L * 1000000000000L / 1000000000000000.0) + 'P';
        } else if (value > 100000000000000L) {
            return Double.toString((value + 50000000000L) / 100000000000L * 100000000000L / 1000000000000.0) + 'T';
        } else if (value > 10000000000000L) {
            return Double.toString((value + 5000000000L) / 10000000000L * 10000000000L / 1000000000000.0) + 'T';
        } else if (value > 1000000000000L) {
            return Double.toString((value + 500000000) / 1000000000 * 1000000000 / 1000000000000.0) + 'T';
        } else if (value > 100000000000L) {
            return Double.toString((value + 50000000) / 100000000 * 100000000 / 1000000000.0) + 'G';
        } else if (value > 10000000000L) {
            return Double.toString((value + 5000000) / 10000000 * 10000000 / 1000000000.0) + 'G';
        } else if (value > 1000000000) {
            return Double.toString((value + 500000) / 1000000 * 1000000 / 1000000000.0) + 'G';
        } else if (value > 100000000) {
            return Double.toString((value + 50000) / 100000 * 100000 / 1000000.0) + 'M';
        } else if (value > 10000000) {
            return Double.toString((value + 5000) / 10000 * 10000 / 1000000.0) + 'M';
        } else if (value > 1000000) {
            return Double.toString((value + 500) / 1000 * 1000 / 1000000.0) + 'M';
        } else if (value > 100000) {
            return Double.toString((value + 50) / 100 * 100 / 1000.0) + 'K';
        } else if (value > 10000) {
            return Double.toString((value + 5) / 10 * 10 / 1000.0) + 'K';
        } else if (value > 1000) {
            return Double.toString(value / 1000.0) + 'K';
        } else {
            return Long.toString(value) + ' ';
        }
    }

    /**
     * "Zips" up two Iterables into one Iterable providing key/value pairs of the zipped up entries.
     *
     * @param keys   the first Iterable
     * @param values the second Iterable
     * @return an Iterable of key/value pairs corresponding to the respective entries of the two Iterables
     */
    public static <K, V> Iterable<Map.Entry<K, V>> zip(final Iterable<K> keys, final Iterable<V> values) {
        return new Iterable<Map.Entry<K, V>>() {
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    private final Iterator<K> keyIterator = keys.iterator();
                    private final Iterator<V> valueIterator = values.iterator();

                    public boolean hasNext() {
                        return keyIterator.hasNext() && valueIterator.hasNext();
                    }

                    public Map.Entry<K, V> next() {
                        final K key = keyIterator.next();
                        final V value = valueIterator.next();
                        return new Map.Entry<K, V>() {
                            public K getKey() {
                                return key;
                            }

                            public V getValue() {
                                return value;
                            }

                            public V setValue(V value) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}

