package swp_compiler_ss13.fuc.backend;

import swp_compiler_ss13.common.backend.BackendException;
import swp_compiler_ss13.common.backend.Quadruple;

import swp_compiler_ss13.common.types.*;
import swp_compiler_ss13.common.types.primitive.*;
import swp_compiler_ss13.common.types.derived.*;

import java.io.PrintWriter;
import java.util.*;
import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import static swp_compiler_ss13.common.types.Type.*;
import static swp_compiler_ss13.common.types.Type.Kind.BOOLEAN;

/**
 * This class allows for the generation of an LLVM IR module.
 * Each methode that begins with "add" generates LLVM IR code
 * and writes it to the <code>PrintWriter</code> <code>out</code>.
 *
 */
public class Module
{
	/**
	 * A list of all the string literals that
	 * exist in this module where each element
	 * describes a string literal's number with its
	 * position in the list and that string literal's
	 * length with the value of the element.
	 *
	 */
	private ArrayList<Integer> stringLiterals;

	/**
	 * This maps a variable's name (not its
	 * identifier, which is %{name}) to the
	 * number of times it has been used in
	 * the module already.
	 * This is necessary because LLVM's IR
	 * allows only for static single assignment
	 * (SSA), wich means we have to store the
	 * variable's actual value as the destination
	 * of a static pointer and generate a new
	 * 'use' variable (%{name}.{use_number})
	 * for every time the variable is read
	 * from or written to and route the pointer
	 * access through that use variable.
	 *
	 */
	private Map<String,Integer> variableUseCount;

	/**
	 * The <code>PrintWriter</code> which
	 * the generated LLVM IR is written with.
	 *
	 */
	private PrintWriter out;

	private Map<String,Map.Entry<String,List<Long>>> references;

	private Map<String,DerivedType> derivedTypes;

	/**
	 * Creates a new <code>Module</code> instance.
	 *
	 * @param out <code>PrintWriter</code> used
	 * to write the LLVM IR
	 */
	public Module(PrintWriter out)
	{
		reset(out);
	}

	/**
	 * Completely resets this <code>Module</code>,
	 * so it can be reused.
	 *
	 * @param out <code>PrintWriter</code> used
	 * to write the LLVM IR
	 */
	public void reset(PrintWriter out)
	{
		stringLiterals = new ArrayList<Integer>();
		variableUseCount = new HashMap<String,Integer>();
		references = new HashMap<String,Map.Entry<String,List<Long>>>();
		derivedTypes = new HashMap<String,DerivedType>();

		/* Add fake temporary variable */
		variableUseCount.put(".tmp", 0);

		this.out = out;
	}

	/**
	 * Prefix the LLVM IR with two spaces
	 * (as it may only exist inside the main
	 * function for now) and write it with <code>out</code>
	 *
	 * @param code a <code>String</code> value
	 */
	private void gen(String code)
	{
		out.println("  " + code);
	}

	/**
	 * Get a string literal's identifier by its
	 * id (i.e. its position in the list of string literals).
	 *
	 * @param id the string literal's id
	 * @return the string literal's identifier
	 */
	private static String getStringLiteralIdentifier(int id)
	{
		return "%.string_" + String.valueOf(id);
	}

	/**
	 * Get a string literal's type by its
	 * id (i.e. [i8 * {length}], where the length
	 * is the string literal's length).
	 *
	 * @param id an <code>int</code> value
	 * @return a <code>String</code> value
	 */
	private String getStringLiteralType(int id)
	{
		return "[" + String.valueOf(stringLiterals.get(id)) + " x i8]";
	}

