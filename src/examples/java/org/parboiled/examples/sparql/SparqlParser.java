/*
 * Copyright (c) 2009 Ken Wenzel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.parboiled.examples.sparql;

import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * SPARQL Parser
 *
 * @author Ken Wenzel, adapted by Mathias Doenitz
 */
@SuppressWarnings({"InfiniteRecursion"})
public class SparqlParser extends BaseParser<Object> {
    // <Parser>
    public Rule Query() {
        return Sequence(WS(), Prologue(), FirstOf(SelectQuery(),
                ConstructQuery(), DescribeQuery(), AskQuery()), EOI);
    }

    public Rule Prologue() {
        return Sequence(Optional(BaseDecl()), ZeroOrMore(PrefixDecl()));
    }

    public Rule BaseDecl() {
        return Sequence(BASE(), IRI_REF());
    }

    public Rule PrefixDecl() {
        return Sequence(PREFIX(), PNAME_NS(), IRI_REF());
    }

    public Rule SelectQuery() {
        return Sequence(SELECT(), Optional(FirstOf(DISTINCT(),
                REDUCED())), FirstOf(OneOrMore(Var()), ASTERISK()),
                ZeroOrMore(DatasetClause()), WhereClause(), SolutionModifier());
    }

    public Rule ConstructQuery() {
        return Sequence(CONSTRUCT(), ConstructTemplate(),
                ZeroOrMore(DatasetClause()), WhereClause(), SolutionModifier());
    }

    public Rule DescribeQuery() {
        return Sequence(DESCRIBE(), FirstOf(OneOrMore(VarOrIRIref()),
                ASTERISK()), ZeroOrMore(DatasetClause()),
                Optional(WhereClause()), SolutionModifier());
    }

    public Rule AskQuery() {
        return Sequence(ASK(), ZeroOrMore(DatasetClause()), WhereClause());
    }

    public Rule DatasetClause() {
        return Sequence(FROM(), FirstOf(DefaultGraphClause(),
                NamedGraphClause()));
    }

    public Rule DefaultGraphClause() {
        return SourceSelector();
    }

    public Rule NamedGraphClause() {
        return Sequence(NAMED(), SourceSelector());
    }

    public Rule SourceSelector() {
        return IriRef();
    }

    public Rule WhereClause() {
        return Sequence(Optional(WHERE()), GroupGraphPattern());
    }

    public Rule SolutionModifier() {
        return Sequence(Optional(OrderClause()), Optional(LimitOffsetClauses()));
    }

    public Rule LimitOffsetClauses() {
        return FirstOf(Sequence(LimitClause(), Optional(OffsetClause())),
                Sequence(OffsetClause(), Optional(LimitClause())));
    }

    public Rule OrderClause() {
        return Sequence(ORDER(), BY(), OneOrMore(OrderCondition()));
    }

    public Rule OrderCondition() {
        return FirstOf(
                Sequence(FirstOf(ASC(), DESC()), BrackettedExpression()),
                FirstOf(Constraint(), Var()));
    }

    public Rule LimitClause() {
        return Sequence(LIMIT(), INTEGER());
    }

    public Rule OffsetClause() {
        return Sequence(OFFSET(), INTEGER());
    }

    public Rule GroupGraphPattern() {
        return Sequence(OPEN_CURLY_BRACE(), Optional(TriplesBlock()),
                ZeroOrMore(Sequence(
                        FirstOf(GraphPatternNotTriples(), Filter()),
                        Optional(DOT()), Optional(TriplesBlock()))),
                CLOSE_CURLY_BRACE());
    }

    public Rule TriplesBlock() {
        return Sequence(TriplesSameSubject(), Optional(Sequence(DOT(),
                Optional(TriplesBlock()))));
    }

    public Rule GraphPatternNotTriples() {
        return FirstOf(OptionalGraphPattern(), GroupOrUnionGraphPattern(),
                GraphGraphPattern());
    }

    public Rule OptionalGraphPattern() {
        return Sequence(OPTIONAL(), GroupGraphPattern());
    }

