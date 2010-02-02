//===========================================================================
//
//  Parsing Expression Grammar for Java 1.5 as a parboiled parser.
//  Based on Chapters 3 and 18 of Java Language Specification, Third Edition (JLS)
//  at http://java.sun.com/docs/books/jls/third_edition/html/j3TOC.html.
//
//---------------------------------------------------------------------------
//
//  Copyright (C) 2010 by Mathias Doenitz
//  Based on a Mouse 1.1 grammar for Java 1.5, which is
//  Copyright (C) 2006,2009 by Roman R Redziejowski (www.romanredz.se).
//
//  The author gives unlimited permission to copy and distribute
//  this file, with or without modifications, as long as this notice
//  is preserved, and any changes are properly documented.
//
//  This file is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//---------------------------------------------------------------------------
//
//  Change log
//    2006-12-06 Posted on Internet.
//    2009-04-04 Modified to conform to Mouse syntax:
//               Underscore removed from names
//               \f in Space replaced by Unicode for FormFeed.
//    2009-07-10 Unused rule THREADSAFE removed.
//    2009-07-10 Copying and distribution conditions relaxed by the author.
//    2010-01-28 Transcribed to parboiled
//    2010-02-01 Fixed problem in rule "formalParameterDecls"
//
//===========================================================================

package org.parboiled.examples.java;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Leaf;

@SuppressWarnings({"InfiniteRecursion"})
public class JavaParser extends BaseParser<Object> {

    //-------------------------------------------------------------------------
    //  Compilation Unit
    //-------------------------------------------------------------------------

    public Rule compilationUnit() {
        return sequence(
                optional(spacing()),
                optional(packageDeclaration()),
                zeroOrMore(importDeclaration()),
                zeroOrMore(typeDeclaration()),
                eoi()
        );
    }

    public Rule packageDeclaration() {
        return sequence(zeroOrMore(annotation()), enforcedSequence(PACKAGE, qualifiedIdentifier(), SEMI));
    }

    public Rule importDeclaration() {
        return enforcedSequence(
                IMPORT,
                optional(STATIC),
                qualifiedIdentifier(),
                optional(enforcedSequence(DOT, STAR)),
                SEMI
        );
    }

    public Rule typeDeclaration() {
        return firstOf(
                sequence(
                        zeroOrMore(modifier()),
                        firstOf(
                                classDeclaration(),
                                enumDeclaration(),
                                interfaceDeclaration(),
                                annotationTypeDeclaration()
                        )
                ),
                SEMI
        );
    }

    //-------------------------------------------------------------------------
    //  Class Declaration
    //-------------------------------------------------------------------------

    public Rule classDeclaration() {
        return enforcedSequence(
                CLASS,
                identifier(),
                optional(typeParameters()),
                optional(enforcedSequence(EXTENDS, classType())),
                optional(enforcedSequence(IMPLEMENTS, classTypeList())),
                classBody()
        );
    }

    public Rule classBody() {
        return sequence(LWING, zeroOrMore(classBodyDeclaration()), RWING);
    }

    public Rule classBodyDeclaration() {
        return firstOf(
                SEMI,
                sequence(optional(STATIC), block()),
                sequence(zeroOrMore(modifier()), memberDecl())
        );
    }

    public Rule memberDecl() {
        return firstOf(
                enforcedSequence(typeParameters(), genericMethodOrConstructorRest()),
                sequence(type(), identifier(), methodDeclaratorRest()),
                sequence(type(), variableDeclarators()),
                enforcedSequence(VOID, identifier(), voidMethodDeclaratorRest()),
                enforcedSequence(identifier(), constructorDeclaratorRest()),
                interfaceDeclaration(),
                classDeclaration(),
                enumDeclaration(),
                annotationTypeDeclaration()
        );
    }

    public Rule genericMethodOrConstructorRest() {
        return firstOf(
                sequence(firstOf(type(), VOID), identifier(), methodDeclaratorRest()),
                sequence(identifier(), constructorDeclaratorRest())
        );
    }

    public Rule methodDeclaratorRest() {
        return sequence(
                formalParameters(),
                zeroOrMore(dim()),
                optional(enforcedSequence(THROWS, classTypeList())),
                firstOf(methodBody(), SEMI)
        );
    }

    public Rule voidMethodDeclaratorRest() {
        return sequence(
                formalParameters(),
                optional(enforcedSequence(THROWS, classTypeList())),
                firstOf(methodBody(), SEMI)
        );
    }

