package org.parboiled;

/**
 * Convenience base class for parser actions. Provides a base implementation of the Actions interface as well
 * as one very common used action.
 */
public class BaseActions implements Actions {

    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Sets the value of the parse tree node to be created for the current rule to the given object.
     *
     * @param value the object to be set as value object
     * @return ActionResult.CONTINUE
     */
    ActionResult setValue(Object value) {
        getContext().setNodeValue(value);
        return ActionResult.CONTINUE;
    }

}
