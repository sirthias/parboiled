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

import org.parboiled.common.ConsoleSink;
import org.parboiled.common.Sink;
import org.parboiled.common.StringUtils;

import java.util.LinkedList;

public class DebuggingValueStack<V> extends DefaultValueStack<V> {
    public final Sink<String> log;
    
    public DebuggingValueStack() {
        this(new ConsoleSink());
    }
    
    public DebuggingValueStack(Sink<String> log) {
        this.log = log;
    }

    public DebuggingValueStack(Iterable<V> values) {
        this(values, new ConsoleSink());
    }
    
    public DebuggingValueStack(Iterable<V> values, Sink<String> log) {
        super(values);
        this.log = log;
    }

    @Override
    public void clear() {
        if (head != null) {
            super.clear();
            log("clear");
        }
    }

    @Override
    public void restoreSnapshot(Object snapshot) {
        if (head == null && snapshot == null || head != null && head.equals(snapshot)) return;
        super.restoreSnapshot(snapshot);
        log("restoreSnapshot");
    }

    @Override
    public void push(V value) {
        super.push(value);
        log("push");
    }

    @Override
    public void push(int down, V value) {
        super.push(down, value);
        log("push");
    }

    @Override
    public V pop(int down) {
        V v = super.pop(down);
        log("pop");
        return v;
    }

    @Override
    public void poke(int down, V value) {
        super.poke(down, value);
        log("poke");
    }

    @Override
    public void swap() {
        super.swap();
        log("swap");
    }

    @Override
    public void swap3() {
        super.swap3();
        log("swap3");
    }

    @Override
    public void swap4() {
        super.swap4();
        log("swap4");
    }

    @Override
    public void swap5() {
        super.swap5();
        log("swap5");
    }

    @Override
    public void swap6() {
        super.swap6();
        log("swap6");
    }

    protected void log(String action) {
        log.receive(action);
        log.receive(StringUtils.repeat(' ', 15 - action.length()));
        log.receive(": ");
        LinkedList<V> elements = new LinkedList<V>();
        for (V v : this) elements.addFirst(v);
        log.receive(StringUtils.join(elements, ", "));
        log.receive("\n");
    }
}
