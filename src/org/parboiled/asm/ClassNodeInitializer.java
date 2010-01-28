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
import org.objectweb.asm.*;
import org.parboiled.support.Checks;

/**
 * Initializes the basic ParserClassNode fields and collects all methods into the ParserClassNode.allMethods list.
 */
class ClassNodeInitializer implements ClassVisitor, ClassTransformer, Opcodes {

    private final ClassTransformer nextTransformer;
    private ParserClassNode classNode;
    private Class<?> ownerClass;

    public ClassNodeInitializer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        this.classNode = classNode;

        // walk up the parser parent class chain
        ownerClass = classNode.parentClass;
        while (!Object.class.equals(ownerClass)) {
            Type ownerType = Type.getType(ownerClass);

            // initialize classNode super types list
            classNode.superTypes.add(ownerType);

            // extract methods from super type
            ClassReader classReader = new ClassReader(ownerType.getClassName());
            classReader.accept(this, ClassReader.SKIP_FRAMES);

            ownerClass = ownerClass.getSuperclass();
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (ownerClass == classNode.parentClass) {
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
        // check, whether we do not already have a method with that name and descriptor
        // if we do we only keep the one we already have since that is the one furthest down in the overriding chain
        for (ParserMethod method : classNode.allMethods) {
            if (method.name.equals(name) && method.desc.equals(desc)) return null;
        }

        // ok, its a new method, collect it
        ParserMethod method = new ParserMethod(ownerClass, access, name, desc, signature, exceptions);
        classNode.allMethods.add(method);
        return method; // return the newly created method in order to have it "filled" with the supers code
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