    public Rule constructorDeclaratorRest() {
        return sequence(formalParameters(), optional(enforcedSequence(THROWS, classTypeList())), methodBody());
    }

    public Rule methodBody() {
        return block();
    }

    //-------------------------------------------------------------------------
    //  Interface Declaration
    //-------------------------------------------------------------------------

    public Rule interfaceDeclaration() {
        return enforcedSequence(
                INTERFACE,
                identifier(),
                optional(typeParameters()),
                optional(enforcedSequence(EXTENDS, classTypeList())),
                interfaceBody()
        );
    }

    public Rule interfaceBody() {
        return sequence(LWING, zeroOrMore(interfaceBodyDeclaration()), RWING);
    }

    public Rule interfaceBodyDeclaration() {
        return firstOf(
                sequence(zeroOrMore(modifier()), interfaceMemberDecl()),
                SEMI
        );
    }

    public Rule interfaceMemberDecl() {
        return firstOf(
                interfaceMethodOrFieldDecl(),
                interfaceGenericMethodDecl(),
                sequence(VOID, identifier(), voidInterfaceMethodDeclaratorsRest()),
                interfaceDeclaration(),
                annotationTypeDeclaration(),
                classDeclaration(),
                enumDeclaration()
        );
    }

    public Rule interfaceMethodOrFieldDecl() {
        return sequence(sequence(type(), identifier()), interfaceMethodOrFieldRest());
    }

    public Rule interfaceMethodOrFieldRest() {
        return firstOf(
                sequence(constantDeclaratorsRest(), SEMI),
                interfaceMethodDeclaratorRest()
        );
    }

    public Rule interfaceMethodDeclaratorRest() {
        return sequence(
                formalParameters(),
                zeroOrMore(dim()),
                optional(enforcedSequence(THROWS, classTypeList())),
                SEMI
        );
    }

    public Rule interfaceGenericMethodDecl() {
        return sequence(typeParameters(), firstOf(type(), VOID), identifier(), interfaceMethodDeclaratorRest());
    }

    public Rule voidInterfaceMethodDeclaratorsRest() {
        return sequence(formalParameters(), optional(enforcedSequence(THROWS, classTypeList())), SEMI);
    }

    public Rule constantDeclaratorsRest() {
        return sequence(constantDeclaratorRest(), zeroOrMore(enforcedSequence(COMMA, constantDeclarator())));
    }

    public Rule constantDeclarator() {
        return sequence(identifier(), constantDeclaratorRest());
    }

    public Rule constantDeclaratorRest() {
        return sequence(zeroOrMore(dim()), EQU, variableInitializer());
    }

    //-------------------------------------------------------------------------
    //  Enum Declaration
    //-------------------------------------------------------------------------

    public Rule enumDeclaration() {
        return enforcedSequence(
                ENUM,
                identifier(),
                optional(enforcedSequence(IMPLEMENTS, classTypeList())),
                enumBody()
        );
    }

    public Rule enumBody() {
        return sequence(
                LWING,
                optional(enumConstants()),
                optional(COMMA),
                optional(enumBodyDeclarations()),
                RWING
        );
    }

    public Rule enumConstants() {
        return sequence(enumConstant(), zeroOrMore(enforcedSequence(COMMA, enumConstant())));
    }

    public Rule enumConstant() {
        return sequence(
                zeroOrMore(annotation()),
                optional(typeArguments()),
                identifier(),
                optional(arguments()),
                optional(classBody())
        );
    }

    public Rule enumBodyDeclarations() {
        return sequence(SEMI, zeroOrMore(classBodyDeclaration()));
    }

    //-------------------------------------------------------------------------
    //  Variable Declarations
    //-------------------------------------------------------------------------    

    public Rule localVariableDeclarationStatement() {
        return sequence(optional(FINAL), type(), variableDeclarators(), SEMI);
    }

    public Rule variableDeclarators() {
        return sequence(variableDeclarator(), zeroOrMore(enforcedSequence(COMMA, variableDeclarator())));
    }

    public Rule variableDeclarator() {
        return sequence(identifier(), zeroOrMore(dim()), optional(enforcedSequence(EQU, variableInitializer())));
    }

    //-------------------------------------------------------------------------
    //  Formal Parameters
    //-------------------------------------------------------------------------

