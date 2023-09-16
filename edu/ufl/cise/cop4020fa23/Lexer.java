package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.IDENT;

import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Lexer implements ILexer {

	String input;
	int currentPos = 0;
	private char[] chars;
	private int pos = 0;

	private static final Set<String> RESERVED_WORDS = new HashSet<>(Arrays.asList("image", "pixel", "int", "string", "void", "boolean", "write", "height", "width", "if", "fi", "do", "od", "red", "green", "blue"));
	private static final Set<String> BOOLEAN_LITERALS = new HashSet<>(Arrays.asList("TRUE", "FALSE"));
	private static final Set<String> CONSTANTS = new HashSet<>(Arrays.asList("Z", "BLACK", "BLUE", "CYAN", "DARK_GRAY", "GRAY", "GREEN", "LIGHT_GRAY", "MAGENTA", "ORANGE", "PINK", "RED", "WHITE", "YELLOW"));

	public Lexer(String input) {
		this.input = input;
		this.chars = input.toCharArray();
	}

	private boolean isLetter(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private IToken identifier(int pos) {
		int startPos = pos;
		pos++;
		while (pos < input.length() && (isLetter(input.charAt(pos)) || isDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
			pos++;
		}
		String lexeme = input.substring(startPos, pos);
		char[] source = lexeme.toCharArray();

		if (RESERVED_WORDS.contains(lexeme)) {
			Kind kind;
			switch (lexeme) {
				case "image":
					kind = Kind.RES_image;
					break;
				case "pixel":
					kind = Kind.RES_pixel;
					break;
				case "int":
					kind = Kind.RES_int;
					break;
				case "string":
					kind = Kind.RES_string;
					break;
				case "void":
					kind = Kind.RES_void;
					break;
				case "boolean":
					kind = Kind.RES_boolean;
					break;
				case "write":
					kind = Kind.RES_write;
					break;
				case "height":
					kind = Kind.RES_height;
					break;
				case "width":
					kind = Kind.RES_width;
					break;
				case "if":
					kind = Kind.RES_if;
					break;
				case "fi":
					kind = Kind.RES_fi;
					break;
				case "do":
					kind = Kind.RES_do;
					break;
				case "od":
					kind = Kind.RES_od;
					break;
				case "red":
					kind = Kind.RES_red;
					break;
				case "green":
					kind = Kind.RES_green;
					break;
				case "blue":
					kind = Kind.RES_blue;
					break;
				default:
					throw new RuntimeException("Unexpected reserved word: " + lexeme);
			}
			currentPos = pos;  // Update the current position here
			return new Token(kind, startPos, pos-startPos, input.toCharArray(), new SourceLocation(1, 1));
		} else if (BOOLEAN_LITERALS.contains(lexeme)) {
			currentPos = pos;  // Update the current position here
			return new Token(Kind.BOOLEAN_LIT, startPos, pos-startPos, input.toCharArray(), new SourceLocation(1, 1));
		} else if (CONSTANTS.contains(lexeme)) {
			currentPos = pos;  // Update the current position here
			return new Token(Kind.CONST, startPos, pos-startPos, input.toCharArray(), new SourceLocation(1, 1));
		}
		currentPos = pos;  // Update the current position here
		return new Token(IDENT, startPos, pos-startPos, input.toCharArray(), new SourceLocation(1, 1));
	}

	private IToken number(int pos) {
		int startPos = pos;
		int length = 1;
		if (input.charAt(pos) == '0') {
			pos++;
			currentPos = pos;  // Update the current position here
			return new Token(Kind.NUM_LIT, startPos, 1, input.toCharArray(), new SourceLocation(1, 1));
		}
		pos++;
		while (pos < input.length() && isDigit(input.charAt(pos))) {
				pos++;
				length++;
			}
		//String lexeme = input.substring(startPos, pos);
		//char[] lexemeCharArray = lexeme.toCharArray();
		currentPos = pos;  // Update the current position here
		return new Token(Kind.NUM_LIT, startPos, length, input.toCharArray(), new SourceLocation(1, 1));
	}
	private IToken stringLit(int pos) throws LexicalException {
		int startPos = pos;
		pos++;
		while (pos < input.length() && input.charAt(pos) != '"') {
			pos++;
		}
		if (pos >= input.length()) {
			throw new LexicalException("Unclosed string literal", pos);
		}
		pos++;
		//String lexeme = input.substring(startPos, pos);
		//char[] lexemeCharArray = lexeme.toCharArray();
		currentPos = pos;  // Update the current position here
		return new Token(Kind.STRING_LIT, startPos, pos-startPos, input.toCharArray(), new SourceLocation(1, 1));
	}


	private int skipWhiteSpaceAndComments(int pos) {
		boolean commentsOrWhitespaceFound;
		do {
			commentsOrWhitespaceFound = false;

			// Skip white space
			while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
				pos++;
			}

			// Skip comments
			if (pos < input.length() - 1 && input.charAt(pos) == '#' && input.charAt(pos + 1) == '#') {
				commentsOrWhitespaceFound = true;
				pos += 2;  // Skip the "##"
				while (pos < input.length() && input.charAt(pos) != '\n') {
					pos++;
				}
			}

			// This loop will continue until we find a position that is neither white space nor the start of a comment.
		} while (commentsOrWhitespaceFound && pos < input.length());

		return pos;
	}


	private boolean match(int pos, String pattern) {
		for (int i = 0; i < pattern.length(); i++) {
			if (pos + i >= chars.length || chars[pos + i] != pattern.charAt(i)) {
				return false;
			}
		}
		return true;
	}

	class TokenResult {
		Token token;
		int newPos;

		TokenResult(Token token, int newPos) {
			this.token = token;
			this.newPos = newPos;
		}

		Token getToken() {
			return token;
		}

		int getNewPos() {
			return newPos;
		}

		int getEndPos() {
			return newPos;
		}
	}


	public TokenResult operatorOrSeparator(int pos) throws LexicalException {
		char ch = chars[pos];
		String lexeme = String.valueOf(ch);
		Kind kind = null;
		int lengthOfToken = 0;
		switch (ch) {
			case ','-> {
				kind = Kind.COMMA;
				lengthOfToken = 1;
			}
			case ';' -> {
				kind = Kind.SEMI;
				lengthOfToken = 1;
			}
			case '?' -> {
				kind = Kind.QUESTION;
				lengthOfToken = 1;
			}
			case ':' -> {
				if (match(pos, ":>")) {
					lexeme += '>';
					kind = Kind.BLOCK_CLOSE;
					lengthOfToken = 2;
				} else {
					kind = Kind.COLON;
					lengthOfToken = 1;
				}
			}
			case '(' -> {
				kind = Kind.LPAREN;
				lengthOfToken = 1;
			}
			case ')' -> {
				kind = Kind.RPAREN;
				lengthOfToken = 1;
			}
			case '<' -> {
				if (match(pos, "<=")) {
					lexeme += '=';
					kind = Kind.LE;
					lengthOfToken = 2;
				} else if (match(pos, "<:")) {
					lexeme += ':';
					kind = Kind.BLOCK_OPEN;
					lengthOfToken = 2;
				} else {
					kind = Kind.LT;
					lengthOfToken = 1;
				}
			}
			case '>' -> {
				if (match(pos, ">=")) {
					lexeme += '=';
					kind = Kind.GE;
					lengthOfToken = 2;
				} else {
					kind = Kind.GT;
					lengthOfToken = 1;
				}
			}
			case '[' -> {
				if (match(pos, "[]")) {
					lexeme += ']';
					kind = Kind.BOX;
					lengthOfToken = 2;
				} else {
					kind = Kind.LSQUARE;
					lengthOfToken = 1;
				}
			}
			case ']' -> {
				kind = Kind.RSQUARE;
				lengthOfToken = 1;
			}
			case '=' -> {
				if (match(pos, "==")) {
					lexeme += '=';
					kind = Kind.EQ;
					lengthOfToken = 2;
				} else {
					kind = Kind.ASSIGN;
					lengthOfToken = 1;
				}
			}
			case '!' -> {
				kind = Kind.BANG;
				lengthOfToken = 1;
			}
			case '&' -> {
				if (match(pos, "&&")) {
					lexeme += '&';
					kind = Kind.AND;
					lengthOfToken = 2;
				} else {
					kind = Kind.BITAND;
					lengthOfToken = 1;
				}
			}
			case '|' ->  {
				if (match(pos, "||")) {
					lexeme += '|';
					kind = Kind.OR;
					lengthOfToken = 2;
				} else {
					kind = Kind.BITOR;
					lengthOfToken = 1;
				}
			}
			case '+' -> {
				kind = Kind.PLUS;
				lengthOfToken = 1;
			}
			case '-' -> {
				if (match(pos, "->")) {
					lexeme += '>';
					kind = Kind.RARROW;
					lengthOfToken = 2;
				} else {
					kind = Kind.MINUS;
					lengthOfToken = 1;
				}
			}
			case '*' -> {
				if (match(pos, "**")) {
					lexeme += '*';
					kind = Kind.EXP;
					lengthOfToken = 2;
				} else {
					kind = Kind.TIMES;
					lengthOfToken = 1;
				}
			}
			case '/' -> {
				kind = Kind.DIV;
			}
			case '%' -> {
				kind = Kind.MOD;
			}
			case '^' -> {
				kind = Kind.RETURN;
				lengthOfToken = 1;
			}
			default ->
				throw new LexicalException("Illegal character", pos);
		}

		return new TokenResult(new Token(kind, pos, lexeme.length(), input.toCharArray(), new SourceLocation(1, 1)), pos + lexeme.length());
	}





	@Override
	public IToken next() throws LexicalException {
		currentPos = skipWhiteSpaceAndComments(currentPos);  // <--- Start from the current position

		if (currentPos >= input.length()) {
			return new Token(Kind.EOF, currentPos, currentPos, "EOF".toCharArray(), new SourceLocation(1, 1));
		}

		char c = input.charAt(currentPos);
		if (isLetter(c)) {
			return identifier(currentPos);
		} else if (isDigit(c)) {
			return number(currentPos);
		} else if (c == '"') {
			return stringLit(currentPos);
		} else {
			TokenResult result = operatorOrSeparator(currentPos);
			currentPos = result.getEndPos();  // Update currentPos after tokenizing
			return result.getToken();
		}
	}


}
