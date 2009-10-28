package org.parboiled;

/**
 * Interface that has to be implemented by classes containing parser action methods.
 */
public interface Actions {

    /**
     * Called immediately before any parser action method invocation. Informs the Actions object about the
     * Context to be used for the coming action call.
     * @param context the context
     */
    void setContext(Context context);

}
