package org.parboiled.matchers;

import org.parboiled.Action;
import org.parboiled.MatcherContext;
import org.parboiled.Rule;

public class DelegatingActionMatcher extends DelegatingMatcher {
	protected Action<?> action;

	public DelegatingActionMatcher(Rule inner, Action<?> action) {
		super(inner);
		this.action = action;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> boolean match(MatcherContext<V> context) {
		((Action<V>)action).run(context);
		return super.match(context);
	}
}
