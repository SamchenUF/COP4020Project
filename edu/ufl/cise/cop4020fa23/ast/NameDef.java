/*Copyright 2023 by Beverly A Sanders
 * 
 * This code is provided for solely for use of students in COP4020 Programming Language Concepts at the 
 * University of Florida during the fall semester 2023 as part of the course project.  
 * 
 * No other use is authorized. 
 * 
 * This code may not be posted on a public web site either during or after the course.  
 */
package edu.ufl.cise.cop4020fa23.ast;

import java.util.Objects;

import edu.ufl.cise.cop4020fa23.IToken;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

<<<<<<< HEAD
/**
 * 
 */
public class NameDef extends AST {
	
	final IToken typeToken;
	final IToken identToken;
	final Dimension dimension;	
	String javaName;
	
	/**
	 * @param firstToken
	 * @param type
	 * @param ident
	 * @param dimension
	 */
	public NameDef(IToken firstToken, IToken typeToken, Dimension dimension, IToken identToken ) {
=======
public class NameDef extends AST {

	final IToken typeToken;
	final IToken identToken;
	final Dimension dimension;

	public NameDef(IToken firstToken, IToken typeToken, Dimension dimension, IToken identToken) {
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
		super(firstToken);
		this.typeToken = typeToken;
		this.identToken = identToken;
		this.dimension = dimension;
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitNameDef(this, arg);
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(dimension, identToken, typeToken);
		return result;
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NameDef other = (NameDef) obj;
		return Objects.equals(dimension, other.dimension) && Objects.equals(identToken, other.identToken)
				&& Objects.equals(typeToken, other.typeToken);
	}

<<<<<<< HEAD

	/**
	 * @return the type
	 */
=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	public IToken getTypeToken() {
		return typeToken;
	}

<<<<<<< HEAD
	public Type getType() {
		return Type.kind2type(typeToken.kind());
	}

	/**
	 * @return the ident
	 */
=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	public IToken getIdentToken() {
		return identToken;
	}

<<<<<<< HEAD
	/**
	 * @return the dimension
	 */
=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	public Dimension getDimension() {
		return dimension;
	}

	public String getName() {
		return getIdentToken().text();
	}

	@Override
	public String toString() {
<<<<<<< HEAD
		return "NameDef [type=" + getType() + ", name=" + getName() + ", dimension=" + dimension + "]";
	}

//	/**
//	 * @param string
//	 */
//	public void setJavaName(String string) {
//		javaName = string;
//	}
//	
//	public String getJavaName() {
//		if (javaName != null) return javaName;
//		throw new IllegalStateException("javaName not initialized");
//	}
	
	
=======
		return "NameDef [typeToken=" + typeToken + ", identToken=" + identToken + ", dimension=" + dimension + "]";
	}

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
}