	/**
	 * Get the LLVM IR type corresponding to
	 * a type from the three address code.
	 *
	 * @param type the type from the three address code
	 * @return the corresponding LLVM IR type
	 */
	private String getIRType(Kind type) {
		return getIRType(type,false);
	}
	private String getIRType(Kind type, boolean as_ptr)
	{
		String irType = "";
		switch(type)
		{
			case LONG:
				/* Longs are 64bit signed integers */
				irType = "i64";
				break;
			case DOUBLE:
				/* Doubles are 64bit IEEE floats */
				irType = "double";
				break;
			case BOOLEAN:
				/* Booleans are 8bit signed integers */
				irType = "i1";
				break;
			case STRING:
				/* Strings are each a pointer to a string literal
				   (which itself is a pointer to a memory segment
				   of signed integers).*/
				irType = "i8*";
				break;
		}

		return irType + (as_ptr ? "*" : "");
	}

	/**
	 * Get the LLVM IR type corresponding to
	 * an *array* type from the three address code.
	 *
	 * @param type the final type of the array, eg: double[][] --> double
	 * @param sizes a list of sizes of the array dimensions, eg:
	 *          double x[5][10][7] --> [5,10,7]
	 * @return the corresponding LLVM IR type for the array
	 */
	private String getIRAType(Type type, List<Integer> dimensions) {
		return getIRAType(type,dimensions,false);
	}
	private String getIRAType(Type type, List<Integer> dimensions, boolean as_ptr)
	{
		String irType = "";
		// write dimensions
		for (int d : dimensions)
			irType = irType + "[" + d + " x ";
		// write actual type
		if(type instanceof PrimitiveType) {
			irType = irType + getIRType(type.getKind());
		}
		else if(type instanceof StructType) {
			irType += getIRSType(Arrays.asList(((StructType) type).members()));
		}
		// write closing brakets
		for (int d : dimensions)
			irType += "]";
		irType.trim();
		return irType + (as_ptr ? "*" : "");
	}

	private String getIRSType(List<Member> members) {
		String irType = "{ ";

		for(Member m: members) {
			Type type = m.getType();

			if(type instanceof PrimitiveType) {
				irType += getIRType(type.getKind());
			}
			else if(type instanceof LLVMBackendArrayType) {
				LLVMBackendArrayType array = (LLVMBackendArrayType) type;
				irType += getIRAType(array.getStorageType(), array.getDimensions());
			}
			else if(type instanceof LLVMBackendStructType) {
				LLVMBackendStructType struct = (LLVMBackendStructType) type;
				irType += getIRSType(Arrays.asList(struct.members()));
			}

			irType += ", ";
		}

		irType = irType.substring(0, irType.length() - 2);

		return irType + " }";
	}

	/**
	 * Gets the LLVM IR instruction for a binary TAC operator
	 *
	 * @param operator the binary TAC operator
	 * @return the corresponding LLVM IR instruction
	 */
	private String getIRBinaryInstruction(Quadruple.Operator operator)
	{
		String irInst = "";

		switch(operator)
		{
			/* Arithmetic */
			case ADD_LONG:
				irInst = "add";
				break;
			case ADD_DOUBLE:
				irInst = "fadd";
				break;
			case SUB_LONG:
				irInst = "sub";
				break;
			case SUB_DOUBLE:
				irInst = "fsub";
				break;
			case MUL_LONG:
				irInst = "mul";
				break;
			case MUL_DOUBLE:
				irInst = "fmul";
				break;
			case DIV_LONG:
				irInst = "sdiv";
				break;
			case DIV_DOUBLE:
				irInst = "fdiv";
				break;

			/* Boolean Arithmetic */
			case OR_BOOLEAN:
				irInst = "or";
				break;
			case AND_BOOLEAN:
				irInst = "and";
				break;

			/* Comparisons */
			case COMPARE_LONG_E:
				irInst = " icmp eq";
				break;
			case COMPARE_LONG_G:
				irInst = " icmp sgt";
			break;
			case COMPARE_LONG_L:
				irInst = " icmp slt";
				break;
			case COMPARE_LONG_GE:
				irInst = " icmp sge";
			break;
			case COMPARE_LONG_LE:
				irInst = " icmp sle";
			break;

			case COMPARE_DOUBLE_E:
				irInst = " fcmp oeq";
			break;
			case COMPARE_DOUBLE_G:
				irInst = " fcmp ogt";
			break;
			case COMPARE_DOUBLE_L:
				irInst = " fcmp olt";
			break;
			case COMPARE_DOUBLE_GE:
				irInst = " fcmp oge";
			break;
			case COMPARE_DOUBLE_LE:
				irInst = " fcmp ole";
			break;
		}

		return irInst;
	}

