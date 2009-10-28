package org.parboiled;

import org.jetbrains.annotations.NotNull;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.ParseError;

import java.util.List;

/**
 * A simple container for encapsulating the result of a parsing run.
 */
public class ParsingResult {

    /**
     * The root node of the parse tree created by the parsing run.
     */
    public final Node root;

    /**
     * The list of parse errors created during the parsing run.
     */
    public final List<ParseError> parseErrors;

    /**
     * The underlying input buffer.
     */
    public final InputBuffer inputBuffer;

    ParsingResult(Node root, @NotNull List<ParseError> parseErrors, InputBuffer inputBuffer) {
        this.root = root;
        this.parseErrors = parseErrors;
        this.inputBuffer = inputBuffer;
    }

    /**
     * @return true if the parsing run was completed without errors.
     */
    public boolean hasErrors() {
        return !parseErrors.isEmpty();
    }

}
