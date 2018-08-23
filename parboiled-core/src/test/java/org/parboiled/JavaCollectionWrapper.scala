package org.parboiled

import scala.collection.JavaConverters._

final class JavaCollectionWrapper[A](underlying: java.util.Collection[A]) extends scala.Iterable[A] {
  def iterator = underlying.iterator.asScala
  override def size = underlying.size
  override def isEmpty = underlying.isEmpty
}
