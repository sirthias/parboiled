/*
 * Copyright (c) 2009-2011 Ken Wenzel and Mathias Doenitz
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

package org.parboiled.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.parboiled.BaseParser;
import org.parboiled.ContextAware;
import org.parboiled.support.Var;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.parboiled.common.Preconditions.checkArgNotNull;

class AsmUtils {

    private static final ConcurrentMap<String, Class<?>> classForDesc = new ConcurrentHashMap<String, Class<?>>();

    public static ClassReader createClassReader(Class<?> clazz) throws IOException {
        checkArgNotNull(clazz, "clazz");
        String classFilename = clazz.getName().replace('.', '/') + ".class";
        InputStream inputStream = clazz.getClassLoader().getResourceAsStream(classFilename);
        return new ClassReader(inputStream);
    }

    public static String getExtendedParserClassName(String parserClassName) {
        checkArgNotNull(parserClassName, "parserClassName");
        return parserClassName + "$$parboiled";
    }

    public static Class<?> getClassForInternalName(String classDesc) {
        checkArgNotNull(classDesc, "classDesc");
        Class<?> clazz = classForDesc.get(classDesc);
        if (clazz == null) {
            if (classDesc.charAt(0) == '[') {
                Class<?> compType = getClassForType(Type.getType(classDesc.substring(1)));
                clazz = Array.newInstance(compType, 0).getClass();
            } else {
                String className = classDesc.replace('/', '.');
                try {
                    clazz = AsmUtils.class.getClassLoader().loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Error loading class '" + className + "' for rule method analysis", e);
                }
            }
            classForDesc.put(classDesc, clazz);
        }
        return clazz;
    }

    public static Class<?> getClassForType(Type type) {
        checkArgNotNull(type, "type");
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

    public static Field getClassField(String classInternalName, String fieldName) {
        checkArgNotNull(classInternalName, "classInternalName");
        checkArgNotNull(fieldName, "fieldName");
        Class<?> clazz = getClassForInternalName(classInternalName);
        Class<?> current = clazz;
        while (true) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
                if (Object.class.equals(current)) {
                    throw new RuntimeException(
                            "Field '" + fieldName + "' not found in '" + clazz + "\' or any superclass", e);
                }
            }
        }
    }

    public static Method getClassMethod(String classInternalName, String methodName, String methodDesc) {
        checkArgNotNull(classInternalName, "classInternalName");
        checkArgNotNull(methodName, "methodName");
        checkArgNotNull(methodDesc, "methodDesc");
        Class<?> clazz = getClassForInternalName(classInternalName);
        Type[] types = Type.getArgumentTypes(methodDesc);
        Class<?>[] argTypes = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            argTypes[i] = getClassForType(types[i]);
        }
        Method method = findMethod(clazz, methodName, argTypes);
        if (method == null) {
            throw new RuntimeException("Method '" + methodName + "' with descriptor '" +
                    methodDesc + "' not found in '" + clazz + "\' or any supertype");
        }
        return method;
    }

    private static Method findMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        Method found = null;
        if (clazz != null) {
            try {
                found = clazz.getDeclaredMethod(methodName, argTypes);
            } catch (NoSuchMethodException e) {
                found = findMethod(clazz.getSuperclass(), methodName, argTypes);
                if (found == null) {
                    for (Class<?> interfaceClass : clazz.getInterfaces()) {
                        found = findMethod(interfaceClass, methodName, argTypes);
                        if (found != null) break;
                    }
                }
            }
        }
        return found;
    }

    public static Constructor getClassConstructor(String classInternalName, String constructorDesc) {
        checkArgNotNull(classInternalName, "classInternalName");
        checkArgNotNull(constructorDesc, "constructorDesc");
        Class<?> clazz = getClassForInternalName(classInternalName);
        Type[] types = Type.getArgumentTypes(constructorDesc);
        Class<?>[] argTypes = new Class<?>[types.length];
        for (int i = 0; i < types.length; i++) {
            argTypes[i] = getClassForType(types[i]);
        }
        try {
            return clazz.getDeclaredConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Constructor with descriptor '" + constructorDesc + "' not found in '" +
                    clazz, e);
        }
    }

    /**
     * Returns the class with the given name if it has already been loaded by the given class loader.
     * Otherwise the method returns null.
     *
     * @param className   the full name of the class to be loaded
     * @param classLoader the class loader to use
     * @return the class instance or null
     */
    public static Class<?> findLoadedClass(String className, ClassLoader classLoader) {
        checkArgNotNull(className, "className");
        checkArgNotNull(classLoader, "classLoader");
        try {
            Class<?> classLoaderBaseClass = Class.forName("java.lang.ClassLoader");
            Method findLoadedClassMethod = classLoaderBaseClass.getDeclaredMethod("findLoadedClass", String.class);

            // protected method invocation
            findLoadedClassMethod.setAccessible(true);
            try {
                return (Class<?>) findLoadedClassMethod.invoke(classLoader, className);
            } finally {
                findLoadedClassMethod.setAccessible(false);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not determine whether class '" + className +
                    "' has already been loaded", e);
        }
    }

    /**
     * Loads the class defined with the given name and bytecode using the given class loader.
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
    public static Class<?> loadClass(String className, byte[] code, ClassLoader classLoader) {
        checkArgNotNull(className, "className");
        checkArgNotNull(code, "code");
        checkArgNotNull(classLoader, "classLoader");
        try {
            Class<?> classLoaderBaseClass = Class.forName("java.lang.ClassLoader");
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

    public static InsnList createArgumentLoaders(String methodDescriptor) {
        checkArgNotNull(methodDescriptor, "methodDescriptor");
        InsnList instructions = new InsnList();
        Type[] types = Type.getArgumentTypes(methodDescriptor);
        for (int i = 0; i < types.length; i++) {
            instructions.add(new VarInsnNode(getLoadingOpcode(types[i]), i + 1));
        }
        return instructions;
    }

    public static int getLoadingOpcode(Type argType) {
        checkArgNotNull(argType, "argType");
        switch (argType.getSort()) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                return Opcodes.ILOAD;
            case Type.DOUBLE:
                return Opcodes.DLOAD;
            case Type.FLOAT:
                return Opcodes.FLOAD;
            case Type.LONG:
                return Opcodes.LLOAD;
            case Type.OBJECT:
            case Type.ARRAY:
                return Opcodes.ALOAD;
            default:
                throw new IllegalStateException();
        }
    }

    /**
     * Determines whether the class with the given descriptor is assignable to the given type.
     *
     * @param classInternalName the class descriptor
     * @param type              the type
     * @return true if the class with the given descriptor is assignable to the given type
     */
    public static boolean isAssignableTo(String classInternalName, Class<?> type) {
        checkArgNotNull(classInternalName, "classInternalName");
        checkArgNotNull(type, "type");
        return type.isAssignableFrom(getClassForInternalName(classInternalName));
    }

    public static boolean isBooleanValueOfZ(AbstractInsnNode insn) {
        checkArgNotNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKESTATIC) return false;
        MethodInsnNode mi = (MethodInsnNode) insn;
        return isBooleanValueOfZ(mi.owner, mi.name, mi.desc);
    }

    public static boolean isBooleanValueOfZ(String methodOwner, String methodName, String methodDesc) {
        checkArgNotNull(methodOwner, "methodOwner");
        checkArgNotNull(methodName, "methodName");
        checkArgNotNull(methodDesc, "methodDesc");
        return "java/lang/Boolean".equals(methodOwner) && "valueOf".equals(methodName) &&
                "(Z)Ljava/lang/Boolean;".equals(methodDesc);
    }

    public static boolean isActionRoot(AbstractInsnNode insn) {
        checkArgNotNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKESTATIC) return false;
        MethodInsnNode mi = (MethodInsnNode) insn;
        return isActionRoot(mi.owner, mi.name);
    }

    public static boolean isActionRoot(String methodOwner, String methodName) {
        checkArgNotNull(methodOwner, "methodOwner");
        checkArgNotNull(methodName, "methodName");
        return "ACTION".equals(methodName) && isAssignableTo(methodOwner, BaseParser.class);
    }

    public static boolean isVarRoot(AbstractInsnNode insn) {
        checkArgNotNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKESPECIAL) return false;
        MethodInsnNode mi = (MethodInsnNode) insn;
        return isVarRoot(mi.owner, mi.name, mi.desc);
    }

    public static boolean isVarRoot(String methodOwner, String methodName, String methodDesc) {
        checkArgNotNull(methodOwner, "methodOwner");
        checkArgNotNull(methodName, "methodName");
        checkArgNotNull(methodDesc, "methodDesc");
        return "<init>".equals(methodName) && "(Ljava/lang/Object;)V".equals(methodDesc) &&
                isAssignableTo(methodOwner, Var.class);
    }

    public static boolean isCallOnContextAware(AbstractInsnNode insn) {
        checkArgNotNull(insn, "insn");
        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL && insn.getOpcode() != Opcodes.INVOKEINTERFACE) return false;
        MethodInsnNode mi = (MethodInsnNode) insn;
        return isAssignableTo(mi.owner, ContextAware.class);
    }

}
