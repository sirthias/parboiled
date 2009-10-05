package org.parboiled;

import com.google.common.base.Preconditions;

public class BaseActions implements Actions {

    private Context context;

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        Preconditions.checkArgument(this.context == null, "Context cannot be changed once set");
        this.context = context;
    }

    ActionResult setValue(Object value) {
        Preconditions.checkArgument(context != null, "Context not set");
        context.setNodeValue(value);
        return ActionResult.CONTINUE;
    }

}
