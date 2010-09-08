package org.parboiled.scala

import org.parboiled.Context

class WithContextAction[R](val action: Context[_] => R) extends (() => R) {
  def apply() = throw new UnsupportedOperationException
}

class WithContextAction1[A, R](val action: (A, Context[_]) => R) extends (A => R) {
  def apply(a: A) = throw new UnsupportedOperationException
}

class WithContextAction2[A, B, R](val action: (A, B, Context[_]) => R) extends ((A, B) => R) {
  def apply(a: A, b: B) = throw new UnsupportedOperationException
}

class WithContextAction3[A, B, C, R](val action: (A, B, C, Context[_]) => R) extends ((A, B, C) => R) {
  def apply(a: A, b: B, c: C) = throw new UnsupportedOperationException
}

class WithContextAction4[A, B, C, D, R](val action: (A, B, C, D, Context[_]) => R) extends ((A, B, C, D) => R) {
  def apply(a: A, b: B, c: C, d: D) = throw new UnsupportedOperationException
}

class WithContextAction5[A, B, C, D, E, R](val action: (A, B, C, D, E, Context[_]) => R) extends ((A, B, C, D, E) => R) {
  def apply(a: A, b: B, c: C, d: D, e: E) = throw new UnsupportedOperationException
}

class WithContextAction6[A, B, C, D, E, F, R](val action: (A, B, C, D, E, F, Context[_]) => R) extends ((A, B, C, D, E, F) => R) {
  def apply(a: A, b: B, c: C, d: D, e: E, f: F) = throw new UnsupportedOperationException
}

class WithContextAction7[A, B, C, D, E, F, G, R](val action: (A, B, C, D, E, F, G, Context[_]) => R) extends ((A, B, C, D, E, F, G) => R) {
  def apply(a: A, b: B, c: C, d: D, e: E, f: F, g: G) = throw new UnsupportedOperationException
}