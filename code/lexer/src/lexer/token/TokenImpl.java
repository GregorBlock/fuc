package lexer.token;

import swp_compiler_ss13.common.lexer.Token;

/**
 * @author Ho, Tay Phuong
 * 
 */
public class TokenImpl implements Token {

	private final String value;
	private final TokenType type;
	private final Integer line;
	private final Integer column;

	/**
	 * 
	 */
	public TokenImpl(String value, TokenType type, Integer line, Integer column) {

		this.value = value;
		this.type = type;
		this.line = line;
		this.column = column;
	}

	/**
	 * @return string readed by lexer for this token
	 */
	@Override
	public String getValue() {
		return this.value;
	}

	/**
	 * @return type of token
	 */
	@Override
	public TokenType getTokenType() {
		return this.type;
	}

	/**
	 * @return line of code in source file
	 */
	@Override
	public Integer getLine() {
		return this.line;
	}

	/**
	 * @return column of code in source file
	 */
	@Override
	public Integer getColumn() {
		return this.column;
	}

}