package org.parboiled.utils;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.MapMaker;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public class Reflector_ {

    private static final Map<Class<?>, Set<Class<?>>> superTypes = new MapMaker().concurrencyLevel(4)
            .makeComputingMap(new Function<Class<?>, Set<Class<?>>>() {
                public Set<Class<?>> apply(Class<?> from) {
                    return getAllSuperTypes(from, new HashSet<Class<?>>());
                }

                private Set<Class<?>> getAllSuperTypes(Class<?> type, Set<Class<?>> result) {
                    if (type != null) {
                        result.add(type);
                        getAllSuperTypes(type.getSuperclass(), result);
                        for (Class<?> interfaceClass : type.getInterfaces()) {
                            getAllSuperTypes(interfaceClass, result);
                        }
                    }
                    return result;
                }
            });

    private final Object object;
    private final Class type;

    protected Reflector_(@NotNull Class type) {
        this.object = null;
        this.type = type;
    }

    protected Reflector_(@NotNull Object object) {
        this.object = object;
        this.type = object.getClass();
    }

    /**
     * Checks whether this type can be assigned from the given type.
     *
     * @param subType the type potentially implementing or extending this type
     * @return true if subType is, extends or implements this type
     */
    @SuppressWarnings("unchecked")
    public boolean canBeCastFrom(@NotNull Class<?> subType) {
        return type.isAssignableFrom(subType);
    }

    /**
     * Checks whether this type can be casted to the given type.
     *
     * @param superType the potential super type
     * @return true if this type is, extends or implements the given type
     */
    @SuppressWarnings("unchecked")
    public boolean canBeCastTo(@NotNull Class<?> superType) {
        return superType.isAssignableFrom(type);
    }

    /**
     * Gets ALL fields (i.e. private, proctected, public and all inherited).
     *
     * @return the list of all fields this type has
     */
    @NotNull
    public List<Field> getAllFields() {
        List<Field> allFields = Lists.newArrayList();
        for (Class<?> superThing : getAllSuperTypes()) {
            allFields.addAll(Arrays.asList(superThing.getDeclaredFields()));
        }
        return allFields;
    }

    /**
     * Gets the field (public, protected or private, also inherited) with the given name.
     *
     * @param fieldName the name of the field
     * @return the value of the field
     */
    public Field getField(String fieldName) {
        for (Class<?> superThing : getAllSuperTypes()) {
            for (Field field : superThing.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) return field;
            }
        }
        return null;
    }

    /**
     * Gets ALL methods (i.e. private, proctected, public and all inherited).
     *
     * @return the list of all methods this type has
     */
    @NotNull
    public List<Method> getAllMethods() {
        List<Method> methods = Lists.newArrayList();
        for (Class<?> superThing : getAllSuperTypes()) {
            methods.addAll(Arrays.asList(superThing.getDeclaredMethods()));
        }
        return methods;
    }

    /**
     * Gets the method (public, protected or private, also inherited) with the given name.
     *
     * @param methodName the name of the method
     * @param argTypes   the argument types of the method to search for
     * @return the method or null if not found
     */
    public Method getMethod(@NotNull String methodName, Class<?>... argTypes) {
        for (Class<?> superThing : getAllSuperTypes()) {
            for (Method method : superThing.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), argTypes)) {
                    return method;
                }
            }
        }
        return null;
    }

    /**
     * Gets the method (public, protected or private, also inherited) with the given name.
     *
     * @param methodName the name of the method
     * @param argTypes   the argument types of the method to search for
     * @return the method
     */
    @NotNull
    public Method getExistingMethod(@NotNull String methodName, Class<?>... argTypes) {
        Method method = getMethod(methodName, argTypes);
        if (method == null) {
            throw new RuntimeException(
                    String.format("Class '%s' does not define a method %s(%s)", Reflector.getShortClassName(type),
                            methodName, StringUtils.join(argTypes, ", ")));
        }
        return method;
    }

    /**
     * Gets the method (public, protected or private, also inherited) with the given name.
     *
     * @param returnType the required return type
     * @param methodName the name of the method
     * @param argTypes   the argument types of the method to search for
     * @return the method or null if not found
     */
    public Method getMethod(@NotNull Class<?> returnType, @NotNull String methodName, Class<?>... argTypes) {
        Method method = getMethod(methodName, argTypes);
        return method != null && method.getReturnType() == returnType ? method : null;
    }

    /**
     * Gets the method (public, protected or private, also inherited) with the given name.
     *
     * @param returnType the required return type
     * @param methodName the name of the method
     * @param argTypes   the argument types of the method to search for
     * @return the method or null if not found
     */
    public Method getExistingMethod(@NotNull Class<?> returnType, @NotNull String methodName, Class<?>... argTypes) {
        Method method = getMethod(returnType, methodName, argTypes);
        if (method == null) {
            throw new RuntimeException(String.format("Class '%s' does not define a %s method %s(%s)",
                    Reflector.getShortClassName(type),
                    Reflector.getShortClassName(returnType), methodName, StringUtils.join(argTypes, ", ")));
        }
        return method;
    }

    /**
     * Gets the value of the field (public, protected or private, also inherited) with
     * the given name.
     *
     * @param fieldName the name of the field
     * @return the value of the field
     */

    public Object getFieldValue(@NotNull String fieldName) {
        Field field = getField(fieldName);
        if (!Modifier.isStatic(field.getModifiers()) && object == null) {
            throw new IllegalStateException("Cannot get instance field atom on a class reflector");
        }
        field.setAccessible(true);
        try {
            return field.get(object);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the value of the field (public, protected or private, also inherited) with
     * the given name.
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(@NotNull String fieldName, Object value) {
        Field field = getField(fieldName);
        if (!Modifier.isStatic(field.getModifiers()) && object == null) {
            throw new IllegalStateException("Cannot set instance field atom on a class reflector");
        }
        field.setAccessible(true);
        try {
            field.set(object, value);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the value of the property (public, protected or private, also inherited) with
     * the given name.
     *
     * @param propertyName the name of the property
     * @return the value of the field
     */
    @SuppressWarnings("unchecked")
    public Object getPropertyValue(@NotNull String propertyName) {
        try {
            Method getter = type.getMethod("get" + propertyName);
            if (!Modifier.isStatic(getter.getModifiers()) && object == null) {
                throw new IllegalStateException("Cannot get instance property atom on a class reflector");
            }
            return getter.invoke(object);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal property name '" + propertyName + '\'');
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the value of the property (public, protected or private, also inherited) with
     * the given name.
     *
     * @param propertyName the name of the field
     * @param value        the value of the field
     */
    public void setPropertyValue(@NotNull String propertyName, Object value) {
        try {
            Method[] methods = type.getMethods();
            String setterName = "set" + propertyName;
            Method setter = null;
            for (Method method : methods) {
                if (setterName.equals(method.getName())) {
                    setter = method;
                    break;
                }
            }
            if (setter == null) throw new NoSuchMethodException();
            if (!Modifier.isStatic(setter.getModifiers()) && object == null) {
                throw new IllegalStateException("Cannot get instance property atom on a class reflector");
            }
            setter.invoke(object, value);
        }
        catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Illegal method name " + propertyName);
        }
        catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the set of all super types (classes and interfaces) of this type.
     * This type itself is also included.
     *
     * @return the set
     */
    public Set<Class<?>> getAllSuperTypes() {
        return superTypes.get(type);
    }

    /**
     * Get the actual type arguments used to extend the given generic base class or interface.
     * (Based on a code copyright 2007 by Ian Robertson).
     *
     * @param base the generic base class or interface
     * @return a list of the raw classes for the actual type arguments.
     */
    @NotNull
    public List<Class<?>> getTypeArguments(@NotNull Class<?> base) {
        Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();

        // first we need to resolve all supertypes up to the required base class or interface
        // and find the right Type for it

        Type type;

        Queue<Type> toCheck = new LinkedList<Type>();
        toCheck.add(this.type);
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
            if (base.equals(Reflector.getClass(type))) break;

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
        List<Class<?>> typeArgumentsAsClasses = Lists.newArrayList();
        // resolve types by chasing down type variables.
        for (Type baseType : actualTypeArguments) {
            while (resolvedTypes.containsKey(baseType)) {
                baseType = resolvedTypes.get(baseType);
            }
            typeArgumentsAsClasses.add(Reflector.getClass(baseType));
        }
        return typeArgumentsAsClasses;
    }

    public Constructor findConstructor(Object[] args) {
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
        throw new RuntimeException("No constructor found for the given arguments");
    }

}

