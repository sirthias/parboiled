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

package org.parboiled.asm;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.support.Checks;

public class ClassNodeInitializer implements ClassVisitor, ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;
    private ParserClassNode classNode;
    private boolean inParentClass;

    public ClassNodeInitializer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        this.classNode = classNode;

        // walk up the parser parent class chain
        Class<?> parentClass = classNode.parentClass;
        while (!Object.class.equals(parentClass)) {
            Type superType = Type.getType(parentClass);

            // initialize classNode super types list
            classNode.superTypes.add(superType);

            // extract methods from super type
            ClassReader classReader = new ClassReader(superType.getClassName());
            classReader.accept(this, ClassReader.SKIP_FRAMES);

            parentClass = parentClass.getSuperclass();
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        inParentClass = classNode.name == null;
        if (inParentClass) {
            Checks.ensure((access & ACC_FINAL) == 0, "Your parser class '" + name + "' must not be final.");
            classNode.visit(V1_5, ACC_PUBLIC, name + "$$parboiled", null, classNode.getParentType().getInternalName(),
                    null);
        }
    }

    public void visitSource(String source, String debug) {
        classNode.visitSource(source, debug);
    }

    @SuppressWarnings("unchecked")
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isRuleCreatingMethod(desc)) {
            Checks.ensure((access & ACC_PRIVATE) == 0,
                    "Illegal parser rule definition '" + name + "':\nRule definition methods must not be private.\n" +
                            "Mark the method protected or package-private if you want to prevent public access!");
            Checks.ensure((access & ACC_FINAL) == 0,
                    "Illegal parser rule definition '" + name + "':\nRule definition methods must not be final.");

            // create method overriding original rule creating method copying the implementation from the super class
            MethodNode method = new MethodNode(access, name, desc, signature, exceptions);
            classNode.methods.add(method);
            return method; // return the newly created method in order to have it "filled" with the supers code
        }

        if (inParentClass && "<init>".equals(name) && (access & ACC_PRIVATE) == 0) {
            classNode.constructors.add(new MethodNode(ACC_PUBLIC, name, desc, signature, exceptions));
        }
        return null;
    }

    private boolean isRuleCreatingMethod(String methodDesc) {
        return AsmUtils.RULE_TYPE.equals(Type.getReturnType(methodDesc)) && Type
                .getArgumentTypes(methodDesc).length == 0;
    }

    public void visitEnd() {
        classNode.visitEnd();
    }

    //********************* unused ClassVisitor members *****************************

    public void visitOuterClass(String owner, String name, String desc) {}

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {}

    public void visitInnerClass(String name, String outerName, String innerName, int access) {}

    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        return null;
    }
}
