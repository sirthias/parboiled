//===========================================================================
//
//  Parsing Expression Grammar for Java 1.6 as a parboiled parser.
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
//    2010-02-01 Fixed problem in rule "FormalParameterDecls"
//    2010-03-29 Fixed problem in "annotation"
//    2010-03-31 Fixed problem in unicode escapes, String literals and line comments
//               (Thanks to Reinier Zwitserloot for the finds)
//    2010-07-26 Fixed problem in LocalVariableDeclarationStatement (accept annotations),
//               HexFloat (HexSignificant) and AnnotationTypeDeclaration (bug in the JLS!)
//
//===========================================================================

package org.parboiled.examples.java;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.annotations.DontLabel;
import org.parboiled.annotations.SuppressNode;
import org.parboiled.annotations.SuppressSubnodes;

@SuppressWarnings({"InfiniteRecursion"})
@BuildParseTree
public class JavaParser extends BaseParser<Object> {

    //-------------------------------------------------------------------------
    //  Compilation Unit
    //-------------------------------------------------------------------------

    public Rule CompilationUnit() {
        return Sequence(
                Spacing(),
                Optional(PackageDeclaration()),
                ZeroOrMore(ImportDeclaration()),
                ZeroOrMore(TypeDeclaration()),
                Eoi()
        );
    }

    public Rule PackageDeclaration() {
        return Sequence(ZeroOrMore(Annotation()), Sequence(PACKAGE, QualifiedIdentifier(), SEMI));
    }

    public Rule ImportDeclaration() {
        return Sequence(
                IMPORT,
                Optional(STATIC),
                QualifiedIdentifier(),
                Optional(Sequence(DOT, STAR)),
                SEMI
        );
    }

    public Rule TypeDeclaration() {
        return FirstOf(
                Sequence(
                        ZeroOrMore(Modifier()),
                        FirstOf(
                                ClassDeclaration(),
                                EnumDeclaration(),
                                InterfaceDeclaration(),
                                AnnotationTypeDeclaration()
                        )
                ),
                SEMI
        );
    }

    //-------------------------------------------------------------------------
    //  Class Declaration
    //-------------------------------------------------------------------------

    public Rule ClassDeclaration() {
        return Sequence(
                CLASS,
                Identifier(),
                Optional(TypeParameters()),
                Optional(Sequence(EXTENDS, ClassType())),
                Optional(Sequence(IMPLEMENTS, ClassTypeList())),
                ClassBody()
        );
    }

    public Rule ClassBody() {
        return Sequence(LWING, ZeroOrMore(ClassBodyDeclaration()), RWING);
    }

    public Rule ClassBodyDeclaration() {
        return FirstOf(
                SEMI,
                Sequence(Optional(STATIC), Block()),
                Sequence(ZeroOrMore(Modifier()), MemberDecl())
        );
    }

    public Rule MemberDecl() {
        return FirstOf(
                Sequence(TypeParameters(), GenericMethodOrConstructorRest()),
                Sequence(Type(), Identifier(), MethodDeclaratorRest()),
                Sequence(Type(), VariableDeclarators()),
                Sequence(VOID, Identifier(), VoidMethodDeclaratorRest()),
                Sequence(Identifier(), ConstructorDeclaratorRest()),
                InterfaceDeclaration(),
                ClassDeclaration(),
                EnumDeclaration(),
                AnnotationTypeDeclaration()
        );
    }

    public Rule GenericMethodOrConstructorRest() {
        return FirstOf(
                Sequence(FirstOf(Type(), VOID), Identifier(), MethodDeclaratorRest()),
                Sequence(Identifier(), ConstructorDeclaratorRest())
        );
    }

    public Rule MethodDeclaratorRest() {
        return Sequence(
                FormalParameters(),
                ZeroOrMore(Dim()),
                Optional(Sequence(THROWS, ClassTypeList())),
                FirstOf(MethodBody(), SEMI)
        );
    }

    public Rule VoidMethodDeclaratorRest() {
        return Sequence(
                FormalParameters(),
                Optional(Sequence(THROWS, ClassTypeList())),
                FirstOf(MethodBody(), SEMI)
        );
    }

    public Rule ConstructorDeclaratorRest() {
        return Sequence(FormalParameters(), Optional(Sequence(THROWS, ClassTypeList())), MethodBody());
    }

    public Rule MethodBody() {
        return Block();
    }

    //-------------------------------------------------------------------------
    //  Interface Declaration
    //-------------------------------------------------------------------------

    public Rule InterfaceDeclaration() {
        return Sequence(
                INTERFACE,
                Identifier(),
                Optional(TypeParameters()),
                Optional(Sequence(EXTENDS, ClassTypeList())),
                InterfaceBody()
        );
    }

