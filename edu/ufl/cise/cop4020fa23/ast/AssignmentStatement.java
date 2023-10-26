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
<<<<<<< HEAD
import edu.ufl.cise.cop4020fa23.ast.LValue;
import edu.ufl.cise.cop4020fa23.ast.Statement;
=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

/**
 * 
 */
public class AssignmentStatement extends Statement {
	
	final LValue lValue;
	final Expr e;
	
	

	/**
	 * @param firstToken
	 * @param lValue
	 * @param e
	 */
	public AssignmentStatement(IToken firstToken, LValue lValue, Expr e) {
		super(firstToken);
		this.lValue = lValue;
		this.e = e;
	}



	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitAssignmentStatement(this, arg);
	}



	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(e, lValue);
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AssignmentStatement other = (AssignmentStatement) obj;
		return Objects.equals(e, other.e) && Objects.equals(lValue, other.lValue);
	}



	public LValue getlValue() {
		return lValue;
	}



	public Expr getE() {
		return e;
	}
<<<<<<< HEAD



	@Override
	public String toString() {
		return "AssignmentStatement [lValue=" + lValue + ", e=" + e + "]";
	}
	
=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	
}
