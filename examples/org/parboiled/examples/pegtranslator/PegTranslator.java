//******************************************************************************
//
//  Copyright (c) 2010 Radu Vlasov
//  Adapted in 2010 by Mathias Doenitz
//  The author gives unlimited permission to copy and distribute
//  this file, with or without modifications, as long as this notice
//  is preserved, and any changes are properly documented.
//
//  This file is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//******************************************************************************
//
//  This is a Parsing Expression Grammar (PEG) `parboiled` parser generator
//
//  Based on:
//    [1] "Parsing Expression Grammars: A Recognition-Based Syntactic Foundation"
//        by Bryan Ford, Copyright (c) 2004 ACM 1-58113-729-X/04/0001
//        http://pdos.csail.mit.edu/~baford/packrat/popl04/
//    [2] The `parboiled` library, Copyright (C) 2010 by Mathias Doenitz
//        http://wiki.github.com/sirthias/parboiled/
//
//  Input:   PEG specified using the "practical syntax for PEGs" presented in [1]
//
//  Output:  Java class implementing a `parboiled` parser for the input PEG
//
//  Notes:   This class is itself a `parboiled` parser for the PEG grammar
//              presented in [1], which grammar describes PEGs themselves
//
//******************************************************************************

package org.parboiled.examples.pegtranslator;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.common.StringUtils;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//~~~~~~~~~~~~~~~~~~~~~
// Notes: - the conversion is performed on-the-fly, without an intermediate AST,
//          by Rule-s "decorated" with actions that build the output parser
//        - the program is supposed to be used as a "one shot" run
//          and therefore it does not focus on performance, but on clarity
//
// TODO: dump the input grammar at the end as // comments; replaceAll (eol, eol+"//")

// TODO: split output Rule-s in >1 lines and indent properly for easy reading
public class PegTranslator extends BaseParser<Object> {

    private final String packageName;
    private final String className;
    private final StringBuilder out = new StringBuilder();

    public PegTranslator(@NotNull String fullyQualifiedClassName) {
        int ix = fullyQualifiedClassName.lastIndexOf('.');
        Preconditions.checkArgument(ix > 0);
        packageName = fullyQualifiedClassName.substring(0, ix);
        className = fullyQualifiedClassName.substring(ix + 1);
    }

    public String getSource() {
        return out.toString();
    }

    //************
    //   Parser
    //************

    // Hierarchical syntax
    // -------------------

    public Rule Grammar() {
        return sequence(
                startClass(),
                Spacing(),
                oneOrMore(Definition()),
                eoi(),
                endClass()
        );
    }

    // no need for protection like close(false), pop(false), or similar
    //    Definition () is only called in one context and if it fails then Grammar() fails as well
    //    so it's nothing to recover, really
    public Rule Definition() {
        return sequence(
                Identifier(),
                outComment(),
                startMethod(theMostRecentSpacedTrimmedTerminal),
                LEFTARROW(),
                Expression(),
                endMethod()
        );
    }

    // could always use firstOf (new Rule[] {...}) for 1 or more rules
    // but it doesn't look nice when there is only one element (the leading Sequence) in that array
    public Rule Expression() {
        return firstOf(
                sequence(
                        push(StackElementType.EXPRESSION),
                        Sequence(),
                        incr(),
                        zeroOrMore(
                                sequence(
                                        SLASH(),
                                        outComma(),   // separator between firstOf() args
                                        Sequence(),
                                        incr()
                                )
                        ),
                        pop(true)
                ),
                pop(false)
        );
    }

    // don't have a separator between Prefix-es like SLASH in Expression! so need to push()
    public Rule Sequence() {
        return firstOf(
                sequence(
                        push(StackElementType.SEQUENCE),
                        zeroOrMore(
                                firstOf(
                                        sequence(
                                                push(StackElementType.SEQUENCE_ELEMENT),
                                                Prefix(),
                                                pop(true),
                                                incr()
                                        ),
                                        pop(false)
                                )
                        ),
                        pop(true)
                ),
                pop(false)
        );
    }

    public Rule Prefix() {
        return firstOf(
                sequence(
                        push(StackElementType.PREFIX),
                        optional(
                                firstOf(
                                        sequence(AND(), set(2)),
                                        sequence(NOT(), set(1))
                                )
                        ),
                        Suffix(),
                        pop(true)
                ),
                pop(false)
        );
    }

    public Rule Suffix() {
        return firstOf(
                sequence(
                        push(StackElementType.SUFFIX),
                        Primary(),
                        optional(
                                firstOf(
                                        sequence(QUESTION(), set(1)),
                                        sequence(STAR(), set(2)),
                                        sequence(PLUS(), set(3))
                                )
                        ),
                        pop(true)
                ),
                pop(false)
        );
    }