	/**
	 * Gets the LLVM IR function residing in the preamble
	 * for a binary TAC operator.
	 *
	 * @param operator the binary TAC operator
	 * @return the corresponding LLVM IR function
	 */
	private String getIRBinaryCall(Quadruple.Operator operator) {
		String irCall = "";

		switch(operator)
		{
			/* Arithmetic */
			case DIV_LONG:
				irCall = "div_long";
				break;
			case DIV_DOUBLE:
				irCall = "div_double";
				break;
			case CONCAT_STRING:
				irCall = "concat_string";
				break;
		}

		return irCall;
	}

	/**
	 * Gets a unique use identifier for
	 * a variable; no two calls will return
	 * the same use identifier.
	 *
	 * @param variable the variable's name
	 * @return a free use identifier for the variable
	 * @exception BackendException if an error occurs
	 */
	private String getUseIdentifierForVariable(String variable) throws BackendException {
		int ssa_suffix = 0;
		try {
			ssa_suffix = variableUseCount.get(variable);
		} catch (NullPointerException e) {
			throw new BackendException("Use of undeclared variable");
		}
		variableUseCount.put(variable, ssa_suffix + 1);
		return "%" + variable + "." + String.valueOf(ssa_suffix);
	}

	/**
	 * Convert a three adress code boolean
	 * to a LLVM IR boolean.
	 * All other public functions expect booleans
	 * to be in the LLVM IR format.
	 *
	 * @param bool a TAC boolean
	 * @return the converted LLVM IR boolean
	 */
	public static String toIRBoolean(String bool)
	{
		if(bool.equals("#FALSE"))
		{
			return "#0";
		}
		else if(bool.equals("#TRUE"))
		{
			return "#1";
		}
		else
		{
			return bool;
		}
	}

	/**
	 * Convert a three adress code string
	 * to a LLVM IR string.
	 * All other public functions expect strings
	 * to be in the LLVM IR format.
	 *
	 * @param str a TAC string
	 * @return the converted LLVM IR string
	 */
	public static String toIRString(String str)
	{
		String irString = "";

		/* Unescape special characters */
		irString = str.replace("\\\"", "\"").
			replace("\\r", "\r").
			replace("\\n", "\n").
			replace("\\t", "\t").
			replace("\\0", "\0");

		return irString;
	}

	/**
	 * Generates a new string literal from a <code>String</code>
	 * value and returns its id (i.e. its position in the
	 * list of string literals <code>stringLiterals</code>).
	 *
	 * @param literal the string to use
	 * @return the new string literal's id
	 */
	private Integer addStringLiteral(String literal) throws BackendException
	{
		int length = 0;

		try
		{
			literal = literal.substring(1, literal.length() - 1);
			byte[] utf8 = literal.getBytes("UTF-8");
			literal = Arrays.toString(utf8).replaceAll("(-?)([0-9]+)(,?)","i8 $1$2$3");

			if(utf8.length > 0)
			{
				literal = literal.replace("]",", i8 0]");
			}
			else
			{
				literal = "[i8 0]";
			}

			length = utf8.length + 1;
		}
		catch(UnsupportedEncodingException e)
		{
			throw new BackendException("LLVM backend cannot handle strings without utf8 support");
		}

		int id = stringLiterals.size();
		stringLiterals.add(length);

		String type = getStringLiteralType(id);

		String identifier = getStringLiteralIdentifier(id);

		gen(identifier + " = alloca " + type);
		gen("store " + type + " " + literal + ", " + type + "* " + identifier);

		return id;
	}

