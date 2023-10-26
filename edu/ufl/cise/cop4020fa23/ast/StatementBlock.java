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
public class StatementBlock extends Statement {

	final Block block;
	
	
=======
public class StatementBlock extends Statement {

	final Block block;

>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	/**
	 * @param firstToken
	 * @param block
	 */
	public StatementBlock(IToken firstToken, Block block) {
		super(firstToken);
		this.block = block;
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public Object visit(ASTVisitor v, Object arg) throws PLCCompilerException {
		return v.visitBlockStatement(this, arg);
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(block);
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
		StatementBlock other = (StatementBlock) obj;
		return Objects.equals(block, other.block);
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	public Block getBlock() {
		return block;
	}

<<<<<<< HEAD

=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59
	@Override
	public String toString() {
		return "StatementBlock [block=" + block + "]";
	}
<<<<<<< HEAD
	
	
=======
>>>>>>> 884ec4636f77576108bb0b4cec7465eb83379a59

}
