package org.parboiled;

import org.parboiled.utils.Preconditions;

public class BaseActions implements Actions {

    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    ActionResult setValue(Object value) {
        Preconditions.checkArgument(context != null, "Context not set");
        context.setNodeValue(value);
        return ActionResult.CONTINUE;
    }

}
