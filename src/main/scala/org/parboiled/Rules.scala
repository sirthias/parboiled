package org.parboiled

import org.parboiled.matchers._
import org.parboiled.support.Characters
import org.parboiled.common.StringUtils.escape
import collection.mutable.ListBuffer
import java.lang.String
import org.parboiled.{Rule => PRule}

trait Rules[V] {

  trait Rule {
    private var proxies: Option[ListBuffer[ProxyMatcher]] = None
    protected val matcherCreator: () => PRule
    var label:String = _
    var suppressNode = false
    var suppressSubnodes = false
    var skipNode = false

    lazy val toMatcher = {
      var matcher = matcherCreator()
      if (label != null) matcher = matcher.label(label)
      if (suppressNode) matcher = matcher.suppressNode()
      if (suppressSubnodes) matcher = matcher.suppressSubnodes()
      if (skipNode) matcher = matcher.skipNode()
      updateProxies(matcher)
    }

    def ~(other: Rule) = new SequenceRule(this, other)

    def |(other: Rule) = new FirstOfRule(this, other)

    def withLabel(label: String) = { this.label = label; this}

    def withNodeSuppressed() = { suppressNode = true; this}

    def withSubnodesSuppressed() = { suppressSubnodes = true; this}

    def withNodeSkipped() = { skipNode = true; this}

    def registerProxy(matcher: ProxyMatcher) {
      proxies match {
        case Some(list) => list += matcher
        case None => proxies = Some(ListBuffer(matcher))
      }
    }

    private def updateProxies(matcher: PRule): PRule = {
      for (list <- proxies; p <- list) p.arm(matcher.asInstanceOf[Matcher])
      matcher
    }

    override def toString = getClass.getSimpleName
  }

  class SimpleRule(creator: => PRule, initialLabel:String) extends Rule {
    label = initialLabel
    protected val matcherCreator = () => creator
  }

  class UnaryRule(val sub:Rule, creator: PRule => PRule, initialLabel:String) extends Rule {
    label = initialLabel
    protected val matcherCreator = () => creator(sub.toMatcher)
  }

  trait BinaryRule extends Rule {
    val left:Rule
    val right:Rule
  }

  class SequenceRule(val left:Rule, val right:Rule) extends BinaryRule {
    protected val matcherCreator = () => new SequenceMatcher(groupLeft.reverse.toArray).label("Sequence")

    private def groupLeft: List[PRule] = left match {
      case left: SequenceRule if (left.label == null) => right.toMatcher :: left.groupLeft
      case _ => List(right.toMatcher, left.toMatcher)
    }
  }

  class FirstOfRule(val left:Rule, val right:Rule) extends BinaryRule {
    protected val matcherCreator = () => new FirstOfMatcher(groupLeft.reverse.toArray).label("FirstOf")

    private def groupLeft: List[PRule] = left match {
      case left: FirstOfRule if (left.label == null) => right.toMatcher :: left.groupLeft
      case _ => List(right.toMatcher, left.toMatcher)
    }
  }

  class CharRule(val c: Char) extends SimpleRule(new CharMatcher(c), '\'' + escape(c) + '\'') {
    def --(upperBound: Char) = new SimpleRule(new CharRangeMatcher(c, upperBound), c + ".." + upperBound)
  }

  class ProxyRule extends Rule {
    private var inner: Rule = _

    protected val matcherCreator = () => {
      require(inner != null)
      val proxy = new ProxyMatcher()
      inner.registerProxy(proxy)
      proxy
    }

    def arm(inner: Rule) { this.inner = inner }

  }

  lazy val EMPTY = new SimpleRule(new EmptyMatcher(), "EMPTY")
  lazy val ANY = new SimpleRule(new AnyMatcher(), "ANY")
  lazy val EOI = new SimpleRule(new CharMatcher(Characters.EOI), "EOI")
}