package org.parboiled.utils;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Provides general, not object specific reflection functionality.
 */
public final class Reflector {

    private Reflector() {}

    /**
     * Creates a ReflectorWrapper for the given object.
     *
     * @param obj the object
     * @return the wrapper
     */
    @NotNull
    public static Reflector_ f(@NotNull Object obj) {
        return new Reflector_(obj);
    }

    /**
     * Creates a ReflectorWrapper for the given class.
     *
     * @param clazz the class
     * @return the wrapper
     */
    @NotNull
    public static Reflector_ f(@NotNull Class<?> clazz) {
        return new Reflector_(clazz);
    }

    /**
     * Returns the most specialized common base type of the given objects or null,
     * if the given array is empty or contains at least one null entry.
     *
     * @param objects the objects
     * @return the common base type or null
     */

    public static Class<?> getCommonType(@NotNull Object[] objects) {
        Class<?> commonBase = null;
        for (Object obj : objects) {
            if (obj == null) return null;

            if (commonBase == null) {
                commonBase = obj.getClass();
                continue;
            }

            Reflector_ clazz = f(obj.getClass());
            while (!clazz.canBeCastTo(commonBase)) {
                commonBase = commonBase.getSuperclass();
            }
        }
        return commonBase;
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
     * Split out the actually class name from a fully qualified name (including the package).
     *
     * @param object the object the get the class name from
     * @return the short class name
     */
    public static String getShortClassName(@NotNull Object object) {
        return getShortClassName(object.getClass());
    }

    /**
     * Split out the actually class name from a fully qualified name (including the package).
     *
     * @param clazz the class
     * @return the short class name
     */
    public static String getShortClassName(@NotNull Class clazz) {
        return getShortClassName(clazz.getName());
    }

    /**
     * Split out the actually class name from a fully qualified name (including the package).
     *
     * @param fullyQualifiedClassname the long name
     * @return the short class name
     */
    public static String getShortClassName(@NotNull String fullyQualifiedClassname) {
        return fullyQualifiedClassname.substring(fullyQualifiedClassname.lastIndexOf('.') + 1);
    }

}

