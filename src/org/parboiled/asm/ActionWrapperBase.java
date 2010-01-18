/*
 * Copyright (C) 2009 Mathias Doenitz
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

package org.parboiled.asm;

import org.parboiled.AbstractAction;
import org.parboiled.Context;
import org.parboiled.MatcherContext;
import org.parboiled.support.Checks;

public abstract class ActionWrapperBase<V> extends AbstractAction<V> {

    protected final void UP() {
		Context<V> parentContext = context.getParent();
		Checks.ensure(parentContext != null, "Illegal UP() call, already at root level");
		this.context = parentContext;
	}

	protected final void DOWN() {
		Context<V> subContext = context.getSubContext();
		Checks.ensure(subContext != null, "Illegal DOWN() call, already at leaf level");
		this.context = subContext;
	}

    protected final boolean inPredicate() {
		return ((MatcherContext<?>) context).inPredicate();
	}

}
