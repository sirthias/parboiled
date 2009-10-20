package org.parboiled;

class StagingRule implements Rule {

    private final BaseParser<?> parser;
    private final Rule innerRule;
    private String label;

    StagingRule(Rule innerRule, BaseParser<?> parser) {
        this.innerRule = innerRule;
        this.parser = parser;
    }

    public Rule label(String label) {
        this.label = label;
        return this;
    }

    public BaseParser<?> getParser() {
        return parser;
    }

    public Matcher toMatcher() {
        // the real rule is has been locked after creation,
        // so we need to inject a wrapper for setting properties
        if (label != null) {
            WrapMatcher wrapper = new WrapMatcher(innerRule);
            if (label != null) wrapper.label(label);
            return wrapper;
        }

        // cast works on all AbstractMatchers as well as Proxies, for being able to call toMatcher() here
        // we would have to complicate the ProxyInterceptor a bit (since we couldn't use a simple LazyLoader anymore)
        return (Matcher) innerRule;
    }

}
