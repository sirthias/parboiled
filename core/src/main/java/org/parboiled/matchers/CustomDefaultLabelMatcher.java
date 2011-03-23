/*
 * Copyright (C) 2009-2011 Mathias Doenitz
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

import org.parboiled.Rule;

abstract class CustomDefaultLabelMatcher<T extends CustomDefaultLabelMatcher<T>> extends AbstractMatcher {
    private String defaultLabel;

    protected CustomDefaultLabelMatcher(Rule subRule, String defaultLabel) {
        super(subRule, null);
        this.defaultLabel = defaultLabel;
    }

    protected CustomDefaultLabelMatcher(Rule[] subRules, String defaultLabel) {
        super(subRules, null);
        this.defaultLabel = defaultLabel;
    }

    @Override
    public String getLabel() {
        return hasCustomLabel() ? super.getLabel() : defaultLabel;
    }

    @Override
    public boolean hasCustomLabel() {
        return super.getLabel() != null;
    }

    @SuppressWarnings( {"unchecked"})
    public T defaultLabel(String defaultLabel) {
        this.defaultLabel = defaultLabel;
        return (T)this;
    }
}