    public Rule GraphGraphPattern() {
        return Sequence(GRAPH(), VarOrIRIref(), GroupGraphPattern());
    }

    public Rule GroupOrUnionGraphPattern() {
        return Sequence(GroupGraphPattern(), ZeroOrMore(Sequence(UNION(),
                GroupGraphPattern())));
    }

    public Rule Filter() {
        return Sequence(FILTER(), Constraint());
    }

    public Rule Constraint() {
        return FirstOf(BrackettedExpression(), BuiltInCall(), FunctionCall());
    }

    public Rule FunctionCall() {
        return Sequence(IriRef(), ArgList());
    }

    public Rule ArgList() {
        return FirstOf(Sequence(OPEN_BRACE(), CLOSE_BRACE()), Sequence(
                OPEN_BRACE(), Expression(), ZeroOrMore(Sequence(COMMA(),
                        Expression())), CLOSE_BRACE()));
    }

    public Rule ConstructTemplate() {
        return Sequence(OPEN_CURLY_BRACE(), Optional(ConstructTriples()),
                CLOSE_CURLY_BRACE());
    }

    public Rule ConstructTriples() {
        return Sequence(TriplesSameSubject(), Optional(Sequence(DOT(),
                Optional(ConstructTriples()))));
    }

    public Rule TriplesSameSubject() {
        return FirstOf(Sequence(VarOrTerm(), PropertyListNotEmpty()), Sequence(
                TriplesNode(), PropertyList()));
    }

    public Rule PropertyListNotEmpty() {
        return Sequence(Verb(), ObjectList(), ZeroOrMore(Sequence(SEMICOLON(),
                Optional(Sequence(Verb(), ObjectList())))));
    }

    public Rule PropertyList() {
        return Optional(PropertyListNotEmpty());
    }

    public Rule ObjectList() {
        return Sequence(Object_(), ZeroOrMore(Sequence(COMMA(), Object_())));
    }

    public Rule Object_() {
        return GraphNode();
    }

    public Rule Verb() {
        return FirstOf(VarOrIRIref(), A());
    }

    public Rule TriplesNode() {
        return FirstOf(Collection(), BlankNodePropertyList());
    }

    public Rule BlankNodePropertyList() {
        return Sequence(OPEN_SQUARE_BRACE(), PropertyListNotEmpty(),
                CLOSE_SQUARE_BRACE());
    }

    public Rule Collection() {
        return Sequence(OPEN_BRACE(), OneOrMore(GraphNode()), CLOSE_BRACE());
    }

    public Rule GraphNode() {
        return FirstOf(VarOrTerm(), TriplesNode());
    }

    public Rule VarOrTerm() {
        return FirstOf(Var(), GraphTerm());
    }

    public Rule VarOrIRIref() {
        return FirstOf(Var(), IriRef());
    }

    public Rule Var() {
        return FirstOf(VAR1(), VAR2());
    }

    public Rule GraphTerm() {
        return FirstOf(IriRef(), RdfLiteral(), NumericLiteral(),
                BooleanLiteral(), BlankNode(), Sequence(OPEN_BRACE(),
                        CLOSE_BRACE()));
    }

    public Rule Expression() {
        return ConditionalOrExpression();
    }

    public Rule ConditionalOrExpression() {
        return Sequence(ConditionalAndExpression(), ZeroOrMore(Sequence(OR(),
                ConditionalAndExpression())));
    }

    public Rule ConditionalAndExpression() {
        return Sequence(ValueLogical(), ZeroOrMore(Sequence(AND(),
                ValueLogical())));
    }

    public Rule ValueLogical() {
        return RelationalExpression();
    }

    public Rule RelationalExpression() {
        return Sequence(NumericExpression(), Optional(FirstOf(//
                Sequence(EQUAL(), NumericExpression()), //
                Sequence(NOT_EQUAL(), NumericExpression()), //
                Sequence(LESS(), NumericExpression()), //
                Sequence(GREATER(), NumericExpression()), //
                Sequence(LESS_EQUAL(), NumericExpression()), //
                Sequence(GREATER_EQUAL(), NumericExpression()) //
        ) //
        ));
    }

