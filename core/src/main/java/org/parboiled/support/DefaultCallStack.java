package org.parboiled.support;

public class DefaultCallStack implements CallStack {
    protected static class Frame {
        Object[] arguments;
        Object[] locals;
        Frame previous;

        public Frame(Object[] arguments, Object[] locals, Frame previous) {
            this.arguments = arguments;
            this.locals = locals;
            this.previous = previous;
        }
    }

    private static Object[] EMPTY = new Object[0];

    protected Frame currentFrame;
    protected Object[] nextArguments;

    @Override
    public Object[] getArguments() {
        return currentFrame.arguments;
    }

    @Override
    public Object[] getLocals() {
        return currentFrame.locals;
    }

    @Override
    public Object getVariable(int i) {
        if (i < currentFrame.arguments.length) {
            return currentFrame.arguments[i];
        }
        return currentFrame.locals[i - currentFrame.arguments.length];
    }

    @Override
    public void popFrame() {
        currentFrame = currentFrame.previous;
    }

    @Override
    public void pushFrame(int numLocals) {
        currentFrame = new Frame(nextArguments == null ? EMPTY : nextArguments, numLocals == 0 ? EMPTY
                : new Object[numLocals], currentFrame);
        nextArguments = null;
    }

    @Override
    public void setArguments(Object[] args) {
        nextArguments = args;
    }

    @Override
    public void setVariable(int i, Object value) {
        if (i < currentFrame.arguments.length) {
            currentFrame.arguments[i] = value;
        } else {
            currentFrame.locals[i - currentFrame.arguments.length] = value;
        }
    }
}
