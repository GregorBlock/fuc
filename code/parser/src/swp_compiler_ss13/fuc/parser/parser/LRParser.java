package swp_compiler_ss13.fuc.parser.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.apache.log4j.Logger;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.ast.nodes.marynary.BlockNode;
import swp_compiler_ss13.common.lexer.Token;
import swp_compiler_ss13.common.lexer.TokenType;
import swp_compiler_ss13.common.report.ReportLog;
import swp_compiler_ss13.common.report.ReportType;
import swp_compiler_ss13.fuc.ast.ASTImpl;
import swp_compiler_ss13.fuc.parser.grammar.Production;
import swp_compiler_ss13.fuc.parser.grammar.TokenEx;
import swp_compiler_ss13.fuc.parser.parser.states.LRParserState;
import swp_compiler_ss13.fuc.parser.parser.tables.LRParsingTable;
import swp_compiler_ss13.fuc.parser.parser.tables.actions.ALRAction;
import swp_compiler_ss13.fuc.parser.parser.tables.actions.Error;
import swp_compiler_ss13.fuc.parser.parser.tables.actions.Reduce;
import swp_compiler_ss13.fuc.parser.parser.tables.actions.Shift;

public class LRParser {
	// --------------------------------------------------------------------------
	// --- variables and constants
	// ----------------------------------------------
	// --------------------------------------------------------------------------

	private final Logger log = Logger.getLogger(LRParser.class);

	// --------------------------------------------------------------------------
	// --- constructors
	// ---------------------------------------------------------
	// --------------------------------------------------------------------------

	// --------------------------------------------------------------------------
	// --- methods
	// --------------------------------------------------------------
	// --------------------------------------------------------------------------
	public AST parse(LexerWrapper lexer, ReportLog reportLog,
			LRParsingTable table) throws ParserException{
		Stack<LRParserState> parserStack = new Stack<>();

		AST ast = new ASTImpl();
		Stack<Object> valueStack = new Stack<>();

		// Initialization
		ALRAction action = null;
		TokenEx token = lexer.getNextToken();
		parserStack.add(table.getStartState());

		WHILE: while (true) {
			LRParserState state = parserStack.peek();

			TokenType tokenType = token.getTokenType();
			switch (tokenType) {
			case NOT_A_TOKEN:
				List<Token> list = new ArrayList<Token>();
				list.add(token);
				reportLog.reportError(ReportType.UNDEFINED, list,
						"Found undefined token '" + token.getValue() + "'!");
				throw new ParserException("undefined Token found");

			case COMMENT:
				// Skip it silently
				token = lexer.getNextToken();
				continue WHILE;
			}

			action = table.getActionTable().get(state, token.getTerminal());
			if (action == null) {
				log.error("Error in Parsetable occured!");
				throw new ParserException("An Error in Parsetable occured");
			}

			switch (action.getType()) {
			case SHIFT: {
				// Shift state
				Shift shift = (Shift) action;
				parserStack.push(shift.getNewState());

				// Push old token on stack
				valueStack.push(token);
				token = lexer.getNextToken();
			}
			break;

			case REDUCE: {
				// pop reduced states from stack
				Reduce reduce = (Reduce) action;
				for (int i = 1; i <= reduce.getPopCount(); i++) {
					parserStack.pop();
				}

				// +++++++++++++++++++++++++++++++++++
				// get action for reduced production
				Production prod = reduce.getProduction();
				ReduceAction reduceAction = null;
				

				reduceAction = ReduceImpl.getReduceAction(prod, reportLog);
				

				// If there is anything to do on the value stack
				// (There might be no reduce-action for Productions like unary
				// -> factor, e.g.)
				if (reduceAction != null) {
					// Pop all values reduced by this production
					int nrOfValuesReduced = reduce.getPopCount();
					LinkedList<Object> valueHandle = new LinkedList<>();
					for (int i = 0; i < nrOfValuesReduced; i++) {
						valueHandle.addFirst(valueStack.pop());
					}
					
					// Execute reduceAction and push onto the stack
					Object newValue = reduceAction.create(arr(valueHandle));
										
					if (newValue == null) {
						log.error("Error occurred! newValue is null");
						throw new ParserException("Error occurred! newValue is null");
					}
					valueStack.push(newValue);
				}

				// check where to go-to... and push next state on stack
				LRParserState newState = table.getGotoTable().get(parserStack.peek(),
						prod.getLHS());
				if (newState.isErrorState()) {
					List<Token> list = new ArrayList<Token>();
					list.add(token);
					reportLog.reportError(ReportType.UNDEFINED, list, "");
					throw new ParserException("Error state occurred");
				}
				parserStack.push(newState);
			}
			break;

			case ACCEPT: {
				if (tokenType != TokenType.EOF) {
					List<Token> list = new ArrayList<Token>();
					list.add(token);
					reportLog.reportError(ReportType.UNRECOGNIZED_TOKEN, list,"");
					throw new ParserException("End of File expected!");
				} else {
					BlockNode programBlock = (BlockNode) valueStack.pop();
					ast.setRootNode(programBlock);
					return ast;
				}
			}

			case ERROR: {
				Error error = (Error) action;
				List<Token> list = new ArrayList<Token>();
				list.add(token);
				reportLog.reportError(ReportType.UNDEFINED,list,
						"An error occurred: " + error.getMsg());
						throw new ParserException("Get Error State from Actiontable");
			}
			}
		}
	}

	private static Object[] arr(List<Object> objs) {
//		for (Object obj : objs) {
//			if (obj instanceof BlockNode) {
//				BlockNode node = (BlockNode) obj;
//				if (node.getDeclarationList().size() != 0) {
//					System.out.println();
//				}
//			}
//			if (obj instanceof ReturnNode) {
//				System.out.println();
//			}
//		}
		return objs.toArray(new Object[objs.size()]);
	}


}
