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

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.support.Checks;

import java.io.IOException;

import static org.parboiled.transform.AsmUtils.createClassReader;
import static org.parboiled.transform.AsmUtils.getExtendedParserClassName;

/**
 * Initializes the basic ParserClassNode fields and collects all methods.
 */
class ClassNodeInitializer extends EmptyVisitor implements Opcodes, Types {

    private ParserClassNode classNode;
    private Class<?> ownerClass;
    private boolean hasExplicitActionOnlyAnnotation;

    public void process(@NotNull ParserClassNode classNode) throws IOException {
        this.classNode = classNode;

        // walk up the parser parent class chain
        ownerClass = classNode.parentClass;
        while (!Object.class.equals(ownerClass)) {
            ClassReader classReader = createClassReader(ownerClass);
            classReader.accept(this, ClassReader.SKIP_FRAMES);
            ownerClass = ownerClass.getSuperclass();
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (ownerClass == classNode.parentClass) {
            Checks.ensure((access & ACC_PRIVATE) == 0, "Parser class '" + name + "' must not be private.");
            Checks.ensure((access & ACC_FINAL) == 0, "Parser class '" + name + "' must not be final.");
            classNode.visit(
                    V1_5,
                    ACC_PUBLIC,
                    getExtendedParserClassName(name),
                    null,
                    classNode.getParentType().getInternalName(),
                    null
            );
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (EXPLICIT_ACTIONS_ONLY_DESC.equals(desc)) {
            hasExplicitActionOnlyAnnotation = true;
            return null;
        }
        // only keep visible annotations on the parser class
        return visible && ownerClass == classNode.parentClass ? classNode.visitAnnotation(desc, true) : null;
    }

    @Override
    public void visitSource(String source, String debug) {
        classNode.visitSource(null, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("<init>".equals(name)) {
            // do not add constructors from super classes or private constructors
            if (ownerClass != classNode.parentClass || (access & ACC_PRIVATE) > 0) {
                return null;
            }
            MethodNode constructor = new MethodNode(access, name, desc, signature, exceptions);
            classNode.constructors.add(constructor);
            return constructor; // return the newly created method in order to have it "filled" with the method code
        }

        // only add methods returning Rules
        if (!Type.getReturnType(desc).equals(RULE)) {
            return null;
        }

        // check, whether we do not already have a method with that name and descriptor
        // if we do we only keep the one we already have since that is the one furthest down in the overriding chain
        for (RuleMethod method : classNode.ruleMethods) {
            if (method.name.equals(name) && method.desc.equals(desc)) return null;
        }

        // ok, its a new Rule method, collect it
        Checks.ensure((access & ACC_PRIVATE) == 0, "Rule method '" + name + "'must not be private.\n" +
                        "Mark the method protected or package-private if you want to prevent public access!");
        Checks.ensure((access & ACC_FINAL) == 0, "Rule method '" + name + "' must not be final.");

        RuleMethod method =
                new RuleMethod(ownerClass, access, name, desc, signature, exceptions, hasExplicitActionOnlyAnnotation);
        classNode.ruleMethods.add(method);
        return method; // return the newly created method in order to have it "filled" with the supers code
    }

    @Override
    public void visitEnd() {
        classNode.visitEnd();
    }
}
