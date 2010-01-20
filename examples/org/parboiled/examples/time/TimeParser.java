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

package org.parboiled.examples.time;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

public class TimeParser extends BaseParser<Object> {

    private final TimeActions actions = new TimeActions();

    public Rule time() {
        return sequence(
                firstOf(time_hh_mm_ss(), time_hhmmss(), time_hmm()),
                SET(actions.convertToTime(
                        (Integer) VALUE(NODE_BY_LABEL("hours")),
                        (Integer) VALUE(NODE_BY_LABEL("minutes")),
                        (Integer) VALUE(NODE_BY_LABEL("seconds"))))
        );
    }

    // hh:mm(:ss)?
    public Rule time_hh_mm_ss() {
        return sequence(
                oneOrTwoDigits().label("hours"), ':',
                twoDigits().label("minutes"),
                optional(sequence(':', twoDigits().label("seconds"))),
                test(eoi())
        );
    }

    // hh(mm(ss)?)?
    public Rule time_hhmmss() {
        return sequence(
                twoDigits().label("hours"),
                optional(
                        sequence(
                                twoDigits().label("minutes"),
                                optional(twoDigits().label("seconds"))
                        )
                ),
                test(eoi())
        );
    }

    // h(mm)?
    public Rule time_hmm() {
        return sequence(
                oneDigit().label("hours"),
                optional(twoDigits().label("minutes")),
                test(eoi())
        );
    }

    public Rule oneOrTwoDigits() {
        return firstOf(twoDigits(), oneDigit());
    }

    public Rule oneDigit() {
        return sequence(digit(), SET(Integer.parseInt(LAST_TEXT())));
    }

    public Rule twoDigits() {
        return sequence(sequence(digit(), digit()), SET(Integer.parseInt(LAST_TEXT())));
    }

    public Rule digit() {
        return charRange('0', '9');
    }

}
