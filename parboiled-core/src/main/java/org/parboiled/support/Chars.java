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

package org.parboiled.support;

public class Chars {

    private Chars() {}

    /**
     * Special non-character used during error recovery. Signals that an illegal input character was skipped at this
     * input location.
     */
    public static final char DEL_ERROR = '\uFDEA';

    /**
     * Special non-character used during error recovery. Signals that the character at the following input location
     * was expected but not present in the input buffer.
     */
    public static final char INS_ERROR = '\uFDEB';

    /**
     * Special non-character used during error recovery. Signals that a rule resynchronization has to be performed
     * at the current input location.
     */
    public static final char RESYNC = '\uFDEC';

    /**
     * Special non-character used during error recovery. Signals that all characters up to the RESYNC_END
     * character need to be skipped as part of a resynchronization.
     */
    public static final char RESYNC_START = '\uFDED';

    /**
     * Special non-character used during error recovery. Signals the end of a resynchronization block.
     */
    public static final char RESYNC_END = '\uFDEE';
    
    /**
     * Special non-character used during error recovery. Signals a resynchronization at EOI.
     */
    public static final char RESYNC_EOI = '\uFDEF';
    
    /**
     * The End-of-Input non-character.
     */
    public static final char EOI = '\uFFFF';

    /**
     * Special non-character used by the {@link org.parboiled.buffers.IndentDedentInputBuffer}.
     */
    public static final char INDENT = '\uFDD0';

    /**
     * Special non-character used by the {@link org.parboiled.buffers.IndentDedentInputBuffer}.
     */
    public static final char DEDENT = '\uFDD1';
    
}
