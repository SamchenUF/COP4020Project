package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.ast.AssignmentStatement;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.Block;
import edu.ufl.cise.cop4020fa23.ast.BooleanLitExpr;
import edu.ufl.cise.cop4020fa23.ast.ChannelSelector;
import edu.ufl.cise.cop4020fa23.ast.ConditionalExpr;
import edu.ufl.cise.cop4020fa23.ast.ConstExpr;
import edu.ufl.cise.cop4020fa23.ast.Declaration;
import edu.ufl.cise.cop4020fa23.ast.Dimension;
import edu.ufl.cise.cop4020fa23.ast.DoStatement;
import edu.ufl.cise.cop4020fa23.ast.ExpandedPixelExpr;
import edu.ufl.cise.cop4020fa23.ast.GuardedBlock;
import edu.ufl.cise.cop4020fa23.ast.IdentExpr;
import edu.ufl.cise.cop4020fa23.ast.IfStatement;
import edu.ufl.cise.cop4020fa23.ast.LValue;
import edu.ufl.cise.cop4020fa23.ast.NameDef;
import edu.ufl.cise.cop4020fa23.ast.NumLitExpr;
import edu.ufl.cise.cop4020fa23.ast.PixelSelector;
import edu.ufl.cise.cop4020fa23.ast.PostfixExpr;
import edu.ufl.cise.cop4020fa23.ast.Program;
import edu.ufl.cise.cop4020fa23.ast.ReturnStatement;
import edu.ufl.cise.cop4020fa23.ast.StatementBlock;
import edu.ufl.cise.cop4020fa23.ast.StringLitExpr;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.WriteStatement;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class CodeGenVisitor implements ASTVisitor{
//I think its supposed to be like this, but i dont really know what a argument of package is
//Every method should also return a string
    @Override
    public String visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg)
            throws PLCCompilerException {
        // Generate code for the left-hand side of the assignment
        String lhsCode = (String) assignmentStatement.getlValue().visit(this, arg);
        // Generate code for the right-hand side of the assignment
        String rhsCode = (String) assignmentStatement.getE().visit(this, arg);
        // Combine the two with an assignment operator
        return lhsCode + " = " + rhsCode + ";\n";
    }

    @Override
    public String visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        // Retrieve the code for the left and right expressions by recursively visiting them
        String leftCode = (String) binaryExpr.getLeftExpr().visit(this, arg);
        String rightCode = (String) binaryExpr.getRightExpr().visit(this, arg);

        // Get the operator token and convert it to the corresponding Java operator
        String operator = getJavaOperator(binaryExpr.getOpKind());

        // Combine the left expression, operator, and right expression into a single expression string
        return "(" + leftCode + " " + operator + " " + rightCode + ")";
    }


    // This method convert our language's operator kind into Java's operator string
    // For example, if we use a specific kind for logical AND, this converts it to '&&' for Java.
    private String getJavaOperator(Kind opKind) {
        switch (opKind) {
            case BITAND: return "&";
            case BITOR: return "|";
            case AND: return "&&";
            case OR: return "||";
            case GT: return ">";
            case LT: return "<";
            case LE: return "<=";
            case GE: return ">=";
            case EQ: return "==";
            case EXP: return "Math.pow"; // Java uses Math.pow(a, b) for exponentiation
            case PLUS: return "+";
            case MINUS: return "-";
            case TIMES: return "*";
            case DIV: return "/";
            case MOD: return "%";
            default:
                throw new UnsupportedOperationException("Operator " + opKind + " not supported yet.");
        }
    }

    @Override
    public String visitBlock(Block block, Object arg) throws PLCCompilerException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n"); // Begin block

        // Visit each block element and append its generated code to the StringBuilder
        for (Block.BlockElem elem : block.getElems()) {
            String elemCode = (String) elem.visit(this, arg);
            sb.append(elemCode);

            // If the code snippet does not already end with a newline, add one
            if (!elemCode.endsWith("\n")) {
                sb.append("\n");
            }
        }

        sb.append("}\n"); // End block
        return sb.toString();
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBlockStatement'");
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitChannelSelector'");
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConditionalExpr'");
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDeclaration'");
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDimension'");
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDoStatement'");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitExpandedPixelExpr'");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitGuardedBlock'");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIdentExpr'");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitLValue'");
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitNameDef'");
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitNumLitExpr'");
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPixelSelector'");
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitPostfixExpr'");
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitProgram'");
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStatement'");
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitStringLitExpr'");
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitUnaryExpr'");
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWriteStatement'");
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitBooleanLitExpr'");
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstExpr'");
    }
    
}
