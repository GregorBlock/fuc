package swp_compiler_ss13.fuc.parser;

import static org.junit.Assert.assertNotNull;
import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.b;
import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.id;
import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.num;
import static swp_compiler_ss13.fuc.parser.GrammarTestHelper.t;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.assignop;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.elsee;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.iff;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.lb;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.not;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.orop;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.print;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.rb;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.returnn;
import static swp_compiler_ss13.fuc.parser.grammar.ProjectGrammar.Complete.sem;

import org.junit.Test;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
import swp_compiler_ss13.common.ast.nodes.unary.UnaryExpressionNode.UnaryOperator;
import swp_compiler_ss13.common.lexer.Lexer;
import swp_compiler_ss13.common.lexer.TokenType;
import swp_compiler_ss13.common.types.primitive.BooleanType;
import swp_compiler_ss13.common.types.primitive.LongType;
import swp_compiler_ss13.common.types.primitive.StringType;
import swp_compiler_ss13.fuc.ast.ASTFactory;
import swp_compiler_ss13.fuc.ast.BranchNodeImpl;
import swp_compiler_ss13.fuc.parser.grammar.Terminal;
import swp_compiler_ss13.fuc.parser.parser.LRParser;

public class M2CondTest {
	@Test
	public void testCond() {
		// Simulate input
		Lexer lexer = new TestLexer(
				t("bool", TokenType.BOOL_SYMBOL), id("b"), t(sem),
				t("bool", TokenType.BOOL_SYMBOL), id("c"), t(sem),
				t("long", TokenType.LONG_SYMBOL), id("l"), t(sem),
				t("string", TokenType.STRING_SYMBOL), id("bla"), t(sem),
				id("bla"), t(assignop), t("\"bla\"", TokenType.STRING), t(sem),
				id("b"), t(assignop), b(true), t(sem),
				id("c"), t(assignop), b(false), t(sem),
				id("l"), t(assignop), num(4), t(sem),
				t(iff), t(lb), id("b"), t(rb),
				t(iff), t(lb), id("c"), t(orop), t(not), id("b"), t(rb),
				t(print), id("bla"), t(sem),
				t(elsee),
				id("l"), t(assignop), num(5), t(sem),
				t(returnn), id("l"), t(sem), t(Terminal.EOF));
		
		// Check output
		AST ast = GrammarTestHelper.parseToAst(lexer);
		checkAst(ast);
	}

	@Test
	public void testCondOrgLexer() throws Exception {
		String input = "# return 5\n"
				+ "# prints nothing\n"
				+ "bool b;\n"
				+ "bool c;\n"
				+ "long l;\n"
				+ "\n"
				+ "string bla;\n"
				+ "bla = \"bla\";\n"
				+ "\n"
				+ "b = true;\n"
				+ "c = false;\n"
				+ "\n"
				+ "l = 4;\n"
				+ "\n"
				+ "# dangling-else should be resolved as given by indentation\n"
				+ "\n"
				+ "if ( b )\n"
				+ "  if ( c || ! b )\n"
				+ "    print bla;\n"
				+ "  else\n"
				+ "    l = 5;\n"
				+ "\n"
				+ "return l;\n";
		
		// Check output
		AST ast = GrammarTestHelper.parseToAst(input);
		checkAst(ast);
	}

	private static void checkAst(AST ast) {
		assertNotNull(ast);

		// Create reference AST
		ASTFactory factory = new ASTFactory();
		factory.addDeclaration("b", new BooleanType());
		factory.addDeclaration("c", new BooleanType());
		factory.addDeclaration("l", new LongType());

		factory.addDeclaration("bla", new StringType(LRParser.STRING_LENGTH));
		factory.addAssignment(factory.newBasicIdentifier("bla"), factory.newLiteral("\"bla\"", new StringType(5L)));

		factory.addAssignment(factory.newBasicIdentifier("b"), factory.newLiteral("true", new BooleanType()));
		factory.addAssignment(factory.newBasicIdentifier("c"), factory.newLiteral("false", new BooleanType()));

		factory.addAssignment(factory.newBasicIdentifier("l"), factory.newLiteral("4", new LongType()));
		
		BranchNode iff = factory.addBranch(factory.newBasicIdentifier("b"));
		BranchNode ifElse = new BranchNodeImpl();
		ifElse.setCondition(factory.newBinaryExpression(
				BinaryOperator.LOGICAL_OR,
				factory.newBasicIdentifier("c"),
				factory.newUnaryExpression(UnaryOperator.LOGICAL_NEGATE,
						factory.newBasicIdentifier("b"))));
		ifElse.setStatementNodeOnTrue(factory.newPrint(factory.newBasicIdentifier("bla")));
		ifElse.setStatementNodeOnFalse(factory.newAssignment(factory.newBasicIdentifier("l"), factory.newLiteral("5", new LongType())));
		iff.setStatementNodeOnTrue(ifElse);
		factory.goToParent();
		
		factory.addReturn(factory.newBasicIdentifier("l"));
		
		AST expected = factory.getAST();
		ASTComparator.compareAST(expected, ast);
	}
}
