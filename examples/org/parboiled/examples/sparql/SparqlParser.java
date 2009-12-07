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

import org.parboiled.Actions;
import org.parboiled.BaseParser;
import org.parboiled.Rule;

/**
 * SPARQL Parser
 *
 * @author Ken Wenzel, adapted by Mathias Doenitz
 */
public class SparqlParser extends BaseParser<Object, Actions<Object>> {

    public Rule query() {
        return enforcedSequence(WS(), prologue(), firstOf(selectQuery(),
                constructQuery(), describeQuery(), askQuery()), eoi());
    }

    public Rule prologue() {
        return enforcedSequence(optional(baseDecl()), zeroOrMore(prefixDecl()));
    }

    public Rule baseDecl() {
        return sequence("BASE", IRI_REF());
    }

    public Rule prefixDecl() {
        return enforcedSequence("PREFIX", PNAME_NS(), IRI_REF());
    }

    public Rule selectQuery() {
        return enforcedSequence("SELECT", optional(firstOf("DISTINCT",
                "REDUCED")), firstOf(oneOrMore(var()), '*'),
                zeroOrMore(datasetClause()), whereClause(), solutionModifier());
    }

    public Rule constructQuery() {
        return sequence("CONSTRUCT", constructTemplate(),
                zeroOrMore(datasetClause()), whereClause(), solutionModifier());
    }

    public Rule describeQuery() {
        return sequence("DESCRIBE", firstOf(oneOrMore(varOrIRIref()),
                '*'), zeroOrMore(datasetClause()),
                optional(whereClause()), solutionModifier());
    }

    public Rule askQuery() {
        return sequence("ASK", zeroOrMore(datasetClause()), whereClause());
    }

    public Rule datasetClause() {
        return sequence("FROM", firstOf(defaultGraphClause(), namedGraphClause()));
    }

    public Rule defaultGraphClause() {
        return sourceSelector();
    }

    public Rule namedGraphClause() {
        return sequence("NAMED", sourceSelector());
    }

    public Rule sourceSelector() {
        return iriRef();
    }

    public Rule whereClause() {
        return sequence(optional("WHERE"), groupGraphPattern());
    }

    public Rule solutionModifier() {
        return sequence(optional(orderClause()), optional(limitOffsetClauses()));
    }

    public Rule limitOffsetClauses() {
        return firstOf(sequence(limitClause(), optional(offsetClause())),
                sequence(offsetClause(), optional(limitClause())));
    }

    public Rule orderClause() {
        return sequence("ORDER", "BY", oneOrMore(orderCondition()));
    }

    public Rule orderCondition() {
        return firstOf(
                sequence(firstOf("ASC", "DESC"), brackettedExpression()),
                firstOf(constraint(), var()));
    }

    public Rule limitClause() {
        return sequence("LIMIT", INTEGER());
    }

    public Rule offsetClause() {
        return sequence("OFFSET", INTEGER());
    }

    public Rule groupGraphPattern() {
        return sequence('{', optional(triplesBlock()),
                zeroOrMore(sequence(
                        firstOf(graphPatternNotTriples(), filter()),
                        optional('.'), optional(triplesBlock()))),
                '}');
    }

    @SuppressWarnings({"InfiniteRecursion"})
    public Rule triplesBlock() {
        return enforcedSequence(triplesSameSubject(), optional(sequence('.', optional(triplesBlock()))));
    }

    public Rule graphPatternNotTriples() {
        return firstOf(optionalGraphPattern(), groupOrUnionGraphPattern(), graphGraphPattern());
    }

    public Rule optionalGraphPattern() {
        return sequence("OPTIONAL", groupGraphPattern());
    }

    public Rule graphGraphPattern() {
        return sequence("GRAPH", varOrIRIref(), groupGraphPattern());
    }

    public Rule groupOrUnionGraphPattern() {
        return sequence(groupGraphPattern(), zeroOrMore(sequence("UNION", groupGraphPattern())));
    }

    public Rule filter() {
        return sequence("FILTER", constraint());
    }

    public Rule constraint() {
        return firstOf(brackettedExpression(), builtInCall(), functionCall());
    }

