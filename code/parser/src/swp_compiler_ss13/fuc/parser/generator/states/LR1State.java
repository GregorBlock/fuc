package swp_compiler_ss13.fuc.parser.generator.states;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import swp_compiler_ss13.fuc.parser.generator.FirstSets;
import swp_compiler_ss13.fuc.parser.generator.GrammarInfo;
import swp_compiler_ss13.fuc.parser.generator.NullableSet;
import swp_compiler_ss13.fuc.parser.generator.items.LR0Item;
import swp_compiler_ss13.fuc.parser.generator.items.LR1Item;
import swp_compiler_ss13.fuc.parser.generator.terminals.ITerminalSet;
import swp_compiler_ss13.fuc.parser.grammar.NonTerminal;
import swp_compiler_ss13.fuc.parser.grammar.Production;
import swp_compiler_ss13.fuc.parser.grammar.Symbol;

/**
 * This class represents a generator state for a LR(1) grammar, and thus consists of a set of LR(1) items. It
 * furthermore holds the {@link #kernel} (LR(0)) state and a map {@link #itemsWithKernels}, which allows to identify
 * the LR(1)-items of this set by their LR(0)-kernels, which is good for speed up, as LR(1)-items change their identity
 * while their {@link LR1Item#getLookaheads()} are constructed. Finally, it has it own implementations of
 * {@link #goTo(Symbol)} and {@link #closure(GrammarInfo)}
 */
public class LR1State extends ALRState<LR1Item> {
	// --------------------------------------------------------------------------
	// --- variables and constants
	// ----------------------------------------------
	// --------------------------------------------------------------------------
	private final LR0State kernel;
	private final LinkedHashMap<LR0Item, LR1Item> itemsWithKernels;

	// cache
	private final int hashCode;
	private LR1State closureCache = null;

	// --------------------------------------------------------------------------
	// --- constructors
	// ---------------------------------------------------------
	// --------------------------------------------------------------------------
	/**
	 * @see LR1State
	 * @param items
	 */
	public LR1State(LinkedHashSet<LR1Item> items) {
		super(items);

		// Extract kernel and calc hash once
		int tmpHashCode = 0;
		itemsWithKernels = new LinkedHashMap<>();
		for (LR1Item item : items) {
			itemsWithKernels.put(item.getLR0Kernel(), item);
			tmpHashCode += item.hashCode();
		}
		kernel = new LR0State(itemsWithKernels.keySet());
		hashCode = tmpHashCode;
	}
	
	/**
	 * @see LR1State
	 * @param items
	 */
	public LR1State(LR1Item... items) {
		super(items);

		// Extract kernel and calc hash once
		int tmpHashCode = 0;
		itemsWithKernels = new LinkedHashMap<>();
		for (LR1Item item : items) {
			itemsWithKernels.put(item.getLR0Kernel(), item);
			tmpHashCode += item.hashCode();
		}
		kernel = new LR0State(itemsWithKernels.keySet());
		hashCode = tmpHashCode;
	}
	

	// --------------------------------------------------------------------------
	// --- methods
	// --------------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public LR1State goTo(Symbol symbol) {
		LinkedHashSet<LR1Item> ret = new LinkedHashSet<>();

		// Find all items where the given symbol follows and add them shifted
		for (LR1Item item : items) {
			if (item.getNextSymbol() == symbol) {
				ret.add(item.shift());
			}
		}

		return new LR1State(ret);
	}

	@Override
	public LR1State closure(GrammarInfo grammarInfo) {
		if (closureCache == null)
			closureCache = new LR1State(closureItems(grammarInfo));
		return closureCache;
	}

	/**
	 * Returns the closure of the {@link LR1Item}s of this state as set of
	 * {@link LR1Item}s. For a description of this algorithm, see Modern
	 * Compiler Implementation for Java, page 63.
	 */
	private LinkedHashSet<LR1Item> closureItems(GrammarInfo grammarInfo) {
		// The closure contains all items of the source...
		LinkedHashMap<LR0Item, ITerminalSet> items = new LinkedHashMap<>();
		for (LR1Item item : this.items) {
			items.put(item.getLR0Kernel(), item.getLookaheads());
		}

		/*
		 * Closure(I)=
		 * 	repeat
		 * 		forany item(A → α.Xβ,z)in I
		 * 			forany production X → γ
		 * 				forany w ϵ FIRST(βz)
		 * 					I ← I join {(X → .γ , w)}
		 * 	until I does not change
		 * return I
		 */
		LinkedList<LR1Item> queue = new LinkedList<LR1Item>();
		queue.addAll(this.items);
		FirstSets firstSets = grammarInfo.getFirstSets();
		NullableSet nullableSet = grammarInfo.getNullableSet();
		while (!queue.isEmpty()) {
			LR1Item item = queue.removeFirst(); // item is A → α.Xβ with
												// lookahead z"
			Symbol nextSymbol = item.getNextSymbol();
			if (nextSymbol != null && nextSymbol.isNonTerminal()) { // nextSymbol is "X"
				ITerminalSet firstSet = item.getNextLookaheads(firstSets,
						nullableSet); // all "w"s
				for (Production p : grammarInfo
						.getProductionsFrom((NonTerminal) nextSymbol)) // p is
																		// "X → γ"
				{
					LR0Item newItemLR0 = new LR0Item(p, 0);
					// look, if there is already a LR(1) item with that LR(0)
					// kernel. if so, add the
					// new lookahead symbols there. If not, add the LR(1) item
					// to the result set.
					ITerminalSet sameKernelItemLookaheads = items
							.get(newItemLR0);
					if (sameKernelItemLookaheads != null) {
						// add to existing LR1Item
						ITerminalSet newLookaheads = sameKernelItemLookaheads
								.plusAll(firstSet);
						// if new lookahead was found, add again to queue
						if (!newLookaheads.equals(sameKernelItemLookaheads)) {
							items.put(newItemLR0, newLookaheads);
							queue.add(new LR1Item(newItemLR0, newLookaheads));
						}
					} else {
						// new item
						items.put(newItemLR0, firstSet);
						queue.add(new LR1Item(newItemLR0, firstSet));
					}
				}
			}
		}
		
		// Collect resulting LR(1) items
		LinkedHashSet<LR1Item> result = new LinkedHashSet<LR1Item>();
		for (LR0Item lr0Item : items.keySet()) {
			result.add(new LR1Item(lr0Item.getProduction(), lr0Item.getPosition(),
					items.get(lr0Item)));
		}
		return result;
	}
	

	// --------------------------------------------------------------------------
	// --- getter/setter
	// --------------------------------------------------------
	// --------------------------------------------------------------------------
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LR1State) {
			LR1State s = (LR1State) obj;
			if (items.size() != s.items.size())
				return false;
			for (LR1Item item : items) {
				if (!s.items.contains(item))
					return false;
			}
			return true;
		}
		return false;
	}

	public LR0State getKernel() {
		return kernel;
	}
}