    // no need to recover if Expression() fails; OPEN/CLOSE are only used here
    // TODO: replace new Rule[] with something better than that
    public Rule Primary() {
        return firstOf(
                sequence(
                        Identifier(),
                        testNot(LEFTARROW()),
                        outIdentifier(theMostRecentSpacedTrimmedTerminal)
                ),
                sequence(
                        OPEN(),
                        out("sequence (new Rule[] {"),
                        Expression(),
                        CLOSE(),
                        out("} )")
                ),
                Literal(),
                Class(),
                DOT()
        );
    }

    // Lexical syntax
    // --------------
    public Rule Identifier() {
        return sequence(
                sequence(
                        IdentStart(),
                        zeroOrMore(IdentCont())
                ),
                setMostRecentSpacedTrimmedTerminal(TEXT(LAST_NODE())),
                Spacing()
        );
    }

    public Rule IdentStart() {
        return firstOf(
                charRange('a', 'z'),
                charRange('A', 'Z'),
                charRange('0', '9'),
                ch('_')
        );
    }

    public Rule IdentCont() {
        return firstOf(
                IdentStart(),
                charRange('0', '9')
        );
    }

    // TODO: as per the PEG there is a zeroOrMore in between the quotes!?
    public Rule Literal() {
        return sequence(
                firstOf(
                        sequence(
                                sequence(ch('\''), zeroOrMore(sequence(testNot(ch('\'')), Char())), ch('\'')),
                                setMostRecentSpacedTrimmedTerminal(TEXT(LAST_NODE())),
                                Spacing()
                        ),
                        sequence(sequence(ch('\"'), zeroOrMore(sequence(testNot(ch('\"')), Char())), ch('\"')),
                                setMostRecentSpacedTrimmedTerminal(TEXT(LAST_NODE())),
                                Spacing()
                        )
                ),
                outLiteral(theMostRecentSpacedTrimmedTerminal)
        );
    }

    // TODO: as per the PEG there is a zeroOrMore in between the []-s!?
    public Rule Class() {
        return sequence(
                sequence(
                        sequence(ch('['), zeroOrMore(sequence(testNot(ch(']')), Range())), ch(']')),
                        setMostRecentSpacedTrimmedTerminal(TEXT(LAST_NODE())),
                        Spacing()
                ),
                outCharacterClass(theMostRecentSpacedTrimmedTerminal)
        );
    }

    public Rule Range() {
        return firstOf(
                sequence(Char(), ch('-'), Char()),
                Char()
        );
    }

    // no whitespace is recognized inside these structures
    public Rule Char() {
        return firstOf(
                sequence(ch('\\'), charSet("nrt'\"[]\\")),
                sequence(ch('\\'), charRange('0', '2'), charRange('0', '7'), charRange('0', '7')),
                sequence(ch('\\'), charRange('0', '7'), optional(charRange('0', '7'))),
                sequence(testNot(ch('\\')), any())
        );
    }

    // the following basic terminals never have their literal values used directly
    //    it's just their presence that affects the parsing process
    //    SO: there is no need to add any Spacing-related logic for them
    //        like done for Identifier, Literal, Class
    public Rule LEFTARROW() { return sequence(string("<-"), Spacing()); }

    public Rule SLASH() { return sequence(ch('/'), Spacing()); }

    public Rule AND() { return sequence(ch('&'), Spacing()); }

    public Rule NOT() { return sequence(ch('!'), Spacing()); }

    public Rule QUESTION() { return sequence(ch('?'), Spacing()); }

    public Rule STAR() { return sequence(ch('*'), Spacing()); }

    public Rule PLUS() { return sequence(ch('+'), Spacing()); }

    public Rule OPEN() { return sequence(ch('('), Spacing()); }

    public Rule CLOSE() { return sequence(ch(')'), Spacing()); }

    public Rule DOT() { return sequence(ch('.'), Spacing(), outAny()); }

    public Rule Spacing() {
        return zeroOrMore(firstOf(Space(), Comment()));
    }

    // TODO: empty lines around and in between comment lines get lost
    public Rule Comment() {
        return sequence(
                ch('#'),
                sequence(
                        sequence(zeroOrMore(sequence(testNot(EndOfLine()), any())), EndOfLine()),
                        addComment(TEXT(LAST_NODE()))
                )
        );
    }

    public Rule Space() {
        return firstOf(ch(' '), ch('\t'), EndOfLine());
    }

