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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

class ParserClassNode extends ClassNode {

    private final Class<?> parentClass;
    private final Type parentType;
    private final List<MethodNode> constructors = new ArrayList<MethodNode>();
    private final Map<String, RuleMethod> ruleMethods = new TreeMap<String, RuleMethod>();
    private byte[] classCode;
    private Class<?> extendedClass;

    public ParserClassNode(Class<?> parentClass) {
        this.parentClass = checkArgNotNull(parentClass, "parentClass");
        parentType = Type.getType(parentClass);
    }

    public Class<?> getParentClass() {
        return parentClass;
    }

    public Type getParentType() {
        return parentType;
    }

    public List<MethodNode> getConstructors() {
        return constructors;
    }

    public Map<String, RuleMethod> getRuleMethods() {
        return ruleMethods;
    }

    public byte[] getClassCode() {
        return classCode;
    }

    public void setClassCode(byte[] classCode) {
        this.classCode = classCode;
    }

    public Class<?> getExtendedClass() {
        return extendedClass;
    }

    public void setExtendedClass(Class<?> extendedClass) {
        this.extendedClass = extendedClass;
    }
}
