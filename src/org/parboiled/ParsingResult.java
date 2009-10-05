package org.parboiled;

import org.parboiled.support.ParseError;
import org.parboiled.support.InputBuffer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ParsingResult {
    public final boolean matched;
    public final Node root;
    public final List<ParseError> parseErrors;
    public final InputBuffer inputBuffer;

    public ParsingResult(boolean matched, Node root, @NotNull List<ParseError> parseErrors, InputBuffer inputBuffer) {
        this.matched = matched;
        this.root = root;
        this.parseErrors = parseErrors;
        this.inputBuffer = inputBuffer;
    }

}
