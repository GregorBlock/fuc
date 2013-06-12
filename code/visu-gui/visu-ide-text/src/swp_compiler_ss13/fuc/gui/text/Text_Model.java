package swp_compiler_ss13.fuc.gui.text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import swp_compiler_ss13.common.ast.AST;
import swp_compiler_ss13.common.backend.Quadruple;
import swp_compiler_ss13.common.lexer.Token;
import swp_compiler_ss13.fuc.gui.ide.mvc.Controller;
import swp_compiler_ss13.fuc.gui.ide.mvc.IDE;
import swp_compiler_ss13.fuc.gui.ide.mvc.Model;

/**
 * 
 * Model of {@link IDE} to convert the <strong>source code</strong>, <strong>
 * {@link Token}</strong>, the <strong>{@link AST}</strong>, <strong>
 * {@link Quadruple}</strong> or the <strong>target</strong> to displayable text
 * in the {@link IDE}
 * 
 * @author "Eduard Wolf"
 * 
 */
public abstract class Text_Model implements Model {

	private Text_Controller controller;
	private List<ModelType> types;
	private final Map<ModelType, List<StringColourPair>> viewInformation;

	/**
	 * @param controller
	 *            of model
	 * @param types
	 *            that are represented by this model
	 */
	public Text_Model(Text_Controller controller, ModelType... types) {
		this.controller = controller;
		this.types = Arrays.asList(types);
		viewInformation = new HashMap<>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Controller getController() {
		return controller;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setSourceCode(String sourceCode) {
		if (types.contains(ModelType.SOURCE_CODE)) {
			viewInformation.put(ModelType.SOURCE_CODE, sourceCodeToViewInformation(sourceCode));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * changes the sourcecode to a representation for the {@link Text_View}
	 * 
	 * @param sourceCode
	 *            of program
	 * @return representation for {@link Text_View}
	 */
	protected List<StringColourPair> sourceCodeToViewInformation(String sourceCode) {
		return Arrays.asList(new StringColourPair().setText(sourceCodeToString(sourceCode)));
	}

	protected String sourceCodeToString(String sourceCode) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setTokens(List<Token> tokens) {
		if (types.contains(ModelType.TOKEN)) {
			viewInformation.put(ModelType.TOKEN, tokenToViewInformation(tokens));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * changes the tokens to a representation for the {@link Text_View}
	 * 
	 * @param tokens
	 *            from lexer
	 * @return representation for {@link Text_View}
	 */
	protected List<StringColourPair> tokenToViewInformation(List<Token> tokens) {
		List<StringColourPair> result;
		if (tokens == null) {
			result = new ArrayList<>(1);
			result.add(tokenToViewInformation((Token) null));
		} else {
			result = new ArrayList<>(tokens.size());
			for (Token token : tokens) {
				result.add(tokenToViewInformation(token));
			}
		}
		return result;
	}

	/**
	 * changes the token to a representation for the {@link Text_View}
	 * 
	 * @param token
	 *            from lexer
	 * @return representation for {@link Text_View}
	 */
	protected StringColourPair tokenToViewInformation(Token token) {
		return new StringColourPair().setText(tokenToString(token));
	}

	protected String tokenToString(Token token) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setAST(AST ast) {
		if (types.contains(ModelType.AST)) {
			viewInformation.put(ModelType.AST, astToViewInformation(ast));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * changes the ast to a representation for the {@link Text_View}
	 * 
	 * @param ast
	 *            from parser or semantic analyser
	 * @return representation for {@link Text_View}
	 */
	protected List<StringColourPair> astToViewInformation(AST ast) {
		return Arrays.asList(new StringColourPair().setText(astToString(ast)));
	}

	protected String astToString(AST ast) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setTAC(List<Quadruple> tac) {
		if (types.contains(ModelType.TAC)) {
			viewInformation.put(ModelType.TAC, tacToViewInformation(tac));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * changes the quadrupeles to a representation for the {@link Text_View}
	 * 
	 * @param tac
	 *            from three address code generator
	 * @return representation for {@link Text_View}
	 */
	protected List<StringColourPair> tacToViewInformation(List<Quadruple> tac) {
		List<StringColourPair> result;
		if (tac == null) {
			result = new ArrayList<>(1);
			result.add(tacToViewInformation((Quadruple) null));
		} else {
			result = new ArrayList<>(tac.size());
			for (Quadruple quadruple : tac) {
				result.add(tacToViewInformation(quadruple));
			}
		}
		return result;
	}

	/**
	 * changes the quadrupele to a representation for the {@link Text_View}
	 * 
	 * @param quadruple
	 *            from three address code generator
	 * @return representation for {@link Text_View}
	 */
	protected StringColourPair tacToViewInformation(Quadruple quadruple) {
		return new StringColourPair().setText(tacToString(quadruple));
	}

	protected String tacToString(Quadruple tac) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean setTargetCode(Map<String, InputStream> target) {
		if (types.contains(ModelType.TARGET)) {
			viewInformation.put(ModelType.TARGET, targetToViewInformation(target));
			return true;
		} else {
			return false;
		}
	}

	/**
	 * changes the target to a representation for the {@link Text_View}
	 * 
	 * @param target
	 *            from three target code generator
	 * @return representation for {@link Text_View}
	 */
	protected List<StringColourPair> targetToViewInformation(Map<String, InputStream> target) {
		return Arrays.asList(new StringColourPair().setText(targetToString(target)));
	}

	protected String targetToString(Map<String, InputStream> target) {
		throw new UnsupportedOperationException();
	}

	public List<StringColourPair> getViewInformation(ModelType type) {
		if (type == null) {
			throw new NullPointerException("type cannot be null");
		}
		return viewInformation.get(type);
	}

	protected enum ModelType {
		SOURCE_CODE, TOKEN, AST, TAC, TARGET;
	}

}