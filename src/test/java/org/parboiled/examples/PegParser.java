package org.parboiled.examples;
import org.parboiled.BaseParser;
import org.parboiled.Rule;


public class PegParser extends BaseParser<Object> {
   // Hierarchical syntax
   // -------------------
    public Rule Grammar () {
        return Sequence(Spacing(), OneOrMore(Definition()), EndOfFile());
    }

    public Rule Definition () {
        return Sequence(Identifier(), LEFTARROW(), Expression());
    }

    public Rule Expression () {
        return Sequence(Sequence(), ZeroOrMore(Sequence(new Rule[] {Sequence(SLASH(), Sequence())})));
    }

    public Rule Sequence () {
        return ZeroOrMore(Prefix());
    }

    public Rule Prefix () {
        return Sequence(Optional(Sequence(new Rule[] {FirstOf(AND(), NOT())})), Suffix());
    }

    public Rule Suffix () {
        return Sequence(Primary(), Optional(Sequence(new Rule[] {FirstOf(QUESTION(), STAR(), PLUS())})));
    }

    public Rule Primary () {
        return FirstOf(Sequence(Identifier(), TestNot(LEFTARROW())), Sequence(OPEN(), Expression(), CLOSE()), Literal(), Class(), DOT());
    }

   // Lexical syntax
   // --------------
    public Rule Identifier () {
        return Sequence(IdentStart(), ZeroOrMore(IdentCont()), Spacing());
    }

    public Rule IdentStart () {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), Ch('_'));
    }

    public Rule IdentCont () {
        return FirstOf(IdentStart(), CharRange('0', '9'));
    }

    public Rule Literal () {
        return FirstOf(Sequence(Ch('\''), ZeroOrMore(Sequence(new Rule[] {Sequence(TestNot(Ch('\'')), Char())})), Ch('\''), Spacing()), Sequence(Ch('"'), ZeroOrMore(Sequence(new Rule[] {Sequence(TestNot(Ch('"')), Char())})), Ch('"'), Spacing()));
    }

    public Rule Class () {
        return Sequence(Ch('['), ZeroOrMore(Sequence(new Rule[] {Sequence(TestNot(Ch(']')), Range())})), Ch(']'), Spacing());
    }

    public Rule Range () {
        return FirstOf(Sequence(Char(), Ch('-'), Char()), Char());
    }

    public Rule Char () {
        return FirstOf(Sequence(Ch('\\'), FirstOf(Ch('n'), Ch('r'), Ch('t'), Ch('\''), Ch('"'), Ch('['), Ch(']'), Ch('\\'))), Sequence(Ch('\\'), CharRange('0', '2'), CharRange('0', '7'), CharRange('0', '7')), Sequence(Ch('\\'), CharRange('0', '7'), Optional(CharRange('0', '7'))), Sequence(TestNot(Ch('\\')), Any()));
    }

    public Rule LEFTARROW () {
        return Sequence(String("<-"), Spacing());
    }

    public Rule SLASH () {
        return Sequence(Ch('/'), Spacing());
    }

    public Rule AND () {
        return Sequence(Ch('&'), Spacing());
    }

    public Rule NOT () {
        return Sequence(Ch('!'), Spacing());
    }

    public Rule QUESTION () {
        return Sequence(Ch('?'), Spacing());
    }

    public Rule STAR () {
        return Sequence(Ch('*'), Spacing());
    }

    public Rule PLUS () {
        return Sequence(Ch('+'), Spacing());
    }

    public Rule OPEN () {
        return Sequence(Ch('('), Spacing());
    }

    public Rule CLOSE () {
        return Sequence(Ch(')'), Spacing());
    }

    public Rule DOT () {
        return Sequence(Ch('.'), Spacing());
    }

    public Rule Spacing () {
        return ZeroOrMore(Sequence(new Rule[] {FirstOf(Space(), Comment())}));
    }

    public Rule Comment () {
        return Sequence(Ch('#'), ZeroOrMore(Sequence(new Rule[] {Sequence(TestNot(EndOfLine()), Any())})), EndOfLine());
    }

    public Rule Space () {
        return FirstOf(Ch(' '), Ch('\t'), EndOfLine());
    }

    public Rule EndOfLine () {
        return FirstOf(String("\r\n"), Ch('\n'), Ch('\r'));
    }

    public Rule EndOfFile () {
        return TestNot(Any());
    }

}
