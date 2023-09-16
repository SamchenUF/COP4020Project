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

import static edu.ufl.cise.cop4020fa23.Kind.EOF;

import java.nio.channels.IllegalSelectorException;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

public class Lexer implements ILexer {
	String input;
	int sentinel;
	int i; //position
	int row;
	int column;
	char[] arr;
	private enum State {
	START, HAVE_EQ, HAVE_AND, HAVE_LT, HAVE_GT, HAVE_STAR, HAVE_STRAIGHT, HAVE_LB, HAVE_COLON, HAVE_HASH, HAVE_DASH, HAVE_OR;
	}
	
	public Lexer(String input) {
		this.input = input;
		sentinel = input.length();
		i = 0;
		row = 1;
		column = 0;
		arr = input.toCharArray();
	}


	@Override
	public IToken next() throws LexicalException {
		State state = State.START;
		int startPos = i;
		boolean temp = true;
		boolean newTok = true;
		if (sentinel == 0) {
			temp = false;
		}
		if (i >= sentinel) {
				temp = false;
			}
		while(temp) {
			char current = input.charAt(i);
			if(newTok) {startPos = i;}
			column++;
			switch (state) {
				case START -> {
					switch(current) {
						case ' ', '\n', '\r' -> {
							i++;
							if (current == ' ') {column++;};
							if(current == '\n') {row++; column = 0;}
						} 
						case '+' -> {
							i++;
							return new Token(Kind.PLUS, startPos, 1, arr, new SourceLocation(row, i));
						}
						case ',' -> {
							i++;
							return new Token(Kind.COMMA, startPos, 1, arr, new SourceLocation(row, column));
						}
						case ';' -> {
							i++;
							return new Token(Kind.SEMI, startPos, 1, arr, new SourceLocation(row, column));
						}
						case '?' -> {
							i++;
							return new Token(Kind.QUESTION, startPos, 1, arr, new SourceLocation(row, column));
						}
						case ':' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.COLON, startPos, 1, arr, new SourceLocation(row, column));}
							else {newTok = false; state = State.HAVE_COLON;}
						}
						case '(' -> {
							i++;
							return new Token(Kind.LPAREN, startPos, 1, arr, new SourceLocation(row, column));
						}
						case ')' -> {
							i++;
							return new Token(Kind.RPAREN, startPos, 1, arr, new SourceLocation(row, column));
						}
						case '[' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.LSQUARE, startPos, 1, arr, new SourceLocation(row, column));}
							else {newTok = false; state = State.HAVE_LB;}
							
						}
						case ']' -> {
							i++;
							return new Token(Kind.RSQUARE, startPos, 1, arr, new SourceLocation(row, column));
						}
						case '<' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.LT, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_LT;}
							
						}
						case '>' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.GT, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_GT;}
						}
						case '=' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.ASSIGN, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_EQ;}
						}
						case '&'-> {
							i++;
							if(i >= sentinel) {return new Token(Kind.BITAND, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_AND;}
						}
						case '%' -> {
							i++;
							return new Token(Kind.MOD, startPos, 1, arr, new SourceLocation(row, column));
							}
						case '#' -> {
							i++;
							if (input.charAt(i) == '#' ){i++; state = State.HAVE_HASH;}
							else {throw new IllegalStateException("not comment");}
						}
						case '/' -> {
							i++;
							System.out.println("Running");
							return new Token(Kind.DIV, startPos, 1, arr, new SourceLocation(row, column));
						}
						case '-' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.MINUS, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_DASH;}
						}
						case '!' -> {
							i++;
							return new Token(Kind.BANG, startPos, 1, arr, new SourceLocation(row, column));
							}
						case '|'-> {
							i++;
							if(i >= sentinel) {return new Token(Kind.BITOR, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_OR;}
						}
						case '*'-> {
							i++;
							if(i >= sentinel) {return new Token(Kind.TIMES, startPos, 1, arr, new SourceLocation(row, column));}
							else{newTok = false; state = State.HAVE_STAR;}
						}
						default -> {
							throw new IllegalStateException("lexer bug");
						}
					}
				}
				case HAVE_STAR -> {
					newTok = true;
					switch(current) {
						case '*' -> {
							i++;
							return new Token(Kind.EXP, startPos, 2, arr, new SourceLocation(row, column));
						}
						default -> {
							return new Token(Kind.TIMES, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_OR -> {
					newTok = true;
					switch(current) {
						case '|' -> {
							i++;
							//state = State.START;
							return new Token(Kind.OR, startPos, 2, arr, new SourceLocation(row, column));
						}
						default -> {
							//state = State.START;
							return new Token(Kind.BITOR, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_DASH -> {
					newTok = true;
					if(current == '>') {
						i++;
						return new Token(Kind.RARROW, startPos, current, arr, new SourceLocation(row, column));
					}
					else {
						//state = State.START;
						return new Token(Kind.MINUS, startPos, current, arr, new SourceLocation(row, column));
					}
				}
				case HAVE_GT -> {
					newTok = true;
					if(current == '=') {
						i++;
						return new Token(Kind.GE, startPos, current, arr, new SourceLocation(row, column));
					}
					else {return new Token(Kind.GT, startPos, current, arr, new SourceLocation(row, column));}
				}
				case HAVE_LT -> {
					newTok = true;
					if(current == '=') {
						i++;
						return new Token(Kind.LE, startPos, current, arr, new SourceLocation(row, column));
					}
					else if(current == ':') {
						i++;
						return new Token(Kind.BLOCK_OPEN, startPos, current, arr, new SourceLocation(row, column));
					}
					else{return new Token(Kind.LT, startPos, current, arr, new SourceLocation(row, column));}
				}
				case HAVE_HASH -> {
					if (current == '\n') {newTok = true; state = State.START;}
					i++;
					}
				case HAVE_EQ -> {
					newTok = true;
					switch(current) {
						case '=' -> {
							i++;
							//state = State.START;
							return new Token(Kind.EQ, startPos, 2, arr, new SourceLocation(row, column));
						}
						default -> {
							//state = State.START;
							return new Token(Kind.ASSIGN, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_AND -> {
					newTok = true;
					switch(current) {
						case '&' -> {
							i++;
							//state = State.START;
							return new Token(Kind.AND, startPos, 2, arr, new SourceLocation(row, column));
						}
						default -> {
							//state = State.START;
							return new Token(Kind.BITAND,startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_LB -> {
					newTok = true;
					switch(current) {
						case ']' -> {
							i++;
							//state = State.START;
							return new Token(Kind.BOX, startPos, 2, arr, new SourceLocation(row, column));
						}
						default -> {
							//state = State.START;
							return new Token(Kind.LSQUARE, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				default -> {
					throw new IllegalStateException("lexer bug");
				}
			}
			if (i >= sentinel) {
				temp = false;
			}
		}
		return new Token(EOF, 0, 0, null, new SourceLocation(1, 1));
	}


}


