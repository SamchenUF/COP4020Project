/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.AND;
import static edu.ufl.cise.cop4020fa23.Kind.BANG;
import static edu.ufl.cise.cop4020fa23.Kind.BITAND;
import static edu.ufl.cise.cop4020fa23.Kind.BITOR;
import static edu.ufl.cise.cop4020fa23.Kind.COLON;
import static edu.ufl.cise.cop4020fa23.Kind.COMMA;
import static edu.ufl.cise.cop4020fa23.Kind.DIV;
import static edu.ufl.cise.cop4020fa23.Kind.EOF;
import static edu.ufl.cise.cop4020fa23.Kind.EQ;
import static edu.ufl.cise.cop4020fa23.Kind.EXP;
import static edu.ufl.cise.cop4020fa23.Kind.GE;
import static edu.ufl.cise.cop4020fa23.Kind.GT;
import static edu.ufl.cise.cop4020fa23.Kind.IDENT;
import static edu.ufl.cise.cop4020fa23.Kind.LE;
import static edu.ufl.cise.cop4020fa23.Kind.LPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.LSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.LT;
import static edu.ufl.cise.cop4020fa23.Kind.MINUS;
import static edu.ufl.cise.cop4020fa23.Kind.MOD;
import static edu.ufl.cise.cop4020fa23.Kind.NUM_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.OR;
import static edu.ufl.cise.cop4020fa23.Kind.PLUS;
import static edu.ufl.cise.cop4020fa23.Kind.QUESTION;
import static edu.ufl.cise.cop4020fa23.Kind.RARROW;
import static edu.ufl.cise.cop4020fa23.Kind.RES_blue;
import static edu.ufl.cise.cop4020fa23.Kind.RES_green;
import static edu.ufl.cise.cop4020fa23.Kind.RES_height;
import static edu.ufl.cise.cop4020fa23.Kind.RES_red;
import static edu.ufl.cise.cop4020fa23.Kind.RES_width;
import static edu.ufl.cise.cop4020fa23.Kind.RPAREN;
import static edu.ufl.cise.cop4020fa23.Kind.RSQUARE;
import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static edu.ufl.cise.cop4020fa23.Kind.TIMES;
import static edu.ufl.cise.cop4020fa23.Kind.CONST;
import static edu.ufl.cise.cop4020fa23.Kind.BOOLEAN_LIT;

import java.util.Arrays;

import edu.ufl.cise.cop4020fa23.ast.AST;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.Expr;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;
/**
Expr::=  ConditionalExpr | LogicalOrExpr    
ConditionalExpr ::=  ?  Expr  :  Expr  :  Expr 
LogicalOrExpr ::= LogicalAndExpr (    (   |   |   ||   ) LogicalAndExpr)*
LogicalAndExpr ::=  ComparisonExpr ( (   &   |  &&   )  ComparisonExpr)*
ComparisonExpr ::= PowExpr ( (< | > | == | <= | >=) PowExpr)*
PowExpr ::= AdditiveExpr ** PowExpr |   AdditiveExpr
AdditiveExpr ::= MultiplicativeExpr ( ( + | -  ) MultiplicativeExpr )*
MultiplicativeExpr ::= UnaryExpr (( * |  /  |  % ) UnaryExpr)*
UnaryExpr ::=  ( ! | - | length | width) UnaryExpr  |  UnaryExprPostfix
UnaryExprPostfix::= PrimaryExpr (PixelSelector | ε ) (ChannelSelector | ε )
PrimaryExpr ::= STRING_LIT | NUM_LIT |  BOOLEAN_LIT | IDENT | ( Expr ) | CONST | ExpandedPixel  
ChannelSelector ::= : red | : green | : blue
PixelSelector  ::= [ Expr , Expr ]
ExpandedPixel ::= [ Expr , Expr , Expr ]
Dimension  ::=  [ Expr , Expr ]                         

 */



