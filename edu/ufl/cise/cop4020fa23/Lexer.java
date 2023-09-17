package edu.ufl.cise.cop4020fa23;
import static edu.ufl.cise.cop4020fa23.Kind.EOF;
import java.nio.channels.IllegalSelectorException;
import java.util.HashSet;
import java.util.Set;

import java.util.HashSet;
import java.util.Set;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import java.math.BigInteger;

public class Lexer implements ILexer {
	String input;
	int sentinel;
	int i; //position
	int row;
	int column;
	char[] arr;
	boolean newRow = true;
	int tempColumn;
	private enum State {
		START, HAVE_EQ, HAVE_AND, HAVE_LT, HAVE_GT, HAVE_STAR, HAVE_STRAIGHT, HAVE_LB, HAVE_COLON, HAVE_HASH, HAVE_DASH, HAVE_OR, HAVE_DIGIT, HAVE_ALPHA;
	}
	Set<String> constant_Set = new HashSet<String>(Set.of("Z", "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW"));
	Set<String> boolean_Set = new HashSet<String>(Set.of("TRUE", "FALSE"));
	boolean newRow = false;
	public Lexer(String input) {
		this.input = input;
		sentinel = input.length();
		i = 0;
		row = 1;
		column = 1;
		arr = input.toCharArray();
	}
	public boolean isAlphaNumericUnder(char c) {
		if ((c >= '0' & c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_')
		  return true;
		return false;
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
			newRow = false;
			
			switch (state) {

				case START -> {
					column++;
					switch(current) {

						case ' ', '\n', '\r' -> {
							i++;
							
							if(current == '\n') {
								row++; 
								column = 1;
								newRow = true;
							}
						} 
						case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.IDENT, startPos, 1, arr, new SourceLocation(row, column-1));}
							else {newTok = false; state = State.HAVE_ALPHA;}
						}
						case '0' -> {
							i++;
							return new Token(Kind.NUM_LIT, startPos, 1, arr, new SourceLocation(row, column-1));
						}
						case '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
							i++;
							if(i >= sentinel) {return new Token(Kind.NUM_LIT, startPos, 1, arr, new SourceLocation(row, column));}
							else {newTok = false; state = State.HAVE_DIGIT;}
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
							if(i >= sentinel) { return new Token(Kind.ASSIGN, startPos, 1, arr, new SourceLocation(row, column));}
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
							else {throw new LexicalException("not comment");}
						}
						case '/' -> {
							i++;
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
							throw new LexicalException("Not valid input");
						}
					}
				}
				case HAVE_ALPHA -> {
					if(isAlphaNumericUnder(current)) {
						i++;
					}

					else {
						if(boolean_Set.contains(String.copyValueOf(arr, startPos, i-startPos))) {
							return new Token(Kind.BOOLEAN_LIT, startPos, i-startPos, arr, new SourceLocation(row, column));
						}
						else if(constant_Set.contains(String.copyValueOf(arr, startPos, i-startPos))) {
							return new Token(Kind.CONST, startPos, i-startPos, arr, new SourceLocation(row, column));
						}
						else
							return new Token(Kind.IDENT, startPos, i-startPos, arr, new SourceLocation(row, column - (i-startPos)));
					}
				}
				case HAVE_STAR -> {
					newTok = true;
					switch(current) {
						case '*' -> {
							i++;
							column++;
							return new Token(Kind.EXP, startPos, 2, arr, new SourceLocation(row, column-2));
						}
						default -> {
							return new Token(Kind.TIMES, startPos, 1, arr, new SourceLocation(row, column-2));
						}
					}
				}
				case HAVE_OR -> {
					newTok = true;
					switch(current) {
						case '|' -> {
							i++;
							column++;
							return new Token(Kind.OR, startPos, 2, arr, new SourceLocation(row, column-2));
						}
						default -> {
							
							return new Token(Kind.BITOR, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_DASH -> {
					newTok = true;
					if(current == '>') {
						i++;
						column++;
						return new Token(Kind.RARROW, startPos, 2, arr, new SourceLocation(row, column-2));
					}
					else {
						
						return new Token(Kind.MINUS, startPos, 1, arr, new SourceLocation(row, column-1));
					}
				}
				case HAVE_GT -> {
					newTok = true;
					if(current == '=') {
						i++;
						column++;
						return new Token(Kind.GE, startPos, 2, arr, new SourceLocation(row, column-2));
					}
					else {return new Token(Kind.GT, startPos, 1, arr, new SourceLocation(row, column-1));}
				}
				case HAVE_LT -> {
					newTok = true;
					if(current == '=') {
						i++;
						column++;
						return new Token(Kind.LE, startPos, 2, arr, new SourceLocation(row, column-2));
					}
					else if(current == ':') {
						i++;
						column++;
						return new Token(Kind.BLOCK_OPEN, startPos, 2, arr, new SourceLocation(row, column-2));
					}
					else{return new Token(Kind.LT, startPos, 1, arr, new SourceLocation(row, column-1));}
				}
				case HAVE_HASH -> {
					if (current == '\n') {newTok = true; state = State.START;}
					i++;
					column++;
					}
				case HAVE_EQ -> {
					newTok = true;
					switch(current) {
						case '=' -> {
							i++;
							column++;
							return new Token(Kind.EQ, startPos, 2, arr, new SourceLocation(row, column-2));
						}
						default -> {
							return new Token(Kind.ASSIGN, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_AND -> {
					newTok = true;
					switch(current) {
						case '&' -> {
							i++;
							column++;
							return new Token(Kind.AND, startPos, 2, arr, new SourceLocation(row, column-2));
						}
						default -> {
							return new Token(Kind.BITAND,startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_LB -> {
					newTok = true;
					switch(current) {
						case ']' -> {
							i++;
							column++;
							return new Token(Kind.BOX, startPos, 2, arr, new SourceLocation(row, column-2));
						}
						default -> {
							return new Token(Kind.LSQUARE, startPos, 1, arr, new SourceLocation(row, column-1));
						}
					}
				}
				case HAVE_DIGIT -> { 
					if(current >= '0' && current <= '9') {
						i++;
						column++;
					}
					else {
						BigInteger maxInt = BigInteger.valueOf(Integer.MAX_VALUE);
						BigInteger stringValue = new BigInteger(String.copyValueOf(input.toCharArray(), startPos, i - startPos));
						if(stringValue.compareTo(maxInt) > 0) {
							throw new LexicalException("Num too big");
						}
						else {
						return new Token(Kind.NUM_LIT, startPos, i-startPos, arr, new SourceLocation(row, column - (i-startPos)));
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
		return new Token(EOF, 0, 0, null, new SourceLocation(row, column));
	}
}