    public Rule formalParameters() {
        return sequence(LPAR, optional(formalParameterDecls()), RPAR);
    }

    public Rule formalParameter() {
        return sequence(zeroOrMore(firstOf(FINAL, annotation())), type(), variableDeclaratorId());
    }

    public Rule formalParameterDecls() {
        return sequence(zeroOrMore(firstOf(FINAL, annotation())), type(), formalParameterDeclsRest());
    }

    public Rule formalParameterDeclsRest() {
        return firstOf(
                sequence(variableDeclaratorId(), optional(enforcedSequence(COMMA, formalParameterDecls()))),
                enforcedSequence(ELLIPSIS, variableDeclaratorId())
        );
    }

    public Rule variableDeclaratorId() {
        return sequence(identifier(), zeroOrMore(dim()));
    }

    //-------------------------------------------------------------------------
    //  Statements
    //-------------------------------------------------------------------------    

    public Rule block() {
        return enforcedSequence(LWING, blockStatements(), RWING);
    }

    public Rule blockStatements() {
        return zeroOrMore(blockStatement());
    }

    public Rule blockStatement() {
        return firstOf(
                localVariableDeclarationStatement(),
                sequence(zeroOrMore(modifier()), firstOf(classDeclaration(), enumDeclaration())),
                statement()
        );
    }

    public Rule statement() {
        return firstOf(
                block(),
                enforcedSequence(ASSERT, expression(), optional(sequence(COLON, expression())), SEMI),
                enforcedSequence(IF, parExpression(), statement(), optional(sequence(ELSE, statement()))),
                enforcedSequence(sequence(FOR, LPAR, optional(forInit()), SEMI),
                        optional(expression()), SEMI, optional(forUpdate()), RPAR, statement()),
                enforcedSequence(FOR, LPAR, formalParameter(), COLON, expression(), RPAR, statement()),
                enforcedSequence(WHILE, parExpression(), statement()),
                enforcedSequence(DO, statement(), WHILE, parExpression(), SEMI),
                enforcedSequence(TRY, block(),
                        firstOf(sequence(oneOrMore(catch_()), optional(finally_())), finally_())),
                enforcedSequence(SWITCH, parExpression(), LWING, switchBlockStatementGroups(), RWING),
                enforcedSequence(SYNCHRONIZED, parExpression(), block()),
                enforcedSequence(RETURN, optional(expression()), SEMI),
                enforcedSequence(THROW, expression(), SEMI),
                enforcedSequence(BREAK, optional(identifier()), SEMI),
                enforcedSequence(CONTINUE, optional(identifier()), SEMI),
                SEMI,
                sequence(statementExpression(), SEMI),
                enforcedSequence(sequence(identifier(), COLON), statement())
        );
    }

    public Rule catch_() {
        return enforcedSequence(CATCH, LPAR, formalParameter(), RPAR, block());
    }

    public Rule finally_() {
        return enforcedSequence(FINALLY, block());
    }

    public Rule switchBlockStatementGroups() {
        return zeroOrMore(switchBlockStatementGroup());
    }

    public Rule switchBlockStatementGroup() {
        return sequence(switchLabel(), blockStatements());
    }

    public Rule switchLabel() {
        return firstOf(
                enforcedSequence(CASE, constantExpression(), COLON),
                enforcedSequence(CASE, enumConstantName(), COLON),
                enforcedSequence(DEFAULT, COLON)
        );
    }

    public Rule forInit() {
        return firstOf(
                sequence(zeroOrMore(firstOf(FINAL, annotation())), type(), variableDeclarators()),
                sequence(statementExpression(), zeroOrMore(enforcedSequence(COMMA, statementExpression())))
        );
    }

    public Rule forUpdate() {
        return sequence(statementExpression(), zeroOrMore(enforcedSequence(COMMA, statementExpression())));
    }

    public Rule enumConstantName() {
        return identifier();
    }

    //-------------------------------------------------------------------------
    //  Expressions
    //-------------------------------------------------------------------------

    // The following is more generous than the definition in section 14.8,
    // which allows only specific forms of Expression.

    public Rule statementExpression() {
        return expression();
    }

    public Rule constantExpression() {
        return expression();
    }

