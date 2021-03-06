package swp_compiler_ss13.fuc.ast;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import swp_compiler_ss13.common.ast.ASTNode;
import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
import swp_compiler_ss13.common.ast.nodes.StatementNode;
import swp_compiler_ss13.common.ast.nodes.ternary.BranchNode;

/**
 * BranchNode implementation
 * 
 * @author "Frank Zechert, Danny Maasch"
 * @version 1
 */
public class BranchNodeImpl extends ASTNodeImpl implements BranchNode {

	/**
	 * The expression to evaluate
	 */
	private ExpressionNode condition;

	/**
	 * The block when the expression evaluates to true
	 */
	private StatementNode trueBlock;

	/**
	 * The block when the expression evaluates to false
	 */
	private StatementNode falseBlock;

	/**
	 * The logger
	 */
	private static Logger logger = Logger.getLogger(BranchNodeImpl.class);

	@Override
	public ASTNodeType getNodeType() {
		return ASTNodeType.BranchNode;
	}

	@Override
	public List<ASTNode> getChildren() {
		List<ASTNode> children = new LinkedList<>();
		if (this.condition != null) {
			children.add(this.condition);
		}
		if (this.trueBlock != null) {
			children.add(this.trueBlock);
		}
		if (this.falseBlock != null) {
			children.add(this.falseBlock);
		}
		return children;
	}

	@Override
	public void setCondition(ExpressionNode condition) {
		if (condition == null) {
			logger.error("The argument condition can not be null!");
			throw new IllegalArgumentException("The argument condition can not be null!");
		}
		
		condition.setParentNode(this);
		
		this.condition = condition;
	}

	@Override
	public ExpressionNode getCondition() {
		if (this.condition == null) {
			logger.warn("Returning null as a condition");
		}
		return this.condition;
	}

	@Override
	public void setStatementNodeOnTrue(StatementNode block) {
		if (block == null) {
			logger.error("The argument block can not be null!");
			throw new IllegalArgumentException("The argument block can not be null!");
		}
		
		block.setParentNode(this);
		
		this.trueBlock = block;
	}

	@Override
	public StatementNode getStatementNodeOnTrue() {
		if (this.trueBlock == null) {
			logger.warn("Returning null as a block");
		}
		return this.trueBlock;
	}

	@Override
	public void setStatementNodeOnFalse(StatementNode block) {
		if (block == null) {
			logger.error("The argument block can not be null!");
			throw new IllegalArgumentException("The argument block can not be null!");
		}
		
		block.setParentNode(this);
		
		this.falseBlock = block;
	}

	@Override
	public StatementNode getStatementNodeOnFalse() {
		if (this.falseBlock == null) {
			logger.warn("Returning null as a block");
		}
		return this.falseBlock;
	}

}
