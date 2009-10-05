package org.parboiled;

public interface ActionResult {

    static final ActionResult CONTINUE = new ActionResult() {};
    static final ActionResult CANCEL_MATCH = new ActionResult() {};

}
