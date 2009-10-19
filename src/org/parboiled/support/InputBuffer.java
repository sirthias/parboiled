package org.parboiled.support;

import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

public class InputBuffer {

    private final char[] buffer;

    public InputBuffer(@NotNull String inputText) {
        this.buffer = inputText.toCharArray();
    }

    public InputBuffer(@NotNull char[] buffer) {
        this.buffer = ArrayUtils.clone(buffer);
    }

    public char[] getBuffer() {
        return buffer;
    }

    public char charAt(int index) {
        return index >= 0 && index < buffer.length ? buffer[index] : Chars.EOF;
    }

    @NotNull
    public String extract(int start, int end) {
        if (start < 0) start = 0;
        if (end >= buffer.length) end = buffer.length;
        if (end <= start) return "";
        return new String(buffer, start, end - start);
    }

}

