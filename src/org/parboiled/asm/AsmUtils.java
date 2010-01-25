/*
 * Copyright (c) 2009-2010 Ken Wenzel and Mathias Doenitz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.parboiled.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;
import org.parboiled.BaseParser;
import org.parboiled.Context;
import org.parboiled.ContextAware;
import org.parboiled.Rule;
import org.parboiled.matchers.AbstractMatcher;
import org.parboiled.matchers.Matcher;
import org.parboiled.matchers.ProxyMatcher;
import org.parboiled.support.DontExtend;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

class AsmUtils {

    public static final Type ABSTRACT_MATCHER_TYPE = Type.getType(AbstractMatcher.class);
    public static final Type ACTION_WRAPPER_BASE_TYPE = Type.getType(ActionWrapperBase.class);
    public static final Type BASE_PARSER_TYPE = Type.getType(BaseParser.class);
    public static final Type BOOLEAN_TYPE = Type.getType(Boolean.class);
    public static final Type CONTEXT_AWARE_TYPE = Type.getType(ContextAware.class);
    public static final Type CONTEXT_TYPE = Type.getType(Context.class);
    public static final Type DONT_EXTEND_ANNOTATION_TYPE = Type.getType(DontExtend.class);
    public static final Type MATCHER_TYPE = Type.getType(Matcher.class);
    public static final Type PROXY_MATCHER_TYPE = Type.getType(ProxyMatcher.class);
    public static final Type RULE_TYPE = Type.getType(Rule.class);

    public static Class<?> getClassForInternalName(@NotNull String internalName) {
        String className = internalName.replace('/', '.');
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Error loading class '" + className + "' for rule method analysis", e);
        }
    }

    public static Class<?> getClassForType(@NotNull Type type) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
                return boolean.class;
            case Type.BYTE:
                return byte.class;
            case Type.CHAR:
                return char.class;
            case Type.DOUBLE:
                return double.class;
            case Type.FLOAT:
                return float.class;
            case Type.INT:
                return int.class;
            case Type.LONG:
                return long.class;
            case Type.SHORT:
                return short.class;
            case Type.VOID:
                return void.class;
            case Type.OBJECT:
            case Type.ARRAY:
                return getClassForInternalName(type.getInternalName());
        }
        throw new IllegalStateException(); // should be unreachable
    }

    public static Field getOwnerField(@NotNull String ownerInternalName, @NotNull String fieldName) {
        Class<?> clazz = getClassForInternalName(ownerInternalName);
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not get field '" + fieldName + "' of class '" + clazz + '\'', e);
        }
    }

    public static Method getOwnerMethod(@NotNull String ownerInternalName, @NotNull String methodName,
                                        @NotNull String methodDesc) {
        Class<?> clazz = getClassForInternalName(ownerInternalName);
        Type[] types = Type.getArgumentTypes(methodDesc);
        Class<?>[] argTypes = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            argTypes[i] = getClassForType(types[i]);
        }
        Class<?> current = clazz;
        while (true) {
            try {
                return current.getDeclaredMethod(methodName, argTypes);
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
                if (Object.class.equals(current)) {
                    throw new RuntimeException("Method '" + methodName + "' with descriptor '" +
                            methodDesc + "' not found in class '" + clazz + "\' or any superclass", e);
                }
            }
        }
    }

    /**
     * Loads the class defined with the given name and bytecode using the given class loader,
     * if a class with the given name has not yet been loaded before.
     * Since package and class idendity includes the ClassLoader instance used to load a class we use reflection
     * on the given class loader to define generated classes. If we used our own class loader (in order to be able
     * to access the protected "defineClass" method) we would likely still be able to load generated classes,
     * however, they would not have access to package-private classes and members of their super classes.
     *
     * @param className   the full name of the class to be loaded
     * @param code        the bytecode of the class to load
     * @param classLoader the class loader to use
     * @return the class instance
     */
    public static synchronized Class<?> loadClass(@NotNull String className, @NotNull byte[] code,
                                     @NotNull ClassLoader classLoader) {
        try {
            Class<?> classLoaderBaseClass = Class.forName("java.lang.ClassLoader");
            Method findLoadedClassMethod = classLoaderBaseClass.getDeclaredMethod("findLoadedClass", String.class);

            // protected method invocation
            findLoadedClassMethod.setAccessible(true);
            try {
                Class<?> clazz = (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
                if (clazz != null) return clazz;
            } finally {
                findLoadedClassMethod.setAccessible(false);
            }

            Method defineClassMethod = classLoaderBaseClass.getDeclaredMethod("defineClass",
                    String.class, byte[].class, int.class, int.class);

            // protected method invocation
            defineClassMethod.setAccessible(true);
            try {
                return (Class<?>) defineClassMethod.invoke(classLoader, className, code, 0, code.length);
            } finally {
                defineClassMethod.setAccessible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load class '" + className + '\'', e);
        }
    }
}
