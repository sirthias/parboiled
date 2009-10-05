package org.parboiled.support;

import org.jetbrains.annotations.NotNull;

public class InputBuffer {

    private final char[] buffer;
    private InputLocation cursor = new InputLocation(0, 0, 0);
    private char lookAhead;

    public InputBuffer(@NotNull String inputText) {
        this(inputText.toCharArray());
    }

    public InputBuffer(@NotNull char[] buffer) {
        this.buffer = buffer;
        updateLA();
    }

    public char[] getBuffer() {
        return buffer;
    }

    @NotNull
    public InputLocation getCurrentLocation() {
        return cursor;
    }

    public void rewind(@NotNull InputLocation location) {
        cursor = location;
        updateLA();
    }

    private void updateLA() {
        lookAhead = charAt(cursor.index);
    }

    public char LA() {
        return lookAhead;
    }

    public char charAt(int index) {
        return index >= 0 && index < buffer.length ? buffer[index] : Chars.EOF;
    }

    public char LA(int delta) {
        return charAt(cursor.index + delta);
    }

    public void consume() {
        if (cursor.index < buffer.length) {
            if (LA() == '\n') {
                cursor = new InputLocation(cursor.index + 1, cursor.row + 1, 0);
            } else {
                cursor = new InputLocation(cursor.index + 1, cursor.row, cursor.column + 1);
            }
            updateLA();
        }
    }

    @NotNull
    public String extract(int start, int end) {
        if (start < 0) start = 0;
        if (end >= buffer.length) end = buffer.length;
        if (end <= start) return "";
        return new String(buffer, start, end - start);
    }

}

