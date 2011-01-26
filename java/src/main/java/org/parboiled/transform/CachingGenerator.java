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

package org.parboiled.transform;

import static org.objectweb.asm.Opcodes.*;
import static org.parboiled.common.Preconditions.*;
import static org.parboiled.common.Utils.toObjectArray;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Wraps the method code with caching and proxying constructs.
 */
class CachingGenerator implements RuleMethodProcessor {

    private ParserClassNode classNode;
    private RuleMethod method;
    private Type[] paramTypes;
    private InsnList instructions;
    private AbstractInsnNode current;
    private String cacheFieldName;

    public boolean appliesTo(ParserClassNode classNode, RuleMethod method) {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        return method.hasCachedAnnotation();
    }

    public void process(ParserClassNode classNode, RuleMethod method) throws Exception {
        checkArgNotNull(classNode, "classNode");
        checkArgNotNull(method, "method");
        checkState(!method.isSuperMethod()); // super methods have flag moved to the overriding method

        this.classNode = classNode;
        this.method = method;
        this.paramTypes = getParameterTypes();
        this.instructions = method.instructions;
        this.current = instructions.getFirst();

        generateCacheHitReturn();
        generateStoreNewProxyMatcher();
        seekToReturnInstruction();
        generateArmProxyMatcher();
        generateStoreInCache();
    }

    // if (<cache> != null) return <cache>;
    private void generateCacheHitReturn() {
        // stack:
        generateGetFromCache();
        // stack: <cachedValue>
        insert(new InsnNode(DUP));
        // stack: <cachedValue> :: <cachedValue>
        LabelNode cacheMissLabel = new LabelNode();
        insert(new JumpInsnNode(IFNULL, cacheMissLabel));
        // stack: <cachedValue>
        insert(new InsnNode(ARETURN));
        // stack: <null>
        insert(cacheMissLabel);
        // stack: <null>
        insert(new InsnNode(POP));
        // stack:
    }
    
	private Type[] getParameterTypes() {
		List<Type> paramTypes = new ArrayList<Type>(Arrays.asList(Type.getArgumentTypes(method.desc)));

		int i = (method.access & ACC_STATIC) != 0 ? 0 : 1;
		for (Iterator<Type> it = paramTypes.iterator(); it.hasNext(); ) {
			it.next();
			
			if (method.getActionParams().get(i)) {
				it.remove();
			}
			i++;
		}
		
		return paramTypes.toArray(new Type[paramTypes.size()]);
	}

