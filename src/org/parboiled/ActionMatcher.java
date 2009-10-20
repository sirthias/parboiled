package org.parboiled;

import net.sf.cglib.proxy.MethodProxy;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.ParsingException;
import org.parboiled.utils.ImmutableList;
import org.parboiled.utils.Preconditions;

import java.util.List;
import java.util.Set;

class ActionMatcher extends AbstractRule<Matcher> implements Matcher, ActionResult {

    private final Actions actionsObject;
    private final MethodProxy methodProxy;
    private final Object[] methodArguments;

    public ActionMatcher(Actions actionsObject, MethodProxy methodProxy, Object[] methodArguments) {
        super(ImmutableList.<Matcher>of());
        this.actionsObject = actionsObject;
        this.methodProxy = methodProxy;
        this.methodArguments = methodArguments;
    }

    public Matcher toMatcher() {
        return this;
    }

    public String getLabel() {
        return "action '" + methodProxy.getSignature().getName() + '\'';
    }

    public boolean match(@NotNull MatcherContext context, boolean enforced) {
        context = (MatcherContext) context.getParent(); // actions want to operate in the parent scope
        Object result;
        try {
            actionsObject.setContext(context);
            result = methodProxy.invokeSuper(actionsObject, buildArguments(context));
        } catch (ParsingException pex) {
            context.addActionError(pex.getMessage());
            return false;
        } catch (Throwable e) {
            throw new RuntimeException("Error during execution of " + context.getPath(), e);
        }
        Preconditions.checkState(result instanceof ActionResult);
        return result == ActionResult.CONTINUE;
    }

    private Object[] buildArguments(@NotNull MatcherContext context) {
        Object[] args = new Object[methodArguments.length];
        for (int i = 0; i < methodArguments.length; i++) {
            Object methodArgument = methodArguments[i];
            if (methodArgument instanceof ActionParameter) {
                methodArgument = ((ActionParameter) methodArgument).getValue(context);
            } else if (methodArgument == BaseParser.NULL) {
                methodArgument = null;
            }
            args[i] = methodArgument;
        }
        return args;
    }

    @NotNull
    public List<Matcher> getChildren() {
        return ImmutableList.of();
    }

    public boolean collectFirstCharSet(@NotNull Set<Character> firstCharSet) {
        return false;
    }

    public String getExpectedString() {
        throw new UnsupportedOperationException();
    }
}