    public Rule InterfaceBody() {
        return Sequence(LWING, ZeroOrMore(InterfaceBodyDeclaration()), RWING);
    }

    public Rule InterfaceBodyDeclaration() {
        return FirstOf(
                Sequence(ZeroOrMore(Modifier()), InterfaceMemberDecl()),
                SEMI
        );
    }

    public Rule InterfaceMemberDecl() {
        return FirstOf(
                InterfaceMethodOrFieldDecl(),
                InterfaceGenericMethodDecl(),
                Sequence(VOID, Identifier(), VoidInterfaceMethodDeclaratorsRest()),
                InterfaceDeclaration(),
                AnnotationTypeDeclaration(),
                ClassDeclaration(),
                EnumDeclaration()
        );
    }

    public Rule InterfaceMethodOrFieldDecl() {
        return Sequence(Sequence(Type(), Identifier()), InterfaceMethodOrFieldRest());
    }

    public Rule InterfaceMethodOrFieldRest() {
        return FirstOf(
                Sequence(ConstantDeclaratorsRest(), SEMI),
                InterfaceMethodDeclaratorRest()
        );
    }

    public Rule InterfaceMethodDeclaratorRest() {
        return Sequence(
                FormalParameters(),
                ZeroOrMore(Dim()),
                Optional(Sequence(THROWS, ClassTypeList())),
                SEMI
        );
    }

    public Rule InterfaceGenericMethodDecl() {
        return Sequence(TypeParameters(), FirstOf(Type(), VOID), Identifier(), InterfaceMethodDeclaratorRest());
    }

    public Rule VoidInterfaceMethodDeclaratorsRest() {
        return Sequence(FormalParameters(), Optional(Sequence(THROWS, ClassTypeList())), SEMI);
    }

    public Rule ConstantDeclaratorsRest() {
        return Sequence(ConstantDeclaratorRest(), ZeroOrMore(Sequence(COMMA, ConstantDeclarator())));
    }

    public Rule ConstantDeclarator() {
        return Sequence(Identifier(), ConstantDeclaratorRest());
    }

    public Rule ConstantDeclaratorRest() {
        return Sequence(ZeroOrMore(Dim()), EQU, VariableInitializer());
    }

    //-------------------------------------------------------------------------
    //  Enum Declaration
    //-------------------------------------------------------------------------

    public Rule EnumDeclaration() {
        return Sequence(
                ENUM,
                Identifier(),
                Optional(Sequence(IMPLEMENTS, ClassTypeList())),
                EnumBody()
        );
    }

    public Rule EnumBody() {
        return Sequence(
                LWING,
                Optional(EnumConstants()),
                Optional(COMMA),
                Optional(EnumBodyDeclarations()),
                RWING
        );
    }

    public Rule EnumConstants() {
        return Sequence(EnumConstant(), ZeroOrMore(Sequence(COMMA, EnumConstant())));
    }

    public Rule EnumConstant() {
        return Sequence(
                ZeroOrMore(Annotation()),
                Optional(TypeArguments()),
                Identifier(),
                Optional(Arguments()),
                Optional(ClassBody())
        );
    }

    public Rule EnumBodyDeclarations() {
        return Sequence(SEMI, ZeroOrMore(ClassBodyDeclaration()));
    }

    //-------------------------------------------------------------------------
    //  Variable Declarations
    //-------------------------------------------------------------------------    

