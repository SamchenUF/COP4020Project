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

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;

public class Lexer implements ILexer {
	String input;
	int sentinel;
	int i; //position
	private enum State {
	START, HAVE_EQ;
	}
	public Lexer(String input) {
		this.input = input;
		sentinel = input.length();
		i = 0;
		
	}

	@Override
	public IToken next() throws LexicalException {
		State state = State.START;
		int startPos;
		boolean temp = true;
		if (sentinel == 0) {
			temp = false;
		}
		while(temp) {
			char current = input.charAt(i);
			state = State.START;
			startPos = i;
			switch (state) {
				case START -> {
					switch(current) {
						case ' ', '\n', '\r' -> i++; 
						case '+' -> {
							i++;
							return new Token(Kind.PLUS, startPos, 1, null, new SourceLocation(1, 1));
						}
						case ',' -> {
							i++;
							return new Token(Kind.COMMA, startPos, 1, null, null);
						}
						case '=' -> {
							i++;
							state = State.HAVE_EQ;
						}
						default -> {
							temp = false;
							throw new IllegalStateException("lexer bug");
						}
					}
					if (i >= sentinel){
						temp = false;
					}
				}
				case HAVE_EQ -> {
					switch(current) {
						case '=' -> {
							i++;
							return new Token(Kind.EQ, startPos, 2, null, new SourceLocation(1, 1));
						}
					}
					if (i >= sentinel){
						temp = false;
					}
				}
				default -> {
					temp = false;
					System.out.println(i);
					throw new IllegalStateException("lexer bug");
				}
			}
		}
		return new Token(EOF, 0, 0, null, new SourceLocation(1, 1));
	}


}


