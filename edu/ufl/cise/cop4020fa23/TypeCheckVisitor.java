package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;

import java.util.List;

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

        // visit the expression on the right hand side (RValue) to determine its type
        Type exprType = (Type) assignmentStatement.getE().visit(this, arg);

        if(lValueType != null && lValueType == exprType) {
            // if the types of LValue and the RValue are the same type, return the type
            return lValueType;
        }

        System.out.println("LValue Type: " + lValueType);
        System.out.println("Expr Type: " + exprType);

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
            // iterate over each element within the statement block
            for(BlockElem elem : statementBlock.getBlock().getElems()) {
            elem.visit(this, arg);
        }
        return statementBlock;
    }


    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        return null;
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
        // iterate over each guarded block within the DoStatement
        for(GuardedBlock guardedBlock : doStatement.getGuardedBlocks()) {
            // get the type of the guard condition
            Type guardType = (Type)guardedBlock.getGuard().visit(this, arg);
            if(guardType != Type.BOOLEAN) {
                throw new TypeCheckException("Do statement guard must be of type BOOLEAN");
            }
            // visit and process the statement block associated with the guard
            guardedBlock.getBlock().visit(this, arg);
        }
        return null;
    }


    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        Type red = (Type)expandedPixelExpr.getRed().visit(this, arg);
        Type blue = (Type)expandedPixelExpr.getBlue().visit(this, arg);
        Type green = (Type)expandedPixelExpr.getGreen().visit(this, arg);
        if (red == Type.INT && green == Type.INT && blue == Type.INT) {
            expandedPixelExpr.setType(Type.PIXEL);
            return Type.PIXEL;
        }
        throw new TypeCheckException("Expanded pixel not all int");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        if (guardedBlock.getGuard().getType() == Type.BOOLEAN) {
            return Type.BOOLEAN;
        }
        throw new TypeCheckException("Guard type is not boolean");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        System.out.println(identExpr.getName());
        if(ST.lookup(identExpr.getName()) != null) {
            identExpr.setNameDef(ST.lookup(identExpr.getName()));
            identExpr.setType(identExpr.getNameDef().getType());
            return identExpr.getType();
        }
        throw new TypeCheckException("Doesn't exist in symbolTable");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // iterate over each guarded block within the IfStatement
        for(GuardedBlock guardedBlock : ifStatement.getGuardedBlocks()) {
            // visit and process the statement block associated with the guard
            guardedBlock.getBlock().visit(this, arg);
        }
        return ifStatement;
    }


    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // if there's a pixel selector, the LValue refers to a pixel of the image
        lValue.setNameDef(ST.lookup(lValue.getName()));
        Type varType = (Type) lValue.getNameDef().visit(this, arg);
        if (lValue.getPixelSelector() != null) {
            lValue.getPixelSelector().visit(this, true);
            varType = Type.IMAGE;
        }
         if (lValue.getPixelSelector() == null && lValue.getChannelSelector() == null) {
            lValue.setType(varType);
            return varType;
        }
        if (lValue.getPixelSelector() != null && lValue.getChannelSelector() == null) {
            lValue.setType(Type.PIXEL);
            return Type.PIXEL;
        }
        if (lValue.getPixelSelector() != null && lValue.getChannelSelector() != null) {
            lValue.setType(Type.INT);
            return Type.INT;
        }
        if (varType == Type.IMAGE && lValue.getPixelSelector() == null && lValue.getChannelSelector() != null) {
            lValue.setType(Type.IMAGE);
            return Type.IMAGE;
        }
        if (varType == Type.PIXEL && lValue.getPixelSelector() == null && lValue.getChannelSelector() != null) {
            lValue.setType(Type.INT);
            return Type.INT;
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
        ST.add(nameDef.getName(), nameDef);
        return type;
        
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        if ((boolean)arg == true) {
            boolean xTypeB = pixelSelector.xExpr() instanceof IdentExpr; 
            boolean yTypeB = pixelSelector.yExpr() instanceof IdentExpr;
            if ((xTypeB || pixelSelector.yExpr() instanceof NumLitExpr) && (yTypeB || pixelSelector.yExpr() instanceof NumLitExpr)) {
                
                IdentExpr temp = (IdentExpr) pixelSelector.xExpr();
                IdentExpr temp2 = (IdentExpr) pixelSelector.yExpr();
                if (xTypeB && ST.lookup(temp.getName()) == null) {
                    System.out.println(temp.getName());
                    ST.add(temp.getName(), new SyntheticNameDef(temp.getName()));

                }
                if (yTypeB && ST.lookup(temp.getName()) == null) {
                    SyntheticNameDef ydef = new SyntheticNameDef(temp2.getName());
                    ST.add(temp2.getName(), new SyntheticNameDef(temp2.getName()));
                }
            }
        }
        // ensure both x and y are of type INT
        // after processing the x and y expressions, return the PIXEL type
        Type xType = (Type) pixelSelector.xExpr().visit(this, arg);
        Type yType = (Type) pixelSelector.yExpr().visit(this, arg);
        if (xType == Type.INT && yType == Type.INT) {
            return Type.INT;
        }
        throw new TypeCheckException("Not both int");
        
        
    }



    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        // get the type of the primary expression
        Type exprType = (Type)postfixExpr.primary().visit(this, arg);

        // if there's a pixel selection post-fix operator, visit it
        if (postfixExpr.pixel() == null && postfixExpr.channel() == null) {
            postfixExpr.setType(exprType);
            return exprType;
        }
        if (exprType == Type.IMAGE && postfixExpr.pixel() != null && postfixExpr.channel() == null) {
            postfixExpr.setType(Type.PIXEL);
             postfixExpr.pixel().visit(this, false);
            return Type.PIXEL;
        }
        if (exprType == Type.IMAGE && postfixExpr.pixel() != null && postfixExpr.channel() != null) {
            postfixExpr.channel().visit(this, arg);
            postfixExpr.pixel().visit(this, false);
            postfixExpr.setType(Type.INT);
            return Type.INT;
        }
        if (exprType == Type.IMAGE && postfixExpr.pixel() == null && postfixExpr.channel() != null) {
            postfixExpr.channel().visit(this, arg);
            postfixExpr.setType(Type.IMAGE);
            return Type.IMAGE;
        }
        if (exprType == Type.PIXEL && postfixExpr.pixel() == null && postfixExpr.channel() != null) {
            postfixExpr.channel().visit(this, arg);
            postfixExpr.setType(Type.INT);
            return Type.INT;
        }
        throw new TypeCheckException("Not inferpostfixtype");
    }


    @Override
    public Object visitProgram(Program program, Object arg) throws PLCCompilerException {
        //Base code that just returns the type does not do any type checking
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
        // visit the expression within the unary expression and get its type
        Type exprType = (Type)unaryExpr.getExpr().visit(this, arg);
        // get the unary operator from the unary expression
        Kind op = unaryExpr.getOp();
        if (op == Kind.MINUS || op == Kind.BANG) {
            // set the type of the unary expression
            unaryExpr.setType(exprType);
            return exprType;
        }
        if (op == Kind.RES_width || op == Kind.RES_height || exprType == Type.IMAGE) {
            unaryExpr.setType(Type.INT);
            return Type.INT;
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
        // check if the name of the constant expression is "Z"
        if (constExpr.getName().equals("Z")) {
            // set the type of the constant expression to INT
            constExpr.setType(Type.INT);
            return Type.INT;
        }
        // if the name is not "Z", set the type of the constant expression to PIXEL
        constExpr.setType(Type.PIXEL);
        return Type.PIXEL;
    }
}