    // The following definition is part of the modification in JLS Chapter 18
    // to minimize look ahead. In JLS Chapter 15.27, Expression is defined
    // as AssignmentExpression, which is effectively defined as
    // (LeftHandSide AssignmentOperator)* ConditionalExpression.
    // The above is obtained by allowing ANY ConditionalExpression
    // as LeftHandSide, which results in accepting statements like 5 = a.

    public Rule expression() {
        return sequence(
                conditionalExpression(),
                zeroOrMore(sequence(assignmentOperator(), conditionalExpression()))
        );
    }

    public Rule assignmentOperator() {
        return firstOf(EQU, PLUSEQU, MINUSEQU, STAREQU, DIVEQU, ANDEQU, OREQU, HATEQU, MODEQU, SLEQU, SREQU, BSREQU);
    }

    public Rule conditionalExpression() {
        return sequence(
                conditionalOrExpression(),
                zeroOrMore(enforcedSequence(QUERY, expression(), COLON, conditionalOrExpression()))
        );
    }

    public Rule conditionalOrExpression() {
        return sequence(
                conditionalAndExpression(),
                zeroOrMore(enforcedSequence(OROR, conditionalAndExpression()))
        );
    }

    public Rule conditionalAndExpression() {
        return sequence(
                inclusiveOrExpression(),
                zeroOrMore(enforcedSequence(ANDAND, inclusiveOrExpression()))
        );
    }

    public Rule inclusiveOrExpression() {
        return sequence(
                exclusiveOrExpression(),
                zeroOrMore(enforcedSequence(OR, exclusiveOrExpression()))
        );
    }

    public Rule exclusiveOrExpression() {
        return sequence(
                andExpression(),
                zeroOrMore(enforcedSequence(HAT, andExpression()))
        );
    }

    public Rule andExpression() {
        return sequence(
                equalityExpression(),
                zeroOrMore(enforcedSequence(AND, equalityExpression()))
        );
    }

    public Rule equalityExpression() {
        return sequence(
                relationalExpression(),
                zeroOrMore(enforcedSequence(firstOf(EQUAL, NOTEQUAL), relationalExpression()))
        );
    }

    public Rule relationalExpression() {
        return sequence(
                shiftExpression(),
                zeroOrMore(
                        firstOf(
                                enforcedSequence(firstOf(LE, GE, LT, GT), shiftExpression()),
                                enforcedSequence(INSTANCEOF, referenceType())
                        )
                )
        );
    }

    public Rule shiftExpression() {
        return sequence(
                additiveExpression(),
                zeroOrMore(enforcedSequence(firstOf(SL, SR, BSR), additiveExpression()))
        );
    }

    public Rule additiveExpression() {
        return sequence(
                multiplicativeExpression(),
                zeroOrMore(enforcedSequence(firstOf(PLUS, MINUS), multiplicativeExpression()))
        );
    }

    public Rule multiplicativeExpression() {
        return sequence(
                unaryExpression(),
                zeroOrMore(enforcedSequence(firstOf(STAR, DIV, MOD), unaryExpression()))
        );
    }

    public Rule unaryExpression() {
        return firstOf(
                sequence(prefixOp(), unaryExpression()),
                sequence(LPAR, type(), RPAR, unaryExpression()),
                sequence(primary(), zeroOrMore(selector()), zeroOrMore(postFixOp()))
        );
    }

    public Rule primary() {
        return firstOf(
                parExpression(),
                enforcedSequence(
                        nonWildcardTypeArguments(),
                        firstOf(explicitGenericInvocationSuffix(), sequence(THIS, arguments()))
                ),
                sequence(THIS, optional(arguments())),
                enforcedSequence(SUPER, superSuffix()),
                literal(),
                enforcedSequence(NEW, creator()),
                sequence(qualifiedIdentifier(), optional(identifierSuffix())),
                enforcedSequence(basicType(), zeroOrMore(dim()), DOT, CLASS),
                enforcedSequence(VOID, DOT, CLASS)
        );
    }

    public Rule identifierSuffix() {
        return firstOf(
                sequence(LBRK,
                        firstOf(
                                sequence(RBRK, zeroOrMore(dim()), DOT, CLASS),
                                sequence(expression(), RBRK)
                        )
                ),
                arguments(),
                sequence(
                        DOT,
                        firstOf(
                                CLASS,
                                explicitGenericInvocation(),
                                THIS,
                                sequence(SUPER, arguments()),
                                sequence(NEW, optional(nonWildcardTypeArguments()), innerCreator())
                        )
                )
        );
    }

