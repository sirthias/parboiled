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

public class InstructionSubSet {
    public final boolean isActionSet;
    public final int firstIndex;
    public final int lastIndex;

    public InstructionSubSet(boolean actionSet, int firstIndex, int lastIndex) {
        isActionSet = actionSet;
        this.firstIndex = firstIndex;
        this.lastIndex = lastIndex;
    }

    public boolean containsInstruction(int index) {
        return firstIndex <= index && index <= lastIndex;
    }

}
