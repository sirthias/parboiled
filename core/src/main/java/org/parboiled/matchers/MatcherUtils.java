package org.parboiled.matchers;

public final class MatcherUtils {
    
    private MatcherUtils() {}
    
    public static Matcher unwrap(Matcher matcher) {
    	if (matcher instanceof DelegatingMatcher) return unwrap(DelegatingMatcher.unwrap(matcher));
        if (matcher instanceof ProxyMatcher) return unwrap(ProxyMatcher.unwrap(matcher));
        if (matcher instanceof VarFramingMatcher) return unwrap(VarFramingMatcher.unwrap(matcher));
        if (matcher instanceof MemoMismatchesMatcher) return unwrap(MemoMismatchesMatcher.unwrap(matcher));
        return matcher; 
    }
}
