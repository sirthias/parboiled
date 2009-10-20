package org.parboiled;

import net.sf.cglib.proxy.*;
import org.jetbrains.annotations.NotNull;
import org.parboiled.support.Checks;
import org.parboiled.support.InputBuffer;
import org.parboiled.support.InputLocation;
import org.parboiled.support.ParseError;
import org.parboiled.utils.Reflector;
import static org.parboiled.utils.Utils.arrayOf;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

public class Parser {

    private Parser() {}

    /**
     * Creates a parser object (usually derived from BaseParser) whose Rule-creating, no-arg instance methods
     * are extended with the following functionality:
     * - Automatically locks all created Rules (which prevents further changes to their properties)
     * - Automatically labels all created Rule objects with the name of the respective creation method
     * - Caches Rules created by such methods, so subsequent calls return the same Rule instance
     * - Prevents infinite recursions during rule construction by inserting proxy objects where required
     * - Automatically injects WrapMatcher to allow for setting of custom properties for cached, locked Rules
     *
     * @param parserType            the type of the parser to create
     * @param actions               the action object to use
     * @param parserConstructorArgs optional arguments to the parser class constructor
     * @return the parser ready to use parser instance
     */
    public static <A extends Actions, P extends BaseParser<A>> P create(@NotNull Class<P> parserType, A actions,
                                                                        Object... parserConstructorArgs) {
        Checks.ensure(actions == null || actions instanceof Factory,
                "Illegal Actions instance, please use Parser.create(...) for creating your parser actions object");
        P parser = create(parserType, new StagingInterceptor(), new CallbackFilter() {
            public int accept(Method method) {
                boolean isRuleCreatingMethod = method.getReturnType() == Rule.class;
                boolean hasNoParameters = method.getParameterTypes().length == 0;
                return isRuleCreatingMethod && hasNoParameters ? 1 : 0;
            }
        }, arrayOf(actions, parserConstructorArgs));
        if (actions != null) {
            //noinspection ConstantConditions
            ((ActionInterceptor) ((Factory) actions).getCallback(1)).setParser(parser);
        }
        return parser;
    }

    /**
     * Creates an action object (usually derived from BaseActions) whose actual action methods (the ones returning an
     * ActionResult) are extended with the ability to create a binding that can be directly used as a matcher.
     *
     * @param actionsType     the type of the action object to create
     * @param constructorArgs optional arguments to the class constructor
     * @return the actions object for the parser creation
     */
    public static <A extends Actions> A create(@NotNull Class<A> actionsType, Object... constructorArgs) {
        return create(actionsType, new ActionInterceptor(), new CallbackFilter() {
            public int accept(Method method) {
                // we need to intercept all methods that return an ActionResult, these are the "real" actions
                return method.getReturnType() == ActionResult.class ? 1 : 0;
            }
        }, constructorArgs);
    }

    /**
     * Runs the given parser rule against the given input string. Note that the rule must be created by a rule
     * creation method from a parser object that was previously created with create().
     *
     * @param rule  the rule
     * @param input the input string
     * @return the ParsingResult for the run
     */
    @NotNull
    public static ParsingResult parse(@NotNull Rule rule, @NotNull String input) {
        Checks.ensure(rule instanceof StagingRule,
                "Illegal rule instance, please use Parser.create(...) for creating your parser object");

        BaseParser<?> parser = ((StagingRule) rule).getParser();
        InputBuffer inputBuffer = new InputBuffer(input);
        InputLocation startLocation = new InputLocation(inputBuffer);
        List<ParseError> parseErrors = new ArrayList<ParseError>();
        Matcher matcher = rule.toMatcher();
        MatcherContext context = new MatcherContextImpl(null, startLocation, matcher, parser.actions, parseErrors);
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
        Constructor constructor = Reflector.f(type).findConstructor(constructorArguments);
        return (T) e.create(constructor.getParameterTypes(), constructorArguments);
    }

}
