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
class MethodCategorizer implements ClassTransformer, Opcodes, Types {
    private final ClassTransformer nextTransformer;
    private ParserMethod method;

    public MethodCategorizer(ClassTransformer nextTransformer) {
        this.nextTransformer = nextTransformer;
    }

    @SuppressWarnings({"unchecked"})
    public ParserClassNode transform(@NotNull ParserClassNode classNode) throws Exception {
        for (ParserMethod method : classNode.allMethods) {
            this.method = method;
            categorize(classNode);
        }

        classNode.methods.addAll(classNode.ruleMethods);
        for (ParserMethod method : classNode.cachedMethods) {
            if (!classNode.methods.contains(method)) classNode.methods.add(method);
        }
        for (ParserMethod method : classNode.labelMethods) {
            if (!classNode.methods.contains(method)) classNode.methods.add(method);
        }
        for (ParserMethod method : classNode.leafMethods) {
            if (!classNode.methods.contains(method)) classNode.methods.add(method);
        }

        return nextTransformer != null ? nextTransformer.transform(classNode) : classNode;
    }

    private void categorize(ParserClassNode classNode) {
        // collect all constructors for later creation of delegation constructors
        if ("<init>".equals(method.name) &&
                method.ownerClass == classNode.parentClass &&
                !method.hasAccess(ACC_PRIVATE)) {
            Checks.ensure(!carriesAnnotation(method, CACHED_TYPE), "@Cached not allowed on construcors");
            Checks.ensure(!carriesAnnotation(method, KEEP_AS_IS_TYPE), "@KeepAsIs not allowed on construcors");
            Checks.ensure(!carriesAnnotation(method, LABEL_TYPE), "@Label not allowed on construcors");
            Checks.ensure(!carriesAnnotation(method, LEAF_TYPE), "@Leaf not allowed on construcors");
            classNode.constructors.add(method);
            return;
        }

        if (Type.getReturnType(method.desc).equals(RULE_TYPE) &&
                Type.getArgumentTypes(method.desc).length == 0) {
            if (!carriesAnnotation(method, KEEP_AS_IS_TYPE)) {
                ensure(!method.hasAccess(ACC_PRIVATE), "Rule methods must not be private.\n" +
                        "Mark the method protected or package-private if you want to prevent public access!");
                ensure(!method.hasAccess(ACC_FINAL),
                        "Rule methods must not be final.");
                classNode.ruleMethods.add(method);
            }
            ensure(!carriesAnnotation(method, CACHED_TYPE),
                    "@Cached annotation not allowed, rule is automatically cached");
        } else {
            Checks.ensure(!carriesAnnotation(method, KEEP_AS_IS_TYPE),
                    "@KeepAsIs not allowed on method '" + method.name + "', only allowed on rule methods");
        }

        if (carriesAnnotation(method, CACHED_TYPE)) {
            ensure(Type.getReturnType(method.desc).equals(RULE_TYPE),
                    "@Cached only allowed on rule creating methods taking at least one parameter");
            ensure(!method.hasAccess(ACC_PRIVATE), "@Cached methods must not be private.\n" +
                    "Mark the method protected or package-private if you want to prevent public access!");
            ensure(!method.hasAccess(ACC_FINAL), "@Cached methods must not be final");
            classNode.cachedMethods.add(method);
        }

        if (carriesAnnotation(method, LABEL_TYPE)) {
            ensure(Type.getReturnType(method.desc).equals(RULE_TYPE),
                    "@Label only allowed on rule creating methods");
            ensure(!method.hasAccess(ACC_PRIVATE), "@Label methods must not be private.\n" +
                    "Mark the method protected or package-private if you want to prevent public access!");
            ensure(!method.hasAccess(ACC_FINAL), "@Label methods must not be final");
            classNode.labelMethods.add(method);
        }

        if (carriesAnnotation(method, LEAF_TYPE)) {
            ensure(Type.getReturnType(method.desc).equals(RULE_TYPE),
                    "@Leaf only allowed on rule creating methods");
            ensure(!method.hasAccess(ACC_PRIVATE), "@Leaf methods must not be private.\n" +
                    "Mark the method protected or package-private if you want to prevent public access!");
            ensure(!method.hasAccess(ACC_FINAL), "@Leaf methods must not be final");
            classNode.leafMethods.add(method);
        }
    }

    private void ensure(boolean condition, String errorMessage) {
        Checks.ensure(condition, "Illegal parser rule method '" + method.name + "':\n" + errorMessage);
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
