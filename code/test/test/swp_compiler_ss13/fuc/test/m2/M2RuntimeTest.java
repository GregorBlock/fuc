package swp_compiler_ss13.fuc.test.m2;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assume;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import swp_compiler_ss13.common.test.ExampleProgs;
import swp_compiler_ss13.fuc.test.Compiler;
import swp_compiler_ss13.fuc.test.TestBase;

/**
 * <p>
 * Runtime tests for the M2 examples.
 * </p>
 * <p>
 * The runtime tests check for results (return values and output) of the
 * execution of the translated examples. The tests require a LLVM installation
 * for executing the LLVM IR. All tests are ignored if no <code>lli</code> is
 * found.
 * </p>
 * <p>
 * All example progs can be found in {@link ExampleProgs}.
 * </p>
 * 
 * @author Jens V. Fischer
 */
public class M2RuntimeTest extends TestBase {

	private static Logger logger = Logger.getLogger(M2RuntimeTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Logger.getRootLogger().setLevel(Level.ERROR);

		/* only run tests if lli (dynamic compiler from LLVM) is found */
		Assume.assumeTrue(checkForLLIInstallation());
	}

	@Before
	public void setUp() throws Exception {
		compiler = new Compiler();
	}

	@Test
	public void testAssignmentProg() throws Exception {
		testProgRuntime(ExampleProgs.assignmentProg());
	}

	@Test
	public void testCondProg() throws Exception {
		testProgRuntime(ExampleProgs.condProg());
	}

	@Test
	public void testPrintProg() throws Exception {
		testProgRuntime(ExampleProgs.printProg());
	}

}
