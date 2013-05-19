/**
 * 
 */
package m1;

import swp_compiler_ss13.common.lexer.Token;
import swp_compiler_ss13.common.lexer.TokenType;
import lexer.LexerImpl;
import lexer.token.TokenImpl;
import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Tay, Ho Phuong
 * 
 */
public class SimpleAddTest {
	private String prog = 
			"# returns 6\n" +
			"long l;\n" +
			"l = 3 + 3;\n" +
			"return l;\n";
	private InputStream stream;
	private LexerImpl lexer;
	private ArrayList<Token> list;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	    this.stream = new ByteArrayInputStream(prog.getBytes());
		this.lexer = new lexer.LexerImpl();
		this.lexer.setSourceStream(this.stream);
		this.list = new ArrayList<Token>(Arrays.asList(
			new TokenImpl("# returns 6", TokenType.COMMENT, 1, 1),
			new TokenImpl("long", TokenType.LONG_SYMBOL, 1, 1),
			new TokenImpl("l", TokenType.ID, 1, 1),
			new TokenImpl(";", TokenType.SEMICOLON, 1, 1),
			new TokenImpl("l", TokenType.ID, 1, 1),
			new TokenImpl("=", TokenType.ASSIGNOP, 1, 1),
			new TokenImpl("3", TokenType.NUM, 1, 1),
			new TokenImpl("+", TokenType.PLUS, 1, 1),
			new TokenImpl("3", TokenType.NUM, 1, 1),
			new TokenImpl(";", TokenType.SEMICOLON, 1, 1),
			new TokenImpl("return", TokenType.RETURN, 1, 1),
			new TokenImpl("l", TokenType.ID, 1, 1),
			new TokenImpl(";", TokenType.SEMICOLON, 1, 1),
			new TokenImpl("$", TokenType.EOF, 1, 1)
		));
	}

	@Test
	public void testgetNextToken() {
		Token token = null;
		Token comparisontoken = null;

		do {
			comparisontoken = list.remove(0);
			token = this.lexer.getNextToken();

			assertTrue(token != null);
			assertEquals(comparisontoken.getValue(), token.getValue());
			assertEquals(comparisontoken.getTokenType(), token.getTokenType());

		} while (token.getTokenType() != TokenType.EOF);
	}

}