package org.parboiled.support;

public class InputLocation {
    public final int index;
    public final int row;
    public final int column;

    public InputLocation(int index, int row, int column) {
        this.index = index;
        this.row = row;
        this.column = column;
    }

}
