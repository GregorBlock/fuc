package swp_compiler_ss13.fuc.parser;

import swp_compiler_ss13.common.lexer.Token;
import swp_compiler_ss13.common.lexer.TokenType;
import swp_compiler_ss13.fuc.parser.TestToken;
import swp_compiler_ss13.fuc.parser.grammar.Terminal;

public class GrammarTestHelper {
	// for long	
	public static Token num(long i) {
		return new TestToken(i + "", TokenType.NUM);
	}

	// for  double
	public static Token real(double i) {
		return new TestToken(i + "", TokenType.REAL);
	}

	


	public static Token t(Terminal terminal) {
		// TODO Handle special terminals better
		if (terminal == Terminal.EOF) {
			return new TestToken(terminal.getId(), TokenType.EOF);
		}
		return new TestToken(terminal.getId(), terminal.getTokenTypes().next());
	}

	public static Token id(String value) {
		return new TestToken(value, TokenType.ID);
	}
}
