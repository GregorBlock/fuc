package swp_compiler_ss13.fuc.test.m2;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import swp_compiler_ss13.common.backend.Backend;
import swp_compiler_ss13.common.backend.BackendException;
import swp_compiler_ss13.common.ir.IntermediateCodeGenerator;
import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
import swp_compiler_ss13.common.lexer.Lexer;
import swp_compiler_ss13.common.parser.Parser;
import swp_compiler_ss13.common.test.ExampleProgs;
import swp_compiler_ss13.fuc.backend.LLVMBackend;
import swp_compiler_ss13.fuc.ir.IntermediateCodeGeneratorImpl;
import swp_compiler_ss13.fuc.lexer.LexerImpl;
import swp_compiler_ss13.fuc.parser.ParserImpl;
import swp_compiler_ss13.fuc.semantic_analyser.SemanticAnalyser;
import swp_compiler_ss13.fuc.test.*;
import swp_compiler_ss13.fuc.test.Compiler;
import swp_compiler_ss13.javabite.backend.BackendJb;
import swp_compiler_ss13.javabite.codegen.IntermediateCodeGeneratorJb;
import swp_compiler_ss13.javabite.lexer.LexerJb;
import swp_compiler_ss13.javabite.parser.ParserJb;
import swp_compiler_ss13.javabite.semantic.SemanticAnalyserJb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <p>
 * The cross tests test for the interchangeability of the fuc and the javabite
 * modules. All 32 possible combinations of the lexer, parser, semantic analyser
 * and backend are tests.
 * </p>
 * <p>
 * For the tests to work, the javabite moduls have to be placed into the
 * fuc/code/dist <code>fuc/code/dist/</code> directory as jar-files. The
 * javabite jars can be obtained be running <code>ant buildCompiler</code> in
 * the javabite repository.
 * </p>
 * <p>
 * The cross test are compilation tests, i.e. the test if the example program
 * compiles through all stages of the compiler, producing some kind of target
 * language code in the end. These tests only test if the compiler runs through
 * without producing errors, not for the correctness of the resulting target
 * language code.
 * </p>
 * <p>
 * All example progs can be found in {@link ExampleProgs}.
 * </p>
 * 
 * @author Jens V. Fischer
 */
@RunWith(Parameterized.class)
public class M2CrossTest extends TestBase {

	private static Logger logger = Logger.getLogger(M2CrossTest.class);
	private static ServiceLoader<Lexer> lexerService;

	private Class lexerToUse;
	private Class parserToUse;
	private Class analyserToUse;
	private Class irgenToUse;
	private Class backToUse;

	private static Lexer lexer;
	private static Parser parser;
	private static swp_compiler_ss13.common.semanticAnalysis.SemanticAnalyser analyser;
	private static IntermediateCodeGenerator irgen;
	private static Backend backend;


	public M2CrossTest(String testname, Class lexerToUse, Class parserToUse, Class analyserToUse, Class irgenToUse, Class backToUse) {
		this.lexerToUse = lexerToUse;
		this.parserToUse = parserToUse;
		this.analyserToUse = analyserToUse;
		this.irgenToUse = irgenToUse;
		this.backToUse = backToUse;
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Logger.getRootLogger().setLevel(Level.ERROR);

		/* only run tests if lli (dynamic compiler from LLVM) is found */
		Assume.assumeTrue(checkForLLIInstallation());
	}

	@Parameterized.Parameters(name= "{index}: {0}")
	public static Collection<Object[]> data() {
		Class[] lexerClasses = new Class[]{LexerJb.class, LexerImpl.class};
		Class[] parserClasses = new Class[]{ParserJb.class, ParserImpl.class};
		Class[] analyserClasses = new Class[]{SemanticAnalyserJb.class, SemanticAnalyser.class};
		Class[] irgenClasses = new Class[]{IntermediateCodeGeneratorJb.class, IntermediateCodeGeneratorImpl.class};
		Class[] backendClasses = new Class[]{BackendJb.class, LLVMBackend.class};
		ArrayList classes = new ArrayList();
		for (Class lexer : lexerClasses) {
			for (Class parser : parserClasses) {
				for (Class analyzer : analyserClasses) {
					for (Class irgen : irgenClasses) {
						for (Class backend : backendClasses) {
							String testname = lexer.getSimpleName() + "->" + parser.getSimpleName() + "->" + analyzer.getSimpleName() + "->" + irgen.getSimpleName() + "->" + backend.getSimpleName();
							classes.add(new Object[]{testname, lexer, parser, analyzer, irgen, backend});
						}

					}
				}
			}
		}

		return classes;
	}

