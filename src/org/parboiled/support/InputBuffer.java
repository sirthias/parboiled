package org.parboiled.support;

import org.jetbrains.annotations.NotNull;

/**
 * Simple immutable wrapper around a char[] buffer providing basic access methods.
 */
public class InputBuffer {

    private final char[] buffer;

    /**
     * The input text to create the InputBuffer from.
     * @param inputText the text
     */
    public InputBuffer(@NotNull String inputText) {
        this.buffer = inputText.toCharArray();
    }

    /**
     * The input buffer to wrap.
     * CAUTION: For performance reasons the given char array is not defensively copied.
     * @param buffer the chars
     */
    public InputBuffer(@NotNull char[] buffer) {
        this.buffer = buffer;
    }

    /**
     * Returns the underlying buffer.
     * CAUTION: For performance reasons the returned char array is not a defensive copy but the actual input buffer
     * instance.
     * @return the characters forming the input
     */
    public char[] getBuffer() {
        return buffer;
    }

    /**
     * Returns the character at the given index. If the index is invalid the method returns Chars.EOI.
     * @param index the index
     * @return the character at the given index or Chars.EOI.
     */
    public char charAt(int index) {
        return index >= 0 && index < buffer.length ? buffer[index] : Chars.EOI;
    }

    /**
     * Constructs a new String object from all character between the given indices.
     * Invalid indices are automatically adjusted to their respective boundary.
     * @param start the start index (inclusively)
     * @param end the end index (exclusively)
     * @return a new String object (non-interned)
     */
    @NotNull
    public String extract(int start, int end) {
        if (start < 0) start = 0;
        if (end >= buffer.length) end = buffer.length;
        if (end <= start) return "";
        return new String(buffer, start, end - start);
    }

}

