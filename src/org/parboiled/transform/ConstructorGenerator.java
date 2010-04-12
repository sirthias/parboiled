/*
 * Copyright (C) 2009-2010 Mathias Doenitz
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

package org.parboiled.transform;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.parboiled.support.Checks;

import static org.parboiled.transform.AsmUtils.createArgumentLoaders;

/**
 * Adds one constructor for each of the ParserClassNode.constructors,
 * which simply delegates to the respective super constructor.
 */
class ConstructorGenerator implements Opcodes, Types {

    public void process(@NotNull ParserClassNode classNode) {
        Checks.ensure(!classNode.getConstructors().isEmpty(),
                "Could not extend parser class '%s', no constructor visible to derived classes found",
                classNode.getParentType().getClassName());
        for (MethodNode constructor : classNode.getConstructors()) {
            createConstuctor(classNode, constructor);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void createConstuctor(ParserClassNode classNode, MethodNode constructor) {
        MethodNode newConstructor =
                new MethodNode(ACC_PUBLIC, constructor.name, constructor.desc, constructor.signature,
                        (String[]) constructor.exceptions.toArray(new String[constructor.exceptions.size()]));

        newConstructor.instructions.add(new VarInsnNode(ALOAD, 0));
        newConstructor.instructions.add(createArgumentLoaders(constructor.desc));
        newConstructor.instructions.add(new MethodInsnNode(INVOKESPECIAL,
                classNode.getParentType().getInternalName(), "<init>", constructor.desc));
        newConstructor.instructions.add(new InsnNode(RETURN));

        classNode.methods.add(newConstructor);
    }

}