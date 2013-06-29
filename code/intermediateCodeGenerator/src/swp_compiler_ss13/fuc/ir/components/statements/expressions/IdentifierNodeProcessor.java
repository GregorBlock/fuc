package swp_compiler_ss13.fuc.ir.components.statements.expressions;

import swp_compiler_ss13.common.ast.nodes.leaf.BasicIdentifierNode;
import swp_compiler_ss13.common.ast.nodes.unary.ArrayIdentifierNode;
import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
import swp_compiler_ss13.common.types.Type;
import swp_compiler_ss13.fuc.ir.ArrayHelper;
import swp_compiler_ss13.fuc.ir.GeneratorExecutor;
import swp_compiler_ss13.fuc.ir.GeneratorState;
import swp_compiler_ss13.fuc.ir.QuadrupleFactory;
import swp_compiler_ss13.fuc.ir.components.NodeProcessor;
import swp_compiler_ss13.fuc.ir.data.IntermediateResult;

/**
 * Processor for a block node
 * 
 * @author "Frank Zechert"
 * @author "Danny Maasch"
 * @author kaworu
 * @version 3
 */
public class IdentifierNodeProcessor extends NodeProcessor {

	/**
	 * Create a new instance of the block node processor.
	 * 
	 * @param state
	 *            the state of the intermediate code generator
	 * @param executor
	 *            the intermediate code generator executor
	 */
	public IdentifierNodeProcessor(GeneratorState state, GeneratorExecutor executor) {
		super(state, executor);
	}

	/**
	 * Process the basic identifier node.
	 * 
	 * @param node
	 *            the basic identifier node to process.
	 * @throws IntermediateCodeGeneratorException
	 *             The given identifier is undeclared
	 */

	public void processBasicIdentifier(BasicIdentifierNode node)
			throws IntermediateCodeGeneratorException {
		// A basic identifier can be pushed to the stack of results immediately
		String identifier = node.getIdentifier();
		Type identifierType = this.state.getCurrentSymbolTable().lookupType(identifier);

		// if the identifier was renamed due to single static assignment
		// get the renamed identifier name
		String actualIdentifier = this.state.loadIdentifier(identifier);
		this.state.pushIntermediateResult(identifierType, actualIdentifier);
	}

	/**
	 * Process the array identifier node.
	 * 
	 * @param node
	 *            the array identifier node to process.
	 * @throws IntermediateCodeGeneratorException
	 *             The given identifier is undeclared
	 */

	public void processArrayIdentifier(ArrayIdentifierNode node)
			throws IntermediateCodeGeneratorException {

		IntermediateResult index = this.executor.process(node.getIndexNode());
		String indexValue = index.getValue();

		IntermediateResult base = this.executor.process(node.getIdentifierNode());
		String baseValue = base.getValue();
		Type arrayType = base.getType();

		Type baseType = ArrayHelper.getBaseType(arrayType);
		boolean isOuterMostDimension = ArrayHelper.isOuterMostDimension(node);
		if (isOuterMostDimension) {
			// we are at the outermost dimension of the array
			boolean isTargetOfAssignment = ArrayHelper.isTargetOfAssignment(node);
			if (isTargetOfAssignment) {
				// the value of the array is not used, but a new value
				// is assigned to this array.
				// now we need to use one of the array_set_{TYPE} methods
				// but this needs to be done by the assignmentNodeProcessor
				// because we do not know the value that is assigned to us
				// already
				// we need to push the intermediate result back to the stack
				// of intermediate results to make it available in
				// assignmentNodeProcessor
				NodeProcessor.logger
						.debug("Pushing back intermediate results, beacuse this array is on the target side of an assignment");
				this.state.pushIntermediateResult(index);
				this.state.pushIntermediateResult(base);
			}
			else {
				// the value of the array is used somewhere
				// now we need to use one of the array_get_{TYPE} methods
				String tmp = this.state.nextTemporaryIdentifier(baseType);
				this.state.addIntermediateCode(QuadrupleFactory.declare(tmp, baseType));
				this.state.addIntermediateCode(QuadrupleFactory.arrayGetType(baseType, baseValue, indexValue, tmp));
				this.state.pushIntermediateResult(baseType, tmp);
			}
		}
		else {
			// we are not at the outermost dimension of the array
			// use ARRAY_GET_REFERENCE to get a reference to this dimension

			// XXX: baseType is actually incorrect here. But there is no
			// XXX: ReferenceType in the interface.
			// XXX: this will work, because no body checks this type later on.
			String tmp = this.state.nextTemporaryIdentifier(baseType);
			this.state.addIntermediateCode(QuadrupleFactory.declareReference(tmp));
			this.state.addIntermediateCode(QuadrupleFactory.arrayReference(baseValue, indexValue, tmp));
			this.state.pushIntermediateResult(arrayType, tmp);
		}
	}
}
