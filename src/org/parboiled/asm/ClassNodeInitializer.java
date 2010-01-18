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

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.MethodNode;

import java.io.IOException;

public class ClassNodeInitializer extends EmptyVisitor implements Opcodes {

    private final ParserClassNode classNode;
    private boolean collectFromSuperClasses;

    public ClassNodeInitializer(ParserClassNode classNode) {
        this.classNode = classNode;
    }

    public void initialize() throws IOException {
        for (int i = 0; i < classNode.ownerTypes.size(); i++) {
            collectFromSuperClasses = i > 0;
            Type type = classNode.ownerTypes.get(i);
            ClassReader classReader = new ClassReader(type.getClassName());
            classReader.accept(this, ClassReader.SKIP_FRAMES);
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (!collectFromSuperClasses) {
            classNode.visit(version, access, name, signature, superName, interfaces);
        }
    }

    @Override
    public void visitSource(String source, String debug) {
        classNode.visitSource(source, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (isRuleCreatingMethod(desc)) {
            // create method overriding original rule creating method copying the implementation from the super class
            MethodNode method = new MethodNode(access, name, desc, signature, exceptions);
            classNode.methods.add(method);
            return method; // return the newly created method in order to have it "filled" with the supers code
        }
        return null;
    }

    private boolean isRuleCreatingMethod(String methodDesc) {
        return Types.RULE_TYPE.equals(Type.getReturnType(methodDesc)) && Type.getArgumentTypes(methodDesc).length == 0;
    }

    @Override
    public void visitEnd() {
        classNode.visitEnd();
    }

}