	@Before
	public void setUp() throws Exception {

		lexer = (Lexer) getModule(Lexer.class, lexerToUse);
		parser = (Parser) getModule(Parser.class, parserToUse);
		analyser = (swp_compiler_ss13.common.semanticAnalysis.SemanticAnalyser) getModule(swp_compiler_ss13.common.semanticAnalysis.SemanticAnalyser.class, analyserToUse);
		irgen = (IntermediateCodeGenerator) getModule(IntermediateCodeGenerator.class, irgenToUse);
		backend = (Backend) getModule(Backend.class, backToUse);

		Assume.assumeTrue("no lexer found, aborting", lexer != null);
		Assume.assumeTrue("no parser found, aborting", parser != null);
		Assume.assumeTrue("no semantic analyser found, aborting", analyser != null);
		Assume.assumeTrue("no irgen found, aborting", irgen != null);
		Assume.assumeTrue("no lexer found, aborting", backend != null);
	}

	/* M1 progs */
	
	@Test
	public void testSimpleAddProg() throws IOException, InterruptedException, BackendException, IntermediateCodeGeneratorException {
		testProgCompilation(ExampleProgs.simpleAddProg());
	}

	@Test
	public void testAddProg() throws IOException, InterruptedException, BackendException, IntermediateCodeGeneratorException {
		testProgCompilation(ExampleProgs.addProg());
	}

	@Test
	public void testSimpleMulProg() throws IOException, InterruptedException, BackendException, IntermediateCodeGeneratorException {
		testProgCompilation(ExampleProgs.simpleMulProg());
	}

	@Test
	public void testParenthesesProg() throws IOException, InterruptedException, BackendException, IntermediateCodeGeneratorException {
		testProgCompilation(ExampleProgs.parenthesesProg());
	}

	/* M1 progs producing errors */
	
	@Test
	public void testDoubleDeclaration() throws Exception {
		testProgCompilation(ExampleProgs.doubleDeclaration());
	}

	@Test
	public void testInvalidIds() throws Exception {
		testProgCompilation(ExampleProgs.invalidIds());
	}

	@Test
	public void testMultipleMinusENotation() throws Exception {
		testProgCompilation(ExampleProgs.multipleMinusENotation());
	}

	@Test
	public void testMultiplePlusesInExp() throws Exception {
		testProgCompilation(ExampleProgs.multiplePlusesInExp());
	}

	@Test
	public void testUndefReturn() throws Exception {
		testProgCompilation(ExampleProgs.undefReturnProg());
	}

	/* M2 progs */

	@Test
	public void testAssignmentProg() throws Exception {
		testProgCompilation(ExampleProgs.assignmentProg());
	}

	@Test
	public void testCondProg() throws Exception {
		testProgCompilation(ExampleProgs.condProg());
	}

	@Test
	public void testPrintProg() throws Exception {
		testProgCompilation(ExampleProgs.printProg());
	}

	/* M2: additional progs */

	/* regression test against return bug */
	@Test
	public void testReturnProg() throws Exception {
		testProgCompilation(ExampleProgs.returnProg());
	}

	@Test
	public void testArrayProg1() throws Exception {
		testProgCompilation(ExampleProgs.arrayProg1());
	}

	@Test
	public void testArrayProg2() throws Exception {
		testProgCompilation(ExampleProgs.arrayProg2());
	}

	private Object getModule(Class moduleClass, Class implClass){
		ServiceLoader serviceLoader = ServiceLoader.load(moduleClass);
		Iterator iterator = serviceLoader.iterator();

		while (iterator.hasNext()) {
			Object module = iterator.next();
			if (module.getClass().equals(implClass)) {
				logger.info("ToDo");
				return module;
			}
		}
		return null;
	}
}