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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.support.Checks;

import static org.parboiled.transform.AsmUtils.createClassReader;
import static org.parboiled.transform.AsmUtils.getExtendedParserClassName;

/**
 * Initializes the basic ParserClassNode fields and collects all methods into the ParserClassNode.allMethods list.
 */
class ClassNodeInitializer extends EmptyVisitor implements ClassTransformer, Opcodes, Types {

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
            ClassReader classReader = createClassReader(ownerClass);
            classReader.accept(this, ClassReader.SKIP_FRAMES);

            ownerClass = ownerClass.getSuperclass();
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (ownerClass == classNode.parentClass) {
            Checks.ensure((access & ACC_FINAL) == 0, "Your parser class '" + name + "' must not be final.");
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

        // do not add methods not returning Rules
        if (!Type.getReturnType(desc).equals(RULE_TYPE)) {
            return null;
        }

        // check, whether we do not already have a method with that name and descriptor
        // if we do we only keep the one we already have since that is the one furthest down in the overriding chain
        for (RuleMethod method : classNode.ruleMethods) {
            if (method.name.equals(name) && method.desc.equals(desc)) return null;
        }

        // ok, its a new Rule method, collect it
        RuleMethod method = new RuleMethod(ownerClass, access, name, desc, signature, exceptions);
        classNode.ruleMethods.add(method);
        return method; // return the newly created method in order to have it "filled" with the supers code
    }

    @Override
    public void visitEnd() {
        classNode.visitEnd();
    }

}
