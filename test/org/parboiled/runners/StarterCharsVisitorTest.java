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

package org.parboiled.runners;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import org.parboiled.examples.java.JavaParser;
import org.parboiled.Parboiled;
import org.parboiled.support.Characters;
import org.parboiled.matchers.Matcher;

public class StarterCharsVisitorTest {

    @SuppressWarnings({"unchecked"})
    @Test
    public void test() {
        JavaParser parser = Parboiled.createParser(JavaParser.class);
        Matcher<Object> matcher = (Matcher<Object>) parser.identifier();
        StarterCharsVisitor<Object> starterCharsVisitor = new StarterCharsVisitor<Object>();
        Characters starterChars = matcher.accept(starterCharsVisitor);
        assertEquals(starterChars.toString(), "[z,y,x,w,v,u,t,s,r,q,p,o,n,m,l,k,j,i,h,g,f,e,d,c,b,a,Z,Y,X,W,V,U,T,S,R,Q,P,O,N,M,L,K,J,I,H,G,F,E,D,C,B,A,_,$]");
    }

}