    public Rule functionCall() {
        return sequence(iriRef(), argList());
    }

    public Rule argList() {
        return firstOf(sequence('(', ')'), sequence('(', expression(), zeroOrMore(sequence(',', expression())), ')'));
    }

    public Rule constructTemplate() {
        return sequence('{', optional(constructTriples()), '}');
    }

    @SuppressWarnings({"InfiniteRecursion"})
    public Rule constructTriples() {
        return sequence(triplesSameSubject(), optional(sequence('.', optional(constructTriples()))));
    }

    public Rule triplesSameSubject() {
        return firstOf(sequence(varOrTerm(), propertyListNotEmpty()), sequence(triplesNode(), propertyList()));
    }

    public Rule propertyListNotEmpty() {
        return sequence(verb(), objectList(), zeroOrMore(sequence(';', optional(sequence(verb(), objectList())))));
    }

    public Rule propertyList() {
        return optional(propertyListNotEmpty());
    }

    public Rule objectList() {
        return sequence(object(), zeroOrMore(sequence(',', object())));
    }

    public Rule object() {
        return graphNode();
    }

    public Rule verb() {
        return firstOf(varOrIRIref(), 'a');
    }

    public Rule triplesNode() {
        return firstOf(collection(), blankNodePropertyList());
    }

    public Rule blankNodePropertyList() {
        return sequence('[', propertyListNotEmpty(), ']');
    }

