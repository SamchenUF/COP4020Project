package edu.ufl.cise.cop4020fa23;

import edu.ufl.cise.cop4020fa23.ast.*;
import edu.ufl.cise.cop4020fa23.ast.Block.BlockElem;

import static org.hamcrest.CoreMatchers.containsString;

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
        ST.enterScope();
        Type lValueType = (Type) assignmentStatement.getlValue().visit(this, arg);
        Type exprType = (Type) assignmentStatement.getE().visit(this, arg);
        //check is children types are compatible if so leavescope and return 
        if ((lValueType == exprType) || (lValueType == Type.PIXEL && exprType == Type.INT) || (lValueType == Type.IMAGE && exprType == Type.PIXEL) || (lValueType == Type.IMAGE && exprType == Type.INT) || (lValueType == Type.IMAGE && exprType == Type.STRING)) {
            ST.leaveScope();
            return assignmentStatement;
        }
        throw new TypeCheckException("Type mismatch in assignment");
    }


    @Override
    public Object visitBinaryExpr(BinaryExpr binaryExpr, Object arg) throws PLCCompilerException {
        //get children types
        Type leftType = (Type)binaryExpr.getLeftExpr().visit(this, arg);
        Type rightType = (Type)binaryExpr.getRightExpr().visit(this, arg);
        Kind op = binaryExpr.getOpKind();
        //check if children types and op is valid combination
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
        //enter scope and visit all block elems
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
        //checks children
        statementBlock.getBlock().visit(this, arg);
        return statementBlock;
    }


    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        return null;
    }


    @Override
    public Object visitConditionalExpr(ConditionalExpr conditionalExpr, Object arg) throws PLCCompilerException {
        //check children types
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
        //Condition: Expr == null|| Expr.type == NameDef.type || (Expr.type == STRING && NameDef.type == IMAGE)
        //if conditions met set type
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
        //get width and height types bu visiting children
        Type width = (Type)dimension.getWidth().visit(this, arg);
        Type length = (Type)dimension.getHeight().visit(this, arg);
        //check if types are int
        if (width == Type.INT && length == Type.INT) {
            return dimension;
        }
        throw new TypeCheckException("Not int dimension");
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // iterate over each guarded block within the DoStatement
        List<GuardedBlock> guardList = doStatement.getGuardedBlocks();
        for(GuardedBlock guardedBlock : guardList) {
            guardedBlock.visit(this, arg);
        }
        return doStatement;
    }


    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        //visit children and get types
        Type red = (Type)expandedPixelExpr.getRed().visit(this, arg);
        Type blue = (Type)expandedPixelExpr.getBlue().visit(this, arg);
        Type green = (Type)expandedPixelExpr.getGreen().visit(this, arg);
        //check if children types are int
        if (red == Type.INT && green == Type.INT && blue == Type.INT) {
            expandedPixelExpr.setType(Type.PIXEL);
            return Type.PIXEL;
        }
        throw new TypeCheckException("Expanded pixel not all int");
    }

    @Override
    public Object visitGuardedBlock(GuardedBlock guardedBlock, Object arg) throws PLCCompilerException {
        //check if expr child has type bool
        if ((Type)guardedBlock.getGuard().visit(this, arg) == Type.BOOLEAN) {
            //visit children block child
            guardedBlock.getBlock().visit(this, arg);
            return guardedBlock;
        }
        throw new TypeCheckException("Guard type is not boolean");
    }

    @Override
    public Object visitIdentExpr(IdentExpr identExpr, Object arg) throws PLCCompilerException {
        //check if ident exist in symboltable
        if(ST.lookup(identExpr.getName()) != null) {
            //if in symbol table set the name def and type
            identExpr.setNameDef(ST.lookup(identExpr.getName()));
            identExpr.setType(identExpr.getNameDef().getType());
            return identExpr.getType();
        }
        throw new TypeCheckException("Doesn't exist in symbolTable");
    }

    @Override
    public Object visitIfStatement(IfStatement ifStatement, Object arg) throws PLCCompilerException {
        // iterate over each guarded block within the IfStatement
        List<GuardedBlock> blockList = ifStatement.getGuardedBlocks();
        for(GuardedBlock guardedBlock : blockList) {
            // visit and process the statement block associated with the guard
            guardedBlock.visit(this, arg);
        }
        return ifStatement;
    }


    @Override
    public Object visitLValue(LValue lValue, Object arg) throws PLCCompilerException {
        // if there's a pixel selector, the LValue refers to a pixel of the image
        //set nameDef and var type
        lValue.setNameDef(ST.lookup(lValue.getName()));
        Type varType = (Type) lValue.getNameDef().visit(this, arg);
        //Check if varType and channel and pixel are valid combinations
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
        nameDef.setJavaName(nameDef.getName()+"$n" + Integer.toString(ST.getScope()));
        ST.add(nameDef.getName(), nameDef);
    
        return type;
        
    }

    @Override
    public Object visitNumLitExpr(NumLitExpr numLitExpr, Object arg) throws PLCCompilerException {
        //set type as int
        numLitExpr.setType(Type.INT);
        return Type.INT;
    }

    @Override
    public Object visitPixelSelector(PixelSelector pixelSelector, Object arg) throws PLCCompilerException {
        //check if parent is Lvalue
        if ((boolean)arg == true) {
            boolean xTypeB = pixelSelector.xExpr() instanceof IdentExpr; 
            boolean yTypeB = pixelSelector.yExpr() instanceof IdentExpr;
            //check if x and y are ident or numlitexpr
            if ((xTypeB || pixelSelector.yExpr() instanceof NumLitExpr) && (yTypeB || pixelSelector.yExpr() instanceof NumLitExpr)) {
                String temp = "";
                String temp2 = "";
                if (xTypeB) {
                    IdentExpr trash = (IdentExpr) pixelSelector.xExpr();
                    temp = trash.getName();
                }
                if (yTypeB) {
                    IdentExpr trash2 = (IdentExpr) pixelSelector.yExpr();
                    temp2 = trash2.getName();
                }
                
                //if x doesnt exist in symboltable then add it as a synthetic name def do same for y
                if (xTypeB && ST.lookup(temp) == null) {
                    SyntheticNameDef hole = new SyntheticNameDef(temp);
                    hole.setJavaName(hole.getName()+"$n" + Integer.toString(ST.getScope()));
                    ST.add(hole.getName(), hole);
                }
                if (yTypeB && ST.lookup(temp2) == null) {
                    SyntheticNameDef holey = new SyntheticNameDef(temp2);
                    holey.setJavaName(holey.getName()+"$n" + Integer.toString(ST.getScope()));
                    ST.add(holey.getName(), holey);
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

        // if there's a pixel selection post-fix operator, visit it and check if combinations are valid
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
        program.getBlock().visit(this, type);
        ST.leaveScope();
        return type;
    }

    @Override
    public Object visitReturnStatement(ReturnStatement returnStatement, Object arg) throws PLCCompilerException {
        //check and see if children type is same as program type if not throw error
        if ((Type)returnStatement.getE().visit(this, arg) == (Type)arg ) {
            return returnStatement;
        }
        else throw new TypeCheckException("return type and prog type not same");
    }

    @Override
    public Object visitStringLitExpr(StringLitExpr stringLitExpr, Object arg) throws PLCCompilerException {
        //set type as string
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
        if ((op == Kind.RES_width || op == Kind.RES_height) && exprType == Type.IMAGE) {
            unaryExpr.setType(Type.INT);
            return Type.INT;
        }
        throw new TypeCheckException("Invalid unary operation");
    }


    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        //visit children
        writeStatement.getExpr().visit(this, arg);
        return writeStatement;
    }

    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        //set type as bool
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

