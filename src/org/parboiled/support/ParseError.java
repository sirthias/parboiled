package org.parboiled.support;

import org.jetbrains.annotations.NotNull;
import org.parboiled.Context;
import org.parboiled.Matcher;
import org.parboiled.Node;
import org.parboiled.utils.StringUtils2;

public class ParseError {

    private final Context context;
    private final InputLocation errorStart;
    private final InputLocation errorEnd;
    private final Matcher failedMatcher;
    private final Node node;
    private final String errorMessage;

    public ParseError(@NotNull Context context, InputLocation errorStart, InputLocation errorEnd,
                      Matcher failedMatcher, Node node, @NotNull String errorMessage) {
        this.context = context;
        this.errorStart = errorStart;
        this.errorEnd = errorEnd;
        this.failedMatcher = failedMatcher;
        this.node = node;
        this.errorMessage = errorMessage;
    }

    @NotNull
    public Context getContext() {
        return context;
    }

    public InputLocation getErrorStart() {
        return errorStart;
    }

    public InputLocation getErrorEnd() {
        return errorEnd;
    }

    public Matcher getFailedMatcher() {
        return failedMatcher;
    }

    public Node getNode() {
        return node;
    }

    @NotNull
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + errorMessage;
    }

    public static String createMessageSuffix(@NotNull InputLocation start, @NotNull InputLocation end) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(" (line %s, pos %s):", start.row + 1, start.column + 1));
        sb.append('\n');

        String line = StringUtils2.getLine(String.valueOf(start.inputBuffer.getBuffer()), start.row);
        sb.append(line);
        sb.append('\n');

        int charCount = Math
                .min(start.row == end.row ? end.index - start.index : 1000, StringUtils2.length(line) - start.column);
        sb.append(StringUtils2.repeat(' ', start.column));
        sb.append(StringUtils2.repeat('^', Math.max(charCount, 1)));
        sb.append('\n');

        return sb.toString();
    }

}

