package org.parboiled;

/**
 * Marker interface used for the return values of parser action methods.
 */
public interface ActionResult {

    /**
     * Return value telling the parser to continue parsing the current rule.
     */
    static final ActionResult CONTINUE = new ActionResult() {};

    /**
     * Return value telling the parser to cancel the current rule match
     * (and either backtrack or generate a parse error).
     */
    static final ActionResult CANCEL_MATCH = new ActionResult() {};

}