	/**
	 * Sets a variable (which must be of the string primitive type)
	 * to contain a pointer to a string literal.
	 *
	 * @param variable the variable's name
	 * @param literalID the string literal's id
	 * @return the used "use identifier" for variable
	 * @exception BackendException if an error occurs
	 */
	private String addLoadStringLiteral(String variable, int literalID) throws BackendException {
		String variableIdentifier = "%" + variable;
		String variableUseIdentifier = getUseIdentifierForVariable(variable);
		String literalIdentifier = getStringLiteralIdentifier(literalID);
		String literalType = getStringLiteralType(literalID);

		gen(variableUseIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");
		gen("store i8* " + variableUseIdentifier + ", i8** " + variableIdentifier);
		return variableUseIdentifier;
	}

	/**
	 * Adds a new variable and return its identifier.
	 *
	 * @param type the new variable's type
	 * @param variable the new variable's name
	 * @return the variable's identifier
	 */
	private String addNewVariable(Kind type, String variable)
	{
		String irType = getIRType(type);
		String variableIdentifier = "%" + variable;
		variableUseCount.put(variable, 0);

		gen(variableIdentifier + " = alloca " + irType);
		return variableIdentifier;
	}

	public String addArrayDeclare(LLVMBackendArrayType type, String identifier)	{
		String irType = getIRAType(type.getStorageType(), type.getDimensions());
		String variableIdentifier = "%" + identifier;
		variableUseCount.put(identifier, 0);
		derivedTypes.put(identifier, type);
		gen(variableIdentifier + " = alloca " + irType);
		return variableIdentifier;
	}

	public String addStructDeclare(LLVMBackendStructType type, String identifier) {
		String irType = getIRSType(Arrays.asList(type.members()));
		String variableIdentifier = "%" + identifier;
		variableUseCount.put(identifier, 0);
		derivedTypes.put(identifier, type);
		gen(variableIdentifier + " = alloca " + irType);
		return variableIdentifier;
	}

	/**
	 * Adds a new variable and optionally sets its
	 * initial value if given.
	 *
	 * @param type the new variable's type
	 * @param variable the new variable's name
	 * @param initializer the new variable's initial value
	 * @exception BackendException if an error occurs
	 */
	public void addPrimitiveDeclare(Kind type, String variable, String initializer) throws BackendException {
		addNewVariable(type, variable);

		/* Set default initializer */
		if(initializer.equals(Quadruple.EmptyArgument)) {
			switch(type) {
				case LONG:
					initializer = "#0";
					break;
				case DOUBLE:
					initializer = "#0.0";
					break;
				case BOOLEAN:
					initializer = "#FALSE";
					break;
				case STRING:
					initializer = "#\"\"";
					break;
			}
		}

		addPrimitiveAssign(type, variable, initializer);
	}

	/**
	 * Assigns either a constant value, or the value of
	 * one variable (<code>src</code>) to another variable
	 * (<code>dst</code>). Only assignments for identical
	 * types are allowed.
	 *
	 * @param type the type of the assignment
	 * @param dst the destination variable's name
	 * @param src the source constant or the source variable's name
	 * @exception BackendException if an error occurs
	 */
	public void addPrimitiveAssign(Kind type, String dst, String src) throws BackendException {
		boolean constantSrc = false;
		if(src.charAt(0) == '#')
		{
			src = src.substring(1);
			constantSrc = true;
		}

		String irType = getIRType(type);
		String dstIdentifier = "%" + dst;

		if(constantSrc)
		{
			if(type == Kind.STRING)
			{
				int id = addStringLiteral(src);
				addLoadStringLiteral(dst, id);
			}
			else
			{
				gen("store " + irType + " " + src + ", " + irType + "* " + dstIdentifier);
			}
		}
		else
		{
			String srcUseIdentifier = getUseIdentifierForVariable(src);
			String srcIdentifier = "%" + src;
			gen(srcUseIdentifier + " = load " + irType + "* " + srcIdentifier);
			gen("store " + irType + " " + srcUseIdentifier + ", " + irType + "* " + dstIdentifier);
		}
	}

	/**
	 *  Conditionally loads a constant or an identifier. If given a constant, just the leading # character
	 *  is removed. If given a variable (anything without leading # character),
	 *  it is being loaded and the temporary register name is returned.
	 *
	 *	@param constantOrIdentifier Name of a constant or identifier.
	 *  @returns name of usable primitive
	 */
	private String cdl(String constantOrIdentifier, Kind type) throws BackendException {
		if(constantOrIdentifier.charAt(0) == '#')
			// constant
			if(type==Kind.STRING)
			{
				int literalID = addStringLiteral(constantOrIdentifier.substring(1));
				String literalIdentifier = getStringLiteralIdentifier(literalID);
				String literalType = getStringLiteralType(literalID);
				String variableUseIdentifier = getUseIdentifierForVariable(".tmp");
				gen(variableUseIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");

				return variableUseIdentifier;
			}
			else
				return constantOrIdentifier.substring(1);
		else {
			// identifier
			String id = getUseIdentifierForVariable(constantOrIdentifier);
			gen(id + " = load " + getIRType(type,true) + " %"+constantOrIdentifier);
			return id;
		}
	}

	public void addReferenceArrayGet(String array, long index, String dst) {
		String src = array;
		List<Long> indices = new LinkedList<Long>();
		indices.add(index);

		while(references.containsKey(src)) {
			Map.Entry<String,List<Long>> reference = references.get(src);
			indices.addAll(reference.getValue());
			src = reference.getKey();
		}

		references.put(dst,new AbstractMap.SimpleEntry<String,List<Long>>(src, indices));
	}

	public void addPrimitiveArrayGet(String array, long index, Kind dstType, String dst) throws BackendException {
		String src = array;
		List<Long> indices = new LinkedList<Long>();
		indices.add(index);

		while(references.containsKey(src)) {
			Map.Entry<String,List<Long>> reference = references.get(src);
			indices.addAll(reference.getValue());
			src = reference.getKey();
		}

		String indexList = "";
		for(Long i: indices) {
			indexList += ", " + i.toString();
		}

		String dstUseIdentifier = getUseIdentifierForVariable(dst);
		String dstIdentifier = "%" + dst;
		String dstIRType = getIRType(dstType);

		String srcUseIdentifier = getUseIdentifierForVariable(src);
		String srcIdentifier = "%" + src;
		LLVMBackendArrayType srcArType = null;
		DerivedType _srcArType = derivedTypes.get(src);
		if(_srcArType instanceof LLVMBackendArrayType) {
			srcArType = (LLVMBackendArrayType) _srcArType;
		}
		else {
			throw new BackendException("Source type for ARRAY_GET_* must be instance of LLVMBackendArrayType, not " + _srcArType.getClass().getName());
		}

		String srcIRType = getIRAType(srcArType.getStorageType(), srcArType.getDimensions());

		gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
		gen(dstUseIdentifier + " = extractvalue " + srcIRType + " " + srcUseIdentifier + indexList);
		gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
	}

	public void addPrimitiveArraySet(String array, long index, Kind srcType, String src) throws BackendException {
		String dst = array;
		List<Long> indices = new LinkedList<Long>();
		indices.add(index);

		while(references.containsKey(dst)) {
			Map.Entry<String,List<Long>> reference = references.get(dst);
			indices.addAll(reference.getValue());
			dst = reference.getKey();
		}

		String indexList = "";
		for(Long i: indices) {
			indexList += ", " + i.toString();
		}

		String srcIRType = getIRType(srcType);

		String dstUseIdentifier1 = getUseIdentifierForVariable(dst);
		String dstUseIdentifier2 = getUseIdentifierForVariable(dst);
		String dstIdentifier = "%" + dst;
		LLVMBackendArrayType dstArType = null;
		DerivedType _dstArType = derivedTypes.get(dst);
		if(_dstArType instanceof LLVMBackendArrayType) {
			dstArType = (LLVMBackendArrayType) _dstArType;
		}
		else {
			throw new BackendException("Source type for ARRAY_GET_* must be instance of LLVMBackendArrayType, not " + _dstArType.getClass().getName());
		}
		String dstIRType = getIRAType(dstArType.getStorageType(), dstArType.getDimensions());

		src = cdl(src, srcType);
		gen(dstUseIdentifier1 + " = load " + dstIRType + "* " + dstIdentifier);
		gen(dstUseIdentifier2 + " = insertvalue " + dstIRType + " " + dstUseIdentifier1 +
		    ", " + srcIRType + " " + src + indexList);
		gen("store " + dstIRType + " " + dstUseIdentifier2 + ", " + dstIRType + "* " + dstIdentifier);
	}

	static public Kind QuadTypeToKind(Quadruple.Operator op) throws BackendException {
		switch (op) {
		case DECLARE_DOUBLE:
		case ARRAY_GET_DOUBLE:
		case ARRAY_SET_DOUBLE:
			return Kind.DOUBLE;
		case DECLARE_BOOLEAN:
		case ARRAY_GET_BOOLEAN:
		case ARRAY_SET_BOOLEAN:
			return Kind.BOOLEAN;
		case DECLARE_LONG:
		case ARRAY_GET_LONG:
		case ARRAY_SET_LONG:
			return Kind.LONG;
		}
		throw new BackendException("QuadTypeToKind: unimplemented OP -> unknown type");
	}

	/**
	 * Assigns the value of one variable (<code>src</code>)
	 * to another variable (<code>dst</code>), where their types
	 * must only be convertible, not identical.
	 *
	 * @param srcType the source variable's type
	 * @param src the source variable's name
	 * @param dstType the destination variable's type
	 * @param dst the destination variable's name
	 * @exception BackendException if an error occurs
	 */
	public void addPrimitiveConversion(Kind srcType,
	                                   String src,
	                                   Kind dstType,
	                                   String dst) throws BackendException {
		String srcUseIdentifier = getUseIdentifierForVariable(src);
		String dstUseIdentifier = getUseIdentifierForVariable(dst);
		String srcIdentifier = "%" + src;
		String dstIdentifier = "%" + dst;
		String srcIRType = getIRType(srcType);
		String dstIRType = getIRType(dstType);

		if((srcType == Kind.LONG) && (dstType == Kind.DOUBLE))
		{
			gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
			gen(dstUseIdentifier + " = sitofp " + srcIRType + " " + srcUseIdentifier + " to " + dstIRType);
			gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
		}
		else if((srcType == Kind.DOUBLE) && (dstType == Kind.LONG))
		{
			gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
			gen(dstUseIdentifier + " = fptosi " + srcIRType + " " + srcUseIdentifier + " to " + dstIRType + "");
			gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
		}
		else if(dstType == Kind.STRING)
		{
			String conversionFunction = "";

			switch(srcType) {
				case BOOLEAN:
					conversionFunction = "btoa";
					break;
				case LONG:
					conversionFunction = "ltoa";
					break;
				case DOUBLE:
					conversionFunction = "dtoa";
					break;
			}

			gen(srcUseIdentifier + " = load " + srcIRType + "* " + srcIdentifier);
			gen(dstUseIdentifier + " = call i8* (" + srcIRType + ")* @" + conversionFunction + "(" + srcIRType + " " + srcUseIdentifier + ")");
			gen("store " + dstIRType + " " + dstUseIdentifier + ", " + dstIRType + "* " + dstIdentifier);
		}
	}

	/**
	 * Adds a generic binary operation of two sources - each can
	 * either be a constant or a variable - the result
	 * of which will be stored in a variable (<code>dst</code>).
	 * All types must be identical.
	 *
	 * @param op the binary operation to add
	 * @param resultType  the result type of the binary operation
	 * @param argumentType the argument type of the binary operation
	 * @param lhs the constant or name of the variable on the left hand side
	 * @param rhs the constant or name of the variable on the right hand side
	 * @param dst the destination variable's name
	 * @exception BackendException if an error occurs
	 */
	public void addPrimitiveBinaryInstruction(Quadruple.Operator op,
	                                          Kind resultType,
	                                          Kind argumentType,
	                                          String lhs,
	                                          String rhs,
	                                          String dst) throws BackendException {
		String irArgumentType = getIRType(argumentType);
		String irResultType = getIRType(resultType);
		String irInst = getIRBinaryInstruction(op);

		if(lhs.charAt(0) == '#')
		{
			lhs = lhs.substring(1);
		}
		else
		{
			String lhsIdentifier = "%" + lhs;
			lhs = getUseIdentifierForVariable(lhs);
			gen(lhs + " = load " + irArgumentType + "* " + lhsIdentifier);
		}

		if(rhs.charAt(0) == '#')
		{
			rhs = rhs.substring(1);
		}
		else
		{
			String rhsIdentifier = "%" + rhs;
			rhs = getUseIdentifierForVariable(rhs);
			gen(rhs + " = load " + irArgumentType + "* " + rhsIdentifier);
		}

		String dstUseIdentifier = getUseIdentifierForVariable(dst);
		String dstIdentifier = "%" + dst;

		gen(dstUseIdentifier + " = " + irInst + " " + irArgumentType + " " + lhs + ", " + rhs);
		gen("store " + irResultType + " " + dstUseIdentifier + ", " + irResultType + "* " + dstIdentifier);
	}

	/**
	 * Adds a call to a argument-homogenous binary functions
	 * where each argument can either be a constant or a variable;
	 * the result of the function will be stored in a variable (<code>dst</code>).
	 * Argument types must both be equal, but may differ
	 * from the result type.
	 *
	 * @param op the binary operation to add
	 * @param resultType the type of the binary operation's result
	 * @param argumentType the type of the binary operation's arguments
	 * @param lhs the constant or name of the variable on the left hand side
	 * @param rhs the constant or name of the variable on the right hand side
	 * @param dst the destination variable's name
	 * @exception BackendException if an error occurs
	 */
	public void addPrimitiveBinaryCall(Quadruple.Operator op,
	                                   Kind resultType,
	                                   Kind argumentType,
	                                   String lhs,
	                                   String rhs,
	                                   String dst) throws BackendException {
		String irArgumentType = getIRType(argumentType);
		String irResultType = getIRType(resultType);
		String irCall = getIRBinaryCall(op);
		boolean constantRHS = false;

		if(rhs.charAt(0) == '#')
		{
			constantRHS = true;
		}

		lhs = cdl(lhs, argumentType);
		rhs = cdl(rhs, argumentType);

		if(constantRHS) {
			if((op == Quadruple.Operator.DIV_LONG) &&
			   (Long.valueOf(rhs).equals(Long.valueOf(0))))
			{
				throw new BackendException("Division by zero");
			}

			if((op == Quadruple.Operator.DIV_DOUBLE) &&
			   ((Double.valueOf(rhs).equals(Double.valueOf(0.0))) ||
			    (Double.valueOf(rhs).equals(Double.valueOf(-0.0)))))
			{
				throw new BackendException("Division by zero");
			}
		}

		String dstUseIdentifier = getUseIdentifierForVariable(dst);
		String dstIdentifier = "%" + dst;

		gen(dstUseIdentifier + " = invoke " + irResultType + " (" + irArgumentType + ", " + irArgumentType + ")* " +
		    "@" + irCall + "(" + irArgumentType + " " + lhs + ", " + irArgumentType + " " + rhs + ") to label " + dstUseIdentifier + ".ok unwind label %UncaughtException");
		gen(dstUseIdentifier.substring(1, dstUseIdentifier.length()) + ".ok:");
		gen("store " + irResultType + " " + dstUseIdentifier + ", " + irResultType + "* " + dstIdentifier);
	}

	/**
	 * Adds a binary not operation, where the source
	 * may be either a constant, or a variable, whereas
	 * the result must be a variable.
	 *
	 * @param source the constant or name of the variable to be negated
	 * @param destination the destination variable's name
	 * @exception BackendException if an error occurs
	 */
	public void addBooleanNot(String source, String destination) throws BackendException {

		String irType = getIRType(BOOLEAN);

		/* source (ir boolean) is #1 or #0 constant */
		if(source.charAt(0) == '#')
		{
			source = source.substring(1);
		}
		/* source is identifier */
		else
		{
			String sourceIdentifier = "%" + source;
			source = getUseIdentifierForVariable(source);
			gen(source + " = load " + irType + "* " + sourceIdentifier);
		}

		String dstUseIdentifier = getUseIdentifierForVariable(destination);
		String dstIdentifier = "%" + destination;

		gen(dstUseIdentifier + " = " + "sub " + irType + " 1, " + source);
		gen("store " + irType + " " + dstUseIdentifier + ", " + irType + "* " + dstIdentifier);
	}


	/**
	 * Adds the return instruction for the
	 * main method.
	 *
	 * @param value the value to return (exit code)
	 * @exception BackendException if an error occurs
	 */
	public void addMainReturn(String value) throws BackendException {
		if(value.charAt(0) == '#')
		{
			gen("ret i64 " + value.substring(1));
		}
		else
		{
			String valueUseIdentifier = getUseIdentifierForVariable(value);
			String valueIdentifier = "%" + value;
			gen(valueUseIdentifier + " = load i64* " + valueIdentifier);
			gen("ret i64 " + valueUseIdentifier);
		}
	}

	/**
	 * Adds a label which may be the target of
	 * branching instructions.
	 *
	 * @param name the label's name, must be unique in the program
	 */
	public void addLabel(String name){
		gen(name+":");
	}

	/**
	 * Adds a branching instruction, which may either
	 * be unconditional (e.g. branch immediately) or
	 * depend on a boolean variable 'condition' (e.g. branch to
	 * one of two target labels depening on the boolean
	 * variable's value at runtime).
	 *
	 * @param target1 target label for unconditional branch, target
	 *                for the condition being true for conditional branch
	 * @param target2 <code>Quadruple.EmptyArgument</code> for unconditional
	 *                branch, target for the condition being false for
	 *                the conditional branch
	 * @param condition <code>Quadruple.EmptyArgument</code> for unconditional
	 *                  branch, the condition being tested for conditional branch
	 * @exception BackendException if an error occurs
	 */
	public void addBranch(String target1, String target2, String condition) throws BackendException {
		/* conditional branch */
		if (!target2.equals(Quadruple.EmptyArgument)){
			String irType = getIRType(Kind.BOOLEAN);
			String conditionUseIdentifier = getUseIdentifierForVariable(condition);
			gen(conditionUseIdentifier + " = load " + irType + "* %" + condition);
			gen("br " + irType + " " + conditionUseIdentifier + ", label %" + target1 + ", label %"+ target2);
		}
		else {
			gen("br label %" + target1);
		}
	}

	/**
	 * Adds a call to the printf function for
	 * the value, which may be either a constant
	 * or the name of a variable.
	 *
	 * @param value the value to be printed
	 * @param type the type of the value (boolean, long, double, string)
	 * @exception BackendException if an error occurs
	 */
	public void addPrint(String value, Kind type) throws BackendException {
		String irType = getIRType(type);
		boolean constantSrc = false;

		/* value is constant */
		if(value.charAt(0) == '#')
		{
			value = value.substring(1);
			constantSrc = true;
		}
		/* value is identifier */
		else
		{
			String valueIdentifier = "%" + value;
			value = getUseIdentifierForVariable(value);
			gen(value + " = load " + irType + "* " + valueIdentifier);
		}

		String temporaryIdentifier = "";

		if(constantSrc)
		{
			temporaryIdentifier = getUseIdentifierForVariable(".tmp");
			int id = addStringLiteral(value);
			String literalIdentifier = getStringLiteralIdentifier(id);
			String literalType = getStringLiteralType(id);

			gen(temporaryIdentifier + " = getelementptr " + literalType + "* " + literalIdentifier + ", i64 0, i64 0");
		}
		else
		{
			temporaryIdentifier = value;
		}

		gen("call i32 (i8*, ...)* @printf(i8* " + temporaryIdentifier + ")");
	}
}
