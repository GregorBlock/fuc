package lexer.token;

import swp_compiler_ss13.common.lexer.RealToken;

/**
 * @author Ho, Tay Phuong
 * 
 */
public class RealTokenImpl implements RealToken {

	private final String value;
	private final TokenType type;
	private final Integer line;
	private final Integer column;

	/**
	 * 
	 */
	public RealTokenImpl(String value, TokenType type, Integer line,
			Integer column) {

		this.value = value;
		this.type = type;
		this.line = line;
		this.column = column;
	}

	/**
	 * @return string readed by lexer for this token
	 */
	public String getValue() {
		return this.value;
	}

	/**
	 * @return type of token
	 */
	public TokenType getTokenType() {
		return this.type;
	}

	/**
	 * @return line of code in source file
	 */
	public Integer getLine() {
		return this.line;
	}

	/**
	 * @return column of code in source file
	 */
	public Integer getColumn() {
		return this.column;
	}

	public Double getDoubleValue() {

		/**
		 * '[0-9]+\.[0-9]+ ((E|e)-?[0-9+]))?'
		 */
		return 0.0;
	}

}