    public Rule explicitGenericInvocation() {
        return enforcedSequence(nonWildcardTypeArguments(), explicitGenericInvocationSuffix());
    }

    public Rule nonWildcardTypeArguments() {
        return sequence(LPOINT, referenceType(), zeroOrMore(enforcedSequence(COMMA, referenceType())), RPOINT);
    }

    public Rule explicitGenericInvocationSuffix() {
        return firstOf(
                enforcedSequence(SUPER, superSuffix()),
                sequence(identifier(), arguments())
        );
    }

    public Rule prefixOp() {
        return firstOf(INC, DEC, BANG, TILDA, PLUS, MINUS);
    }

    public Rule postFixOp() {
        return firstOf(INC, DEC);
    }

    public Rule selector() {
        return firstOf(
                sequence(DOT, identifier(), optional(arguments())),
                sequence(DOT, explicitGenericInvocation()),
                sequence(DOT, THIS),
                sequence(DOT, SUPER, superSuffix()),
                sequence(DOT, NEW, optional(nonWildcardTypeArguments()), innerCreator()),
                dimExpr()
        );
    }

    public Rule superSuffix() {
        return firstOf(arguments(), sequence(DOT, identifier(), optional(arguments())));
    }

    public Rule basicType() {
        return sequence(
                firstOf("byte", "short", "char", "int", "long", "float", "double", "boolean"),
                testNot(letterOrDigit()),
                optional(spacing())
        );
    }

    public Rule arguments() {
        return sequence(
                LPAR,
                optional(sequence(expression(), zeroOrMore(enforcedSequence(COMMA, expression())))),
                RPAR
        );
    }

    public Rule creator() {
        return firstOf(
                sequence(optional(nonWildcardTypeArguments()), createdName(), classCreatorRest()),
                sequence(optional(nonWildcardTypeArguments()), firstOf(classType(), basicType()), arrayCreatorRest())
        );
    }

    public Rule createdName() {
        return sequence(
                identifier(), optional(nonWildcardTypeArguments()),
                zeroOrMore(sequence(DOT, identifier(), optional(nonWildcardTypeArguments())))
        );
    }

    public Rule innerCreator() {
        return sequence(identifier(), classCreatorRest());
    }

    // The following is more generous than JLS 15.10. According to that definition,
    // BasicType must be followed by at least one DimExpr or by ArrayInitializer.
    public Rule arrayCreatorRest() {
        return sequence(
                LBRK,
                firstOf(
                        sequence(RBRK, zeroOrMore(dim()), arrayInitializer()),
                        sequence(expression(), RBRK, zeroOrMore(dimExpr()), zeroOrMore(dim()))
                )
        );
    }

    public Rule classCreatorRest() {
        return sequence(arguments(), optional(classBody()));
    }

    public Rule arrayInitializer() {
        return sequence(
                LWING,
                optional(
                        sequence(
                                variableInitializer(),
                                zeroOrMore(sequence(COMMA, variableInitializer())),
                                optional(COMMA)
                        )
                ),
                RWING
        );
    }

    public Rule variableInitializer() {
        return firstOf(arrayInitializer(), expression());
    }

    public Rule parExpression() {
        return sequence(LPAR, expression(), RPAR);
    }

    public Rule qualifiedIdentifier() {
        return sequence(identifier(), zeroOrMore(sequence(DOT, identifier())));
    }

    public Rule dim() {
        return sequence(LBRK, RBRK);
    }

    public Rule dimExpr() {
        return sequence(LBRK, expression(), RBRK);
    }

    //-------------------------------------------------------------------------
    //  Types and Modifiers
    //-------------------------------------------------------------------------

    public Rule type() {
        return sequence(firstOf(basicType(), classType()), zeroOrMore(dim()));
    }

    public Rule referenceType() {
        return firstOf(
                sequence(basicType(), oneOrMore(dim())),
                sequence(classType(), zeroOrMore(dim()))
        );
    }

    public Rule classType() {
        return sequence(
                identifier(), optional(typeArguments()),
                zeroOrMore(sequence(DOT, identifier(), optional(typeArguments())))
        );
    }

    public Rule classTypeList() {
        return sequence(classType(), zeroOrMore(enforcedSequence(COMMA, classType())));
    }

    public Rule typeArguments() {
        return sequence(LPOINT, typeArgument(), zeroOrMore(enforcedSequence(COMMA, typeArgument())), RPOINT);
    }

