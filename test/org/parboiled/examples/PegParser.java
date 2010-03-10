package org.parboiled.examples;
import org.parboiled.BaseParser;
import org.parboiled.Rule;


public class PegParser extends BaseParser<Object> {
   // Hierarchical syntax
   // -------------------
    public Rule Grammar () {
        return sequence (Spacing(), oneOrMore (Definition()), EndOfFile());
    }

    public Rule Definition () {
        return sequence (Identifier(), LEFTARROW(), Expression());
    }

    public Rule Expression () {
        return sequence (Sequence(), zeroOrMore (sequence (new Rule[] {sequence (SLASH(), Sequence())} )));
    }

    public Rule Sequence () {
        return zeroOrMore (Prefix());
    }

    public Rule Prefix () {
        return sequence (optional (sequence (new Rule[] {firstOf (AND(), NOT())} )), Suffix());
    }

    public Rule Suffix () {
        return sequence (Primary(), optional (sequence (new Rule[] {firstOf (QUESTION(), STAR(), PLUS())} )));
    }

    public Rule Primary () {
        return firstOf (sequence (Identifier(), testNot (LEFTARROW())), sequence (OPEN(), Expression(), CLOSE()), Literal(), Class(), DOT());
    }

   // Lexical syntax
   // --------------
    public Rule Identifier () {
        return sequence (IdentStart(), zeroOrMore (IdentCont()), Spacing());
    }

    public Rule IdentStart () {
        return firstOf (charRange ('a', 'z'), charRange ('A', 'Z'), ch ('_'));
    }

    public Rule IdentCont () {
        return firstOf (IdentStart(), charRange ('0', '9'));
    }

    public Rule Literal () {
        return firstOf (sequence (ch ('\''), zeroOrMore (sequence (new Rule[] {sequence (testNot (ch ('\'')), Char())} )), ch ('\''), Spacing()), sequence (ch ('"'), zeroOrMore (sequence (new Rule[] {sequence (testNot (ch ('"')), Char())} )), ch ('"'), Spacing()));
    }

    public Rule Class () {
        return sequence (ch ('['), zeroOrMore (sequence (new Rule[] {sequence (testNot (ch (']')), Range())} )), ch (']'), Spacing());
    }

    public Rule Range () {
        return firstOf (sequence (Char(), ch ('-'), Char()), Char());
    }

    public Rule Char () {
        return firstOf (sequence (ch ('\\'), firstOf (ch ('n'), ch ('r'), ch ('t'), ch ('\''), ch ('"'), ch ('['), ch (']'), ch ('\\'))), sequence (ch ('\\'), charRange ('0', '2'), charRange ('0', '7'), charRange ('0', '7')), sequence (ch ('\\'), charRange ('0', '7'), optional (charRange ('0', '7'))), sequence (testNot (ch ('\\')), any ()));
    }

    public Rule LEFTARROW () {
        return sequence (string ("<-"), Spacing());
    }

    public Rule SLASH () {
        return sequence (ch ('/'), Spacing());
    }

    public Rule AND () {
        return sequence (ch ('&'), Spacing());
    }

    public Rule NOT () {
        return sequence (ch ('!'), Spacing());
    }

    public Rule QUESTION () {
        return sequence (ch ('?'), Spacing());
    }

    public Rule STAR () {
        return sequence (ch ('*'), Spacing());
    }

    public Rule PLUS () {
        return sequence (ch ('+'), Spacing());
    }

    public Rule OPEN () {
        return sequence (ch ('('), Spacing());
    }

    public Rule CLOSE () {
        return sequence (ch (')'), Spacing());
    }

    public Rule DOT () {
        return sequence (ch ('.'), Spacing());
    }

    public Rule Spacing () {
        return zeroOrMore (sequence (new Rule[] {firstOf (Space(), Comment())} ));
    }

    public Rule Comment () {
        return sequence (ch ('#'), zeroOrMore (sequence (new Rule[] {sequence (testNot (EndOfLine()), any ())} )), EndOfLine());
    }

    public Rule Space () {
        return firstOf (ch (' '), ch ('\t'), EndOfLine());
    }

    public Rule EndOfLine () {
        return firstOf (string ("\r\n"), ch ('\n'), ch ('\r'));
    }

    public Rule EndOfFile () {
        return testNot (any ());
    }

}
