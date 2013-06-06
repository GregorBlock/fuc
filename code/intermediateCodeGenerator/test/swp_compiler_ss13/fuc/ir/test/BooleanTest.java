package swp_compiler_ss13.fuc.ir.test;

import java.util.List;

import junit.extensions.PA;

import org.junit.Before;
import org.junit.Test;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.ast.nodes.binary.BinaryExpressionNode.BinaryOperator;
import swp_compiler_ss13.common.backend.Quadruple;
import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
import swp_compiler_ss13.common.types.primitive.BooleanType;
import swp_compiler_ss13.fuc.ast.ASTFactory;
import swp_compiler_ss13.fuc.ir.IntermediateCodeGeneratorImpl;
import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;

public class BooleanTest {

	private AST ast;

	@Before
	public void setUp() throws Exception {
		PA.setValue(SymbolTableImpl.class, "ext", 0);
		ASTFactory astf = new ASTFactory();
		astf.addDeclaration("b", new BooleanType());
		astf.addDeclaration("c", new BooleanType());

		astf.addAssignment(astf.newBasicIdentifier("b"),
				astf.newLiteral("true", new BooleanType()));
		astf.addAssignment(astf.newBasicIdentifier("c"),
				astf.newLiteral("false", new BooleanType()));

		astf.addAssignment(
				astf.newBasicIdentifier("b"),
				astf.newBinaryExpression(BinaryOperator.LOGICAL_AND,
						astf.newBasicIdentifier("b"),
						astf.newBasicIdentifier("c")));

		ast = astf.getAST();
	}

	@Test
	public void testboolean() throws IntermediateCodeGeneratorException {
		IntermediateCodeGeneratorImpl irg = new IntermediateCodeGeneratorImpl();
		List<Quadruple> irc = irg.generateIntermediateCode(ast);

		StringBuilder actual = new StringBuilder();
		for (Quadruple q : irc) {
			actual.append(String.format("(%s|%s|%s|%s)\n", q.getOperator(),
					q.getArgument1(), q.getArgument2(), q.getResult()));
		}
		System.out.println(actual);
	}

}
