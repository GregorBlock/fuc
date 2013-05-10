package swp_compiler_ss13.fuc.parser.parseTableGenerator.test;

import static org.junit.Assert.*;

//import java.io.FileInputStream;
//import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import swp_compiler_ss13.fuc.parser.parseTableGenerator.Grammar;
import swp_compiler_ss13.fuc.parser.parseTableGenerator.Production;
import swp_compiler_ss13.fuc.parser.parseTableGenerator.Symbol;
import swp_compiler_ss13.fuc.parser.parseTableGenerator.Terminal;
import swp_compiler_ss13.fuc.parser.parseTableGenerator.Variable;
import swp_compiler_ss13.fuc.parser.parseTableGenerator.WrongGrammarFormatException;

public class GrammarTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testReadFromFile() {
		//fail("Not yet implemented");
		StringReader file = new StringReader(testGrammar);
		Grammar grammar = new Grammar();
		try {
			grammar.readFromFile(file);
		}
		catch(WrongGrammarFormatException e)
		{
			file.close();
			fail("WrongGrammarFormatException while parsing the grammar, namely:\n"
					+ e.getMessage()
			);
		}
		catch(IOException e)
		{
			file.close();
			fail("IOException while parsing the grammar!");
		}
		// manually create a production:
		List<Symbol> rightSide = new ArrayList<Symbol>();
		rightSide.add(new Variable("Expr"));
		rightSide.add(new Terminal("+"));
		rightSide.add(new Variable("Term"));
		Production prod0 = new Production(new Variable("Expr"), rightSide);
		rightSide.set(1, new Terminal("-"));
		Production prod1 = new Production(new Variable("Expr"), rightSide);
		file.close();
		assertTrue(
				"Production \"Expr -> Expr + Term\" should have been added",
				grammar.getProductions().get(0).getLeft().compareTo(prod0.getLeft()) == 0
		);
		assertTrue(
				"Production \"Expr -> Expr - Term\" should have been added",
				grammar.getProductions().get(1).getLeft().compareTo(prod1.getLeft()) == 0
		);
	}
	private static String testGrammar = 
			"symbols:\n" +
			"num,real,+,-,*,/\n" +
			"variables:\n" +
			"Expr,Term,Fac\n" +
			"productions:\n" +
			"Expr:\n" +
			"Expr + Term\n" +
			"Expr - Term\n" +
			"Term\n" +
			"Term:\n" +
			"Term * Fac\n" +
			"Term / Fac\n" +
			"Fac\n" +
			"Fac:\n" +
			"num\n" +
			"real\n";
}