    @SuppressWarnings( {"unchecked"})
    private void generateGetFromCache() {
        cacheFieldName = findUnusedCacheFieldName();

        // if we have no parameters we use a simple Rule field as cache, otherwise a HashMap
        String cacheFieldDesc = paramTypes.length == 0 ? Types.RULE_DESC : "Ljava/util/HashMap;";
        classNode.fields.add(new FieldNode(ACC_PRIVATE, cacheFieldName, cacheFieldDesc, null, null));

        // stack:
        insert(new VarInsnNode(ALOAD, 0));
        // stack: <this>
        insert(new FieldInsnNode(GETFIELD, classNode.name, cacheFieldName, cacheFieldDesc));
        // stack: <cache>

        if (paramTypes.length == 0) return; // if we have no parameters we are done

        // generate: if (<cache> == null) <cache> = new HashMap<Object, Rule>();

        // stack: <hashMap>
        insert(new InsnNode(DUP));
        // stack: <hashMap> :: <hashMap>
        LabelNode alreadyInitialized = new LabelNode();
        insert(new JumpInsnNode(IFNONNULL, alreadyInitialized));
        // stack: <null>
        insert(new InsnNode(POP));
        // stack:
        insert(new VarInsnNode(ALOAD, 0));
        // stack: <this>
        insert(new TypeInsnNode(NEW, "java/util/HashMap"));
        // stack: <this> :: <hashMap>
        insert(new InsnNode(DUP_X1));
        // stack: <hashMap> :: <this> :: <hashMap>
        insert(new InsnNode(DUP));
        // stack: <hashMap> :: <this> :: <hashMap> :: <hashMap>
        insert(new MethodInsnNode(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V"));
        // stack: <hashMap> :: <this> :: <hashMap>
        insert(new FieldInsnNode(PUTFIELD, classNode.name, cacheFieldName, cacheFieldDesc));
        // stack: <hashMap>
        insert(alreadyInitialized);
        // stack: <hashMap>

        // if we have more than one parameter or the parameter is an array we have to wrap with our Arguments class
        // since we need to unroll all inner arrays and apply custom hashCode(...) and equals(...) implementations
        if (paramTypes.length > 1 || paramTypes[0].getSort() == Type.ARRAY) {
            // generate: push new Arguments(new Object[] {<params>})

            String arguments = Type.getInternalName(Arguments.class);
            // stack: <hashMap>
            insert(new TypeInsnNode(NEW, arguments));
            // stack: <hashMap> :: <arguments>
            insert(new InsnNode(DUP));
            // stack: <hashMap> :: <arguments> :: <arguments>
            generatePushNewParameterObjectArray(paramTypes);
            // stack: <hashMap> :: <arguments> :: <arguments> :: <array>
            insert(new MethodInsnNode(INVOKESPECIAL, arguments, "<init>", "([Ljava/lang/Object;)V"));
            // stack: <hashMap> :: <arguments>
        } else {
            // stack: <hashMap>
            generatePushParameterAsObject(paramTypes, 0);
            // stack: <hashMap> :: <param>
        }

        // generate: <hashMap>.get(...)

        // stack: <hashMap> :: <mapKey>
        insert(new InsnNode(DUP));
        // stack: <hashMap> :: <mapKey> :: <mapKey>
        insert(new VarInsnNode(ASTORE, method.maxLocals));
        // stack: <hashMap> :: <mapKey>
        insert(new MethodInsnNode(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;"));
        // stack: <object>
        insert(new TypeInsnNode(CHECKCAST, Types.RULE.getInternalName()));
        // stack: <rule>
    }

    @SuppressWarnings( {"unchecked"})
    private String findUnusedCacheFieldName() {
        String name = "cache$" + method.name;
        int i = 2;
        while (hasField(name)) {
            name = "cache$" + method.name + i++;
        }
        return name;
    }

    public boolean hasField(String fieldName) {
        for (Object field : classNode.fields) {
            if (fieldName.equals(((FieldNode) field).name)) return true;
        }
        return false;
    }

    private void generatePushNewParameterObjectArray(Type[] paramTypes) {
        // stack: ...
        insert(new IntInsnNode(BIPUSH, paramTypes.length));
        // stack: ... :: <length>
        insert(new TypeInsnNode(ANEWARRAY, "java/lang/Object"));
        // stack: ... :: <array>

        for (int i = 0; i < paramTypes.length; i++) {
            // stack: ... :: <array>
            insert(new InsnNode(DUP));
            // stack: ... :: <array> :: <array>
            insert(new IntInsnNode(BIPUSH, i));
            // stack: ... :: <array> :: <array> :: <index>
            generatePushParameterAsObject(paramTypes, i);
            // stack: ... :: <array> :: <array> :: <index> :: <param>
            insert(new InsnNode(AASTORE));
            // stack: ... :: <array>
        }
        // stack: ... :: <array>
    }

    private void generatePushParameterAsObject(Type[] paramTypes, int parameterNr) {
        switch (paramTypes[parameterNr++].getSort()) {
            case Type.BOOLEAN:
                insert(new VarInsnNode(ILOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;"));
                return;
            case Type.CHAR:
                insert(new VarInsnNode(ILOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;"));
                return;
            case Type.BYTE:
                insert(new VarInsnNode(ILOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;"));
                return;
            case Type.SHORT:
                insert(new VarInsnNode(ILOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;"));
                return;
            case Type.INT:
                insert(new VarInsnNode(ILOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;"));
                return;
            case Type.FLOAT:
                insert(new VarInsnNode(FLOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;"));
                return;
            case Type.LONG:
                insert(new VarInsnNode(LLOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;"));
                return;
            case Type.DOUBLE:
                insert(new VarInsnNode(DLOAD, parameterNr));
                insert(new MethodInsnNode(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;"));
                return;
            case Type.ARRAY:
            case Type.OBJECT:
                insert(new VarInsnNode(ALOAD, parameterNr));
                return;
            case Type.VOID:
            default:
                throw new IllegalStateException();
        }
    }

    // <cache> = new ProxyMatcher();
    private void generateStoreNewProxyMatcher() {
        String proxyMatcherType = Types.PROXY_MATCHER.getInternalName();

        // stack:
        insert(new TypeInsnNode(NEW, proxyMatcherType));
        // stack: <proxyMatcher>
        insert(new InsnNode(DUP));
        // stack: <proxyMatcher> :: <proxyMatcher>
        insert(new MethodInsnNode(INVOKESPECIAL, proxyMatcherType, "<init>", "()V"));
        // stack: <proxyMatcher>
        generateStoreInCache();
        // stack: <proxyMatcher>
    }

    private void seekToReturnInstruction() {
        while (current.getOpcode() != ARETURN) {
            current = current.getNext();
        }
    }

    // <proxyMatcher>.arm(<rule>)
    private void generateArmProxyMatcher() {
        String proxyMatcherType = Types.PROXY_MATCHER.getInternalName();

        // stack: <proxyMatcher> :: <rule>
        insert(new InsnNode(DUP_X1));
        // stack: <rule> :: <proxyMatcher> :: <rule>
        insert(new TypeInsnNode(CHECKCAST, Types.MATCHER.getInternalName()));
        // stack: <rule> :: <proxyMatcher> :: <matcher>
        insert(new MethodInsnNode(INVOKEVIRTUAL, proxyMatcherType, "arm", '(' + Types.MATCHER_DESC + ")V"));
        // stack: <rule>
    }

    private void generateStoreInCache() {
        // stack: <rule>
        insert(new InsnNode(DUP));
        // stack: <rule> :: <rule>

        if (paramTypes.length == 0) {
            // stack: <rule> :: <rule>
            insert(new VarInsnNode(ALOAD, 0));
            // stack: <rule> :: <rule> :: <this>
            insert(new InsnNode(SWAP));
            // stack: <rule> :: <this> :: <rule>
            insert(new FieldInsnNode(PUTFIELD, classNode.name, cacheFieldName, Types.RULE_DESC));
            // stack: <rule>
            return;
        }

        // stack: <rule> :: <rule>
        insert(new VarInsnNode(ALOAD, method.maxLocals));
        // stack: <rule> :: <rule> :: <mapKey>
        insert(new InsnNode(SWAP));
        // stack: <rule> :: <mapKey> :: <rule>
        insert(new VarInsnNode(ALOAD, 0));
        // stack: <rule> :: <mapKey> :: <rule> :: <this>
        insert(new FieldInsnNode(GETFIELD, classNode.name, cacheFieldName, "Ljava/util/HashMap;"));
        // stack: <rule> :: <mapKey> :: <rule> :: <hashMap>
        insert(new InsnNode(DUP_X2));
        // stack: <rule> :: <hashMap> :: <mapKey> :: <rule> :: <hashMap>
        insert(new InsnNode(POP));
        // stack: <rule> :: <hashMap> :: <mapKey> :: <rule>
        insert(new MethodInsnNode(INVOKEVIRTUAL, "java/util/HashMap", "put",
                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"));
        // stack: <rule> :: <null>
        insert(new InsnNode(POP));
        // stack: <rule>
    }

    private void insert(AbstractInsnNode instruction) {
        instructions.insertBefore(current, instruction);
    }

    public static class Arguments {
        private final Object[] params;

        public Arguments(Object[] params) {
            // we need to "unroll" all inner Object arrays
            List<Object> list = new ArrayList<Object>();
            unroll(params, list);
            this.params = list.toArray();
        }

        private void unroll(Object[] params, List<Object> list) {
            for (Object param : params) {
                if (param != null && param.getClass().isArray()) {
                    switch (Type.getType(param.getClass().getComponentType()).getSort()) {
                        case Type.BOOLEAN:
                            unroll(toObjectArray((boolean[]) param), list);
                            continue;
                        case Type.BYTE:
                            unroll(toObjectArray((byte[]) param), list);
                            continue;
                        case Type.CHAR:
                            unroll(toObjectArray((char[]) param), list);
                            continue;
                        case Type.DOUBLE:
                            unroll(toObjectArray((double[]) param), list);
                            continue;
                        case Type.FLOAT:
                            unroll(toObjectArray((float[]) param), list);
                            continue;
                        case Type.INT:
                            unroll(toObjectArray((int[]) param), list);
                            continue;
                        case Type.LONG:
                            unroll(toObjectArray((long[]) param), list);
                            continue;
                        case Type.SHORT:
                            unroll(toObjectArray((short[]) param), list);
                            continue;
                        case Type.OBJECT:
                        case Type.ARRAY:
                            unroll((Object[]) param, list);
                            continue;
                        default:
                            throw new IllegalStateException();
                    }
                }
                list.add(param);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Arguments)) return false;
            Arguments that = (Arguments) o;
            return Arrays.equals(params, that.params);
        }

        @Override
        public int hashCode() {
            return params != null ? Arrays.hashCode(params) : 0;
        }
    }
}
