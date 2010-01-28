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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.support.Checks;

/**
 * Iterates through ParserClassNode.allMethods and sorts them into the three categories
 * - constructors
 * - rule methods
 * - cached methods
 */
class MethodCategorizer implements ClassTransformer, Opcodes {
    private final ClassTransformer nextTransformer;

    public MethodCategorizer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (ParserMethod method : classNode.allMethods) {
            categorize(classNode, method);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void categorize(ParserClassNode classNode, ParserMethod method) {
        // collect all constructors for later creation of delegation constructors
        if ("<init>".equals(method.name) &&
                method.ownerClass == classNode.parentClass &&
                !method.hasAccess(ACC_PRIVATE)) {
            Checks.ensure(!carriesAnnotation(method, AsmUtils.CACHED_TYPE), "@Cached not allowed on construcors");
            Checks.ensure(!carriesAnnotation(method, AsmUtils.KEEP_AS_IS_TYPE), "@KeepAsIs not allowed on construcors");
            classNode.constructors.add(method);
            return;
        }

        if (Type.getReturnType(method.desc).equals(AsmUtils.RULE_TYPE) &&
                Type.getArgumentTypes(method.desc).length == 0) {
            if (!carriesAnnotation(method, AsmUtils.KEEP_AS_IS_TYPE)) {
                Checks.ensure(!method.hasAccess(ACC_PRIVATE),
                        "Illegal parser rule definition '" + method.name + "':\n" +
                                "Rule definition methods must not be private.\n" +
                                "Mark the method protected or package-private if you want to prevent public access!");
                Checks.ensure(!method.hasAccess(ACC_FINAL),
                        "Illegal parser rule definition '" + method.name + "':\n" +
                                "Rule definition methods must not be final.");
                Checks.ensure(!carriesAnnotation(method, AsmUtils.CACHED_TYPE),
                        "Illegal parser rule definition '" + method.name + "':\n" +
                                "@Cached annotation not allowed, rule is automatically cached");
                classNode.ruleMethods.add(method);
            }
            return;
        }

        if (carriesAnnotation(method, AsmUtils.CACHED_TYPE)) {
            Checks.ensure(Type.getReturnType(method.desc).equals(AsmUtils.RULE_TYPE),
                    "@Cached not allowed on method '" + method.name +
                            "', only allowed on rule creating methods taking at least one parameter");
            classNode.cachedMethods.add(method);
        }

        Checks.ensure(!carriesAnnotation(method, AsmUtils.KEEP_AS_IS_TYPE),
                "@KeepAsIs not allowed on method '" + method.name + "', only allowed on rule definition methods");
    }

    private boolean carriesAnnotation(MethodNode method, Type annotationType) {
        if (method.visibleAnnotations != null) {
            for (Object annotationObj : method.visibleAnnotations) {
                AnnotationNode annotation = (AnnotationNode) annotationObj;
                if (annotation.desc.equals(annotationType.getDescriptor())) return true;
            }
        }
        return false;
    }
}
