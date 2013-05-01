package org.parboiled.examples.indenting;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class SimpleIndent extends BaseParser<IndentNode> {

	Rule Parent() {
		return Sequence(push(new IndentNode("root")), OneOrMore(Data()), EOI);
	}

	Rule Data() {
		return Sequence(Identifier(), push(new IndentNode(match())), peek(1)
				.addChild(peek()),
				Optional(Sequence(Spacing(), ChildNodeList())), drop());
	}

	Rule ChildNodeList() {
		return Sequence(INDENT, Spacing(), OneOrMore(Data(), Spacing()), DEDENT);
	}

	Rule Identifier() {
		return Sequence(PN_CHARS_U(), ZeroOrMore(PN_CHARS_DIGIT_U()));
	}

	public Rule PN_CHARS_DIGIT_U() {
		return FirstOf(PN_CHARS_U(), DIGIT());
	}

	public Rule PN_CHARS_U() {
		return FirstOf(PN_CHARS_BASE(), '_');
	}

	public Rule PN_CHARS_BASE() {
		return FirstOf( 
				CharRange('A', 'Z'),
				CharRange('a', 'z'), 
				CharRange('\u00C0', '\u00D6'), 
				CharRange('\u00D8', '\u00F6'), 
				CharRange('\u00F8', '\u02FF'), 
				CharRange('\u0370', '\u037D'), 
				CharRange('\u037F', '\u1FFF'), 
				CharRange('\u200C', '\u200D'), 
				CharRange('\u2070', '\u218F'), 
				CharRange('\u2C00', '\u2FEF'), 
				CharRange('\u3001', '\uD7FF'), 
				CharRange('\uF900', '\uFDCF'), 
				CharRange('\uFDF0', '\uFFFD') 
		);
	}

	public Rule DIGIT() {
		return CharRange('0', '9');
	}

	Rule Spacing() {
		return ZeroOrMore(AnyOf(" \t\r\n\f").label("Whitespace"));
	}
}
