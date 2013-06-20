package swp_compiler_ss13.fuc.semantic_analyser;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.ast.nodes.binary.AssignmentNode;
import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
import swp_compiler_ss13.common.ast.nodes.leaf.LiteralNode;
import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;
import swp_compiler_ss13.common.ast.nodes.unary.DeclarationNode;
import swp_compiler_ss13.common.ast.nodes.unary.ReturnNode;
import swp_compiler_ss13.common.parser.SymbolTable;
import swp_compiler_ss13.common.report.ReportType;
import swp_compiler_ss13.common.types.primitive.BooleanType;
import swp_compiler_ss13.common.types.primitive.LongType;
import swp_compiler_ss13.common.types.primitive.StringType;
import swp_compiler_ss13.fuc.ast.ASTFactory;
import swp_compiler_ss13.fuc.ast.ASTImpl;
import swp_compiler_ss13.fuc.ast.AssignmentNodeImpl;
import swp_compiler_ss13.fuc.ast.BasicIdentifierNodeImpl;
import swp_compiler_ss13.fuc.ast.BlockNodeImpl;
import swp_compiler_ss13.fuc.ast.DeclarationNodeImpl;
import swp_compiler_ss13.fuc.ast.LiteralNodeImpl;
import swp_compiler_ss13.fuc.ast.ReturnNodeImpl;
import swp_compiler_ss13.fuc.errorLog.LogEntry;
import swp_compiler_ss13.fuc.errorLog.ReportLogImpl;
import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;

public class OtherTests {

	private SemanticAnalyser analyser;
	private ReportLogImpl log;

	public OtherTests() {
	}

	@Before
	public void setUp() {
		log = new ReportLogImpl();
		analyser = new SemanticAnalyser();
		analyser.setReportLog(log);
	}

	@After
	public void tearDown() {
		analyser = null;
		log = null;
	}

	/**
	 * # error: statement after return<br/>
	 * bool b; return;<br/>
	 * b = true;
	 */
	@Test
	public void testStatmentAfterReturnError() {
		// bool b;
		DeclarationNode declaration_b = new DeclarationNodeImpl();
		declaration_b.setIdentifier("b");
		declaration_b.setType(new BooleanType());

		// return;
		ReturnNode returnNode = new ReturnNodeImpl();

		// b = true;
		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
		identifier_b.setIdentifier("b");
		LiteralNode literal_true = new LiteralNodeImpl();
		literal_true.setLiteral("true");
		literal_true.setLiteralType(new BooleanType());

		AssignmentNode assignment_b = new AssignmentNodeImpl();
		assignment_b.setLeftValue(identifier_b);
		assignment_b.setRightValue(literal_true);
		identifier_b.setParentNode(assignment_b);
		literal_true.setParentNode(assignment_b);

		// main block
		SymbolTable symbolTable = new SymbolTableImpl();
		symbolTable.insert("b", new BooleanType());

		BlockNode blockNode = new BlockNodeImpl();
		blockNode.addDeclaration(declaration_b);
		blockNode.addStatement(returnNode);
		blockNode.addStatement(assignment_b);
		blockNode.setSymbolTable(symbolTable);
		declaration_b.setParentNode(blockNode);
		returnNode.setParentNode(blockNode);
		assignment_b.setParentNode(blockNode);

		// analyse AST
		AST ast = new ASTImpl();
		ast.setRootNode(blockNode);
		analyser.analyse(ast);

		System.out.println(log);
		List<LogEntry> errors = log.getErrors();
		assertEquals(errors.size(), 1);
		assertEquals(errors.get(0).getReportType(), ReportType.UNDEFINED);
	}

	/**
	 * # error: usage of bool as return type<br/>
	 * bool b;<br/>
	 * return b;
	 */
	@Test
	public void testReturnTypeError() {
		// bool b;
		DeclarationNode declaration_b = new DeclarationNodeImpl();
		declaration_b.setIdentifier("b");
		declaration_b.setType(new BooleanType());

		// return b;
		BasicIdentifierNode identifier_b = new BasicIdentifierNodeImpl();
		identifier_b.setIdentifier("b");

		ReturnNode returnNode = new ReturnNodeImpl();
		returnNode.setRightValue(identifier_b);
		identifier_b.setParentNode(returnNode);

		// main block
		SymbolTable symbolTable = new SymbolTableImpl();
		symbolTable.insert("b", new BooleanType());

		BlockNode blockNode = new BlockNodeImpl();
		blockNode.addDeclaration(declaration_b);
		blockNode.addStatement(returnNode);
		blockNode.setSymbolTable(symbolTable);
		declaration_b.setParentNode(blockNode);
		returnNode.setParentNode(blockNode);

		// analyse AST
		AST ast = new ASTImpl();
		ast.setRootNode(blockNode);
		analyser.analyse(ast);

		System.out.println(log);
		List<LogEntry> errors = log.getErrors();
		assertEquals(errors.size(), 1);
		assertEquals(errors.get(0).getReportType(), ReportType.TYPE_MISMATCH);
	}

