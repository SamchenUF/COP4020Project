package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;

import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.attribute.PosixFilePermission;
import java.util.List;
import edu.ufl.cise.cop4020fa23.SymbolTable;
import edu.ufl.cise.cop4020fa23.Kind.*;
import edu.ufl.cise.cop4020fa23.exceptions.TypeCheckException;
import edu.ufl.cise.cop4020fa23.exceptions.LexicalException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;

public class TypeCheckVisitor implements ASTVisitor{
        private SymbolTable ST;
        public TypeCheckVisitor() throws LexicalException {
            ST = new SymbolTable();  
        }
    @Override
    public Object visitAssignmentStatement(AssignmentStatement assignmentStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub

        throw new UnsupportedOperationException("Unimplemented method 'visitAssignmentStatement'");
    }

    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        Type leftType = (Type)binaryExpr.getLeftExpr().visit(this, arg);
        Type rightType = (Type)binaryExpr.getRightExpr().visit(this, arg);
        Kind op = binaryExpr.getOpKind();
        if (leftType == Type.PIXEL && (op == Kind.BITAND || op == Kind.BITOR) && rightType == Type.PIXEL) {
            binaryExpr.setType(Type.PIXEL);
            return Type.PIXEL;
        }
        else if (leftType == Type.BOOLEAN && (op == Kind.AND || op == Kind.OR) && rightType == Type.BOOLEAN) {
            binaryExpr.setType(Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else if (leftType == Type.INT && (op == Kind.GT || op == Kind.LT || op == Kind.LE || op == Kind.GE) && rightType == Type.INT) {
            binaryExpr.setType(Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else if (leftType == rightType && op == Kind.EQ) {
            binaryExpr.setType(Type.BOOLEAN);
            return Type.BOOLEAN;
        }
        else if (leftType == Type.INT && op == Kind.EXP && rightType == Type.INT) {
            binaryExpr.setType(Type.INT);
            return Type.INT;
        }
        else if (leftType == Type.PIXEL && op == Kind.EXP && rightType == Type.INT) {
            binaryExpr.setType(Type.PIXEL);
            return Type.PIXEL;
        }
        else if (leftType == rightType && op == Kind.PLUS) {
            binaryExpr.setType(leftType);
            return leftType;
        }
        else if ((leftType == Type.IMAGE || leftType == Type.PIXEL || leftType == Type.INT) && (op == Kind.MINUS || op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) && rightType == leftType) {
            binaryExpr.setType(leftType);
            return leftType;
        }
        else if ((leftType == Type.PIXEL || leftType == Type.IMAGE) && (op == Kind.TIMES || op == Kind.DIV || op == Kind.MOD) && rightType == Type.INT) {
            binaryExpr.setType(leftType);
            return leftType;
        }
        throw new TypeCheckException("Not valid binary type combo");
    }

    @Override
    public Object visitBlock(Block block, Object arg) throws PLCCompilerException {
        ST.enterScope();
        List<BlockElem> blockList = block.getElems();
        for (BlockElem elem : blockList) {
            elem.visit(this, arg);
        }
        ST.leaveScope();
        return block;
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
        Type guard = (Type)conditionalExpr.getGuardExpr().visit(this, arg);
        Type trueE = (Type)conditionalExpr.getTrueExpr().visit(this, arg);
        Type falseE = (Type)conditionalExpr.getFalseExpr().visit(this, arg);
        if(guard == Type.BOOLEAN && trueE == falseE) {
            conditionalExpr.setType(trueE);
            return conditionalExpr.getType();
        }
        throw new TypeCheckException("Conditional for conditional Expr not met");
    }

    @Override
    public Object visitDeclaration(Declaration declaration, Object arg) throws PLCCompilerException {
        if (declaration.getInitializer() == null) {
            return (Type)declaration.getNameDef().visit(this, arg);
        }
        Type exprType = (Type)declaration.getInitializer().visit(this, arg);
        Type nameType = (Type)declaration.getNameDef().visit(this, arg);
        if (exprType == nameType || (exprType == Type.STRING && nameType == Type.IMAGE)) {
            return nameType;
        }
        throw new TypeCheckException("Didn't meet condition for declaration type");
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        Type width = (Type)dimension.getWidth().visit(this, arg);
        Type length = (Type)dimension.getHeight().visit(this, arg);
        if (width == Type.INT && length == Type.INT) {
            return dimension;
        }
        throw new TypeCheckException("Not int dimension");
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
        if (guardedBlock.getGuard().getType() == Type.BOOLEAN) {
            return Type.BOOLEAN;
        }
        throw new TypeCheckException("Guard type is not boolean");
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
        Type type = nameDef.getType();
        //Checks if the dimension is empty, if it is then check if the types are one of the allowed one
        if (nameDef.getDimension() == null) { //if the type isn't one of the allowed one then throw error
            if (type != Type.INT && type != Type.BOOLEAN && type != Type.STRING && type != Type.PIXEL && type != Type.IMAGE) {
                throw new TypeCheckException("Type not allowed for a empty dimension");
            }
        }
        else { //If a nonempty dimension is not image type then throw image
            nameDef.getDimension().visit(this, arg);
            if (type != Type.IMAGE) {
                throw new TypeCheckException("Type not allowed for a nonempty dimension");
            }
        }
        //This runs only if the 1 of 2 cases pass: dim is empty and types are good or dim is not empty and type is image
        if(ST.lookup(nameDef.getName()) == null) {
            ST.add(nameDef.getName(), nameDef);
            return type;
        }
        else {
            throw new TypeCheckException("Already in symbol table");
        }
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        numLitExpr.setType(Type.INT);
        System.out.println("running");
        return Type.INT;
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
        //Base code that just returns the type does not do anyy type checking
        Type type = Type.kind2type(program.getTypeToken().kind());
        program.setType(type);
        if (type == null) {
            throw new TypeCheckException("Can't be null type");
        }
        ST.enterScope();
        List<NameDef> paramList = program.getParams();
        for (NameDef parameters : paramList) {
                parameters.visit(this, arg);
            }   
        program.getBlock().visit(this, arg);
        ST.leaveScope();
        return type;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitReturnStatement'");
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
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
        booleanLitExpr.setType(Type.BOOLEAN);
        return Type.BOOLEAN;
    }

    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        if (constExpr.getName().equals("Z")) {
            constExpr.setType(Type.INT);
            return Type.INT;
        }
        constExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }
}