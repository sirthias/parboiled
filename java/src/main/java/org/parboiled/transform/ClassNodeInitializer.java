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

import static org.parboiled.common.Preconditions.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.support.Checks;

import java.io.IOException;

import static org.parboiled.transform.AsmUtils.createClassReader;
import static org.parboiled.transform.AsmUtils.getExtendedParserClassName;
import static org.objectweb.asm.Opcodes.*;

/**
 * Initializes the basic ParserClassNode fields and collects all methods.
 */
class ClassNodeInitializer extends EmptyVisitor {

    private ParserClassNode classNode;
    private Class<?> ownerClass;
    private boolean hasBuildParseTree;
    private boolean hasExplicitActionOnlyAnnotation;
    private boolean hasDontLabelAnnotation;
    private boolean hasSkipActionsInPredicates;

    public void process(ParserClassNode classNode) throws IOException {
        this.classNode = checkArgNotNull(classNode, "classNode");

        // walk up the parser parent class chain
        ownerClass = classNode.getParentClass();
        while (!Object.class.equals(ownerClass)) {
            hasExplicitActionOnlyAnnotation = false;
            hasDontLabelAnnotation = false;
            hasSkipActionsInPredicates = false;

            ClassReader classReader = createClassReader(ownerClass);
            classReader.accept(this, ClassReader.SKIP_FRAMES);
            ownerClass = ownerClass.getSuperclass();
        }

        for (RuleMethod method : classNode.getRuleMethods().values()) {
            // move all flags from the super methods to their overriding methods
            if (method.isSuperMethod()) {
                RuleMethod overridingMethod = classNode.getRuleMethods().get(method.name.substring(1) + method.desc);
                method.moveFlagsTo(overridingMethod);
            } else {
                if (!hasBuildParseTree) {
                    method.suppressNode();
                } else {
                    // as soon as we see the first non-super method we can break since the methods are sorted so that
                    // the super methods precede all others
                    break;
                }
            }
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (ownerClass == classNode.getParentClass()) {
            Checks.ensure((access & ACC_PRIVATE) == 0, "Parser class '%s' must not be private", name);
            Checks.ensure((access & ACC_FINAL) == 0, "Parser class '%s' must not be final.", name);
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
        if (Types.EXPLICIT_ACTIONS_ONLY_DESC.equals(desc)) {
            hasExplicitActionOnlyAnnotation = true;
            return null;
        }
        if (Types.DONT_LABEL_DESC.equals(desc)) {
            hasDontLabelAnnotation = true;
            return null;
        }
        if (Types.SKIP_ACTIONS_IN_PREDICATES_DESC.equals(desc)) {
            hasSkipActionsInPredicates = true;
            return null;
        }
        if (Types.BUILD_PARSE_TREE_DESC.equals(desc)) {
            hasBuildParseTree = true;
            return null;
        }

        // only keep visible annotations on the parser class
        return visible && ownerClass == classNode.getParentClass() ? classNode.visitAnnotation(desc, true) : null;
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
            if (ownerClass != classNode.getParentClass() || (access & ACC_PRIVATE) > 0) {
                return null;
            }
            MethodNode constructor = new MethodNode(access, name, desc, signature, exceptions);
            classNode.getConstructors().add(constructor);
            return constructor; // return the newly created method in order to have it "filled" with the method code
        }

        // only add non-native, non-abstract methods returning Rules
        if (!Type.getReturnType(desc).equals(Types.RULE) || (access & (ACC_NATIVE | ACC_ABSTRACT)) > 0) {
            return null;
        }

        Checks.ensure((access & ACC_PRIVATE) == 0, "Rule method '%s'must not be private.\n" +
                "Mark the method protected or package-private if you want to prevent public access!", name);
        Checks.ensure((access & ACC_FINAL) == 0, "Rule method '%s' must not be final.", name);

        // check, whether we do not already have a method with that name and descriptor
        // if we do we add the method with a "$" prefix in order to have it processed and be able to reference it
        // later if we have to
        String methodKey = name.concat(desc);
        while (classNode.getRuleMethods().containsKey(methodKey)) {
            name = '$' + name;
            methodKey = name.concat(desc);
        }

        RuleMethod method = new RuleMethod(ownerClass, access, name, desc, signature, exceptions,
                hasExplicitActionOnlyAnnotation, hasDontLabelAnnotation, hasSkipActionsInPredicates);
        classNode.getRuleMethods().put(methodKey, method);
        return method; // return the newly created method in order to have it "filled" with the actual method code
    }

    @Override
    public void visitEnd() {
        classNode.visitEnd();
    }
}
