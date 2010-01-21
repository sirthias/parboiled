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

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class ParserClassNode extends ClassNode {

    public final Class<?> parentClass;
    public final Type parentType;
    public final List<Type> superTypes = new ArrayList<Type>();
    public final List<RuleMethodInfo> methodInfos = new ArrayList<RuleMethodInfo>();
    public final List<ActionClassGenerator> actionClassGenerators = new ArrayList<ActionClassGenerator>();
    public MethodNode constructor;
    public byte[] classCode;
    public Class<?> extendedClass;

    public ParserClassNode(Class<?> parentClass) {
        this.parentClass = parentClass;
        parentType = Type.getType(parentClass);
    }

    public Type getParentType() {
        return parentType;
    }

    public String getDescriptor() {
        return 'L' + name + ';';
    }

}