    public Rule typeArgument() {
        return firstOf(
                referenceType(),
                enforcedSequence(QUERY, optional(enforcedSequence(firstOf(EXTENDS, SUPER), referenceType())))
        );
    }

    public Rule typeParameters() {
        return sequence(LPOINT, typeParameter(), zeroOrMore(enforcedSequence(COMMA, typeParameter())), RPOINT);
    }

    public Rule typeParameter() {
        return sequence(identifier(), optional(enforcedSequence(EXTENDS, bound())));
    }

    public Rule bound() {
        return sequence(classType(), zeroOrMore(enforcedSequence(AND, classType())));
    }

    // the following common definition of Modifier is part of the modification
    // in JLS Chapter 18 to minimize look ahead. The main body of JLS has
    // different lists of modifiers for different language elements.
    public Rule modifier() {
        return firstOf(
                annotation(),
                sequence(
                        firstOf("public", "protected", "private", "static", "abstract", "final", "native",
                                "synchronized", "transient", "volatile", "strictfp"),
                        testNot(letterOrDigit()),
                        optional(spacing())
                )
        );
    }

    //-------------------------------------------------------------------------
    //  Annotations
    //-------------------------------------------------------------------------    

    public Rule annotationTypeDeclaration() {
        return enforcedSequence(sequence(AT, INTERFACE), identifier(), annotationTypeBody());
    }

    public Rule annotationTypeBody() {
        return sequence(LWING, zeroOrMore(annotationTypeElementDeclaration()), RWING);
    }

    public Rule annotationTypeElementDeclaration() {
        return sequence(zeroOrMore(modifier()), annotationTypeElementRest());
    }

    public Rule annotationTypeElementRest() {
        return firstOf(
                sequence(type(), identifier(), annotationMethodOrConstantRest(), SEMI),
                classDeclaration(),
                enumDeclaration(),
                interfaceDeclaration(),
                annotationTypeDeclaration()
        );
    }

    public Rule annotationMethodOrConstantRest() {
        return firstOf(annotationMethodRest(), annotationConstantRest());
    }

    public Rule annotationMethodRest() {
        return sequence(LPAR, RPAR, optional(defaultValue()));
    }

    public Rule annotationConstantRest() {
        return variableDeclarators();
    }

    public Rule defaultValue() {
        return enforcedSequence(DEFAULT, elementValue());
    }

    public Rule annotation() {
        return sequence(
                AT,
                qualifiedIdentifier(),
                optional(
                        sequence(
                                LPAR,
                                optional(sequence(identifier(), EQU)),
                                elementValue(),
                                RPAR
                        )
                )
        );
    }

    public Rule elementValue() {
        return firstOf(conditionalExpression(), annotation(), elementValueArrayInitializer());
    }

    public Rule elementValueArrayInitializer() {
        return sequence(LWING, optional(elementValues()), optional(COMMA), RWING);
    }

    public Rule elementValues() {
        return sequence(elementValue(), zeroOrMore(enforcedSequence(COMMA, elementValue())));
    }

    //-------------------------------------------------------------------------
    //  JLS 3.6-7  Spacing
    //-------------------------------------------------------------------------

    @Leaf
    public Rule spacing() {
        return zeroOrMore(firstOf(

                // whitespace
                oneOrMore(charSet(" \t\r\n\u000c")),

                // traditional comment
                sequence("/*", zeroOrMore(sequence(testNot("*/"), any())), "*/"),

                // end of line comment
                sequence("//", zeroOrMore(sequence(testNot(charSet("\r\n")), any())), charSet("\r\n"))
        ));
    }

    //-------------------------------------------------------------------------
    //  JLS 3.8  Identifiers
    //-------------------------------------------------------------------------

    @Leaf
    public Rule identifier() {
        return sequence(testNot(keyword()), letter(), zeroOrMore(letterOrDigit()), optional(spacing()));
    }

    // The following are traditional definitions of letters and digits.
    // JLS defines letters and digits as Unicode characters recognized
    // as such by special Java procedures, which is difficult
    // to express in terms of Parsing Expressions.

    public Rule letter() {
        return firstOf(charRange('a', 'z'), charRange('A', 'Z'), '_', '$');
    }

    public Rule letterOrDigit() {
        return firstOf(charRange('a', 'z'), charRange('A', 'Z'), charRange('0', '9'), '_', '$');
    }

