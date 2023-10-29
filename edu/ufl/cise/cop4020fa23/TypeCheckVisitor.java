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
        LValue lValue = assignmentStatement.getlValue();
        Type lValueType = (Type) lValue.visit(this, arg);
        Type exprType = (Type) assignmentStatement.getE().visit(this, arg);
        if(lValueType == exprType) {
            return lValueType;
        }
        throw new TypeCheckException("Type mismatch in assignment");
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
        for(BlockElem elem : statementBlock.getBlock().getElems()) {
            elem.visit(this, arg);
        }
        return statementBlock;
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
        for(GuardedBlock guardedBlock : doStatement.getGuardedBlocks()) {
            Type guardType = (Type)guardedBlock.getGuard().visit(this, arg);
            if(guardType != Type.BOOLEAN) {
                throw new TypeCheckException("Do statement guard must be of type BOOLEAN");
            }
            guardedBlock.getBlock().visit(this, arg);
        }
        return null;
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
        if(ST.lookup(identExpr.getName()) != null) {
            identExpr.setNameDef(ST.lookup(identExpr.getName()));
            identExpr.setType((Type)(identExpr.getNameDef().visit(this, arg)));
            return identExpr.getType();
        }
        throw new TypeCheckException("Doesn't exist in symbolTable");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        for(GuardedBlock guardedBlock : ifStatement.getGuardedBlocks()) {
            Type guardType = (Type)guardedBlock.getGuard().visit(this, arg);
            if(guardType != Type.BOOLEAN) {
                throw new TypeCheckException("Guard in If statement must be of type BOOLEAN");
            }
            guardedBlock.getBlock().visit(this, arg);
        }
        return null;
    }


    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        NameDef def = ST.lookup(lValue.getName());
        if (def != null) {
            lValue.setType(def.getType());
            return def.getType();
        }
        throw new TypeCheckException("LValue not found in symbol table");
    }


    @Override
    public Object visitNameDef(NameDef nameDef, Object arg) throws PLCCompilerException {
        Type type = Type.kind2type(nameDef.getTypeToken().kind());
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
        ST.add(nameDef.getName(), ST.lookup(nameDef.getName()));
        return type;
        
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        numLitExpr.setType(Type.INT);
        System.out.println("running");
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        pixelSelector.xExpr().visit(this, arg);
        pixelSelector.yExpr().visit(this, arg);
        return Type.PIXEL;
    }


    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        Type exprType = (Type)postfixExpr.primary().visit(this, arg);
        if (postfixExpr.pixel() != null) {
            postfixExpr.pixel().visit(this, arg);
        }
        if (postfixExpr.channel() != null) {
            postfixExpr.channel().visit(this, arg);
        }
        postfixExpr.setType(exprType);
        return exprType;
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
        return returnStatement.getE().visit(this, arg);
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        stringLitExpr.setType(Type.STRING);
        return Type.STRING;
    }

    @Override
    public Object visitUnaryExpr(UnaryExpr unaryExpr, Object arg) throws PLCCompilerException {
        Type exprType = (Type)unaryExpr.getExpr().visit(this, arg);
        Kind op = unaryExpr.getOp();
        if (op == Kind.MINUS || op == Kind.BANG) {
            unaryExpr.setType(exprType);
            return exprType;
        }
        throw new TypeCheckException("Invalid unary operation");
    }


    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        writeStatement.getExpr().visit(this, arg);
        return writeStatement;
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