    public Rule NumericExpression() {
        return AdditiveExpression();
    }

    public Rule AdditiveExpression() {
        return Sequence(MultiplicativeExpression(), //
                ZeroOrMore(FirstOf(
                        Sequence(PLUS(), MultiplicativeExpression()), //
                        Sequence(MINUS(), MultiplicativeExpression()), //
                        NumericLiteralPositive(), NumericLiteralNegative()) //
                ));
    }

    public Rule MultiplicativeExpression() {
        return Sequence(UnaryExpression(), ZeroOrMore(FirstOf(Sequence(
                ASTERISK(), UnaryExpression()), Sequence(DIVIDE(),
                UnaryExpression()))));
    }

    public Rule UnaryExpression() {
        return FirstOf(Sequence(NOT(), PrimaryExpression()), Sequence(PLUS(),
                PrimaryExpression()), Sequence(MINUS(), PrimaryExpression()),
                PrimaryExpression());
    }

    public Rule PrimaryExpression() {
        return FirstOf(BrackettedExpression(), BuiltInCall(),
                IriRefOrFunction(), RdfLiteral(), NumericLiteral(),
                BooleanLiteral(), Var());
    }

    public Rule BrackettedExpression() {
        return Sequence(OPEN_BRACE(), Expression(), CLOSE_BRACE());
    }

