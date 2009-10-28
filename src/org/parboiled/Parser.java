package org.parboiled;

import net.sf.cglib.proxy.*;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;
import static org.parboiled.utils.Utils.arrayOf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class providing the high-level entrypoints into the parboiled library.
 */
public class Parser {

    private Parser() {}

    /**
     * Creates a parser object whose rule creation methods can then be used with the parse(...) method.
     * Since parboiled needs to extends your parser object with certain extra logic (e.g. to prevent infinite recursions
     * in recursive rule definitions) you cannot create your parser object yourself, but have to go through this method.
     * Still your parser object can be of any type (even though it is usually derived from BaseParser) and can
     * define arbitrary constructors.
     * If you want to use an non-default constructor you also have to provide its arguments to this method.
     *
     * @param parserType            the type of the parser to create
     * @param actions               the action object to use (if not null it must have been created with createActions(...)
     * @param parserConstructorArgs optional arguments to the parser class constructor
     * @return the ready to use parser instance
     */
    public static <A extends Actions, P extends BaseParser<A>> P create(@NotNull Class<P> parserType, A actions,
                                                                        Object... parserConstructorArgs) {
        ActionInterceptor actionInterceptor = null;
        if (actions instanceof Factory) {
            Callback actionsCallback = ((Factory) actions).getCallback(1);
            if (actionsCallback instanceof ActionInterceptor) {
                actionInterceptor = (ActionInterceptor) actionsCallback;
            }
        }

        Checks.ensure(actions == null || actionInterceptor != null,
                "Illegal Actions instance, please use Parser.createActions(...) for creating your parser actions object");

        // intercept all no-arg Rule creation methods with a StagingInterceptor
        P parser = create(parserType, new StagingInterceptor(), new CallbackFilter() {
            public int accept(Method method) {
                boolean isRuleCreatingMethod = method.getReturnType() == Rule.class;
                boolean hasNoParameters = method.getParameterTypes().length == 0;
                return isRuleCreatingMethod && hasNoParameters ? 1 : 0;
            }
        }, arrayOf(actions, parserConstructorArgs));

        if (actions != null) {
            // signal to the ActionInterceptor that we are in the rule construction phase by informing it
            // about the parser object instance
            //noinspection ConstantConditions
            actionInterceptor.setParser(parser);
        }
        return parser;
    }

    /**
     * Creates an action object whose actual action methods (the ones returning an ActionResult) can be directly used
     * in your rule definitions.
     * Since parboiled needs to extends your parser actions object with certain extra logic you cannot create your
     * actions object yourself, but have to go through this method. Still your actions object can be of any base type
     * (even though it is often times derived from BaseActions) and can define arbitrary constructors.
     * If you want to use an non-default constructor you also have to provide its arguments to this method.
     *
     * @param actionsType     the type of the action object to create
     * @param constructorArgs optional arguments to the class constructor
     * @return the actions object for the parser creation
     */
    public static <A extends Actions> A createActions(@NotNull Class<A> actionsType, Object... constructorArgs) {
        return create(actionsType, new ActionInterceptor(), new CallbackFilter() {
            public int accept(Method method) {
                // we need to intercept all methods that return an ActionResult, these are the "real" actions
                return method.getReturnType() == ActionResult.class ? 1 : 0;
            }
        }, constructorArgs);
    }

    /**
     * Runs the given parser rule against the given input string. Note that the rule must be created by a rule
     * creation method from a parser object that was previously created with create(...).
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @NotNull
    public static ParsingResult parse(@NotNull Rule rule, @NotNull String input) {
        Checks.ensure(rule instanceof StagingRule,
                "Illegal rule instance, please use Parser.createActions(...) for creating your parser object");

        // prepare
        BaseParser<?> parser = ((StagingRule) rule).getParser();
        InputBuffer inputBuffer = new InputBuffer(input);
        InputLocation startLocation = new InputLocation(inputBuffer);
        List<ParseError> parseErrors = new ArrayList<ParseError>();
        Matcher matcher = rule.toMatcher();
        MatcherContext context = new MatcherContext(null, startLocation, matcher, parser.actions, parseErrors);

        // the matcher tree has already been built during the call to Parser.parse(...), usually immediately
        // before the invocation of this method, we need to signal to the ActionInterceptor that rule construction
        // is over and all further action calls should not continue to createActions ActionMatchers but actually be
        // "routed through" to the actual action method implementations
        if (parser.actions != null) {
            ActionInterceptor actionInterceptor = (ActionInterceptor) ((Factory) parser.actions).getCallback(1);
            actionInterceptor.setParser(null);
        }

        // run the actual matcher tree
        context.runMatcher(true);

        return new ParsingResult(context.getNode(), parseErrors, inputBuffer);
    }

    @SuppressWarnings({"unchecked"})
    private static <T> T create(@NotNull Class<T> type, Callback interceptor, CallbackFilter filter,
                                Object... constructorArguments) {
        Enhancer e = new Enhancer();
        e.setSuperclass(type);
        e.setCallbackFilter(filter);
        e.setCallbacks(new Callback[] {
                NoOp.INSTANCE, // index 0: just call the underlying method directly
                interceptor    // index 1: use the Interceptor
        });
        Constructor constructor = findConstructor(type, constructorArguments);
        return (T) e.create(constructor.getParameterTypes(), constructorArguments);
    }

    private static Constructor findConstructor(Class<?> type, Object[] args) {
        outer:
        for (Constructor constructor : type.getConstructors()) {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            if (paramTypes.length != args.length) continue;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg != null && !paramTypes[i].isAssignableFrom(arg.getClass())) continue outer;
                if (arg == null && paramTypes[i].isPrimitive()) continue outer;
            }
            return constructor;
        }
        throw new RuntimeException("No constructor found for the given arguments");
    }

}
