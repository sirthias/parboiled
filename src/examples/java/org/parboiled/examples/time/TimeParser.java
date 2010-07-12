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

/**
 * Parser for very relaxed time literals. Demonstrates parse tree node selection via labels.
 */
public class TimeParser extends BaseParser<Object> {

    public Rule Time() {
        return Sequence(
                FirstOf(Time_HH_MM_SS(), Time_HHMMSS(), Time_HMM()),
                set(convertToTime(
                        (Integer) value(nodeByLabel("hours")),
                        (Integer) value(nodeByLabel("minutes")),
                        (Integer) value(nodeByLabel("seconds"))))
        );
    }

    // hh:mm(:ss)?

    public Rule Time_HH_MM_SS() {
        return Sequence(
                OneOrTwoDigits().label("hours"), ':',
                TwoDigits().label("minutes"),
                Optional(Sequence(':', TwoDigits().label("seconds"))),
                Eoi()
        );
    }

    // hh(mm(ss)?)?

    public Rule Time_HHMMSS() {
        return Sequence(
                TwoDigits().label("hours"),
                Optional(
                        Sequence(
                                TwoDigits().label("minutes"),
                                Optional(TwoDigits().label("seconds"))
                        )
                ),
                Eoi()
        );
    }

    // h(mm)?

    public Rule Time_HMM() {
        return Sequence(
                OneDigit().label("hours"),
                Optional(TwoDigits().label("minutes")),
                Eoi()
        );
    }

    public Rule OneOrTwoDigits() {
        return FirstOf(TwoDigits(), OneDigit());
    }

    public Rule OneDigit() {
        return Sequence(Digit(), set(Integer.parseInt(lastText())));
    }

    public Rule TwoDigits() {
        return Sequence(Sequence(Digit(), Digit()), set(Integer.parseInt(lastText())));
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    // ************************* ACTIONS *****************************

    protected String convertToTime(Integer hours, Integer minutes, Integer seconds) {
        return String.format("%s h, %s min, %s s",
                hours != null ? hours : 0,
                minutes != null ? minutes : 0,
                seconds != null ? seconds : 0);
    }

}
