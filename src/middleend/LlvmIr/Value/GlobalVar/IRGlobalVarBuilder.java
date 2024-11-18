package middleend.LlvmIr.Value.GlobalVar;

import frontend.Parser.ParserTreeNode;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;
import frontend.SymbolParser.SymbolType;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Value.Constant.IRConstantInt;
import middleend.LlvmIr.Value.Constant.IRConstantIntArray;
import middleend.Symbol.Symbol;
import middleend.Symbol.SymbolConst;
import middleend.Symbol.SymbolTable;
import middleend.Symbol.SymbolVar;

import java.util.ArrayList;

public class IRGlobalVarBuilder {
    private final SymbolTable symbolTable;
    private final ParserTreeNode decl;

    public IRGlobalVarBuilder(SymbolTable symbolTable, ParserTreeNode decl) {
        this.symbolTable = symbolTable;
        this.decl = decl;
    }

    public ArrayList<IRGlobalVar> genIrGlobalVar() {
        ArrayList<IRGlobalVar> globalVars = new ArrayList<>(); // 因为一个声明可能对应多个全局变量

        ParserTreeNode declElement = decl.getChildren().get(0);
        if (declElement.getType() == SyntaxType.ConstDecl) {
            ParserTreeNode constDecl = declElement;
            boolean ischar = constDecl.getChildren().get(1).getChildren().get(0).getToken().type() == TokenType.CHARTK;
            for (int i = 2; i < constDecl.getChildren().size(); i+=2) {
                globalVars.add(generateConstGlobalVar(constDecl.getChildren().get(i), ischar));
            }
        } else if (declElement.getType() == SyntaxType.VarDecl) {
            ParserTreeNode varDecl = declElement;

            globalVars.add(globalVar);
        } else {
            System.err.println("Error in IRGlobalVarBuilder: unexpected decl element type");
        }
        return globalVars;
    }

    // constVar生成
    private IRGlobalVar generateConstGlobalVar(ParserTreeNode constDef, boolean isChar) {
        Token ident = constDef.getChildren().get(0).getToken();

        SymbolType symbolType;
        if (isChar && constDef.hasLbrack()) {
            symbolType = SymbolType.ConstCharArray;
        } else if (isChar) {
            symbolType = SymbolType.ConstChar;
        } else if (constDef.hasLbrack()) {
            symbolType = SymbolType.ConstIntArray;
        } else {
            symbolType = SymbolType.ConstInt;
        }
        SymbolConst symbolConst = new SymbolConst(ident.value(), symbolType);
        setConstInit(symbolConst, constDef.getChildren().get(constDef.getChildren().size() - 1));
        this.symbolTable.addSymbol(symbolConst);

        IRGlobalVar globalVar = null;
        String name = "GlobalConst_" + IRGlobalVarNameCnt.getCount();
        if (isChar && constDef.hasLbrack()) {
            ArrayList<Integer> initVal = symbolConst.getValueIntArray();
            ArrayList<IRConstantInt> constantInts = new ArrayList<>();
            for (int i = 0; i < initVal.size(); i++) {
                constantInts.add(new IRConstantInt((int) initVal.get(i), IRIntegerType.get8()));
            }
            IRConstantIntArray constantIntArray = new IRConstantIntArray(constantInts, constantInts.size(), IRIntegerType.get8());
            globalVar = new IRGlobalVar(IRIntegerType.get8(), name, constantIntArray, true);
            symbolConst.setValue(globalVar);
        } else if (isChar) {
            IRConstantInt constantInt = new IRConstantInt(symbolConst.getValueInt(), IRIntegerType.get8());
            globalVar = new IRGlobalVar(IRIntegerType.get8(), name, constantInt, true);
            symbolConst.setValue(globalVar);
        } else if (constDef.hasLbrack()) {
            ArrayList<Integer> initVal = symbolConst.getValueIntArray();
            ArrayList<IRConstantInt> constantInts = new ArrayList<>();
            for (int i = 0; i < initVal.size(); i++) {
                constantInts.add(new IRConstantInt(initVal.get(i), IRIntegerType.get32()));
            }
            IRConstantIntArray constantIntArray = new IRConstantIntArray(constantInts,constantInts.size() , IRIntegerType.get32());
            globalVar = new IRGlobalVar(IRIntegerType.get32(), name, constantIntArray, true);
            symbolConst.setValue(globalVar);
        } else {
            IRConstantInt constantInt = new IRConstantInt(symbolConst.getValueInt(), IRIntegerType.get32());
            globalVar = new IRGlobalVar(IRIntegerType.get32(), name, constantInt, true);
            symbolConst.setValue(globalVar);
        }
        return globalVar;
    }

    // const的情况
    private void setConstInit(Symbol symbol, ParserTreeNode constInitVal) {
        // const的情况一定会有initVal
        SymbolConst symbolConst = (SymbolConst) symbol;
        if (symbol.isArray()) {
            ArrayList<Integer> initVal = new ArrayList<>();
            for (ParserTreeNode exp : constInitVal.getInitValList()) {
                initVal.add(exp.calIntInitVal(symbolTable));
            }
            symbolConst.setValueIntArray(initVal);
        } else {
            symbolConst.setValueInt(constInitVal.calIntInitVal(symbolTable));
            // char和int 都用int存储
        }
    }

    // var生成
    private IRGlobalVar generateVarGlobalVar(ParserTreeNode varDef, boolean isChar) {
        Token ident = varDef.getChildren().get(0).getToken();
        SymbolType symbolType;

        if (isChar && varDef.hasLbrack()) {
            symbolType = SymbolType.CharArray;
        } else if (isChar) {
            symbolType = SymbolType.Char;
        } else if (varDef.hasLbrack()) {
            symbolType = SymbolType.IntArray;
        } else {
            symbolType = SymbolType.Int;
        }

        SymbolVar symbolVar = new SymbolVar(ident.value(), symbolType);
        if (varDef.varDefHasAssign()) {
            // 有初始化
            setVarInit(symbolVar, varDef.getChildren().get(varDef.getChildren().size() - 1), isChar);
        } else {
            // 没有初始化
            setVarInit(symbolVar, null, isChar);
        }
        this.symbolTable.addSymbol(symbolVar);
    }

    // var的情况
    private void setVarInit(SymbolVar symbolVar, ParserTreeNode initVal, boolean isChar) {
        if (initVal != null) {
            if (symbolVar.isArray()) {
                // 数组的初始化
                ArrayList<Integer> initVals = new ArrayList<>();
                for (ParserTreeNode exp : initVal.getInitValList()) {
                    initVals.add(exp.calIntInitVal(symbolTable));
                }
                symbolVar.setInitValArray(initVals);
            } else {
                // 非数组的初始化, initVal是Exp
                symbolVar.setInitVal(initVal.calIntInitVal(symbolTable));
            }
        } else {
            // 没有初始化,全部初始化为0
            if (symbolVar.isArray()) {
                ArrayList<Integer> initVals = new ArrayList<>();
                for (int i = 0; i < symbolVar.getDimension(); i++) {
                    initVals.add(0);
                }
                symbolVar.setInitValArray(initVals);
            } else {
                symbolVar.setInitVal(0);
            }
        }
    }
}