public class ExpressionParser implements IParser {
	
	final ILexer lexer;
	private IToken t;
	

	/**
	 * @param lexer
	 * @throws LexicalException 
	 */
	public ExpressionParser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}

	protected boolean match(Kind kind) {
		return t.kind() == kind;		
	}

	protected boolean match(Kind... kinds) {
		for (Kind k : kinds) {
			if (k == t.kind()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public AST parse() throws PLCCompilerException {
		Expr e = expr();
		return e;
	}


	private Expr expr() throws PLCCompilerException {
		IToken firstToken = t;
		postFix(firstToken);
		return primaryExpr(firstToken);
	}

	private void postFix(IToken firstToken) throws PLCCompilerException {
		primaryExpr(firstToken);
		t = lexer.next();
		if(!match(EOF)) {
			pixelSelect(t);
			t = lexer.next();
		}
		if (!match(EOF)) {
			channelSelector(t);
			t = lexer.next();
		}
	}

	private void pixelSelect(IToken firsToken) throws PLCCompilerException {
		if(match(LSQUARE)) {
			t = lexer.next();
			expr();
			match(RSQUARE);
			t = lexer.next();
		}
		else
			throw new SyntaxException("Not valid syntax");
	}

	private Expr primaryExpr(IToken firstToken) throws PLCCompilerException {
		if (match(STRING_LIT)) {
			t = lexer.next();
			return new StringLitExpr(firstToken);
		}
		else if (match(NUM_LIT)) {
			t = lexer.next();
			return new NumLitExpr(firstToken);
		}
		else if(match(BOOLEAN_LIT)) {
			t = lexer.next();
			return new BooleanLitExpr(firstToken);
		}
		else if (match(IDENT)) {
			t  = lexer.next();
			return new IdentExpr(firstToken);
		}
		else if (match(LPAREN)) {
			t = lexer.next();
			expr();
			match(RPAREN);
		}
		else if (match(CONST)) {
			t = lexer.next();
			return new ConstExpr(firstToken);
		}
		throw new SyntaxException("Not valid syntax");
	}

	private ChannelSelector channelSelector(IToken firstToken) throws PLCCompilerException { //Use this for postFix Expr
		t = lexer.next();
		if(match(RES_blue, RES_green, RES_red)) {
			return new ChannelSelector(firstToken, t);
		}
		throw new SyntaxException("Not valid syntax");
	}

	private Expr conditionalExpr() throws PLCCompilerException {
		IToken firstToken = t;
		if (match(QUESTION)) {
			t = lexer.next();
			Expr expr1 = expr();
			if (match(COLON)) {
				t = lexer.next();
				Expr expr2 = expr();
				if (match(COLON)) {
					t = lexer.next();
					Expr expr3 = expr();
					return new ConditionalExpr(firstToken, expr1, expr2, expr3);
				}
				throw new SyntaxException("Expected second colon in conditional expression");
			}
			throw new SyntaxException("Expected colon in conditional expression");
		}
		throw new SyntaxException("Incorrect start for ConditionalExpr");
	}

	private Expr logicalOrExpr() throws PLCCompilerException {
		Expr e0 = logicalAndExpr();
		while (match(BITOR, OR)) {
			IToken op = t;
			t = lexer.next();
			Expr e1 = logicalAndExpr();
			e0 = new BinaryExpr(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	private Expr logicalAndExpr() throws PLCCompilerException {
		Expr e0 = comparisonExpr();
		while (match(BITAND, AND)) {
			IToken op = t;
			t = lexer.next();
			Expr e1 = comparisonExpr();
			e0 = new BinaryExpr(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	private Expr comparisonExpr() throws PLCCompilerException {
		Expr e0 = powExpr();
		while (match(LT, GT, EQ, LE, GE)) {
			IToken op = t;
			t = lexer.next();
			Expr e1 = powExpr();
			e0 = new BinaryExpr(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	private Expr powExpr() throws PLCCompilerException {
		Expr e0 = additiveExpr();
		if (match(EXP)) {
			IToken op = t;
			t = lexer.next();
			Expr e1 = powExpr();
			e0 = new BinaryExpr(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	private Expr additiveExpr() throws PLCCompilerException {
		Expr e0 = multiplicativeExpr();
		while (match(PLUS, MINUS)) {
			IToken op = t;
			t = lexer.next();
			Expr e1 = multiplicativeExpr();
			e0 = new BinaryExpr(e0.firstToken(), e0, op, e1);
		}
		return e0;
	}

	private Expr multiplicativeExpr() throws PLCCompilerException {
		Expr e0 = unaryExpr();
		while (match(TIMES, DIV, MOD)) {
			IToken op = t;
			t = lexer.next();
			Expr e1 = unaryExpr();
			e0 = new BinaryExpr(e0.firstToken, e0, op, e1);
		}
		return e0;
	}

	private Expr unaryExpr() throws PLCCompilerException {
		// This is an initial skeleton; it needs additional cases for "length" and "width"
		if (match(BANG, MINUS)) {
			IToken op = t;
			t = lexer.next();
			Expr e = unaryExpr();
			return new UnaryExpr(e.firstToken, op, e);
		} else {
			return unaryExprPostfix();
		}
	}

	private Expr unaryExprPostfix() throws PLCCompilerException {
		Expr e0 = primaryExpr(t);
		// Check for PixelSelector or epsilon
		if (match(LSQUARE)) {
			e0 = pixelSelector(e0);
		}
		// Check for ChannelSelector or epsilon
		if (match(COLON)) {
			e0 = channelSelector(e0);
		}
		return e0;
	}

	private ChannelSelector channelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		if (match(COLON)) {
			t = lexer.next(); // consume the COLON
			if (match(RES_red)) {
				t = lexer.next(); // consume the red
				return new ChannelSelector(firstToken, t);
			} else if (match(RES_green)) {
				t = lexer.next(); // consume the green
				return new ChannelSelector(firstToken, t);
			} else if (match(RES_blue)) {
				t = lexer.next(); // consume the blue
				return new ChannelSelector(firstToken, t);
			} else {
				throw new SyntaxException(t.sourceLocation(), ": followed by an invalid color");
			}
		}
		throw new SyntaxException(t.sourceLocation(), "Expected a color selector but found " + t.kind());
	}


	private Expr pixelSelector(Expr e0) throws PLCCompilerException {
		IToken firstToken = t;
		if (match(LSQUARE)) {
			t = lexer.next();
			Expr e1 = expr();
			if (match(COMMA)) {
				t = lexer.next();
				Expr e2 = expr();
				if (match(RSQUARE)) {
					t = lexer.next();
					return new Expr(firstToken, e0, e1);

				}
				throw new SyntaxException("Expected closing square bracket for PixelSelector");
			}
			throw new SyntaxException("Expected comma in PixelSelector");
		}
		throw new SyntaxException("Incorrect start for PixelSelector");
	}

	private Expr expandedPixel() throws PLCCompilerException {
		IToken firstToken = t;
		if (match(LSQUARE)) {
			t = lexer.next();
			Expr red = expr();
			if (match(COMMA)) {
				t = lexer.next();
				Expr green = expr();
				if (match(COMMA)) {
					t = lexer.next();
					Expr blue = expr();
					if (match(RSQUARE)) {
						t = lexer.next();
						return new ExpandedPixelExpr(firstToken, red, green, blue);
					}
					throw new SyntaxException("Expected closing square bracket for ExpandedPixel");
				}
				throw new SyntaxException("Expected second comma in ExpandedPixel");
			}
			throw new SyntaxException("Expected first comma in ExpandedPixel");
		}
		throw new SyntaxException("Incorrect start for ExpandedPixel");
	}

}
