package edu.ufl.cise.cop4020fa23;

import static org.hamcrest.CoreMatchers.containsString;

import java.util.List;

import edu.ufl.cise.cop4020fa23.ast.ASTVisitor;
import edu.ufl.cise.cop4020fa23.ast.AssignmentStatement;
import edu.ufl.cise.cop4020fa23.ast.BinaryExpr;
import edu.ufl.cise.cop4020fa23.ast.Block;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;
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
import edu.ufl.cise.cop4020fa23.ast.Type;
import edu.ufl.cise.cop4020fa23.ast.UnaryExpr;
import edu.ufl.cise.cop4020fa23.ast.WriteStatement;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class CodeGenVisitor implements ASTVisitor{
//I think its supposed to be like this, but i dont really know what a argument of package is
//Every method should also return a string
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append(assignmentStatement.getlValue().visit(this, arg));
        javaString.append(" = ");
        javaString.append(assignmentStatement.getE().visit(this, arg));
        return javaString;
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        if (binaryExpr.getLeftExpr().getType() == Type.STRING && binaryExpr.getOpKind() == Kind.EQ) {
            javaString.append(binaryExpr.getLeftExpr().visit(this, arg));
            javaString.append(".equals(");
            javaString.append(binaryExpr.getRightExpr().visit(this, arg));
            javaString.append(")");
            return javaString;
        }
        if (binaryExpr.getOpKind() == Kind.EXP) {
            javaString.append("((int)Math.round(Math.pow(");
            javaString.append(binaryExpr.getLeftExpr().visit(this, arg));
            javaString.append(", ");
            javaString.append(binaryExpr.getRightExpr().visit(this, arg));
            javaString.append(")))");
            return javaString;
        }
        javaString.append("(");
        javaString.append(binaryExpr.getLeftExpr().visit(this, arg));
        javaString.append(" ");
        switch (binaryExpr.getOpKind()) {
            case BITAND:
                javaString.append("&");
                break;
            case BITOR:
                javaString.append("|");
                break;
            case AND:
                javaString.append("&&");
                break;
            case OR:
                javaString.append("||");
                break;
            case LT:
                javaString.append("<");
                break;
            case GT:
                javaString.append(">");
                break;
            case GE:
                javaString.append(">=");
                break;
            case LE:
                javaString.append("<=");
                break;
            case EQ: 
                javaString.append("==");
                break;
            case PLUS:
                javaString.append("+");
                break;
            case MINUS:
                javaString.append("-");
                break;
            case TIMES:
                javaString.append("*");
                break;
            case DIV:
                javaString.append("/");
                break;
            case MOD:
                javaString.append("%");
                break;
            default:
                break;
        }
        javaString.append(" ");
        javaString.append(binaryExpr.getRightExpr().visit(this, arg));
        javaString.append(")");
        return javaString;
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append("{ ");
        List<BlockElem> blockList = block.getElems();
        for (BlockElem elem : blockList) {
            javaString.append(elem.visit(this, arg));
            javaString.append("; ");
        }
        javaString.append("}");
        return javaString;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
    
        return statementBlock.getBlock().visit(this, arg);
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitChannelSelector'");
    }

    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append("(");
        javaString.append(conditionalExpr.getGuardExpr().visit(this, arg));
        javaString.append(" ? ");
        javaString.append(conditionalExpr.getTrueExpr().visit(this, arg));
        javaString.append(" : ");
        javaString.append(conditionalExpr.getFalseExpr().visit(this, arg));
        javaString.append(")");
        return javaString;
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append(declaration.getNameDef().visit(this, arg));
        if (declaration.getInitializer() == null) {
            return javaString;
        }
        javaString.append(" = ");
        javaString.append(declaration.getInitializer().visit(this, arg));
        return javaString;

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
        StringBuilder javaString = new StringBuilder();
        javaString.append(identExpr.getNameDef().getJavaName());
        return javaString;
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitIfStatement'");
    }

    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append(lValue.getNameDef().getJavaName());
        return javaString;
    }

    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        switch (nameDef.getType()) {
            case BOOLEAN:
                javaString.append("boolean");
                break;
            case INT:
                javaString.append("int");
                break;
            case STRING:
                javaString.append("String");
                break;
            case VOID:
                javaString.append("void");
                break;
        }
        javaString.append(" ");
        javaString.append(nameDef.getJavaName());
        return javaString;
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append(numLitExpr.getText());
        return javaString;
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
        StringBuilder javaString = new StringBuilder();
        //in case of package
        if(arg != null && !arg.equals("")) {
            javaString.append("package ");
            javaString.append(arg);
            javaString.append("; ");
        }

        javaString.append("public class ");
        javaString.append((program.getName()));
        javaString.append(" { public static ");
        if (program.getTypeToken().text().equals("string")) {
            javaString.append("String");
        }
        else javaString.append(program.getTypeToken().text());
        javaString.append(" apply(");
        List<NameDef> paramList = program.getParams();
        for (NameDef parameters : paramList) {
            javaString.append(parameters.visit(this, arg));
            if (paramList.size() != 1 && parameters != paramList.get(paramList.size()-1)) {
                javaString.append(", ");
            }
        }
        javaString.append(") ");
        javaString.append(program.getBlock().visit(this, arg));
        javaString.append(" }");
        return javaString.toString();
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append("return ");
        javaString.append(returnStatement.getE().visit(this, arg));
        return javaString;
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append(stringLitExpr.getText());
        return javaString;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append("(");
        switch (unaryExpr.getOp()) {
            case MINUS:
                javaString.append("-");
                break;
            case BANG:
                javaString.append("!");
                break;
            default:
                break;
        }
        javaString.append(unaryExpr.getExpr().visit(this, arg));
        javaString.append(")");
        return javaString;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitWriteStatement'");
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        if (booleanLitExpr.getText()== "TRUE") {
            javaString.append("true");
            return javaString;
        }
        javaString.append("false");
        return javaString;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitConstExpr'");
    }
    
}