    public Rule BuiltInCall() {
        return FirstOf(
                Sequence(STR(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                Sequence(LANG(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                Sequence(LANGMATCHES(), OPEN_BRACE(), Expression(), COMMA(),
                        Expression(), CLOSE_BRACE()),
                Sequence(DATATYPE(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                Sequence(BOUND(), OPEN_BRACE(), Var(), CLOSE_BRACE()),
                Sequence(SAMETERM(), OPEN_BRACE(), Expression(), COMMA(),
                        Expression(), CLOSE_BRACE()),
                Sequence(ISIRI(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                Sequence(ISURI(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                Sequence(ISBLANK(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                Sequence(ISLITERAL(), OPEN_BRACE(), Expression(), CLOSE_BRACE()),
                RegexExpression());
    }

    public Rule RegexExpression() {
        return Sequence(REGEX(), OPEN_BRACE(), Expression(), COMMA(),
                Expression(), Optional(Sequence(COMMA(), Expression())),
                CLOSE_BRACE());
    }

    public Rule IriRefOrFunction() {
        return Sequence(IriRef(), Optional(ArgList()));
    }

    public Rule RdfLiteral() {
        return Sequence(String(), Optional(FirstOf(LANGTAG(), Sequence(
                REFERENCE(), IriRef()))));
    }

    public Rule NumericLiteral() {
        return FirstOf(NumericLiteralUnsigned(), NumericLiteralPositive(),
                NumericLiteralNegative());
    }

    public Rule NumericLiteralUnsigned() {
        return FirstOf(DOUBLE(), DECIMAL(), INTEGER());
    }

    public Rule NumericLiteralPositive() {
        return FirstOf(DOUBLE_POSITIVE(), DECIMAL_POSITIVE(),
                INTEGER_POSITIVE());
    }

    public Rule NumericLiteralNegative() {
        return FirstOf(DOUBLE_NEGATIVE(), DECIMAL_NEGATIVE(),
                INTEGER_NEGATIVE());
    }

    public Rule BooleanLiteral() {
        return FirstOf(TRUE(), FALSE());
    }

    public Rule String() {
        return FirstOf(STRING_LITERAL_LONG1(), STRING_LITERAL1(),
                STRING_LITERAL_LONG2(), STRING_LITERAL2());
    }

    public Rule IriRef() {
        return FirstOf(IRI_REF(), PrefixedName());
    }

    public Rule PrefixedName() {
        return FirstOf(PNAME_LN(), PNAME_NS());
    }

    public Rule BlankNode() {
        return FirstOf(BLANK_NODE_LABEL(), Sequence(OPEN_SQUARE_BRACE(),
                CLOSE_SQUARE_BRACE()));
    }
    // </Parser>

    // <Lexer>

    public Rule WS() {
        return ZeroOrMore(FirstOf(COMMENT(), WS_NO_COMMENT()));
    }

    public Rule WS_NO_COMMENT() {
        return FirstOf(Ch(' '), Ch('\t'), Ch('\f'), EOL());
    }

    public Rule PNAME_NS() {
        return Sequence(Optional(PN_PREFIX()), ChWS(':'));
    }

    public Rule PNAME_LN() {
        return Sequence(PNAME_NS(), PN_LOCAL());
    }

    public Rule BASE() {
        return StringIgnoreCaseWS("BASE");
    }

    public Rule PREFIX() {
        return StringIgnoreCaseWS("PREFIX");
    }

    public Rule SELECT() {
        return StringIgnoreCaseWS("SELECT");
    }

    public Rule DISTINCT() {
        return StringIgnoreCaseWS("DISTINCT");
    }

    public Rule REDUCED() {
        return StringIgnoreCaseWS("REDUCED");
    }

    public Rule CONSTRUCT() {
        return StringIgnoreCaseWS("CONSTRUCT");
    }

    public Rule DESCRIBE() {
        return StringIgnoreCaseWS("DESCRIBE");
    }

    public Rule ASK() {
        return StringIgnoreCaseWS("ASK");
    }

    public Rule FROM() {
        return StringIgnoreCaseWS("FROM");
    }

    public Rule NAMED() {
        return StringIgnoreCaseWS("NAMED");
    }

    public Rule WHERE() {
        return StringIgnoreCaseWS("WHERE");
    }

    public Rule ORDER() {
        return StringIgnoreCaseWS("ORDER");
    }

    public Rule BY() {
        return StringIgnoreCaseWS("BY");
    }

    public Rule ASC() {
        return StringIgnoreCaseWS("ASC");
    }

    public Rule DESC() {
        return StringIgnoreCaseWS("DESC");
    }

    public Rule LIMIT() {
        return StringIgnoreCaseWS("LIMIT");
    }

    public Rule OFFSET() {
        return StringIgnoreCaseWS("OFFSET");
    }

    public Rule OPTIONAL() {
        return StringIgnoreCaseWS("OPTIONAL");
    }

    public Rule GRAPH() {
        return StringIgnoreCaseWS("GRAPH");
    }

    public Rule UNION() {
        return StringIgnoreCaseWS("UNION");
    }

    public Rule FILTER() {
        return StringIgnoreCaseWS("FILTER");
    }

    public Rule A() {
        return ChWS('a');
    }

    public Rule STR() {
        return StringIgnoreCaseWS("STR");
    }

    public Rule LANG() {
        return StringIgnoreCaseWS("LANG");
    }

    public Rule LANGMATCHES() {
        return StringIgnoreCaseWS("LANGMATCHES");
    }

    public Rule DATATYPE() {
        return StringIgnoreCaseWS("DATATYPE");
    }

    public Rule BOUND() {
        return StringIgnoreCaseWS("BOUND");
    }

    public Rule SAMETERM() {
        return StringIgnoreCaseWS("SAMETERM");
    }

    public Rule ISIRI() {
        return StringIgnoreCaseWS("ISIRI");
    }

    public Rule ISURI() {
        return StringIgnoreCaseWS("ISURI");
    }

    public Rule ISBLANK() {
        return StringIgnoreCaseWS("ISBLANK");
    }

    public Rule ISLITERAL() {
        return StringIgnoreCaseWS("ISLITERAL");
    }

    public Rule REGEX() {
        return StringIgnoreCaseWS("REGEX");
    }

    public Rule TRUE() {
        return StringIgnoreCaseWS("TRUE");
    }

    public Rule FALSE() {
        return StringIgnoreCaseWS("FALSE");
    }

    public Rule IRI_REF() {
        return Sequence(LESS_NO_COMMENT(), //
                ZeroOrMore(Sequence(TestNot(FirstOf(LESS_NO_COMMENT(), GREATER(), '"', OPEN_CURLY_BRACE(),
                        CLOSE_CURLY_BRACE(), '|', '^', '\\', '`', CharRange('\u0000', '\u0020'))), ANY)), //
                GREATER());
    }

    public Rule BLANK_NODE_LABEL() {
        return Sequence("_:", PN_LOCAL(), WS());
    }

    public Rule VAR1() {
        return Sequence('?', VARNAME(), WS());
    }

    public Rule VAR2() {
        return Sequence('$', VARNAME(), WS());
    }

    public Rule LANGTAG() {
        return Sequence('@', OneOrMore(PN_CHARS_BASE()), ZeroOrMore(Sequence(
                MINUS(), OneOrMore(Sequence(PN_CHARS_BASE(), DIGIT())))), WS());
    }

    public Rule INTEGER() {
        return Sequence(OneOrMore(DIGIT()), WS());
    }

    public Rule DECIMAL() {
        return Sequence(FirstOf( //
                Sequence(OneOrMore(DIGIT()), DOT(), ZeroOrMore(DIGIT())), //
                Sequence(DOT(), OneOrMore(DIGIT())) //
        ), WS());
    }

    public Rule DOUBLE() {
        return Sequence(FirstOf(//
                Sequence(OneOrMore(DIGIT()), DOT(), ZeroOrMore(DIGIT()),
                        EXPONENT()), //
                Sequence(DOT(), OneOrMore(DIGIT()), EXPONENT()), //
                Sequence(OneOrMore(DIGIT()), EXPONENT())), WS());
    }

    public Rule INTEGER_POSITIVE() {
        return Sequence(PLUS(), INTEGER());
    }

    public Rule DECIMAL_POSITIVE() {
        return Sequence(PLUS(), DECIMAL());
    }

    public Rule DOUBLE_POSITIVE() {
        return Sequence(PLUS(), DOUBLE());
    }

    public Rule INTEGER_NEGATIVE() {
        return Sequence(MINUS(), INTEGER());
    }

    public Rule DECIMAL_NEGATIVE() {
        return Sequence(MINUS(), DECIMAL());
    }

    public Rule DOUBLE_NEGATIVE() {
        return Sequence(MINUS(), DOUBLE());
    }

    public Rule EXPONENT() {
        return Sequence(IgnoreCase('e'), Optional(FirstOf(PLUS(), MINUS())),
                OneOrMore(DIGIT()));
    }

    public Rule STRING_LITERAL1() {
        return Sequence("'", ZeroOrMore(FirstOf(Sequence(TestNot(FirstOf("'",
                '\\', '\n', '\r')), ANY), ECHAR())), "'", WS());
    }

    public Rule STRING_LITERAL2() {
        return Sequence('"', ZeroOrMore(FirstOf(Sequence(TestNot(AnyOf("\"\\\n\r")), ANY), ECHAR())), '"', WS());
    }

    public Rule STRING_LITERAL_LONG1() {
        return Sequence("'''", ZeroOrMore(Sequence(
                Optional(FirstOf("''", "'")), FirstOf(Sequence(TestNot(FirstOf(
                        "'", "\\")), ANY), ECHAR()))), "'''", WS());
    }

    public Rule STRING_LITERAL_LONG2() {
        return Sequence("\"\"\"", ZeroOrMore(Sequence(Optional(FirstOf("\"\"", "\"")),
                FirstOf(Sequence(TestNot(FirstOf("\"", "\\")), ANY), ECHAR()))), "\"\"\"", WS());
    }

    public Rule ECHAR() {
        return Sequence('\\', AnyOf("tbnrf\\\"\'"));
    }

    public Rule PN_CHARS_U() {
        return FirstOf(PN_CHARS_BASE(), '_');
    }

    public Rule VARNAME() {
        return Sequence(FirstOf(PN_CHARS_U(), DIGIT()), ZeroOrMore(FirstOf(
                PN_CHARS_U(), DIGIT(), '\u00B7', CharRange('\u0300', '\u036F'), CharRange('\u203F', '\u2040'))), WS());
    }

    public Rule PN_CHARS() {
        return FirstOf(MINUS(), DIGIT(), PN_CHARS_U(), '\u00B7',
                CharRange('\u0300', '\u036F'), CharRange('\u203F', '\u2040'));
    }

    public Rule PN_PREFIX() {
        return Sequence(PN_CHARS_BASE(), Optional(ZeroOrMore(FirstOf(PN_CHARS(), Sequence(DOT(), PN_CHARS())))));
    }

    public Rule PN_LOCAL() {
        return Sequence(FirstOf(PN_CHARS_U(), DIGIT()),
                Optional(ZeroOrMore(FirstOf(PN_CHARS(), Sequence(DOT(), PN_CHARS())))), WS());
    }

    public Rule PN_CHARS_BASE() {
        return FirstOf( //
                CharRange('A', 'Z'),//
                CharRange('a', 'z'), //
                CharRange('\u00C0', '\u00D6'), //
                CharRange('\u00D8', '\u00F6'), //
                CharRange('\u00F8', '\u02FF'), //
                CharRange('\u0370', '\u037D'), //
                CharRange('\u037F', '\u1FFF'), //
                CharRange('\u200C', '\u200D'), //
                CharRange('\u2070', '\u218F'), //
                CharRange('\u2C00', '\u2FEF'), //
                CharRange('\u3001', '\uD7FF'), //
                CharRange('\uF900', '\uFDCF'), //
                CharRange('\uFDF0', '\uFFFD') //
        );
    }

    public Rule DIGIT() {
        return CharRange('0', '9');
    }

    public Rule COMMENT() {
        return Sequence('#', ZeroOrMore(Sequence(TestNot(EOL()), ANY)), EOL());
    }

    public Rule EOL() {
        return AnyOf("\n\r");
    }

    public Rule REFERENCE() {
        return StringWS("^^");
    }

    public Rule LESS_EQUAL() {
        return StringWS("<=");
    }

    public Rule GREATER_EQUAL() {
        return StringWS(">=");
    }

    public Rule NOT_EQUAL() {
        return StringWS("!=");
    }

    public Rule AND() {
        return StringWS("&&");
    }

    public Rule OR() {
        return StringWS("||");
    }

    public Rule OPEN_BRACE() {
        return ChWS('(');
    }

    public Rule CLOSE_BRACE() {
        return ChWS(')');
    }

    public Rule OPEN_CURLY_BRACE() {
        return ChWS('{');
    }

    public Rule CLOSE_CURLY_BRACE() {
        return ChWS('}');
    }

    public Rule OPEN_SQUARE_BRACE() {
        return ChWS('[');
    }

    public Rule CLOSE_SQUARE_BRACE() {
        return ChWS(']');
    }

    public Rule SEMICOLON() {
        return ChWS(';');
    }

    public Rule DOT() {
        return ChWS('.');
    }

    public Rule PLUS() {
        return ChWS('+');
    }

    public Rule MINUS() {
        return ChWS('-');
    }

    public Rule ASTERISK() {
        return ChWS('*');
    }

    public Rule COMMA() {
        return ChWS(',');
    }

    public Rule NOT() {
        return ChWS('!');
    }

    public Rule DIVIDE() {
        return ChWS('/');
    }

    public Rule EQUAL() {
        return ChWS('=');
    }

    public Rule LESS_NO_COMMENT() {
        return Sequence(Ch('<'), ZeroOrMore(WS_NO_COMMENT()));
    }

    public Rule LESS() {
        return ChWS('<');
    }

    public Rule GREATER() {
        return ChWS('>');
    }
    // </Lexer>

    public Rule ChWS(char c) {
        return Sequence(Ch(c), WS());
    }

    public Rule StringWS(String s) {
        return Sequence(String(s), WS());
    }

    public Rule StringIgnoreCaseWS(String string) {
        return Sequence(IgnoreCase(string), WS());
    }

}