    public Rule EndOfLine() {
        return firstOf(string("\r\n"), ch('\n'), ch('\r'));
    }

    //***********
    //   Stack
    //***********

    private enum StackElementType {
        EXPRESSION, SEQUENCE, PREFIX, SUFFIX, SEQUENCE_ELEMENT,
    }

    private class StackElement {
        StackElementType stackElementType;
        StringBuilder stringBuilder;
        int n;                 // used as children counter, flag or discriminant

        StackElement(StackElementType stackElementType) {
            this.stackElementType = stackElementType;
            this.stringBuilder = new StringBuilder();
            this.n = 0;
        }

        void incr() { n++; }

        void set(int n) { this.n = n; }

        void end() {
            String bodyStr = stringBuilder.toString();

            switch (stackElementType) {
                case EXPRESSION:
                    if (n > 1) {
                        out("firstOf (");
                        out(bodyStr);
                        out(")");
                    } else { out(bodyStr); }
                    break;

                case SEQUENCE:
                    if (n > 1) {
                        out("sequence (");
                        out(bodyStr);
                        out(")");
                    } else if (n == 1) { out(bodyStr); } else { out("empty ()"); }
                    break;

                case PREFIX:
                    if (n == 2) {
                        out("test (");
                        out(bodyStr);
                        out(")");
                    }    // AND
                    else if (n == 1) {
                        out("testNot (");
                        out(bodyStr);
                        out(")");
                    }    // NOT
                    else { out(bodyStr); }    // no prefix
                    break;

                case SUFFIX:
                    if (n == 1) {
                        out("optional (");
                        out(bodyStr);
                        out(")");
                    }    // ?
                    else if (n == 2) {
                        out("zeroOrMore (");
                        out(bodyStr);
                        out(")");
                    }    // *
                    else if (n == 3) {
                        out("oneOrMore (");
                        out(bodyStr);
                        out(")");
                    }    // +
                    else { out(bodyStr); }    // no prefix
                    break;

                case SEQUENCE_ELEMENT:
                    StackElement parentSequence = stack
                            .lastElement();   // at this point the SEQUENCE_ELEMENT had been popped out of the stack

                    // ASSERT
                    if (parentSequence.stackElementType != StackElementType.SEQUENCE) {
                        throw new RuntimeException(
                                "parent stack element not a " + StackElementType.SEQUENCE + ": " + parentSequence.stackElementType);
                    }

                    if (parentSequence.n > 0) {
                        out(", ");
                        out(bodyStr);
                    } else { out(bodyStr); }
                    break;
            }
            // end switch
        }
    }

    // TODO: needed this function because expressions like  " push (new StackElement (...)) "  are NOT ALLOWED as element in a sequence()
    StackElement newStackElement(StackElementType stackElementType) {
        return new StackElement(stackElementType);
    }

    Stack<StackElement> stack = new Stack<StackElement>(); // holds output until the parsing process decides what's next

    boolean push(StackElementType stackElementType) {
        stack.push(newStackElement(stackElementType));
        return true;
    }

    boolean incr() {
        stack.lastElement().incr();
        return true;
    }

    boolean set(int n) {
        stack.lastElement().set(n);
        return true;
    }

    boolean pop(boolean ret) {
        if (ret) {
            stack.pop().end();
        } else {
            stack.pop();
        }
        return ret;
    }

    //************
    //   Output
    //************

    boolean startClass() {
        outLn("package " + packageName + ';');
        outLn("import org.parboiled.BaseParser;");
        outLn("import org.parboiled.Rule;");
        outLn();
        outLn();
        outLn("public class " + className + " extends BaseParser<Object> {");
        return true;
    }

    boolean endClass() {
        outLn("}");
        return true;
    }

    boolean startMethod(String methodName) {
        outLn("    public Rule " + methodName + " () {");
        out("        return ");
        return true;
    }

    boolean endMethod() {
        outLn(";");
        outLn("    }");
        outLn();
        return true;
    }

    boolean outLn() {
        return out(StringUtils.NL);
    }

    boolean outLn(String str) {
        return out(str + StringUtils.NL);
    }

    boolean out(String str) {
        if (stack.size() == 0) {
            out.append(str);
        } else {
            stack.lastElement().stringBuilder.append(str);
        }
        return true;
    }

    boolean outIdentifier(String str) {
        return out(str + "()")
                // && outComment () // output comments "attached" to this identifier
                // the generated code is syntactically correct,
                // but, most of the time, it will break the generated statement flow,
                // when the comments were not targeted to the identifier
                // but to a higher level grammar construct
                ;
    }