    //-------------------------------------------------------------------------
    //  JLS 3.9  Keywords
    //-------------------------------------------------------------------------

    public Rule keyword() {
        return sequence(
                firstOf("assert", "break", "case", "catch", "class", "continue", "default", "do", "else", "enum",
                        "extends", "finally", "final", "for", "if", "implements", "import", "interface", "instanceof",
                        "new", "package", "return", "static", "super", "switch", "synchronized", "this", "throws",
                        "throw", "try", "void", "while"),
                testNot(letterOrDigit())
        );
    }

    public final Rule ASSERT = keyword("assert");
    public final Rule BREAK = keyword("break");
    public final Rule CASE = keyword("case");
    public final Rule CATCH = keyword("catch");
    public final Rule CLASS = keyword("class");
    public final Rule CONTINUE = keyword("continue");
    public final Rule DEFAULT = keyword("default");
    public final Rule DO = keyword("do");
    public final Rule ELSE = keyword("else");
    public final Rule ENUM = keyword("enum");
    public final Rule EXTENDS = keyword("extends");
    public final Rule FINALLY = keyword("finally");
    public final Rule FINAL = keyword("final");
    public final Rule FOR = keyword("for");
    public final Rule IF = keyword("if");
    public final Rule IMPLEMENTS = keyword("implements");
    public final Rule IMPORT = keyword("import");
    public final Rule INTERFACE = keyword("interface");
    public final Rule INSTANCEOF = keyword("instanceof");
    public final Rule NEW = keyword("new");
    public final Rule PACKAGE = keyword("package");
    public final Rule RETURN = keyword("return");
    public final Rule STATIC = keyword("static");
    public final Rule SUPER = keyword("super");
    public final Rule SWITCH = keyword("switch");
    public final Rule SYNCHRONIZED = keyword("synchronized");
    public final Rule THIS = keyword("this");
    public final Rule THROWS = keyword("throws");
    public final Rule THROW = keyword("throw");
    public final Rule TRY = keyword("try");
    public final Rule VOID = keyword("void");
    public final Rule WHILE = keyword("while");