    public Rule LocalVariableDeclarationStatement() {
        return Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), VariableDeclarators(), SEMI);
    }

    public Rule VariableDeclarators() {
        return Sequence(VariableDeclarator(), ZeroOrMore(Sequence(COMMA, VariableDeclarator())));
    }

    public Rule VariableDeclarator() {
        return Sequence(Identifier(), ZeroOrMore(Dim()), Optional(Sequence(EQU, VariableInitializer())));
    }

    //-------------------------------------------------------------------------
    //  Formal Parameters
    //-------------------------------------------------------------------------

    public Rule FormalParameters() {
        return Sequence(LPAR, Optional(FormalParameterDecls()), RPAR);
    }

    public Rule FormalParameter() {
        return Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), VariableDeclaratorId());
    }

    public Rule FormalParameterDecls() {
        return Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), FormalParameterDeclsRest());
    }

    public Rule FormalParameterDeclsRest() {
        return FirstOf(
                Sequence(VariableDeclaratorId(), Optional(Sequence(COMMA, FormalParameterDecls()))),
                Sequence(ELLIPSIS, VariableDeclaratorId())
        );
    }

    public Rule VariableDeclaratorId() {
        return Sequence(Identifier(), ZeroOrMore(Dim()));
    }

    //-------------------------------------------------------------------------
    //  Statements
    //-------------------------------------------------------------------------    

    public Rule Block() {
        return Sequence(LWING, BlockStatements(), RWING);
    }

    public Rule BlockStatements() {
        return ZeroOrMore(BlockStatement());
    }

    public Rule BlockStatement() {
        return FirstOf(
                LocalVariableDeclarationStatement(),
                Sequence(ZeroOrMore(Modifier()), FirstOf(ClassDeclaration(), EnumDeclaration())),
                Statement()
        );
    }

    public Rule Statement() {
        return FirstOf(
                Block(),
                Sequence(ASSERT, Expression(), Optional(Sequence(COLON, Expression())), SEMI),
                Sequence(IF, ParExpression(), Statement(), Optional(Sequence(ELSE, Statement()))),
                Sequence(FOR, LPAR, Optional(ForInit()), SEMI, Optional(Expression()), SEMI, Optional(ForUpdate()),
                        RPAR, Statement()),
                Sequence(FOR, LPAR, FormalParameter(), COLON, Expression(), RPAR, Statement()),
                Sequence(WHILE, ParExpression(), Statement()),
                Sequence(DO, Statement(), WHILE, ParExpression(), SEMI),
                Sequence(TRY, Block(),
                        FirstOf(Sequence(OneOrMore(Catch_()), Optional(Finally_())), Finally_())),
                Sequence(SWITCH, ParExpression(), LWING, SwitchBlockStatementGroups(), RWING),
                Sequence(SYNCHRONIZED, ParExpression(), Block()),
                Sequence(RETURN, Optional(Expression()), SEMI),
                Sequence(THROW, Expression(), SEMI),
                Sequence(BREAK, Optional(Identifier()), SEMI),
                Sequence(CONTINUE, Optional(Identifier()), SEMI),
                Sequence(Sequence(Identifier(), COLON), Statement()),
                Sequence(StatementExpression(), SEMI),
                SEMI
        );
    }

    public Rule Catch_() {
        return Sequence(CATCH, LPAR, FormalParameter(), RPAR, Block());
    }

    public Rule Finally_() {
        return Sequence(FINALLY, Block());
    }

    public Rule SwitchBlockStatementGroups() {
        return ZeroOrMore(SwitchBlockStatementGroup());
    }

    public Rule SwitchBlockStatementGroup() {
        return Sequence(SwitchLabel(), BlockStatements());
    }

    public Rule SwitchLabel() {
        return FirstOf(
                Sequence(CASE, ConstantExpression(), COLON),
                Sequence(CASE, EnumConstantName(), COLON),
                Sequence(DEFAULT, COLON)
        );
    }

    public Rule ForInit() {
        return FirstOf(
                Sequence(ZeroOrMore(FirstOf(FINAL, Annotation())), Type(), VariableDeclarators()),
                Sequence(StatementExpression(), ZeroOrMore(Sequence(COMMA, StatementExpression())))
        );
    }

    public Rule ForUpdate() {
        return Sequence(StatementExpression(), ZeroOrMore(Sequence(COMMA, StatementExpression())));
    }

    public Rule EnumConstantName() {
        return Identifier();
    }

    //-------------------------------------------------------------------------
    //  Expressions
    //-------------------------------------------------------------------------

    // The following is more generous than the definition in section 14.8,
    // which allows only specific forms of Expression.

    public Rule StatementExpression() {
        return Expression();
    }

    public Rule ConstantExpression() {
        return Expression();
    }

    // The following definition is part of the modification in JLS Chapter 18
    // to minimize look ahead. In JLS Chapter 15.27, Expression is defined
    // as AssignmentExpression, which is effectively defined as
    // (LeftHandSide AssignmentOperator)* ConditionalExpression.
    // The following is obtained by allowing ANY ConditionalExpression
    // as LeftHandSide, which results in accepting statements like 5 = a.

    public Rule Expression() {
        return Sequence(
                ConditionalExpression(),
                ZeroOrMore(Sequence(AssignmentOperator(), ConditionalExpression()))
        );
    }

    public Rule AssignmentOperator() {
        return FirstOf(EQU, PLUSEQU, MINUSEQU, STAREQU, DIVEQU, ANDEQU, OREQU, HATEQU, MODEQU, SLEQU, SREQU, BSREQU);
    }

    public Rule ConditionalExpression() {
        return Sequence(
                ConditionalOrExpression(),
                ZeroOrMore(Sequence(QUERY, Expression(), COLON, ConditionalOrExpression()))
        );
    }

    public Rule ConditionalOrExpression() {
        return Sequence(
                ConditionalAndExpression(),
                ZeroOrMore(Sequence(OROR, ConditionalAndExpression()))
        );
    }

    public Rule ConditionalAndExpression() {
        return Sequence(
                InclusiveOrExpression(),
                ZeroOrMore(Sequence(ANDAND, InclusiveOrExpression()))
        );
    }

    public Rule InclusiveOrExpression() {
        return Sequence(
                ExclusiveOrExpression(),
                ZeroOrMore(Sequence(OR, ExclusiveOrExpression()))
        );
    }

    public Rule ExclusiveOrExpression() {
        return Sequence(
                AndExpression(),
                ZeroOrMore(Sequence(HAT, AndExpression()))
        );
    }

    public Rule AndExpression() {
        return Sequence(
                EqualityExpression(),
                ZeroOrMore(Sequence(AND, EqualityExpression()))
        );
    }

    public Rule EqualityExpression() {
        return Sequence(
                RelationalExpression(),
                ZeroOrMore(Sequence(FirstOf(EQUAL, NOTEQUAL), RelationalExpression()))
        );
    }

    public Rule RelationalExpression() {
        return Sequence(
                ShiftExpression(),
                ZeroOrMore(
                        FirstOf(
                                Sequence(FirstOf(LE, GE, LT, GT), ShiftExpression()),
                                Sequence(INSTANCEOF, ReferenceType())
                        )
                )
        );
    }

    public Rule ShiftExpression() {
        return Sequence(
                AdditiveExpression(),
                ZeroOrMore(Sequence(FirstOf(SL, SR, BSR), AdditiveExpression()))
        );
    }

    public Rule AdditiveExpression() {
        return Sequence(
                MultiplicativeExpression(),
                ZeroOrMore(Sequence(FirstOf(PLUS, MINUS), MultiplicativeExpression()))
        );
    }

    public Rule MultiplicativeExpression() {
        return Sequence(
                UnaryExpression(),
                ZeroOrMore(Sequence(FirstOf(STAR, DIV, MOD), UnaryExpression()))
        );
    }

    public Rule UnaryExpression() {
        return FirstOf(
                Sequence(PrefixOp(), UnaryExpression()),
                Sequence(LPAR, Type(), RPAR, UnaryExpression()),
                Sequence(Primary(), ZeroOrMore(Selector()), ZeroOrMore(PostFixOp()))
        );
    }

    public Rule Primary() {
        return FirstOf(
                ParExpression(),
                Sequence(
                        NonWildcardTypeArguments(),
                        FirstOf(ExplicitGenericInvocationSuffix(), Sequence(THIS, Arguments()))
                ),
                Sequence(THIS, Optional(Arguments())),
                Sequence(SUPER, SuperSuffix()),
                Literal(),
                Sequence(NEW, Creator()),
                Sequence(QualifiedIdentifier(), Optional(IdentifierSuffix())),
                Sequence(BasicType(), ZeroOrMore(Dim()), DOT, CLASS),
                Sequence(VOID, DOT, CLASS)
        );
    }

    public Rule IdentifierSuffix() {
        return FirstOf(
                Sequence(LBRK,
                        FirstOf(
                                Sequence(RBRK, ZeroOrMore(Dim()), DOT, CLASS),
                                Sequence(Expression(), RBRK)
                        )
                ),
                Arguments(),
                Sequence(
                        DOT,
                        FirstOf(
                                CLASS,
                                ExplicitGenericInvocation(),
                                THIS,
                                Sequence(SUPER, Arguments()),
                                Sequence(NEW, Optional(NonWildcardTypeArguments()), InnerCreator())
                        )
                )
        );
    }

    public Rule ExplicitGenericInvocation() {
        return Sequence(NonWildcardTypeArguments(), ExplicitGenericInvocationSuffix());
    }

    public Rule NonWildcardTypeArguments() {
        return Sequence(LPOINT, ReferenceType(), ZeroOrMore(Sequence(COMMA, ReferenceType())), RPOINT);
    }

    public Rule ExplicitGenericInvocationSuffix() {
        return FirstOf(
                Sequence(SUPER, SuperSuffix()),
                Sequence(Identifier(), Arguments())
        );
    }

    public Rule PrefixOp() {
        return FirstOf(INC, DEC, BANG, TILDA, PLUS, MINUS);
    }

    public Rule PostFixOp() {
        return FirstOf(INC, DEC);
    }

    public Rule Selector() {
        return FirstOf(
                Sequence(DOT, Identifier(), Optional(Arguments())),
                Sequence(DOT, ExplicitGenericInvocation()),
                Sequence(DOT, THIS),
                Sequence(DOT, SUPER, SuperSuffix()),
                Sequence(DOT, NEW, Optional(NonWildcardTypeArguments()), InnerCreator()),
                DimExpr()
        );
    }

    public Rule SuperSuffix() {
        return FirstOf(Arguments(), Sequence(DOT, Identifier(), Optional(Arguments())));
    }

    public Rule BasicType() {
        return Sequence(
                FirstOf("byte", "short", "char", "int", "long", "float", "double", "boolean"),
                TestNot(LetterOrDigit()),
                Spacing()
        );
    }

    public Rule Arguments() {
        return Sequence(
                LPAR,
                Optional(Sequence(Expression(), ZeroOrMore(Sequence(COMMA, Expression())))),
                RPAR
        );
    }

    public Rule Creator() {
        return FirstOf(
                Sequence(Optional(NonWildcardTypeArguments()), CreatedName(), ClassCreatorRest()),
                Sequence(Optional(NonWildcardTypeArguments()), FirstOf(ClassType(), BasicType()), ArrayCreatorRest())
        );
    }

    public Rule CreatedName() {
        return Sequence(
                Identifier(), Optional(NonWildcardTypeArguments()),
                ZeroOrMore(Sequence(DOT, Identifier(), Optional(NonWildcardTypeArguments())))
        );
    }

    public Rule InnerCreator() {
        return Sequence(Identifier(), ClassCreatorRest());
    }

    // The following is more generous than JLS 15.10. According to that definition,
    // BasicType must be followed by at least one DimExpr or by ArrayInitializer.
    public Rule ArrayCreatorRest() {
        return Sequence(
                LBRK,
                FirstOf(
                        Sequence(RBRK, ZeroOrMore(Dim()), ArrayInitializer()),
                        Sequence(Expression(), RBRK, ZeroOrMore(DimExpr()), ZeroOrMore(Dim()))
                )
        );
    }

    public Rule ClassCreatorRest() {
        return Sequence(Arguments(), Optional(ClassBody()));
    }

    public Rule ArrayInitializer() {
        return Sequence(
                LWING,
                Optional(
                        Sequence(
                                VariableInitializer(),
                                ZeroOrMore(Sequence(COMMA, VariableInitializer())),
                                Optional(COMMA)
                        )
                ),
                RWING
        );
    }

    public Rule VariableInitializer() {
        return FirstOf(ArrayInitializer(), Expression());
    }

    public Rule ParExpression() {
        return Sequence(LPAR, Expression(), RPAR);
    }

    public Rule QualifiedIdentifier() {
        return Sequence(Identifier(), ZeroOrMore(Sequence(DOT, Identifier())));
    }

    public Rule Dim() {
        return Sequence(LBRK, RBRK);
    }

    public Rule DimExpr() {
        return Sequence(LBRK, Expression(), RBRK);
    }

    //-------------------------------------------------------------------------
    //  Types and Modifiers
    //-------------------------------------------------------------------------

    public Rule Type() {
        return Sequence(FirstOf(BasicType(), ClassType()), ZeroOrMore(Dim()));
    }

    public Rule ReferenceType() {
        return FirstOf(
                Sequence(BasicType(), OneOrMore(Dim())),
                Sequence(ClassType(), ZeroOrMore(Dim()))
        );
    }

    public Rule ClassType() {
        return Sequence(
                Identifier(), Optional(TypeArguments()),
                ZeroOrMore(Sequence(DOT, Identifier(), Optional(TypeArguments())))
        );
    }

    public Rule ClassTypeList() {
        return Sequence(ClassType(), ZeroOrMore(Sequence(COMMA, ClassType())));
    }

    public Rule TypeArguments() {
        return Sequence(LPOINT, TypeArgument(), ZeroOrMore(Sequence(COMMA, TypeArgument())), RPOINT);
    }

    public Rule TypeArgument() {
        return FirstOf(
                ReferenceType(),
                Sequence(QUERY, Optional(Sequence(FirstOf(EXTENDS, SUPER), ReferenceType())))
        );
    }

    public Rule TypeParameters() {
        return Sequence(LPOINT, TypeParameter(), ZeroOrMore(Sequence(COMMA, TypeParameter())), RPOINT);
    }

    public Rule TypeParameter() {
        return Sequence(Identifier(), Optional(Sequence(EXTENDS, Bound())));
    }

    public Rule Bound() {
        return Sequence(ClassType(), ZeroOrMore(Sequence(AND, ClassType())));
    }

    // the following common definition of Modifier is part of the modification
    // in JLS Chapter 18 to minimize look ahead. The main body of JLS has
    // different lists of modifiers for different language elements.
    public Rule Modifier() {
        return FirstOf(
                Annotation(),
                Sequence(
                        FirstOf("public", "protected", "private", "static", "abstract", "final", "native",
                                "synchronized", "transient", "volatile", "strictfp"),
                        TestNot(LetterOrDigit()),
                        Spacing()
                )
        );
    }

    //-------------------------------------------------------------------------
    //  Annotations
    //-------------------------------------------------------------------------    

    public Rule AnnotationTypeDeclaration() {
        return Sequence(AT, INTERFACE, Identifier(), AnnotationTypeBody());
    }

    public Rule AnnotationTypeBody() {
        return Sequence(LWING, ZeroOrMore(AnnotationTypeElementDeclaration()), RWING);
    }

    public Rule AnnotationTypeElementDeclaration() {
        return Sequence(ZeroOrMore(Modifier()), AnnotationTypeElementRest());
    }

    public Rule AnnotationTypeElementRest() {
        return FirstOf(
                Sequence(Type(), AnnotationMethodOrConstantRest(), SEMI),
                ClassDeclaration(),
                EnumDeclaration(),
                InterfaceDeclaration(),
                AnnotationTypeDeclaration()
        );
    }

    public Rule AnnotationMethodOrConstantRest() {
        return FirstOf(AnnotationMethodRest(), AnnotationConstantRest());
    }

    public Rule AnnotationMethodRest() {
        return Sequence(Identifier(), LPAR, RPAR, Optional(DefaultValue()));
    }

    public Rule AnnotationConstantRest() {
        return VariableDeclarators();
    }

    public Rule DefaultValue() {
        return Sequence(DEFAULT, ElementValue());
    }

    public Rule Annotation() {
        return Sequence(AT, QualifiedIdentifier(), Optional(AnnotationRest()));
    }

    public Rule AnnotationRest() {
        return FirstOf(NormalAnnotationRest(), SingleElementAnnotationRest());
    }

    public Rule NormalAnnotationRest() {
        return Sequence(LPAR, Optional(ElementValuePairs()), RPAR);
    }

    public Rule ElementValuePairs() {
        return Sequence(ElementValuePair(), ZeroOrMore(Sequence(COMMA, ElementValuePair())));
    }

    public Rule ElementValuePair() {
        return Sequence(Identifier(), EQU, ElementValue());
    }

    public Rule ElementValue() {
        return FirstOf(ConditionalExpression(), Annotation(), ElementValueArrayInitializer());
    }

    public Rule ElementValueArrayInitializer() {
        return Sequence(LWING, Optional(ElementValues()), Optional(COMMA), RWING);
    }

    public Rule ElementValues() {
        return Sequence(ElementValue(), ZeroOrMore(Sequence(COMMA, ElementValue())));
    }

    public Rule SingleElementAnnotationRest() {
        return Sequence(LPAR, ElementValue(), RPAR);
    }

    //-------------------------------------------------------------------------
    //  JLS 3.6-7  Spacing
    //-------------------------------------------------------------------------

    @SuppressNode
    public Rule Spacing() {
        return ZeroOrMore(FirstOf(

                // whitespace
                OneOrMore(CharSet(" \t\r\n\f")),

                // traditional comment
                Sequence("/*", ZeroOrMore(Sequence(TestNot("*/"), Any())), "*/"),

                // end of line comment
                Sequence(
                        "//",
                        ZeroOrMore(Sequence(TestNot(CharSet("\r\n")), Any())),
                        FirstOf("\r\n", '\r', '\n', Eoi())
                )
        ));
    }

    //-------------------------------------------------------------------------
    //  JLS 3.8  Identifiers
    //-------------------------------------------------------------------------

    @SuppressSubnodes
    public Rule Identifier() {
        return Sequence(TestNot(Keyword()), Letter(), ZeroOrMore(LetterOrDigit()), Spacing());
    }

    // The following are traditional definitions of letters and digits.
    // JLS defines letters and digits as Unicode characters recognized
    // as such by special Java procedures, which is difficult
    // to express in terms of Parsing Expressions.

    public Rule Letter() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), '_', '$');
    }

    public Rule LetterOrDigit() {
        return FirstOf(CharRange('a', 'z'), CharRange('A', 'Z'), CharRange('0', '9'), '_', '$');
    }

    //-------------------------------------------------------------------------
    //  JLS 3.9  Keywords
    //-------------------------------------------------------------------------

    public Rule Keyword() {
        return Sequence(
                FirstOf("assert", "break", "case", "catch", "class", "continue", "default", "do", "else", "enum",
                        "extends", "finally", "final", "for", "if", "implements", "import", "interface", "instanceof",
                        "new", "package", "return", "static", "super", "switch", "synchronized", "this", "throws",
                        "throw", "try", "void", "while"),
                TestNot(LetterOrDigit())
        );
    }

    public final Rule ASSERT = Keyword("assert");
    public final Rule BREAK = Keyword("break");
    public final Rule CASE = Keyword("case");
    public final Rule CATCH = Keyword("catch");
    public final Rule CLASS = Keyword("class");
    public final Rule CONTINUE = Keyword("continue");
    public final Rule DEFAULT = Keyword("default");
    public final Rule DO = Keyword("do");
    public final Rule ELSE = Keyword("else");
    public final Rule ENUM = Keyword("enum");
    public final Rule EXTENDS = Keyword("extends");
    public final Rule FINALLY = Keyword("finally");
    public final Rule FINAL = Keyword("final");
    public final Rule FOR = Keyword("for");
    public final Rule IF = Keyword("if");
    public final Rule IMPLEMENTS = Keyword("implements");
    public final Rule IMPORT = Keyword("import");
    public final Rule INTERFACE = Keyword("interface");
    public final Rule INSTANCEOF = Keyword("instanceof");
    public final Rule NEW = Keyword("new");
    public final Rule PACKAGE = Keyword("package");
    public final Rule RETURN = Keyword("return");
    public final Rule STATIC = Keyword("static");
    public final Rule SUPER = Keyword("super");
    public final Rule SWITCH = Keyword("switch");
    public final Rule SYNCHRONIZED = Keyword("synchronized");
    public final Rule THIS = Keyword("this");
    public final Rule THROWS = Keyword("throws");
    public final Rule THROW = Keyword("throw");
    public final Rule TRY = Keyword("try");
    public final Rule VOID = Keyword("void");
    public final Rule WHILE = Keyword("while");

    @SuppressNode
    @DontLabel
    public Rule Keyword(String keyword) {
        return Terminal(keyword, LetterOrDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.10  Literals
    //-------------------------------------------------------------------------

    public Rule Literal() {
        return Sequence(
                FirstOf(
                        FloatLiteral(),
                        IntegerLiteral(),
                        CharLiteral(),
                        StringLiteral(),
                        Sequence("true", TestNot(LetterOrDigit())),
                        Sequence("false", TestNot(LetterOrDigit())),
                        Sequence("null", TestNot(LetterOrDigit()))
                ),
                Spacing()
        );
    }

    @SuppressSubnodes
    public Rule IntegerLiteral() {
        return Sequence(FirstOf(HexNumeral(), OctalNumeral(), DecimalNumeral()), Optional(CharSet("lL")));
    }

    @SuppressSubnodes
    public Rule DecimalNumeral() {
        return FirstOf('0', Sequence(CharRange('1', '9'), ZeroOrMore(Digit())));
    }

    @SuppressSubnodes
    public Rule HexNumeral() {
        return Sequence(FirstOf("0x", "0X"), OneOrMore(HexDigit()));
    }

    public Rule HexDigit() {
        return FirstOf(CharRange('a', 'f'), CharRange('A', 'F'), CharRange('0', '9'));
    }

    @SuppressSubnodes
    public Rule OctalNumeral() {
        return Sequence('0', OneOrMore(CharRange('0', '7')));
    }

    public Rule FloatLiteral() {
        return FirstOf(HexFloat(), DecimalFloat());
    }

    @SuppressSubnodes
    public Rule DecimalFloat() {
        return FirstOf(
                Sequence(OneOrMore(Digit()), '.', ZeroOrMore(Digit()), Optional(Exponent()), Optional(CharSet("fFdD"))),
                Sequence('.', OneOrMore(Digit()), Optional(Exponent()), Optional(CharSet("fFdD"))),
                Sequence(OneOrMore(Digit()), Exponent(), Optional(CharSet("fFdD"))),
                Sequence(OneOrMore(Digit()), Optional(Exponent()), CharSet("fFdD"))
        );
    }

    public Rule Exponent() {
        return Sequence(CharSet("eE"), Optional(CharSet("+-")), OneOrMore(Digit()));
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    @SuppressSubnodes
    public Rule HexFloat() {
        return Sequence(HexSignificant(), BinaryExponent(), Optional(CharSet("fFdD")));
    }

    public Rule HexSignificant() {
        return FirstOf(
                Sequence(FirstOf("0x", "0X"), ZeroOrMore(HexDigit()), '.', OneOrMore(HexDigit())),
                Sequence(HexNumeral(), Optional('.'))
        );
    }

    public Rule BinaryExponent() {
        return Sequence(CharSet("pP"), Optional(CharSet("+-")), OneOrMore(Digit()));
    }

    public Rule CharLiteral() {
        return Sequence(
                '\'',
                FirstOf(Escape(), Sequence(TestNot(CharSet("'\\")), Any())).suppressSubnodes(),
                '\''
        );
    }

    public Rule StringLiteral() {
        return Sequence(
                '"',
                ZeroOrMore(
                        FirstOf(
                                Escape(),
                                Sequence(TestNot(CharSet("\r\n\"\\")), Any())
                        )
                ).suppressSubnodes(),
                '"'
        );
    }

    public Rule Escape() {
        return Sequence('\\', FirstOf(CharSet("btnfr\"\'\\"), OctalEscape(), UnicodeEscape()));
    }

    public Rule OctalEscape() {
        return FirstOf(
                Sequence(CharRange('0', '3'), CharRange('0', '7'), CharRange('0', '7')),
                Sequence(CharRange('0', '7'), CharRange('0', '7')),
                CharRange('0', '7')
        );
    }

    public Rule UnicodeEscape() {
        return Sequence(OneOrMore('u'), HexDigit(), HexDigit(), HexDigit(), HexDigit());
    }

    //-------------------------------------------------------------------------
    //  JLS 3.11-12  Separators, Operators
    //-------------------------------------------------------------------------

    public final Rule AT = Terminal("@");
    public final Rule AND = Terminal("&", CharSet("=&"));
    public final Rule ANDAND = Terminal("&&");
    public final Rule ANDEQU = Terminal("&=");
    public final Rule BANG = Terminal("!", Ch('='));
    public final Rule BSR = Terminal(">>>", Ch('='));
    public final Rule BSREQU = Terminal(">>>=");
    public final Rule COLON = Terminal(":");
    public final Rule COMMA = Terminal(",");
    public final Rule DEC = Terminal("--");
    public final Rule DIV = Terminal("/", Ch('='));
    public final Rule DIVEQU = Terminal("/=");
    public final Rule DOT = Terminal(".");
    public final Rule ELLIPSIS = Terminal("...");
    public final Rule EQU = Terminal("=", Ch('='));
    public final Rule EQUAL = Terminal("==");
    public final Rule GE = Terminal(">=");
    public final Rule GT = Terminal(">", CharSet("=>"));
    public final Rule HAT = Terminal("^", Ch('='));
    public final Rule HATEQU = Terminal("^=");
    public final Rule INC = Terminal("++");
    public final Rule LBRK = Terminal("[");
    public final Rule LE = Terminal("<=");
    public final Rule LPAR = Terminal("(");
    public final Rule LPOINT = Terminal("<");
    public final Rule LT = Terminal("<", CharSet("=<"));
    public final Rule LWING = Terminal("{");
    public final Rule MINUS = Terminal("-", CharSet("=-"));
    public final Rule MINUSEQU = Terminal("-=");
    public final Rule MOD = Terminal("%", Ch('='));
    public final Rule MODEQU = Terminal("%=");
    public final Rule NOTEQUAL = Terminal("!=");
    public final Rule OR = Terminal("|", CharSet("=|"));
    public final Rule OREQU = Terminal("|=");
    public final Rule OROR = Terminal("||");
    public final Rule PLUS = Terminal("+", CharSet("=+"));
    public final Rule PLUSEQU = Terminal("+=");
    public final Rule QUERY = Terminal("?");
    public final Rule RBRK = Terminal("]");
    public final Rule RPAR = Terminal(")");
    public final Rule RPOINT = Terminal(">");
    public final Rule RWING = Terminal("}");
    public final Rule SEMI = Terminal(";");
    public final Rule SL = Terminal("<<", Ch('='));
    public final Rule SLEQU = Terminal("<<=");
    public final Rule SR = Terminal(">>", CharSet("=>"));
    public final Rule SREQU = Terminal(">>=");
    public final Rule STAR = Terminal("*", Ch('='));
    public final Rule STAREQU = Terminal("*=");
    public final Rule TILDA = Terminal("~");

    //-------------------------------------------------------------------------
    //  helper methods
    //-------------------------------------------------------------------------

    @Override
    protected Rule FromCharLiteral(char c) {
        // turn of creation of parse tree nodes for single characters
        return super.FromCharLiteral(c).suppressNode();
    }

    @SuppressNode
    @DontLabel
    public Rule Terminal(String string) {
        return Sequence(string, Spacing()).label('\'' + string + '\'');
    }

    @SuppressNode
    @DontLabel
    public Rule Terminal(String string, Rule mustNotFollow) {
        return Sequence(string, TestNot(mustNotFollow), Spacing()).label('\'' + string + '\'');
    }

}
