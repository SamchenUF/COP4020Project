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
public class Declaration extends Block.BlockElem {
  
	final NameDef nameDef;
	final Expr initializer;
=======
public class Declaration extends Block.BlockElem {

	final NameDef nameDef;
	final Expr initializer;

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	/**
	 * @param firstToken
	 * @param nameDef
	 * @param initializer
	 */
	public Declaration(IToken firstToken, NameDef nameDef, Expr initializer) {
		super(firstToken);
		this.nameDef = nameDef;
		this.initializer = initializer;
	}
<<<<<<< HEAD
=======

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitDeclaration(this, arg);
	}
<<<<<<< HEAD
=======

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(initializer, nameDef);
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
		Declaration other = (Declaration) obj;
		return Objects.equals(initializer, other.initializer) && Objects.equals(nameDef, other.nameDef);
	}
<<<<<<< HEAD
	/**
	 * @return the nameDef
	 */
	public NameDef getNameDef() {
		return nameDef;
	}
	/**
	 * @return the initializer
	 */
	public Expr getInitializer() {
		return initializer;
	}
=======

	public NameDef getNameDef() {
		return nameDef;
	}

	public Expr getInitializer() {
		return initializer;
	}

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public String toString() {
		return "Declaration [nameDef=" + nameDef + ", initializer=" + initializer + "]";
	}
<<<<<<< HEAD
	
	
=======

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
}
