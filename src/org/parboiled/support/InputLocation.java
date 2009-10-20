package org.parboiled.support;

import org.jetbrains.annotations.NotNull;

public class InputLocation {
    public final InputBuffer inputBuffer;
    public final int index;
    public final int row;
    public final int column;
    public final char currentChar;

    public InputLocation(@NotNull InputBuffer inputBuffer) {
        this(inputBuffer, 0, 0, 0);
    }

    private InputLocation(@NotNull InputBuffer inputBuffer, int index, int row, int column) {
        this.inputBuffer = inputBuffer;
        this.index = index;
        this.row = row;
        this.column = column;
        this.currentChar = inputBuffer.charAt(index);
    }

    public InputLocation advance() {
        switch (currentChar) {
            case Chars.EOF:
                return this;
            case '\n':
                return new InputLocation(inputBuffer, index + 1, row + 1, 0);
            default:
                return new InputLocation(inputBuffer, index + 1, row, column + 1);
        }
    }

    public char lookAhead(int delta) {
        return inputBuffer.charAt(index + delta);
    }

}

