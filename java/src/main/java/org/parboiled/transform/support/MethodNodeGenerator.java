/*
 * Copyright (c) 2009 Ken Wenzel
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
package org.parboiled.transform.support;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;

public class MethodNodeGenerator extends InsnListGenerator {
    public MethodNode mn;

    public MethodNodeGenerator(MethodNode mn) {
        super(mn);
        this.mn = mn;
    }

    public MethodNodeGenerator(int access, String name, String desc, String signature, String[] exceptions) {
        this(new MethodNode(access, name, desc, signature, exceptions));
    }

    public MethodNodeGenerator(int access, String name, String desc) {
        this(access, name, desc, null, null);
    }

    @SuppressWarnings("unchecked")
    public InsnListGenerator createSubGenerator() {
        return new InsnListGenerator(new MethodNode(mn.access, mn.name, mn.desc, mn.signature, (String[]) mn.exceptions
                .toArray(new String[mn.exceptions.size()])));
    }

    /**
     * Makes the given class visitor visit the generated method.
     * 
     * @param cv
     *            a class visitor.
     */
    public void accept(final ClassVisitor cv) {
        mn.accept(cv);
    }

    /**
     * Makes the given method visitor visit the generated method.
     * 
     * @param mv
     *            a method visitor.
     */
    public void accept(final MethodVisitor mv) {
        mn.accept(mv);
    }
}