    boolean outLiteral(String str) {
        // ASSERT
        if (!(str.startsWith("'") && str.endsWith("'") ||
                str.startsWith("\"") && str.endsWith("\""))) {
            // TODO: keep this in sync with Literal()
            throw new RuntimeException("literal not quoted: [" + str + "]");
        }

        String retStr = str.substring(1, str.length() - 1);       // trim the leading/trailing quotes

        // startsWith() is called just to avoid the pattern matching in the most common situations
        if (retStr.length() == 1 || retStr.startsWith("\\") && escapedCharPat.matcher(retStr).matches()) {
            // single character, includes escaped ones
            retStr = "ch ('" + escapeChar(retStr) + "')";
        } else {
            // >1 characters
            retStr = "string (\"" + escapeString(retStr) + "\")";
        }

        return out(retStr);
    }

    // TODO: could group >1 adjacent single characters and output them under a charSet() instead of as individual ch()-s
    boolean outCharacterClass(String str) {
        // ASSERT
        if (!str.startsWith("[") || !str.endsWith("]"))
        // TODO: keep this in sync with Class()
        {
            throw new RuntimeException(
                    "character class not surrounded by parenthesis: [" + str + "]" + theMostRecentSpacedTrimmedTerminal);
        }

        str = str.substring(1, str.length() - 1)     // trim the surrounding square brackets
                .replaceAll("\\\\\\[", "[")        // [ is only special in the PEG
                .replaceAll("\\\\\\]", "]");       // ] is only special in the PEG

        int matchCounter = 0;
        boolean endWasHit = false;

        StringBuilder sb = new StringBuilder();
        Matcher mat = rangePat.matcher(str);

        while (mat.find()) {
            matchCounter++;

            if (matchCounter > 1) {
                sb.append(", ");
            }

            if (mat.group(1) != null) {
                // real range
                sb.append("charRange ('").append(escapeChar(mat.group(2))).append("', '")
                        .append(escapeChar(mat.group(3))).append("')");
            } else if (mat.group(4) != null) {
                // single character
                sb.append("ch ('").append(escapeChar(mat.group(4))).append("')");
            }

            endWasHit = true;
        }
        // end while (mat.find ())

        // ASSERT
        if (!endWasHit) {
            throw new RuntimeException("character class not recognized: [" + str + "]");
        }

        if (matchCounter > 1) {
            sb.insert(0, "firstOf (");
            sb.append(")");
        } else if (matchCounter == 1) {
            // do nothing
        } else {
            // character class with no characters !? ARE allowed by the PEG grammar
            sb.append("empty ()");
        }

        return out(sb.toString());
    }

    boolean outComment() {
        if (theMostRecentComment.length() > 0) {
            out(theMostRecentComment.toString());
            theMostRecentComment.delete(0, theMostRecentComment.length());
        }
        return true;
    }

    boolean outAny() {
        return out("any ()");
    }

    boolean outComma() {
        return out(", ");
    }

    //************
    //  Helpers
    //************

    String theMostRecentSpacedTrimmedTerminal = null;
    StringBuilder theMostRecentComment = new StringBuilder();

    boolean setMostRecentSpacedTrimmedTerminal(String str) {
        theMostRecentSpacedTrimmedTerminal = str;
        return true;
    }

    boolean addComment(String str) {
        theMostRecentComment.append("   //").append(str.replaceFirst("\\s+$", "")).append(StringUtils.NL);
        return true;
    }

    static final String ESCAPED_CHAR_PAT = "\\\\(?:[nrt'\\\\\"\\[\\]]|[0-2][0-7][0-7]|[0-7][0-7]?)";
    static final String CHAR_PAT = "(" + ESCAPED_CHAR_PAT + "|.)";                       // always need the ()-s
    static final String RANGE_PAT = "(" + CHAR_PAT + "-" + CHAR_PAT + ")|" + CHAR_PAT;    // no trailing * since we're going to loop using find()
    // TODO: keep the patterns in sync with Char() and Range()

    static final Pattern escapedCharPat = Pattern.compile(ESCAPED_CHAR_PAT);
    static final Pattern rangePat = Pattern.compile(RANGE_PAT);

    static final String PH_DQUOTE = "###DQ###";

    static String escapeChar(String str) {
        String retStr = str;
        if (retStr.equals("'")) {
            retStr = "\\'";
        }
        return retStr;
    }

    // TODO: make it more reliable
    static String escapeString(String str) {
        return str.replaceAll("\\\\\"", PH_DQUOTE)      // save escaped "-s
                .replaceAll("\"", "\\\\\"")           // escape all "-s
                .replaceAll(PH_DQUOTE, "\\\\\"");
    }
}


