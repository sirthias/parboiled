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

package org.parboiled.scala

import org.parboiled.Context

class WithContextAction1[A, R](val action: (A, Context[Any]) => R) extends (A => R) {
  def apply(a: A) = throw new UnsupportedOperationException
}

class WithContextAction2[A, B, R](val action: (A, B, Context[Any]) => R) extends ((A, B) => R) {
  def apply(a: A, b: B) = throw new UnsupportedOperationException
}

class WithContextAction3[A, B, C, R](val action: (A, B, C, Context[Any]) => R) extends ((A, B, C) => R) {
  def apply(a: A, b: B, c: C) = throw new UnsupportedOperationException
}

class WithContextAction4[A, B, C, D, R](val action: (A, B, C, D, Context[Any]) => R) extends ((A, B, C, D) => R) {
  def apply(a: A, b: B, c: C, d: D) = throw new UnsupportedOperationException
}

class WithContextAction5[A, B, C, D, E, R](val action: (A, B, C, D, E, Context[Any]) => R) extends ((A, B, C, D, E) => R) {
  def apply(a: A, b: B, c: C, d: D, e: E) = throw new UnsupportedOperationException
}

class WithContextAction6[A, B, C, D, E, F, R](val action: (A, B, C, D, E, F, Context[Any]) => R) extends ((A, B, C, D, E, F) => R) {
  def apply(a: A, b: B, c: C, d: D, e: E, f: F) = throw new UnsupportedOperationException
}

class WithContextAction7[A, B, C, D, E, F, G, R](val action: (A, B, C, D, E, F, G, Context[Any]) => R) extends ((A, B, C, D, E, F, G) => R) {
  def apply(a: A, b: B, c: C, d: D, e: E, f: F, g: G) = throw new UnsupportedOperationException
}