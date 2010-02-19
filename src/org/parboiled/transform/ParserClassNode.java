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
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.List;

class ParserClassNode extends ClassNode {

    public final Class<?> parentClass;
    public final Type parentType;
    public final List<Type> superTypes = new ArrayList<Type>();

    public final List<ParserMethod> allMethods = new ArrayList<ParserMethod>();
    public final List<ParserMethod> constructors = new ArrayList<ParserMethod>();
    public final List<ParserMethod> ruleMethods = new ArrayList<ParserMethod>(); // no-arg, Rule returning methods
    public final List<ParserMethod> cachedMethods = new ArrayList<ParserMethod>(); // @Cached, Rule returning methods
    public final List<ParserMethod> labelMethods = new ArrayList<ParserMethod>(); // @Label, Rule returning methods
    public final List<ParserMethod> leafMethods = new ArrayList<ParserMethod>(); // @Leaf, Rule returning methods

    public final List<ActionClassGenerator> actionClassGenerators = new ArrayList<ActionClassGenerator>();
    public byte[] classCode;
    public Class<?> extendedClass;

    public ParserClassNode(@NotNull Class<?> parentClass) {
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
