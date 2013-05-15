package lexer.string;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import junit.extensions.PA;
import lexer.LexerImpl;
import lexer.util.Constants;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import swp_compiler_ss13.common.lexer.Lexer;
import swp_compiler_ss13.common.lexer.Token;
import swp_compiler_ss13.common.lexer.TokenType;

/**
 * Testclass for tokenizing of keywords
 * 
 * @author "Thomas Benndorf"
 * 
 */
public class KeywordTest {
	private Lexer lexer;

	@Before
	public void setUp() throws Exception {
		this.lexer = new LexerImpl();
	}

	/**
	 * Test for matching of keywords
	 */
	@Test
	public void matchingKeywordsTest() {
		TokenType tokenType = (TokenType) PA.invokeMethod(this.lexer,
				"matchToken(java.lang.String)", Constants.IFSTRING);
		assertEquals(TokenType.IF, tokenType);

		tokenType = (TokenType) PA.invokeMethod(this.lexer,
				"matchToken(java.lang.String)", Constants.WHILESTRING);
		assertEquals(TokenType.WHILE, tokenType);

		tokenType = (TokenType) PA.invokeMethod(this.lexer,
				"matchToken(java.lang.String)", Constants.DOSTRING);
		assertEquals(TokenType.DO, tokenType);

		tokenType = (TokenType) PA.invokeMethod(this.lexer,
				"matchToken(java.lang.String)", Constants.BREAKSTRING);
		assertEquals(TokenType.BREAK, tokenType);

		tokenType = (TokenType) PA.invokeMethod(this.lexer,
				"matchToken(java.lang.String)", Constants.RETURNSTRING);
		assertEquals(TokenType.RETURN, tokenType);

		tokenType = (TokenType) PA.invokeMethod(this.lexer,
				"matchToken(java.lang.String)", Constants.PRINTSTRING);
		assertEquals(TokenType.PRINT, tokenType);
	}

	/**
	 * Test for tokenizing of keywords
	 * 
	 * @throws UnsupportedEncodingException
	 *             : UTF-8 encoding not supported
	 */
	@Test
	public void simpleTokenizingKeywordsTest()
			throws UnsupportedEncodingException {

		String simpleKeywordString = Constants.IFSTRING + " "
				+ Constants.WHILESTRING + " " + Constants.DOSTRING + " "
				+ Constants.BREAKSTRING + " " + Constants.RETURNSTRING + " "
				+ Constants.PRINTSTRING;

		this.lexer.setSourceStream(new ByteArrayInputStream(simpleKeywordString
				.getBytes("UTF-8")));

		Token token = this.lexer.getNextToken();
		assertEquals(Constants.IFSTRING, token.getValue());
		assertEquals(TokenType.IF, token.getTokenType());
		assertEquals(1, token.getLine().intValue());
		assertEquals(1, token.getColumn().intValue());

		token = this.lexer.getNextToken();
		assertEquals(Constants.WHILESTRING, token.getValue());
		assertEquals(TokenType.WHILE, token.getTokenType());
		assertEquals(1, token.getLine().intValue());
		assertEquals(4, token.getColumn().intValue());

		token = this.lexer.getNextToken();
		assertEquals(Constants.DOSTRING, token.getValue());
		assertEquals(TokenType.DO, token.getTokenType());
		assertEquals(1, token.getLine().intValue());
		assertEquals(10, token.getColumn().intValue());

		token = this.lexer.getNextToken();
		assertEquals(Constants.BREAKSTRING, token.getValue());
		assertEquals(TokenType.BREAK, token.getTokenType());
		assertEquals(1, token.getLine().intValue());
		assertEquals(13, token.getColumn().intValue());

		token = this.lexer.getNextToken();
		assertEquals(Constants.RETURNSTRING, token.getValue());
		assertEquals(TokenType.RETURN, token.getTokenType());
		assertEquals(1, token.getLine().intValue());
		assertEquals(19, token.getColumn().intValue());

		token = this.lexer.getNextToken();
		assertEquals(Constants.PRINTSTRING, token.getValue());
		assertEquals(TokenType.PRINT, token.getTokenType());
		assertEquals(1, token.getLine().intValue());
		assertEquals(26, token.getColumn().intValue());

	}

	/**
	 * @deprecated not supported by grammar
	 * 
	 * @throws UnsupportedEncodingException
	 *             : UTF-8 encoding not supported
	 */
	@Deprecated
	@Ignore
	public void permutatedKeywordsTokenizingTest()
			throws UnsupportedEncodingException {
		/*
		 * permutate "if"-String
		 */
		ArrayList<String> permutatedKeywords = this
				.permutateKeyword(Constants.IFSTRING);
		for (int i = 0; i < permutatedKeywords.size(); i++) {
			System.out.println(permutatedKeywords.get(i));
			this.lexer.setSourceStream(new ByteArrayInputStream(
					permutatedKeywords.get(i).getBytes("UTF-8")));
			assertEquals(permutatedKeywords.get(i), this.lexer.getNextToken()
					.getValue());
		}

	}

	private ArrayList<String> permutateKeyword(String simpleKeyword) {
		ArrayList<String> permutatedKeywords = new ArrayList<>();
		char[] keywordChars = simpleKeyword.toCharArray();
		for (int i = 0, n = (int) Math.pow(2, keywordChars.length); i < n; i++) {
			char[] permutation = new char[keywordChars.length];
			for (int j = 0; j < keywordChars.length; j++) {
				if ((i >> j & 1) != 0) {
					permutation[j] = Character.toUpperCase(keywordChars[j]);
				} else {
					permutation[j] = keywordChars[j];
				}
			}
			permutatedKeywords.add(new String(permutation));
		}
		return permutatedKeywords;
	}
}
