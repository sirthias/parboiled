/*
 * Copyright (C) 2009-2010 Mathias Doenitz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.parboiled.matchers;

import org.parboiled.MatcherContext;
import org.parboiled.Rule;
import org.parboiled.matchervisitors.MatcherVisitor;

import java.util.List;

import com.sun.istack.internal.NotNull;

public class DelegatingMatcher implements Matcher {

	private Matcher inner;

	public DelegatingMatcher(Rule inner) {
		this.inner = (Matcher) inner;
	}

	public <V> boolean match(MatcherContext<V> context) {
		return inner.match(context);
	}

	// GraphNode

	@NotNull
	public List<Matcher> getChildren() {
		return inner.getChildren();
	}
	
	// Rule
	public Rule label(String label) {
		inner = (Matcher) inner.label(label);
		return this;
	}

	public Rule suppressNode() {
		inner = (Matcher) inner.suppressNode();
		return this;
	}

	public Rule suppressSubnodes() {
		inner = (Matcher) inner.suppressSubnodes();
		return this;
	}

	public Rule skipNode() {
		inner = (Matcher) inner.skipNode();
		return this;
	}

	// Matcher

	public String getLabel() {
		return inner.getLabel();
	}
	
	@Override
	public boolean hasCustomLabel() {
		return inner.hasCustomLabel();
	}

	public boolean isNodeSuppressed() {
		return inner.isNodeSuppressed();
	}

	public boolean areSubnodesSuppressed() {
		return inner.areSubnodesSuppressed();
	}

	public boolean isNodeSkipped() {
		return inner.isNodeSkipped();
	}

	public MatcherContext getSubContext(MatcherContext context) {
		MatcherContext subContext = inner.getSubContext(context);
		subContext.setMatcher(this); // we need to inject ourselves here
										// otherwise we get cut out
		return subContext;
	}

	public <R> R accept(@NotNull MatcherVisitor<R> visitor) {
		return inner.accept(visitor);
	}

	@Override
	public String toString() {
		return inner.toString();
	}

	/**
	 * Retrieves the innermost Matcher that is not a DelegatingMatcher.
	 * 
	 * @param matcher
	 *            the matcher to unwrap
	 * @param <V>
	 *            the type of the value field of a parse tree node
	 * @return the given instance if it is not a VarFramingMatcher, otherwise
	 *         the innermost Matcher
	 */
	public static Matcher unwrap(Matcher matcher) {
		while (matcher instanceof DelegatingMatcher) {
			matcher = ((DelegatingMatcher) matcher).inner;
		}
		return matcher;
	}

	@Override
	public Rule memoMismatches() {
		inner = (Matcher) inner.memoMismatches();
		return this;
	}

	@Override
	public boolean areMismatchesMemoed() {
		return inner.areMismatchesMemoed();
	}

	@Override
	public void setTag(Object tagObject) {
		inner.setTag(tagObject);
	}

	@Override
	public Object getTag() {
		return inner.getTag();
	}
	
	public Matcher getDelegate() {
	    return inner;
	}

}