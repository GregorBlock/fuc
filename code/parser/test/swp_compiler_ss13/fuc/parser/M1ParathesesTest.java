package swp_compiler_ss13.fuc.parser;

import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.id;
import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.num;
import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.t;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.M1.*;

import java.io.ByteArrayInputStream;

import lexer.LexerImpl;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.lexer.Lexer;
import swp_compiler_ss13.common.lexer.TokenType;
import swp_compiler_ss13.common.parser.ReportLog;
import swp_compiler_ss13.fuc.parser.errorHandling.ReportLogImpl;
import swp_compiler_ss13.fuc.parser.generator.ALRGenerator;
import swp_compiler_ss13.fuc.parser.generator.LR0Generator;
import swp_compiler_ss13.fuc.parser.generator.items.LR0Item;
import swp_compiler_ss13.fuc.parser.generator.states.LR0State;
import swp_compiler_ss13.fuc.parser.grammar.Grammar;
import swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar;
import swp_compiler_ss13.fuc.parser.grammar.Terminal;
import swp_compiler_ss13.fuc.parser.parser.LRParser;
import swp_compiler_ss13.fuc.parser.parser.LexerWrapper;
import swp_compiler_ss13.fuc.parser.parser.tables.LRParsingTable;

public class M1ParathesesTest {
	static {
		BasicConfigurator.configure();
	}

	@Test
	public void testParatheses() {
		// String input = "# returns 8 or does it?"
		// + "long l;\n"
		// + "l = ( 3 + 3 ) * 2 - ( l = ( 2 + ( 16 / 8 ) ) );\n"
		// + "return l;";
		// Generate parsing table
		Grammar grammar = new ProjectGrammar.M1().getGrammar();
		ALRGenerator<LR0Item, LR0State> generator = new LR0Generator(grammar);
		LRParsingTable table = generator.getParsingTable();

		// Simulate input
		Lexer lexer = new TestLexer(new TestToken("", TokenType.COMMENT),
				new TestToken("long", TokenType.LONG_SYMBOL), id("l"), t(sem),
				id("l"), t(assignop), t(lb), num(3), t(plus), num(3), t(rb),
				t(times), num(2), t(minus), t(lb), id("l"), t(assignop), t(lb),
				num(2), t(plus), t(lb), num(16), t(div), num(8), t(rb), t(rb),
				t(rb), t(sem), t(Terminal.EOF));

		// Run LR-parser with table
		LRParser lrParser = new LRParser();
		LexerWrapper lexWrapper = new LexerWrapper(lexer, grammar);
		ReportLog reportLog = new ReportLogImpl();
		AST ast = lrParser.parse(lexWrapper, reportLog, table);

		checkAst(ast);
	}

	private static void checkAst(AST ast) {
		assertNotNull(ast);
		// TODO Validate ast
	}

	@Test
	public void testParathesesOrgLexer() {
		String input = "# returns 8 or does it?" + "long l;\n"
				+ "l = ( 3 + 3 ) * 2 - ( l = ( 2 + ( 16 / 8 ) ) );\n"
				+ "return l;";
		// Generate parsing table
		Grammar grammar = new ProjectGrammar.M1().getGrammar();
		ALRGenerator<LR0Item, LR0State> generator = new LR0Generator(grammar);
		LRParsingTable table = generator.getParsingTable();

		// Simulate input
		Lexer lexer = new LexerImpl();
		lexer.setSourceStream(new ByteArrayInputStream(input.getBytes()));

		// Run LR-parser with table
		LRParser lrParser = new LRParser();
		LexerWrapper lexWrapper = new LexerWrapper(lexer, grammar);
		ReportLog reportLog = new ReportLogImpl();
		AST ast = lrParser.parse(lexWrapper, reportLog, table);

		checkAst(ast);
	}
}