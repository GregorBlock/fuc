package swp_compiler_ss13.fuc.parser.grammar;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import swp_compiler_ss13.fuc.parser.parser.IGrammarImpl;

/**
 * This is a helper class for defining {@link Grammar}s directly in Java.<br/>
 * Usage: simply derive from this class and instantiate {@link Terminal}s,
 * {@link NonTerminal}s and {@link Production}s as class members. This class
 * creates the {@link Grammar} automatically. For an example, see
 * {@link ProjectGrammar}
 * 
 * @author Gero
 */
public abstract class AGrammarSpec {
	// --------------------------------------------------------------------------
	// --- variables and constants
	// ----------------------------------------------
	// --------------------------------------------------------------------------
	private Grammar grammar = null;

	// --------------------------------------------------------------------------
	// --- constructors
	// ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @see AGrammarSpec
	 */
	public AGrammarSpec() {
		try {
			List<Terminal> terminals = getAllMembers(Terminal.class);
			List<NonTerminal> nonTerminals = getAllMembers(NonTerminal.class);
			List<Production> productions = getAllMembers(Production.class);
			List<OpAssociativities> opAssociativitiesList = getAllMembers(OpAssociativities.class);
			OpAssociativities associativities = opAssociativitiesList.size() == 0 ?
					new OpAssociativities() : opAssociativitiesList.get(0);
			this.grammar = new Grammar(terminals, nonTerminals, productions, associativities);
		} catch (IllegalArgumentException err) {
			err.printStackTrace();
		} catch (IllegalAccessException err) {
			err.printStackTrace();
		}
	}

	// --------------------------------------------------------------------------
	// --- methods
	// --------------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * Returns a list of all values of a this classes instances of a given type
	 * 
	 * @param clazz
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private <T> List<T> getAllMembers(Class<T> clazz)
			throws IllegalArgumentException, IllegalAccessException {
		List<T> result = new LinkedList<>();
		for (Field field : this.getClass().getDeclaredFields()) {
			boolean access = false;
			if (!field.isAccessible()) {
				access = true;
				field.setAccessible(true);
			}
			Object obj = field.get(this);
			if (clazz.isInstance(obj)) {
				result.add(clazz.cast(obj));
			}
			if (access) {
				field.setAccessible(false);
			}
		}
		return result;
	}
	
	public abstract IGrammarImpl getGrammarImpl();

	// --------------------------------------------------------------------------
	// --- getter/setter
	// --------------------------------------------------------
	// --------------------------------------------------------------------------
	public Grammar getGrammar() {
		return grammar;
	}
}
