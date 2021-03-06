package swp_compiler_ss13.fuc.ir.test;

import static org.junit.Assert.assertEquals;

import java.util.List;

import junit.extensions.PA;

import org.junit.Before;
import org.junit.Test;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.backend.Quadruple;
import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
import swp_compiler_ss13.common.types.primitive.BooleanType;
import swp_compiler_ss13.common.types.primitive.LongType;
import swp_compiler_ss13.common.types.primitive.StringType;
import swp_compiler_ss13.fuc.ast.ASTFactory;
import swp_compiler_ss13.fuc.ir.IntermediateCodeGeneratorImpl;
import swp_compiler_ss13.fuc.symbolTable.SymbolTableImpl;

public class IdentifierShadowTest {

	private AST ast;

	@Before
	public void setUp() throws IntermediateCodeGeneratorException {
		PA.setValue(SymbolTableImpl.class, "ext", 0);
		ASTFactory astf = new ASTFactory();
		astf.addDeclaration("shadow", new BooleanType());
		astf.addAssignment(astf.newBasicIdentifier("shadow"),
				astf.newLiteral("true", new BooleanType()));
		astf.addPrint(astf.newBasicIdentifier("shadow"));
		astf.addBlock();
		astf.addDeclaration("shadow", new LongType());
		astf.addAssignment(astf.newBasicIdentifier("shadow"),
				astf.newLiteral("25", new LongType()));
		astf.addPrint(astf.newBasicIdentifier("shadow"));
		astf.addBlock();
		astf.addDeclaration("shadow", new StringType(10L));
		astf.addAssignment(astf.newBasicIdentifier("shadow"),
				astf.newLiteral("\"Hallo \\\" Welt\"", new StringType(10L)));
		astf.addPrint(astf.newBasicIdentifier("shadow"));
		astf.goToParent();
		astf.addPrint(astf.newBasicIdentifier("shadow"));
		astf.goToParent();
		astf.addPrint(astf.newBasicIdentifier("shadow"));
		this.ast = astf.getAST();
	}

	@Test
	public void test() throws IntermediateCodeGeneratorException {
		IntermediateCodeGeneratorImpl irg = new IntermediateCodeGeneratorImpl();
		List<Quadruple> irc = irg.generateIntermediateCode(this.ast);

		StringBuilder b = new StringBuilder();
		for (Quadruple q : irc) {
			b.append(String.format("(%s|%s|%s|%s)\n", q.getOperator(),
					q.getArgument1(), q.getArgument2(), q.getResult()));
		}
		String actual = b.toString();

		String expected = "(DECLARE_BOOLEAN|!|!|shadow)" + "\n" +
				"(ASSIGN_BOOLEAN|#TRUE|!|shadow)" + "\n" +
				"(DECLARE_STRING|!|!|tmp0)" + "\n" +
				"(BOOLEAN_TO_STRING|shadow|!|tmp0)" + "\n" +
				"(PRINT_STRING|tmp0|!|!)" + "\n" +
				"(DECLARE_LONG|!|!|tmp1)" + "\n" +
				"(ASSIGN_LONG|#25|!|tmp1)" + "\n" +
				"(DECLARE_STRING|!|!|tmp2)" + "\n" +
				"(LONG_TO_STRING|tmp1|!|tmp2)" + "\n" +
				"(PRINT_STRING|tmp2|!|!)" + "\n" +
				"(DECLARE_STRING|!|!|tmp3)" + "\n" +
				"(ASSIGN_STRING|#\"Hallo \\\" Welt\"|!|tmp3)" + "\n" +
				"(PRINT_STRING|tmp3|!|!)" + "\n" +
				"(DECLARE_STRING|!|!|tmp4)" + "\n" +
				"(LONG_TO_STRING|tmp1|!|tmp4)" + "\n" +
				"(PRINT_STRING|tmp4|!|!)" + "\n" +
				"(DECLARE_STRING|!|!|tmp5)" + "\n" +
				"(BOOLEAN_TO_STRING|shadow|!|tmp5)" + "\n" +
				"(PRINT_STRING|tmp5|!|!)" + "\n";
		System.out.println(actual);
		assertEquals(expected, actual);
	}

}