    @Leaf
    public Rule keyword(String keyword) {
        return terminal(keyword, letterOrDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.10  Literals
    //-------------------------------------------------------------------------

    public Rule literal() {
        return sequence(
                firstOf(
                        floatLiteral(),
                        integerLiteral(),
                        charLiteral(),
                        stringLiteral(),
                        sequence("true", testNot(letterOrDigit())),
                        sequence("false", testNot(letterOrDigit())),
                        sequence("null", testNot(letterOrDigit()))
                ),
                optional(spacing())
        );
    }

    public Rule integerLiteral() {
        return sequence(firstOf(hexNumeral(), octalNumeral(), decimalNumeral()), optional(charSet("lL")));
    }

    @Leaf
    public Rule decimalNumeral() {
        return firstOf('0', sequence(charRange('1', '9'), zeroOrMore(digit())));
    }

    @Leaf
    public Rule hexNumeral() {
        return sequence(firstOf("0x", "0X"), oneOrMore(hexDigit()));
    }

    public Rule hexDigit() {
        return firstOf(charRange('a', 'f'), charRange('A', 'F'), charRange('0', '9'));
    }

    @Leaf
    public Rule octalNumeral() {
        return sequence('0', oneOrMore(charRange('0', '7')));
    }

    public Rule floatLiteral() {
        return firstOf(hexFloat(), decimalFloat());
    }

    @Leaf
    public Rule decimalFloat() {
        return firstOf(
                sequence(oneOrMore(digit()), '.', zeroOrMore(digit()), optional(exponent()), optional(charSet("fFdD"))),
                sequence('.', oneOrMore(digit()), optional(exponent()), optional(charSet("fFdD"))),
                sequence(oneOrMore(digit()), exponent(), optional(charSet("fFdD"))),
                sequence(oneOrMore(digit()), optional(exponent()), charSet("fFdD"))
        );
    }

    public Rule exponent() {
        return sequence(charSet("eE"), optional(charSet("+-")), oneOrMore(digit()));
    }

    public Rule digit() {
        return charRange('0', '9');
    }

    @Leaf
    public Rule hexFloat() {
        return sequence(hexSignificant(), binaryExponent(), optional(charSet("fFdD")));
    }

    public Rule hexSignificant() {
        return firstOf(
                sequence(hexNumeral(), optional('.')),
                sequence(firstOf("0x", "0X"), zeroOrMore(hexDigit()), '.', oneOrMore(hexDigit()))
        );
    }

    public Rule binaryExponent() {
        return sequence(charSet("pP"), optional(charSet("+-")), oneOrMore(digit()));
    }

    @Leaf
    public Rule charLiteral() {
        return sequence('\'', firstOf(escape(), sequence(testNot(charSet("'\\")), any())), '\'');
    }

    @Leaf
    public Rule stringLiteral() {
        return sequence(
                '"',
                zeroOrMore(
                        firstOf(
                                escape(),
                                sequence(testNot(charSet("\"\\")), any())
                        )
                ),
                '"'
        );
    }

    @Leaf
    public Rule escape() {
        return sequence('\\', firstOf('b', 't', 'n', 'f', 'r', '"', '\'', '\\', octalEscape(), unicodeEscape()));
    }

    public Rule octalEscape() {
        return firstOf(
                sequence(charRange('0', '3'), charRange('0', '7'), charRange('0', '7')),
                sequence(charRange('0', '7'), charRange('0', '7')),
                charRange('0', '7')
        );
    }

    public Rule unicodeEscape() {
        return enforcedSequence('u', hexDigit(), hexDigit(), hexDigit(), hexDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.11-12  Separators, Operators
    //-------------------------------------------------------------------------

    public final Rule AT = terminal("@");
    public final Rule AND = terminal("&", charSet("=&"));
    public final Rule ANDAND = terminal("&&");
    public final Rule ANDEQU = terminal("&=");
    public final Rule BANG = terminal("!", ch('='));
    public final Rule BSR = terminal(">>>", ch('='));
    public final Rule BSREQU = terminal(">>>=");
    public final Rule COLON = terminal(":");
    public final Rule COMMA = terminal(",");
    public final Rule DEC = terminal("--");
    public final Rule DIV = terminal("/", ch('='));
    public final Rule DIVEQU = terminal("/=");
    public final Rule DOT = terminal(".");
    public final Rule ELLIPSIS = terminal("...");
    public final Rule EQU = terminal("=", ch('='));
    public final Rule EQUAL = terminal("==");
    public final Rule GE = terminal(">=");
    public final Rule GT = terminal(">", charSet("=>"));
    public final Rule HAT = terminal("^", ch('='));
    public final Rule HATEQU = terminal("^=");
    public final Rule INC = terminal("++");
    public final Rule LBRK = terminal("[");
    public final Rule LE = terminal("<=");
    public final Rule LPAR = terminal("(");
    public final Rule LPOINT = terminal("<");
    public final Rule LT = terminal("<", charSet("=<"));
    public final Rule LWING = terminal("{");
    public final Rule MINUS = terminal("-", charSet("=-"));
    public final Rule MINUSEQU = terminal("-=");
    public final Rule MOD = terminal("%", ch('='));
    public final Rule MODEQU = terminal("%=");
    public final Rule NOTEQUAL = terminal("!=");
    public final Rule OR = terminal("|", charSet("=|"));
    public final Rule OREQU = terminal("|=");
    public final Rule OROR = terminal("||");
    public final Rule PLUS = terminal("+", charSet("=+"));
    public final Rule PLUSEQU = terminal("+=");
    public final Rule QUERY = terminal("?");
    public final Rule RBRK = terminal("]");
    public final Rule RPAR = terminal(")");
    public final Rule RPOINT = terminal(">");
    public final Rule RWING = terminal("}");
    public final Rule SEMI = terminal(";");
    public final Rule SL = terminal("<<", ch('='));
    public final Rule SLEQU = terminal("<<=");
    public final Rule SR = terminal(">>", charSet("=>"));
    public final Rule SREQU = terminal(">>=");
    public final Rule STAR = terminal("*", ch('='));
    public final Rule STAREQU = terminal("*=");
    public final Rule TILDA = terminal("~");

    //-------------------------------------------------------------------------
    //  helper methods
    //-------------------------------------------------------------------------

    @Leaf
    public Rule terminal(String string) {
        return sequence(string, optional(spacing())).label(string);
    }

    @Leaf
    public Rule terminal(String string, Rule mustNotFollow) {
        return sequence(string, testNot(mustNotFollow), optional(spacing())).label(string);
    }

}
