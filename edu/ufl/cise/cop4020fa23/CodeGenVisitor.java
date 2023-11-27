package edu.ufl.cise.cop4020fa23;

import static edu.ufl.cise.cop4020fa23.Kind.STRING_LIT;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assumptions.abort;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import edu.ufl.cise.cop4020fa23.ast.Expr;
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
import edu.ufl.cise.cop4020fa23.exceptions.CodeGenException;
import edu.ufl.cise.cop4020fa23.exceptions.PLCCompilerException;
import edu.ufl.cise.cop4020fa23.runtime.FileURLIO;
import edu.ufl.cise.cop4020fa23.runtime.ImageOps;
import edu.ufl.cise.cop4020fa23.runtime.PixelOps;

public class CodeGenVisitor implements ASTVisitor{
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
        Boolean imported = false;
        //Map<Boolean, StringBuilder> hash = new HashMap<Boolean, StringBuilder>(); 
        LinkedHashSet<Object> set = new LinkedHashSet<Object> (); 
        StringBuilder javaString = new StringBuilder();
        javaString.append("{ ");
        List<BlockElem> blockList = block.getElems();
        for (BlockElem elem : blockList) {
            Object temp = elem.visit(this, arg);

            if (temp instanceof LinkedHashSet) {
                for (Object itr : (LinkedHashSet)temp) { 
                    if (itr.toString().contains("import")) {
                        if (!set.contains(itr)) {
                        System.out.println("true");
                        set.add(itr);
                        }
                    }
                    else { 
                        javaString.append(itr.toString()); 
                    }
                } 
            }
            else {
                javaString.append(temp);
            }
            javaString.append("; ");
        }
        javaString.append("}");
        set.add(javaString);
        return set;
    }

    @Override
    public Object visitBlockStatement(StatementBlock statementBlock, Object arg) throws PLCCompilerException {
    
        return statementBlock.getBlock().visit(this, arg);
    }

    @Override
    public Object visitChannelSelector(ChannelSelector channelSelector, Object arg) throws PLCCompilerException {
        if (arg.equals("PostFixExpr")) {
            switch (channelSelector.color()) {
                case RES_red: return "Red";
                case RES_blue: return "Blue";
                case RES_green: return "Green";
                default: break;
            }
        }
        throw new CodeGenException("Unsuporrted");
       
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
        LinkedHashSet<Object> stringSet = new LinkedHashSet<Object> ();
        if (declaration.getNameDef().getType() != Type.IMAGE) {
            javaString.append(declaration.getNameDef().visit(this, arg));
            if (declaration.getInitializer() == null) {
                return javaString;
            }
            javaString.append(" = ");
            javaString.append(declaration.getInitializer().visit(this, arg));
            return javaString;
        }
        else {
            //NameDef type is image
            javaString.append("final BufferedImage ");
            javaString.append(declaration.getNameDef().getJavaName()); 
            if (declaration.getInitializer() == null) { //case for when namedef is image but no expr
                if (declaration.getNameDef().getDimension() == null) {
                    throw new CodeGenException("No dimension in declaration");
                }
                javaString.append(" = ImageOps.makeImage(");
                javaString.append(declaration.getNameDef().getDimension().visit(this, arg));
                javaString.append(")");
                return javaString;
            }
            if (declaration.getInitializer().getType() == Type.STRING) { //if there is expr and its a string
                stringSet.add("import edu.ufl.cise.cop4020fa23.runtime.FileURLIO");
                javaString.append(" = FileURLIO.readImage( ");
                javaString.append(declaration.getInitializer().visit(this, arg));
                if (declaration.getNameDef().getDimension() != null) {
                    javaString.append(", ");
                    javaString.append(declaration.getNameDef().getDimension().visit(this, arg));
                }
                javaString.append(" )");
            }
            else if (declaration.getInitializer().getType() == Type.IMAGE) {
                stringSet.add("import edu.ufl.cise.cop4020fa23.runtime.FileURLIO");
                if (declaration.getNameDef().getDimension() == null) {
                    javaString.append(" = ImageOps.cloneImage(");
                    javaString.append(declaration.getInitializer().visit(this, arg));
                    javaString.append(")");
                }
                else {
                    javaString.append(" = ImageOps.copyAndResize(");
                    javaString.append(declaration.getInitializer().visit(this, arg));
                    javaString.append(")");
                }
            }
            stringSet.add(javaString);
            return stringSet;
        }
    }

    @Override
    public Object visitDimension(Dimension dimension, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append(dimension.getWidth().visit(this, arg));
        javaString.append(", ");
        javaString.append(dimension.getHeight().visit(this, arg));
        return javaString;
    }

    @Override
    public Object visitDoStatement(DoStatement doStatement, Object arg) throws PLCCompilerException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'visitDoStatement'");
    }

    @Override
    public Object visitExpandedPixelExpr(ExpandedPixelExpr expandedPixelExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        javaString.append("PixelOps.pack( ");
        javaString.append(expandedPixelExpr.getRed().visit(this, arg));
        javaString.append(expandedPixelExpr.getGreen().visit(this, arg));
        javaString.append(expandedPixelExpr.getBlue().visit(this, arg));
        javaString.append(" )");
        return javaString;
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
            case IMAGE:
                javaString.append("Bufferedimage");
                break;
            case PIXEL:
                javaString.append("int");
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
            case BOOLEAN:
                javaString.append("boolean");
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
        StringBuilder javaString = new StringBuilder();
        javaString.append(pixelSelector.xExpr().visit(this, arg));
        javaString.append(", ");
        javaString.append(pixelSelector.yExpr().visit(this, arg));
        return javaString;
    }

    @Override
    public Object visitPostfixExpr(PostfixExpr postfixExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        if (postfixExpr.getType() == Type.PIXEL) {
            javaString.append(postfixExpr.channel().visit(this, arg));
            javaString.append("( ");
            javaString.append(postfixExpr.primary().visit(this, arg));
            javaString.append(" )");
            return javaString;
        }
        if (postfixExpr.pixel() != null && postfixExpr.channel() == null) {
            javaString.append("ImageOps.getRGB(");
            javaString.append(postfixExpr.primary().visit(this, arg));
            javaString.append(", ");
            javaString.append(postfixExpr.pixel().visit(this, arg));
            javaString.append(" )");
        }
        else if (postfixExpr.pixel() != null && postfixExpr.channel() != null) {
            javaString.append(postfixExpr.channel().visit(this, "PostFixExpr"));
            javaString.append(" (ImageOps.getRGB( ");
            javaString.append(postfixExpr.primary().visit(this, arg));
            javaString.append(", ");
            javaString.append(postfixExpr.pixel().visit(this, arg));
            javaString.append(" ))");
        }
        else if (postfixExpr.pixel() == null && postfixExpr.channel() != null) {
            javaString.append("ImageOps.extract");
            javaString.append(postfixExpr.channel().visit(this, arg));
            javaString.append("( ");
            javaString.append(postfixExpr.primary().visit(this, arg));
            javaString.append(" )");
        }
        return javaString;
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
       
        switch(program.getType()) {
            case IMAGE: 
                javaString.append("BufferedImage");
                break;
            case BOOLEAN:
                javaString.append("Boolean");
                break;
            case INT:
                javaString.append("int");
                break;
            case PIXEL:
                javaString.append("int");
                break;
            case STRING:
                javaString.append("String");
                break;
            case VOID:
                javaString.append("void");
                break;
        }
        javaString.append(" apply(");
        List<NameDef> paramList = program.getParams();
        for (NameDef parameters : paramList) {
            javaString.append(parameters.visit(this, arg));
            if (paramList.size() != 1 && parameters != paramList.get(paramList.size()-1)) {
                javaString.append(", ");
            }
        }
        javaString.append(") ");
        LinkedHashSet <Object> retSet = (LinkedHashSet<Object>)program.getBlock().visit(this, arg);
        for (Object itr : retSet) { 
            if (itr.toString().contains("import")) {
                javaString.insert(34, itr);
            }
            else {
                javaString.append(itr);
            }
        } 
        
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
            case RES_height:
                javaString.append("(");
                javaString.append(unaryExpr.getExpr().visit(this, arg));
                javaString.append(".getHeight())");
                return javaString;
            case RES_width:
                javaString.append("(");
                javaString.append(unaryExpr.getExpr().visit(this, arg));
                javaString.append(".getWidth())");
                return javaString;
            default:
                break;
        }
        javaString.append(unaryExpr.getExpr().visit(this, arg));
        javaString.append(")");
        return javaString;
    }

    @Override
    public Object visitWriteStatement(WriteStatement writeStatement, Object arg) throws PLCCompilerException {
        LinkedHashSet<Object> set = new LinkedHashSet<Object> (); 
        StringBuilder javaString = new StringBuilder();
        set.add("import edu.ufl.cise.cop4020fa23.runtime.ConsoleIO; ");
        javaString.append("ConsoleIO.write(");
        javaString.append(writeStatement.getExpr().visit(this, arg));
        javaString.append(")");
        set.add(javaString);
        
        return set;
    }


    @Override
    public Object visitBooleanLitExpr(BooleanLitExpr booleanLitExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        if (booleanLitExpr.getText().equals("TRUE")) {
            javaString.append("true");
            return javaString;
        }
        javaString.append("false");
        return javaString;
    }


    @Override
    public Object visitConstExpr(ConstExpr constExpr, Object arg) throws PLCCompilerException {
        StringBuilder javaString = new StringBuilder();
        switch (constExpr.getName()) {
            case "Z":
                javaString.append("255");
                break;
            case "RED":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.RED.getRGB()));
                break;
            case "GREEN":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.GREEN.getRGB()));
                break;
            case "BLUE":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.BLUE.getRGB()));
                break;
            case "YELLOW":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.YELLOW.getRGB()));
                break;
            case "MAGENTA":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.MAGENTA.getRGB()));
                break;
            case "CYAN":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.CYAN.getRGB()));
                break;
            case "WHITE":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.WHITE.getRGB()));
                break;
            case "BLACK":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.BLACK.getRGB()));
                break;
            case "GRAY":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.GRAY.getRGB()));
                break;
            case "LIGHT_GRAY":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.LIGHT_GRAY.getRGB()));
                break;
            case "DARK_GRAY":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.DARK_GRAY.getRGB()));
                break;
            case "PINK":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.PINK.getRGB()));
                break;
            case "ORANGE":
                javaString.append("0x" + Integer.toHexString(java.awt.Color.ORANGE.getRGB()));
                break;
        }
        return javaString;
    }

    
}
