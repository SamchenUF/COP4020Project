package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class TypeCheckVistor implements ASTVisitor{
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        
    }
}

//Idk if this class is supposed to be created here