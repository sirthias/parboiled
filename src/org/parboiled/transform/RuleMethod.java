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

import com.google.common.base.Preconditions;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodNode;
import org.parboiled.support.Checks;

import java.util.List;

import static org.parboiled.transform.AsmUtils.hasAnnotation;
import static org.parboiled.transform.AsmUtils.isBooleanValueOfZ;

class RuleMethod extends MethodNode implements Opcodes, Types {

    private final Class<?> ownerClass;

    /**
     * Method contains action expressions and therefore needs to be rewritten.
     */
    private boolean toBeRewritten;

    /**
     * Method has no parameters and no @KeepAsIs annotation
     * or is has parameters and a @Cached annotion.
     */
    private boolean toBeCached;

    /**
     * Method has no parameters and no @KeepAsIs annotation
     * or is has parameters and a @Labelled annotion.
     */
    private boolean toBeLabelled;

    /**
     * Method has a @Leaf annotion.
     */
    private boolean toBeLeafed;

    private InstructionGraphNode[] instructionGraphNodes;
    private List<InstructionSubSet> instructionSubSets;

    public RuleMethod(Class<?> ownerClass, int access, String name, String desc, String signature,
                      String[] exceptions) {
        super(access, name, desc, signature, exceptions);
        this.ownerClass = ownerClass;
    }

    public Class<?> getOwnerClass() {
        return ownerClass;
    }

    public boolean isToBeRewritten() {
        return toBeRewritten;
    }

    public boolean isToBeCached() {
        return toBeCached;
    }

    public boolean isToBeLabelled() {
        return toBeLabelled;
    }

    public boolean isToBeLeafed() {
        return toBeLeafed;
    }

    public boolean hasAccess(int access) {
        return (this.access & access) > 0;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        // if the method contains a call to Boolean.valueOf(boolean) is assumed to contain an action expression
        toBeRewritten = toBeRewritten || opcode == INVOKESTATIC && isBooleanValueOfZ(owner, name, desc);
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitEnd() {
        if (Type.getArgumentTypes(desc).length > 0) {
            ensure(!hasAnnotation(this, KEEP_AS_IS_TYPE),
                    "@KeepAsIs annotation not allowed, only allowed on rule methods");
            if (hasAnnotation(this, CACHED_TYPE)) {
                ensure(!hasAccess(ACC_PRIVATE), "@Cached methods must not be private.\n" +
                        "Mark the method protected or package-private if you want to prevent public access!");
                ensure(!hasAccess(ACC_FINAL), "@Cached methods must not be final");
                toBeCached = true;
            }
            if (hasAnnotation(this, LABEL_TYPE)) {
                ensure(!hasAccess(ACC_PRIVATE), "@Label methods must not be private.\n" +
                        "Mark the method protected or package-private if you want to prevent public access!");
                ensure(!hasAccess(ACC_FINAL), "@Label methods must not be final");
                toBeLabelled = true;
            }
            if (hasAnnotation(this, LEAF_TYPE)) {
                ensure(!hasAccess(ACC_PRIVATE), "@Leaf methods must not be private.\n" +
                        "Mark the method protected or package-private if you want to prevent public access!");
                ensure(!hasAccess(ACC_FINAL), "@Leaf methods must not be final");
                toBeLeafed = true;
            }
        } else {
            ensure(!hasAnnotation(this, CACHED_TYPE),
                    "@Cached annotation not allowed, rule is automatically cached");
            if (hasAnnotation(this, KEEP_AS_IS_TYPE)) {
                ensure(!hasAnnotation(this, LABEL_TYPE),
                        "@Label annotation not allowed together with @KeepAsIs");
                ensure(!hasAnnotation(this, LEAF_TYPE),
                        "@Leaf annotation not allowed together with @KeepAsIs");
            } else {
                toBeCached = true;
                toBeLabelled = true;
                toBeLeafed = hasAnnotation(this, LEAF_TYPE);
                ensure(!hasAccess(ACC_PRIVATE), "Rule methods must not be private.\n" +
                        "Mark the method protected or package-private if you want to prevent public access!");
                ensure(!hasAccess(ACC_FINAL),
                        "Rule methods must not be final.");
            }
        }

        super.visitEnd();
    }

    protected void ensure(boolean condition, String errorMessage) {
        Checks.ensure(condition, "Illegal parser rule method '" + name + "':\n" + errorMessage);
    }

    public InstructionGraphNode[] getInstructionGraphNodes() {
        if (instructionGraphNodes == null) {
            instructionGraphNodes = new InstructionGraphNode[instructions.size()];
        }
        return instructionGraphNodes;
    }

    public InstructionGraphNode getReturnNode() {
        InstructionGraphNode node = instructionGraphNodes[instructionGraphNodes.length - 2];
        Preconditions.checkState(node == null || node.instruction.getOpcode() == Opcodes.ARETURN);
        return node;
    }

    public List<InstructionSubSet> getInstructionSubSets() {
        return instructionSubSets;
    }

    protected void setInstructionSubSets(List<InstructionSubSet> instructionSubSets) {
        this.instructionSubSets = instructionSubSets;
    }

}
