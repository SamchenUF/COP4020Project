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

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.exceptions.SyntaxException;

import static edu.ufl.cise.cop4020fa23.Kind.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.lang.model.element.Name;

public class Parser implements IParser {
	
	final ILexer lexer;
	private IToken t;

	public Parser(ILexer lexer) throws LexicalException {
		super();
		this.lexer = lexer;
		t = lexer.next();
	}

	protected boolean match(Kind kind) {
		return t.kind() == kind;
	}

	// Check if current token's kind matches any of the given kinds
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
		AST e = program();
		t = lexer.next();
		if(match(EOF)) {
			return e;
		}
		throw new PLCCompilerException("Not EOF");
	}

	private AST program() throws PLCCompilerException {
		IToken firstToken = t;
		List<NameDef> e = new ArrayList<NameDef>();
		IToken type = type();
		t = lexer.next();
		if(match(IDENT)) {
			IToken name = t;
			t = lexer.next(); //consume ident
			if(match(LPAREN)) {
				t = lexer.next(); //comsume lparen
				if(!match(RPAREN)) {
					e = paramList();
				}
				if(match(RPAREN)) {
					t = lexer.next(); //consume rparen
					Block f = block();
					if(lexer.next().kind() == (EOF)) {
						return new Program(firstToken, type, name, e, f);
					}
					throw new SyntaxException("Not EOF");
				}
				throw new SyntaxException("No closing paren");
			}
			throw new SyntaxException("No opening paren");
		}
		throw new SyntaxException("No ident");
	}

	private Block block() throws PLCCompilerException {
		List<BlockElem> l1 = new ArrayList<>();
		IToken firstToken = t;

		if (!match(Kind.BLOCK_OPEN)) {
			throw new SyntaxException("Expected '<:' at the start of block");
		}
		t = lexer.next();  // Consume the '<:' token
		while (match(RES_write, RES_do, RES_if, RETURN, IDENT, LT, RES_image, RES_pixel, RES_string, RES_boolean, RES_int, RES_void, BLOCK_OPEN)) {
			BlockElem e0;
			if (match(RES_image, RES_pixel, RES_string, RES_boolean, RES_int, RES_void)) {
				e0 = declaration();
			} 
			else {
				e0 = statement();
			}
			if(!match(SEMI)) {
				throw new SyntaxException("No semi to end");
			}
			t = lexer.next();
			
			l1.add(e0);
		}

		if (!match(Kind.BLOCK_CLOSE)) {
			throw new SyntaxException("Expected ':>' at the end of block");
		}
		return new Block(firstToken, l1);
	}
	

	private Declaration declaration() throws PLCCompilerException {
		IToken firstToken = t;

		// Parse the name definition
		NameDef nDef = nameDef();

		Expr initializer = null;
		t = lexer.next();
		// Check if there's an initializer
		if(match(Kind.ASSIGN)) {
			t = lexer.next();
			initializer = expr();
			//t = lexer.next();
		}
		
		return new Declaration(firstToken, nDef, initializer);
	}


	private LValue lValue() throws PLCCompilerException {
		//Already matched ident in statement
		IToken nameToken = t;
		t = lexer.next();  // Consume the identifier

		PixelSelector pixelSelector = null;
		// Check if a pixel selector is after the identifier
		if (match(Kind.LSQUARE)) {
			pixelSelector = pixelSelector();
			t = lexer.next();	// Consume the token after the pixel selector
		}
		ChannelSelector channelSelector = null;
		// Check if a channel selector is after the identifier or pixel selector
		if (match(Kind.COLON)) {
			channelSelector = channelSelector();
			t = lexer.next();	// Consume the token after the channel selector
		}

		return new LValue(nameToken, nameToken, pixelSelector, channelSelector);
	}

	private Statement statement() throws PLCCompilerException {
		IToken firstToken = t;
		// If the statement starts with "write"
		if (match(Kind.RES_write)) {
			t = lexer.next();  // Consume the "write" token
			Expr expr = expr();
			return new WriteStatement(firstToken, expr);
		}
		// If the statement is an assignment: LValue = Expr
		else if (match(Kind.IDENT)) {  // Assuming LValue starts with an IDENT
			LValue lvalue = lValue();
			if (match(Kind.ASSIGN)) {
				t = lexer.next();  // Consume the "=" token
				Expr expr = expr();
				return new AssignmentStatement(firstToken, lvalue, expr);
			} 
			else {
				throw new SyntaxException("Expected '=' after LValue");
			}
		}
		// If the statement starts with "do ... od"
		else if (match(Kind.RES_do)) {
			List<GuardedBlock> guardedBlocks = new ArrayList<>();
			t = lexer.next();  // Consume the "do" token
			guardedBlocks.add(guardedBlock());
			t = lexer.next();
			while (match(Kind.BOX)) {
				t = lexer.next();  // Consume the "[" token
				guardedBlocks.add(guardedBlock());
				t = lexer.next();  // Consume the "]" token
			}
			if (!match(Kind.RES_od)) {
				throw new SyntaxException("Expected 'od' after do-statement");
			}
			t = lexer.next();  // Consume the "od" token
			return new DoStatement(firstToken, guardedBlocks);
		}
		// If the statement starts with "if ... fi"
		else if (match(Kind.RES_if)) {
			List<GuardedBlock> guardedBlocks = new ArrayList<>();
			t = lexer.next();  // Consume the "if" token
			guardedBlocks.add(guardedBlock());
			t = lexer.next();
			while (match(Kind.BOX)) {
				t = lexer.next();  // Consume the "[" token
				guardedBlocks.add(guardedBlock());
				t = lexer.next();  // Consume the "]" token
			}
			if (!match(Kind.RES_fi)) {
				throw new SyntaxException("Expected 'fi' after if-statement");
			}
			t = lexer.next();  // Consume the "fi" token
			return new IfStatement(firstToken, guardedBlocks);
		}
		// If the statement starts with "^"
		else if (match(Kind.RETURN)) {
			t = lexer.next();  // Consume the "^" token
			Expr expr = expr();
			return new ReturnStatement(firstToken, expr);
		}
		// If the statement starts with a block
		else if (match(Kind.BLOCK_OPEN)) {
			StatementBlock block = blockStatement();
			t = lexer.next();
			return block;
		}
		throw new PLCCompilerException("Not valid statement");
	}


	private GuardedBlock guardedBlock() throws PLCCompilerException {
		IToken first = t;
		Expr guard = expr();  // Parse the guard expression
		if (!match(Kind.RARROW)) {  // Assuming '<:' begins a block
			throw new SyntaxException("Expected '<:' to start a block after guard at " + t);
		}
		t = lexer.next();
		Block block = block();
		
		return new GuardedBlock(first, guard, block);
	}


	private List<NameDef> paramList() throws PLCCompilerException {
		List<NameDef> l1 = new ArrayList<NameDef>();
		NameDef e0 = nameDef();
		l1.add(e0);
		t = lexer.next(); // go to comma
		while(match(COMMA)) {
			t = lexer.next(); // consume comma
			e0 = nameDef();
			l1.add(e0);
			t = lexer.next(); // go to comma
		}
		return l1;
	}

	private NameDef nameDef() throws PLCCompilerException {
		IToken firstToken = t;

		// Parse the type of the identifier
		IToken type = type();
		Dimension e0 = null;
		t = lexer.next();

		// Check if there's a dimension
		if(match(LSQUARE)) {
			e0 = dimension();
			t = lexer.next(); //consume close bracket
		}
		// Check if the identifier is present
		if(match(IDENT)) {
			IToken ident = t;
			return new NameDef(firstToken, type, e0, ident);
		}
		throw new SyntaxException("Not valid namedef");
	}
	
	private Dimension dimension() throws PLCCompilerException {
		IToken firsToken = t;
		Expr e0 = null;
		Expr e1 = null;
		// Check if there's a dimension given by a '['
		if(match(LSQUARE)) {
			t = lexer.next();
			e0 = expr();	// Parse the first expression inside the bracket
			t = lexer.next();
			e1 = expr();	// Parse the second expression inside the bracket
			if(match(RSQUARE)) {
				return new Dimension(firsToken, e0, e1);
			}
			throw new SyntaxException("No closing bracket");
		}
		throw new SyntaxException("No opening bracket");
	}

	// Returns the current token if it matches any of the valid types, if not throw an exception
	private IToken type() throws PLCCompilerException {
		IToken ret = t;
		// Check if the current token is one of the following valid types
		if(match(RES_image, RES_pixel, RES_int, RES_string, RES_void, RES_boolean)) {
			return ret;
		} 
		throw new SyntaxException("Not valid type");
	}

	private StatementBlock blockStatement() throws PLCCompilerException {
		IToken firstToken = t;

		// Parse the block of code
		Block blockCode = block();

		return new StatementBlock(firstToken, blockCode);
	}


	private Expr expr() throws PLCCompilerException {
		IToken firstToken = t;

		// Check if expression starts with a question mark, which indicates a conditional expression
		if(match(QUESTION)) {
			return conditionalExpr(firstToken);
		}
		// If it's not a conditional expression, parse as a logical OR expression
		else {
			return logicalOrExpr();
		}
	}


	// Method that parses primary expressions like literals, identifiers, parenthesized expressions, and constants
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
		// For parenthesized expressions, parse the enclosed expression
		else if (match(LPAREN)) {
			Expr e0 = null;
			t = lexer.next();
			e0 = expr();
			if (match(RPAREN)) {
				t = lexer.next();
				return e0;
			}
		}
		else if (match(CONST)) {
			t = lexer.next();
			return new ConstExpr(firstToken);
		}
		// Parse an expanded pixel definition
		else if (match(LSQUARE)) {
			return expandedPixel();
		}
		// If there's no recognized form that is found, throw an exception
		throw new SyntaxException("Not valid syntax");
	}

	// Method that parses conditional expressions
	private Expr conditionalExpr(IToken firstToken) throws PLCCompilerException {
		if (match(QUESTION)) {
			t = lexer.next();
			Expr expr1 = expr();
			if (match(RARROW)) {
				t = lexer.next();
				Expr expr2 = expr();
				if (match(COMMA)) {
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

	// Method that parses logical OR expressions
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

	// Method that parses logical AND expressions
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

	// Method that parses comparison expressions
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

	// Method that parses power expressions
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

	// Method that parses additive expressions
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

	// Method that parses multiplicative expressions
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

	// Method that parsed unary expressions
	private Expr unaryExpr() throws PLCCompilerException {
		IToken firsToken = t;
		if (match(BANG, MINUS, RES_width, RES_height)) {
			IToken op = t;
			t = lexer.next();
			Expr e = unaryExpr();
			return new UnaryExpr(e.firstToken, op, e);
		} else {
			return unaryExprPostfix(firsToken);
		}
	}

	// Method that parses postfix unary expressions
	private Expr unaryExprPostfix(IToken first) throws PLCCompilerException {
		Expr e0 = primaryExpr(t);
		PixelSelector e1 = null;
		ChannelSelector e2 = null;
		// Check for PixelSelector or epsilon
		if (match(LSQUARE)) {
			e1 = pixelSelector();
			t = lexer.next();
		}
		// Check for ChannelSelector or epsilon
		if (match(COLON)) {
			e2 = channelSelector();
			t = lexer.next();
		}
		if(e1 != null || e2 != null) {
		e0 = new PostfixExpr(first, e0, e1, e2);
		}
		return e0;
	}

	// Method that parses channel selectors like :blue, :green, and :red
	private ChannelSelector channelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		if (match(COLON)) {
			t = lexer.next();
			if(match(RES_blue, RES_green, RES_red)) {
				return new ChannelSelector(firstToken, t);
			}
			else {
				throw new SyntaxException(t.sourceLocation(), ": followed by an invalid color");
			}
		}
		throw new SyntaxException(t.sourceLocation(), "Expected a color selector but found " + t.kind());
	}


	// Method that parses pixel selectors
	private PixelSelector pixelSelector() throws PLCCompilerException {
		IToken firstToken = t;
		if (match(LSQUARE)) {
			t = lexer.next();
			Expr e1 = expr();
			if (match(COMMA)) {
				t = lexer.next();
				Expr e2 = expr();
				if (match(RSQUARE)) {
					return new PixelSelector(firstToken, e1, e2);
				}
				throw new SyntaxException("Expected closing square bracket for PixelSelector");
			}
			throw new SyntaxException("Expected comma in PixelSelector");
		}
		throw new SyntaxException("Incorrect start for PixelSelector");
	}

	// Method that parses expanded pixel expressions
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
