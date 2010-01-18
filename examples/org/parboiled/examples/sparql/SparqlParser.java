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
    public Rule query() {
        return enforcedSequence(WS(), prologue(), firstOf(selectQuery(),
                constructQuery(), describeQuery(), askQuery()), eoi());
    }

    public Rule prologue() {
        return enforcedSequence(optional(baseDecl()), zeroOrMore(prefixDecl()));
    }

    public Rule baseDecl() {
        return sequence(BASE(), IRI_REF());
    }

    public Rule prefixDecl() {
        return enforcedSequence(PREFIX(), PNAME_NS(), IRI_REF());
    }

    public Rule selectQuery() {
        return enforcedSequence(SELECT(), optional(firstOf(DISTINCT(),
                REDUCED())), firstOf(oneOrMore(var()), ASTERISK()),
                zeroOrMore(datasetClause()), whereClause(), solutionModifier());
    }

    public Rule constructQuery() {
        return sequence(CONSTRUCT(), constructTemplate(),
                zeroOrMore(datasetClause()), whereClause(), solutionModifier());
    }

    public Rule describeQuery() {
        return sequence(DESCRIBE(), firstOf(oneOrMore(varOrIRIref()),
                ASTERISK()), zeroOrMore(datasetClause()),
                optional(whereClause()), solutionModifier());
    }

    public Rule askQuery() {
        return sequence(ASK(), zeroOrMore(datasetClause()), whereClause());
    }

    public Rule datasetClause() {
        return sequence(FROM(), firstOf(defaultGraphClause(),
                namedGraphClause()));
    }

    public Rule defaultGraphClause() {
        return sourceSelector();
    }

    public Rule namedGraphClause() {
        return sequence(NAMED(), sourceSelector());
    }

    public Rule sourceSelector() {
        return iriRef();
    }

    public Rule whereClause() {
        return sequence(optional(WHERE()), groupGraphPattern());
    }

    public Rule solutionModifier() {
        return sequence(optional(orderClause()), optional(limitOffsetClauses()));
    }

    public Rule limitOffsetClauses() {
        return firstOf(sequence(limitClause(), optional(offsetClause())),
                sequence(offsetClause(), optional(limitClause())));
    }

    public Rule orderClause() {
        return sequence(ORDER(), BY(), oneOrMore(orderCondition()));
    }

    public Rule orderCondition() {
        return firstOf(
                sequence(firstOf(ASC(), DESC()), brackettedExpression()),
                firstOf(constraint(), var()));
    }

    public Rule limitClause() {
        return sequence(LIMIT(), INTEGER());
    }

    public Rule offsetClause() {
        return sequence(OFFSET(), INTEGER());
    }

    public Rule groupGraphPattern() {
        return sequence(OPEN_CURLY_BRACE(), optional(triplesBlock()),
                zeroOrMore(sequence(
                        firstOf(graphPatternNotTriples(), filter()),
                        optional(DOT()), optional(triplesBlock()))),
                CLOSE_CURLY_BRACE());
    }

    public Rule triplesBlock() {
        return enforcedSequence(triplesSameSubject(), optional(sequence(DOT(),
                optional(triplesBlock()))));
    }

    public Rule graphPatternNotTriples() {
        return firstOf(optionalGraphPattern(), groupOrUnionGraphPattern(),
                graphGraphPattern());
    }

    public Rule optionalGraphPattern() {
        return sequence(OPTIONAL(), groupGraphPattern());
    }

    public Rule graphGraphPattern() {
        return sequence(GRAPH(), varOrIRIref(), groupGraphPattern());
    }

    public Rule groupOrUnionGraphPattern() {
        return sequence(groupGraphPattern(), zeroOrMore(sequence(UNION(),
                groupGraphPattern())));
    }

    public Rule filter() {
        return sequence(FILTER(), constraint());
    }

    public Rule constraint() {
        return firstOf(brackettedExpression(), builtInCall(), functionCall());
    }

    public Rule functionCall() {
        return sequence(iriRef(), argList());
    }

    public Rule argList() {
        return firstOf(sequence(OPEN_BRACE(), CLOSE_BRACE()), sequence(
                OPEN_BRACE(), expression(), zeroOrMore(sequence(COMMA(),
                        expression())), CLOSE_BRACE()));
    }

    public Rule constructTemplate() {
        return sequence(OPEN_CURLY_BRACE(), optional(constructTriples()),
                CLOSE_CURLY_BRACE());
    }

    public Rule constructTriples() {
        return sequence(triplesSameSubject(), optional(sequence(DOT(),
                optional(constructTriples()))));
    }

    public Rule triplesSameSubject() {
        return firstOf(sequence(varOrTerm(), propertyListNotEmpty()), sequence(
                triplesNode(), propertyList()));
    }

    public Rule propertyListNotEmpty() {
        return sequence(verb(), objectList(), zeroOrMore(sequence(SEMICOLON(),
                optional(sequence(verb(), objectList())))));
    }

    public Rule propertyList() {
        return optional(propertyListNotEmpty());
    }

    public Rule objectList() {
        return sequence(object(), zeroOrMore(sequence(COMMA(), object())));
    }

    public Rule object() {
        return graphNode();
    }

    public Rule verb() {
        return firstOf(varOrIRIref(), A());
    }

    public Rule triplesNode() {
        return firstOf(collection(), blankNodePropertyList());
    }

    public Rule blankNodePropertyList() {
        return sequence(OPEN_SQUARE_BRACE(), propertyListNotEmpty(),
                CLOSE_SQUARE_BRACE());
    }

    public Rule collection() {
        return sequence(OPEN_BRACE(), oneOrMore(graphNode()), CLOSE_BRACE());
    }

    public Rule graphNode() {
        return firstOf(varOrTerm(), triplesNode());
    }

    public Rule varOrTerm() {
        return firstOf(var(), graphTerm());
    }

    public Rule varOrIRIref() {
        return firstOf(var(), iriRef());
    }

    public Rule var() {
        return firstOf(VAR1(), VAR2());
    }

    public Rule graphTerm() {
        return firstOf(iriRef(), rdfLiteral(), numericLiteral(),
                booleanLiteral(), blankNode(), sequence(OPEN_BRACE(),
                        CLOSE_BRACE()));
    }

    public Rule expression() {
        return conditionalOrExpression();
    }

    public Rule conditionalOrExpression() {
        return sequence(conditionalAndExpression(), zeroOrMore(sequence(OR(),
                conditionalAndExpression())));
    }

    public Rule conditionalAndExpression() {
        return sequence(valueLogical(), zeroOrMore(sequence(AND(),
                valueLogical())));
    }

    public Rule valueLogical() {
        return relationalExpression();
    }

    public Rule relationalExpression() {
        return sequence(numericExpression(), optional(firstOf(//
                sequence(EQUAL(), numericExpression()), //
                sequence(NOT_EQUAL(), numericExpression()), //
                sequence(LESS(), numericExpression()), //
                sequence(GREATER(), numericExpression()), //
                sequence(LESS_EQUAL(), numericExpression()), //
                sequence(GREATER_EQUAL(), numericExpression()) //
        ) //
        ));
    }

    public Rule numericExpression() {
        return additiveExpression();
    }

    public Rule additiveExpression() {
        return sequence(multiplicativeExpression(), //
                zeroOrMore(firstOf(
                        sequence(PLUS(), multiplicativeExpression()), //
                        sequence(MINUS(), multiplicativeExpression()), //
                        numericLiteralPositive(), numericLiteralNegative()) //
                ));
    }

    public Rule multiplicativeExpression() {
        return sequence(unaryExpression(), zeroOrMore(firstOf(sequence(
                ASTERISK(), unaryExpression()), sequence(DIVIDE(),
                unaryExpression()))));
    }

    public Rule unaryExpression() {
        return firstOf(sequence(NOT(), primaryExpression()), sequence(PLUS(),
                primaryExpression()), sequence(MINUS(), primaryExpression()),
                primaryExpression());
    }

    public Rule primaryExpression() {
        return firstOf(brackettedExpression(), builtInCall(),
                iriRefOrFunction(), rdfLiteral(), numericLiteral(),
                booleanLiteral(), var());
    }

    public Rule brackettedExpression() {
        return sequence(OPEN_BRACE(), expression(), CLOSE_BRACE());
    }

    public Rule builtInCall() {
        return firstOf(
                sequence(STR(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                sequence(LANG(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                sequence(LANGMATCHES(), OPEN_BRACE(), expression(), COMMA(),
                        expression(), CLOSE_BRACE()),
                sequence(DATATYPE(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                sequence(BOUND(), OPEN_BRACE(), var(), CLOSE_BRACE()),
                sequence(SAMETERM(), OPEN_BRACE(), expression(), COMMA(),
                        expression(), CLOSE_BRACE()),
                sequence(ISIRI(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                sequence(ISURI(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                sequence(ISBLANK(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                sequence(ISLITERAL(), OPEN_BRACE(), expression(), CLOSE_BRACE()),
                regexExpression());
    }

    public Rule regexExpression() {
        return sequence(REGEX(), OPEN_BRACE(), expression(), COMMA(),
                expression(), optional(sequence(COMMA(), expression())),
                CLOSE_BRACE());
    }

    public Rule iriRefOrFunction() {
        return sequence(iriRef(), optional(argList()));
    }

    public Rule rdfLiteral() {
        return sequence(string(), optional(firstOf(LANGTAG(), sequence(
                REFERENCE(), iriRef()))));
    }

    public Rule numericLiteral() {
        return firstOf(numericLiteralUnsigned(), numericLiteralPositive(),
                numericLiteralNegative());
    }

    public Rule numericLiteralUnsigned() {
        return firstOf(DOUBLE(), DECIMAL(), INTEGER());
    }

    public Rule numericLiteralPositive() {
        return firstOf(DOUBLE_POSITIVE(), DECIMAL_POSITIVE(),
                INTEGER_POSITIVE());
    }

    public Rule numericLiteralNegative() {
        return firstOf(DOUBLE_NEGATIVE(), DECIMAL_NEGATIVE(),
                INTEGER_NEGATIVE());
    }

    public Rule booleanLiteral() {
        return firstOf(TRUE(), FALSE());
    }

    public Rule string() {
        return firstOf(STRING_LITERAL_LONG1(), STRING_LITERAL1(),
                STRING_LITERAL_LONG2(), STRING_LITERAL2());
    }

    public Rule iriRef() {
        return firstOf(IRI_REF(), prefixedName());
    }

    public Rule prefixedName() {
        return firstOf(PNAME_LN(), PNAME_NS());
    }

    public Rule blankNode() {
        return firstOf(BLANK_NODE_LABEL(), sequence(OPEN_SQUARE_BRACE(),
                CLOSE_SQUARE_BRACE()));
    }
    // </Parser>

    // <Lexer>

    public Rule WS() {
        return zeroOrMore(firstOf(COMMENT(), WS_NO_COMMENT()));
    }

    public Rule WS_NO_COMMENT() {
        return firstOf(ch(' '), ch('\t'), ch('\f'), EOL());
    }

    public Rule PNAME_NS() {
        return sequence(optional(PN_PREFIX()), chWS(':'));
    }

    public Rule PNAME_LN() {
        return sequence(PNAME_NS(), PN_LOCAL());
    }

    public Rule BASE() {
        return stringIgnoreCaseWS("BASE");
    }

    public Rule PREFIX() {
        return stringIgnoreCaseWS("PREFIX");
    }

    public Rule SELECT() {
        return stringIgnoreCaseWS("SELECT");
    }

    public Rule DISTINCT() {
        return stringIgnoreCaseWS("DISTINCT");
    }

    public Rule REDUCED() {
        return stringIgnoreCaseWS("REDUCED");
    }

    public Rule CONSTRUCT() {
        return stringIgnoreCaseWS("CONSTRUCT");
    }

    public Rule DESCRIBE() {
        return stringIgnoreCaseWS("DESCRIBE");
    }

    public Rule ASK() {
        return stringIgnoreCaseWS("ASK");
    }

    public Rule FROM() {
        return stringIgnoreCaseWS("FROM");
    }

    public Rule NAMED() {
        return stringIgnoreCaseWS("NAMED");
    }

    public Rule WHERE() {
        return stringIgnoreCaseWS("WHERE");
    }

    public Rule ORDER() {
        return stringIgnoreCaseWS("ORDER");
    }

    public Rule BY() {
        return stringIgnoreCaseWS("BY");
    }

    public Rule ASC() {
        return stringIgnoreCaseWS("ASC");
    }

    public Rule DESC() {
        return stringIgnoreCaseWS("DESC");
    }

    public Rule LIMIT() {
        return stringIgnoreCaseWS("LIMIT");
    }

    public Rule OFFSET() {
        return stringIgnoreCaseWS("OFFSET");
    }

    public Rule OPTIONAL() {
        return stringIgnoreCaseWS("OPTIONAL");
    }

    public Rule GRAPH() {
        return stringIgnoreCaseWS("GRAPH");
    }

    public Rule UNION() {
        return stringIgnoreCaseWS("UNION");
    }

    public Rule FILTER() {
        return stringIgnoreCaseWS("FILTER");
    }

    public Rule A() {
        return chWS('a');
    }

    public Rule STR() {
        return stringIgnoreCaseWS("STR");
    }

    public Rule LANG() {
        return stringIgnoreCaseWS("LANG");
    }

    public Rule LANGMATCHES() {
        return stringIgnoreCaseWS("LANGMATCHES");
    }

    public Rule DATATYPE() {
        return stringIgnoreCaseWS("DATATYPE");
    }

    public Rule BOUND() {
        return stringIgnoreCaseWS("BOUND");
    }

    public Rule SAMETERM() {
        return stringIgnoreCaseWS("SAMETERM");
    }

    public Rule ISIRI() {
        return stringIgnoreCaseWS("ISIRI");
    }

    public Rule ISURI() {
        return stringIgnoreCaseWS("ISURI");
    }

    public Rule ISBLANK() {
        return stringIgnoreCaseWS("ISBLANK");
    }

    public Rule ISLITERAL() {
        return stringIgnoreCaseWS("ISLITERAL");
    }

    public Rule REGEX() {
        return stringIgnoreCaseWS("REGEX");
    }

    public Rule TRUE() {
        return stringIgnoreCaseWS("TRUE");
    }

    public Rule FALSE() {
        return stringIgnoreCaseWS("FALSE");
    }

    public Rule IRI_REF() {
        return sequence(LESS_NO_COMMENT(), //
                zeroOrMore(sequence(testNot(firstOf(LESS_NO_COMMENT(), GREATER(), '"', OPEN_CURLY_BRACE(),
                        CLOSE_CURLY_BRACE(), '|', '^', '\\', '`', charRange('\u0000', '\u0020'))), any())), //
                GREATER());
    }

    public Rule BLANK_NODE_LABEL() {
        return sequence("_:", PN_LOCAL(), WS());
    }

    public Rule VAR1() {
        return sequence('?', VARNAME(), WS());
    }

    public Rule VAR2() {
        return sequence('$', VARNAME(), WS());
    }

    public Rule LANGTAG() {
        return sequence('@', oneOrMore(PN_CHARS_BASE()), zeroOrMore(sequence(
                MINUS(), oneOrMore(sequence(PN_CHARS_BASE(), DIGIT())))), WS());
    }

    public Rule INTEGER() {
        return sequence(oneOrMore(DIGIT()), WS());
    }

    public Rule DECIMAL() {
        return sequence(firstOf( //
                sequence(oneOrMore(DIGIT()), DOT(), zeroOrMore(DIGIT())), //
                sequence(DOT(), oneOrMore(DIGIT())) //
        ), WS());
    }

    public Rule DOUBLE() {
        return sequence(firstOf(//
                sequence(oneOrMore(DIGIT()), DOT(), zeroOrMore(DIGIT()),
                        EXPONENT()), //
                sequence(DOT(), oneOrMore(DIGIT()), EXPONENT()), //
                sequence(oneOrMore(DIGIT()), EXPONENT())), WS());
    }

    public Rule INTEGER_POSITIVE() {
        return sequence(PLUS(), INTEGER());
    }

    public Rule DECIMAL_POSITIVE() {
        return sequence(PLUS(), DECIMAL());
    }

    public Rule DOUBLE_POSITIVE() {
        return sequence(PLUS(), DOUBLE());
    }

    public Rule INTEGER_NEGATIVE() {
        return sequence(MINUS(), INTEGER());
    }

    public Rule DECIMAL_NEGATIVE() {
        return sequence(MINUS(), DECIMAL());
    }

    public Rule DOUBLE_NEGATIVE() {
        return sequence(MINUS(), DOUBLE());
    }

    public Rule EXPONENT() {
        return sequence(firstOf('e', 'E'), optional(firstOf(PLUS(), MINUS())),
                oneOrMore(DIGIT()));
    }

    public Rule STRING_LITERAL1() {
        return sequence("'", zeroOrMore(firstOf(sequence(testNot(firstOf("'",
                '\\', '\n', '\r')), any()), ECHAR())), "'", WS());
    }

    public Rule STRING_LITERAL2() {
        return sequence('"', zeroOrMore(firstOf(sequence(testNot(firstOf('"',
                '\\', '\n', '\r')), any()), ECHAR())), '"', WS());
    }

    public Rule STRING_LITERAL_LONG1() {
        return sequence("'''", zeroOrMore(sequence(
                optional(firstOf("''", "'")), firstOf(sequence(testNot(firstOf(
                        "'", "\\")), any()), ECHAR()))), "'''", WS());
    }

    public Rule STRING_LITERAL_LONG2() {
        return sequence("\"\"\"", zeroOrMore(sequence(optional(firstOf("\"\"", "\"")),
                firstOf(sequence(testNot(firstOf("\"", "\\")), any()), ECHAR()))), "\"\"\"", WS());
    }

    public Rule ECHAR() {
        return sequence('\\', firstOf('t', 'b', 'n', 'r', 'f', '\\', '"', '\''));
    }

    public Rule PN_CHARS_U() {
        return firstOf(PN_CHARS_BASE(), '_');
    }

    public Rule VARNAME() {
        return sequence(firstOf(PN_CHARS_U(), DIGIT()), zeroOrMore(firstOf(
                PN_CHARS_U(), DIGIT(), '\u00B7', charRange('\u0300', '\u036F'), charRange('\u203F', '\u2040'))), WS());
    }

    public Rule PN_CHARS() {
        return firstOf(MINUS(), DIGIT(), PN_CHARS_U(), '\u00B7',
                charRange('\u0300', '\u036F'), charRange('\u203F', '\u2040'));
    }

    public Rule PN_PREFIX() {
        return sequence(PN_CHARS_BASE(), optional(zeroOrMore(firstOf(PN_CHARS(), sequence(DOT(), PN_CHARS())))));
    }

    public Rule PN_LOCAL() {
        return sequence(firstOf(PN_CHARS_U(), DIGIT()),
                optional(zeroOrMore(firstOf(PN_CHARS(), sequence(DOT(), PN_CHARS())))), WS());
    }

    public Rule PN_CHARS_BASE() {
        return firstOf( //
                charRange('A', 'Z'),//
                charRange('a', 'z'), //
                charRange('\u00C0', '\u00D6'), //
                charRange('\u00D8', '\u00F6'), //
                charRange('\u00F8', '\u02FF'), //
                charRange('\u0370', '\u037D'), //
                charRange('\u037F', '\u1FFF'), //
                charRange('\u200C', '\u200D'), //
                charRange('\u2070', '\u218F'), //
                charRange('\u2C00', '\u2FEF'), //
                charRange('\u3001', '\uD7FF'), //
                charRange('\uF900', '\uFDCF'), //
                charRange('\uFDF0', '\uFFFD') //
        );
    }

    public Rule DIGIT() {
        return charRange('0', '9');
    }

    public Rule COMMENT() {
        return sequence('#', zeroOrMore(sequence(testNot(EOL()), any())), EOL());
    }

    public Rule EOL() {
        return firstOf('\n', '\r');
    }

    public Rule REFERENCE() {
        return stringWS("^^");
    }

    public Rule LESS_EQUAL() {
        return stringWS("<=");
    }

    public Rule GREATER_EQUAL() {
        return stringWS(">=");
    }

    public Rule NOT_EQUAL() {
        return stringWS("!=");
    }

    public Rule AND() {
        return stringWS("&&");
    }

    public Rule OR() {
        return stringWS("||");
    }

    public Rule OPEN_BRACE() {
        return chWS('(');
    }

    public Rule CLOSE_BRACE() {
        return chWS(')');
    }

    public Rule OPEN_CURLY_BRACE() {
        return chWS('{');
    }

    public Rule CLOSE_CURLY_BRACE() {
        return chWS('}');
    }

    public Rule OPEN_SQUARE_BRACE() {
        return chWS('[');
    }

    public Rule CLOSE_SQUARE_BRACE() {
        return chWS(']');
    }

    public Rule SEMICOLON() {
        return chWS(';');
    }

    public Rule DOT() {
        return chWS('.');
    }

    public Rule PLUS() {
        return chWS('+');
    }

    public Rule MINUS() {
        return chWS('-');
    }

    public Rule ASTERISK() {
        return chWS('*');
    }

    public Rule COMMA() {
        return chWS(',');
    }

    public Rule NOT() {
        return chWS('!');
    }

    public Rule DIVIDE() {
        return chWS('/');
    }

    public Rule EQUAL() {
        return chWS('=');
    }

    public Rule LESS_NO_COMMENT() {
        return sequence(ch('<'), zeroOrMore(WS_NO_COMMENT()));
    }

    public Rule LESS() {
        return chWS('<');
    }

    public Rule GREATER() {
        return chWS('>');
    }
    // </Lexer>

    public Rule chWS(char c) {
        return sequence(ch(c), WS());
    }

    public Rule stringWS(String s) {
        return sequence(string(s), WS());
    }

    public Rule stringIgnoreCaseWS(String string) {
        return sequence(stringIgnoreCase(string), WS());
    }

}