	/**
	 * # errors: usage of bool as return type;<br/>
	 * # statement after return;<br/>
	 * # type error in assignment<br/>
	 * bool b;<br/>
	 * return b;<br/>
	 * b = 1;
	 */
	@Test
	public void testMultipleErrors() {
		// bool b;
		DeclarationNode declaration_b = new DeclarationNodeImpl();
		declaration_b.setIdentifier("b");
		declaration_b.setType(new BooleanType());

		// return b;
		BasicIdentifierNode identifier_b1 = new BasicIdentifierNodeImpl();
		identifier_b1.setIdentifier("b");

		ReturnNode returnNode = new ReturnNodeImpl();
		returnNode.setRightValue(identifier_b1);
		identifier_b1.setParentNode(returnNode);

		// b = 1;
		BasicIdentifierNode identifier_b2 = new BasicIdentifierNodeImpl();
		identifier_b2.setIdentifier("b");
		LiteralNode literal_1 = new LiteralNodeImpl();
		literal_1.setLiteral("1");
		literal_1.setLiteralType(new LongType());

		AssignmentNode assignment_b = new AssignmentNodeImpl();
		assignment_b.setLeftValue(identifier_b2);
		assignment_b.setRightValue(literal_1);
		identifier_b2.setParentNode(assignment_b);
		literal_1.setParentNode(assignment_b);

		// main block
		SymbolTable symbolTable = new SymbolTableImpl();
		symbolTable.insert("b", new BooleanType());

		BlockNode blockNode = new BlockNodeImpl();
		blockNode.addDeclaration(declaration_b);
		blockNode.addStatement(returnNode);
		blockNode.addStatement(assignment_b);
		blockNode.setSymbolTable(symbolTable);
		declaration_b.setParentNode(blockNode);
		returnNode.setParentNode(blockNode);
		assignment_b.setParentNode(blockNode);

		// analyse AST
		AST ast = new ASTImpl();
		ast.setRootNode(blockNode);
		analyser.analyse(ast);

		System.out.println(log);
		assertEquals(log.getErrors().size(), 3);
	}

	/**
	 * <pre>
	 * if ( true )
	 *   print bla;
	 * return;
	 * </pre>
	 */
	@Test
	public void testUndeclaredVariableInIfBranch() {
		ASTFactory factory = new ASTFactory();
		BranchNode iff = factory.addBranch(factory.newLiteral("true", new BooleanType()));
		iff.setStatementNodeOnTrue(factory.newPrint(factory.newBasicIdentifier("bla")));
		factory.goToParent();
		factory.addReturn(null);

		AST expected = factory.getAST();
		analyser.analyse(expected);

		assertEquals(1, log.getErrors().size());
		LogEntry entry = log.getErrors().get(0);

		assertEquals(LogEntry.Type.ERROR, entry.getLogType());
		assertEquals(ReportType.UNDECLARED_VARIABLE_USAGE, entry.getReportType());
	}

	/**
	 * <pre>
	 * bla = "bla";
	 * return;
	 * </pre>
	 */
	@Test
	public void testUndeclaredVariableAssign() {
		ASTFactory factory = new ASTFactory();
		factory.addAssignment(factory.newBasicIdentifier("bla"), factory.newLiteral("\"bla\"", new StringType(7L)));
		factory.addReturn(null);

		AST expected = factory.getAST();
		analyser.analyse(expected);

		assertEquals(1, log.getErrors().size());
		LogEntry entry = log.getErrors().get(0);

		assertEquals(LogEntry.Type.ERROR, entry.getLogType());
		assertEquals(ReportType.UNDECLARED_VARIABLE_USAGE, entry.getReportType());
	}
}