    public Rule collection() {
        return sequence('(', oneOrMore(graphNode()), ')');
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
                booleanLiteral(), blankNode(), sequence('(', ')'));
    }

    public Rule expression() {
        return conditionalOrExpression();
    }

    public Rule conditionalOrExpression() {
        return sequence(conditionalAndExpression(), zeroOrMore(sequence("||", conditionalAndExpression())));
    }

    public Rule conditionalAndExpression() {
        return sequence(valueLogical(), zeroOrMore(sequence("&&", valueLogical())));
    }

    public Rule valueLogical() {
        return relationalExpression();
    }

    public Rule relationalExpression() {
        return sequence(numericExpression(), optional(firstOf(//
                sequence('=', numericExpression()), //
                sequence("!=", numericExpression()), //
                sequence('<', numericExpression()), //
                sequence('>', numericExpression()), //
                sequence("<=", numericExpression()), //
                sequence(">=", numericExpression()) //
        ) //
        ));
    }

    public Rule numericExpression() {
        return additiveExpression();
    }

    public Rule additiveExpression() {
        return sequence(multiplicativeExpression(), //
                zeroOrMore(firstOf(
                        sequence('+', multiplicativeExpression()), //
                        sequence('-', multiplicativeExpression()), //
                        numericLiteralPositive(), numericLiteralNegative()) //
                ));
    }

    public Rule multiplicativeExpression() {
        return sequence(unaryExpression(), zeroOrMore(firstOf(sequence(
                '*', unaryExpression()), sequence('/', unaryExpression()))));
    }

    public Rule unaryExpression() {
        return firstOf(sequence('!', primaryExpression()), sequence('+',
                primaryExpression()), sequence('-', primaryExpression()),
                primaryExpression());
    }

    public Rule primaryExpression() {
        return firstOf(brackettedExpression(), builtInCall(),
                iriRefOrFunction(), rdfLiteral(), numericLiteral(),
                booleanLiteral(), var());
    }

    public Rule brackettedExpression() {
        return sequence('(', expression(), ')');
    }

    public Rule builtInCall() {
        return firstOf(
                sequence("STR", '(', expression(), ')'),
                sequence("LANG", '(', expression(), ')'),
                sequence("LANGMATCHES", '(', expression(), ',',
                        expression(), ')'),
                sequence("DATATYPE", '(', expression(), ')'),
                sequence("BOUND", '(', var(), ')'),
                sequence("SAMETERM", '(', expression(), ',',
                        expression(), ')'),
                sequence("ISIRI", '(', expression(), ')'),
                sequence("ISURI", '(', expression(), ')'),
                sequence("ISBLANK", '(', expression(), ')'),
                sequence("ISLITERAL", '(', expression(), ')'),
                regexExpression());
    }

    public Rule regexExpression() {
        return sequence("REGEX", '(', expression(), ',', expression(), optional(sequence(',', expression())), ')');
    }

    public Rule iriRefOrFunction() {
        return sequence(iriRef(), optional(argList()));
    }

    public Rule rdfLiteral() {
        return sequence(string(), optional(firstOf(LANGTAG(), sequence("^^", iriRef()))));
    }

    public Rule numericLiteral() {
        return firstOf(numericLiteralUnsigned(), numericLiteralPositive(), numericLiteralNegative());
    }

    public Rule numericLiteralUnsigned() {
        return firstOf(DOUBLE(), DECIMAL(), INTEGER());
    }

    public Rule numericLiteralPositive() {
        return firstOf(DOUBLE_POSITIVE(), DECIMAL_POSITIVE(), INTEGER_POSITIVE());
    }

    public Rule numericLiteralNegative() {
        return firstOf(DOUBLE_NEGATIVE(), DECIMAL_NEGATIVE(), INTEGER_NEGATIVE());
    }

    public Rule booleanLiteral() {
        return firstOf("TRUE", "FALSE");
    }

    public Rule string() {
        return firstOf(STRING_LITERAL_LONG1(), STRING_LITERAL1(), STRING_LITERAL_LONG2(), STRING_LITERAL2());
    }

    public Rule iriRef() {
        return firstOf(IRI_REF(), prefixedName());
    }

    public Rule prefixedName() {
        return firstOf(PNAME_LN(), PNAME_NS());
    }

    public Rule blankNode() {
        return firstOf(BLANK_NODE_LABEL(), sequence('[', ']'));
    }

    public Rule WS() {
        return zeroOrMore(firstOf(COMMENT(), WS_NO_COMMENT()));
    }

    public Rule WS_NO_COMMENT() {
        return firstOf(ch(' '), ch('\t'), ch('\f'), EOL());
    }

    public Rule PNAME_NS() {
        return sequence(optional(PN_PREFIX()), ':');
    }

    public Rule PNAME_LN() {
        return sequence(PNAME_NS(), PN_LOCAL());
    }

    public Rule IRI_REF() {
        return sequence(
                LESS_NO_COMMENT(),
                zeroOrMore(sequence(
                        testNot(firstOf(LESS_NO_COMMENT(), ch('>'), ch('"'), ch('{'), ch('}'), ch('|'), ch('^'),
                                ch('\\'), ch('`'), charRange('\u0000', '\u0020'))),
                        any())),
                '>');

        // FIXME using oneOrMore (although incorrect in this case) reveals bug
        // in parboiled parser
        // -> an illegal state exception is thrown
        //
        // return sequence(LESS(), //
        // oneOrMore(sequence(testNot(firstOf(LESS(), GREATER(), '"',
        // OPEN_CURLY_BRACE(), CLOSE_CURLY_BRACE(), '|', '^',
        // '\\', '`', charRange('\u0000', '\u0020'))), any())), //
        // GREATER());
    }

    public Rule BLANK_NODE_LABEL() {
        return sequence(string("_:"), PN_LOCAL(), WS());
    }

    public Rule VAR1() {
        return sequence(ch('?'), VARNAME(), WS());
    }

    public Rule VAR2() {
        return sequence(ch('$'), VARNAME(), WS());
    }

    public Rule LANGTAG() {
        return sequence(ch('@'), oneOrMore(PN_CHARS_BASE()), zeroOrMore(sequence(
                '-', oneOrMore(sequence(PN_CHARS_BASE(), DIGIT())))), WS());
    }

    public Rule INTEGER() {
        return sequence(oneOrMore(DIGIT()), WS());
    }

    public Rule DECIMAL() {
        return sequence(firstOf(
                sequence(oneOrMore(DIGIT()), '.', zeroOrMore(DIGIT())),
                sequence('.', oneOrMore(DIGIT()))
        ), WS());
    }

    public Rule DOUBLE() {
        return sequence(firstOf(
                sequence(oneOrMore(DIGIT()), '.', zeroOrMore(DIGIT()), EXPONENT()),
                sequence('.', oneOrMore(DIGIT()), EXPONENT()),
                sequence(oneOrMore(DIGIT()), EXPONENT())), WS());
    }

    public Rule INTEGER_POSITIVE() {
        return sequence('+', INTEGER());
    }

    public Rule DECIMAL_POSITIVE() {
        return sequence('+', DECIMAL());
    }

    public Rule DOUBLE_POSITIVE() {
        return sequence('+', DOUBLE());
    }

    public Rule INTEGER_NEGATIVE() {
        return sequence('-', INTEGER());
    }

    public Rule DECIMAL_NEGATIVE() {
        return sequence('-', DECIMAL());
    }

    public Rule DOUBLE_NEGATIVE() {
        return sequence('-', DOUBLE());
    }

    public Rule EXPONENT() {
        return sequence(charIgnoreCase('e'), optional(firstOf('+', '-')), oneOrMore(DIGIT()));
    }

    public Rule STRING_LITERAL1() {
        return sequence(ch('\''), zeroOrMore(firstOf(sequence(testNot(
                firstOf(ch('\''), ch('\\'), ch('\n'), ch('\r'))), any()), ECHAR())), ch('\''), WS());
    }

    public Rule STRING_LITERAL2() {
        return sequence(ch('"'), zeroOrMore(firstOf(sequence(testNot(firstOf(ch('"'),
                ch('\\'), ch('\n'), ch('\r'))), any()), ECHAR())), ch('"'), WS());
    }

    public Rule STRING_LITERAL_LONG1() {
        return sequence(string("'''"), zeroOrMore(sequence(
                optional(firstOf(string("''"), ch('\''))), firstOf(sequence(testNot(firstOf(
                        ch('\''), ch('\\'))), any()), ECHAR()))), string("'''"), WS());
    }

    public Rule STRING_LITERAL_LONG2() {
        return sequence(string("\"\"\""), zeroOrMore(sequence(optional(firstOf(string("\"\""),
                ch('\"'))), firstOf(sequence(testNot(firstOf(ch('\"'), ch('\\'))), any()),
                ECHAR()))), string("\"\"\""), WS());
    }

    public Rule ECHAR() {
        return sequence(ch('\\'), firstOf(ch('t'), ch('b'), ch('n'), ch('r'), ch('f'), ch('\\'), ch('"'), ch('\'')));
    }

    public Rule PN_CHARS_U() {
        return firstOf(PN_CHARS_BASE(), ch('_'));
    }

    public Rule VARNAME() {
        return sequence(firstOf(PN_CHARS_U(), DIGIT()), zeroOrMore(firstOf(
                PN_CHARS_U(), DIGIT(), ch('\u00B7'), charRange('\u0300', '\u036F'),
                charRange('\u203F', '\u2040'))), WS());
    }

    public Rule PN_CHARS() {
        return firstOf('-', DIGIT(), PN_CHARS_U(), ch('\u00B7'),
                charRange('\u0300', '\u036F'), charRange('\u203F', '\u2040'));
    }

    public Rule PN_PREFIX() {
        return sequence(PN_CHARS_BASE(), optional(zeroOrMore(firstOf(
                PN_CHARS(), sequence('.', PN_CHARS())))));
    }

    public Rule PN_LOCAL() {
        return sequence(firstOf(PN_CHARS_U(), DIGIT()),
                optional(zeroOrMore(firstOf(PN_CHARS(), sequence('.', PN_CHARS())))), WS());
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
        return sequence(ch('#'), zeroOrMore(sequence(testNot(EOL()), any())), EOL());
    }

    public Rule EOL() {
        return firstOf(ch('\n'), ch('\r'));
    }

    public Rule LESS_NO_COMMENT() {
        return sequence(ch('<'), zeroOrMore(WS_NO_COMMENT()));
    }


    @Override
    protected Rule fromCharLiteral(char c) {
        return sequence(ch(c), WS());
    }

    @Override
    protected Rule fromStringLiteral(String string) {
        return sequence(stringIgnoreCase(string), WS());
    }

}