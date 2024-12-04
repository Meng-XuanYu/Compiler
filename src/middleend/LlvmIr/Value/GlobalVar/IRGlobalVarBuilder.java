package middleend.LlvmIr.Value.GlobalVar;

import frontend.Parser.ParserTreeNode;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;
import frontend.SymbolParser.SymbolType;
import middleend.LlvmIr.Types.IRIntArrayType;
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
            boolean ischar = declElement.getChildren().get(1).getChildren().get(0).getToken().type() == TokenType.CHARTK;
            for (int i = 2; i < declElement.getChildren().size(); i+=2) {
                globalVars.add(generateConstGlobalVar(declElement.getChildren().get(i), ischar));
            }
        } else if (declElement.getType() == SyntaxType.VarDecl) {
            boolean ischar = declElement.getChildren().get(0).getChildren().get(0).getToken().type() == TokenType.CHARTK;
            for (int i = 1; i < declElement.getChildren().size(); i+=2) {
                globalVars.add(generateVarGlobalVar(declElement.getChildren().get(i), ischar));
            }
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
        setConstInit(symbolConst, constDef.getChildren().get(constDef.getChildren().size() - 1), this.symbolTable);
        this.symbolTable.addSymbol(symbolConst);

        IRGlobalVar globalVar = null;
        String name = "@GlobalConst_" + IRGlobalVarNameCnt.getCount();
        if (isChar && constDef.hasLbrack()) {
            ArrayList<Integer> initVal = symbolConst.getValueIntArray();
            ArrayList<IRConstantInt> constantInts = new ArrayList<>();
            for (int i = 0; i < initVal.size(); i++) {
                constantInts.add(new IRConstantInt(initVal.get(i), IRIntegerType.get8()));
            }
            IRIntArrayType irIntArrayType = new IRIntArrayType(IRIntegerType.get8(), initVal.size());
            IRConstantIntArray constantIntArray = new IRConstantIntArray(constantInts, constantInts.size(), IRIntegerType.get8());
            globalVar = new IRGlobalVar(irIntArrayType, name, constantIntArray, true);
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
            IRIntArrayType irIntArrayType = new IRIntArrayType(IRIntegerType.get32(), initVal.size());
            IRConstantIntArray constantIntArray = new IRConstantIntArray(constantInts,constantInts.size() , IRIntegerType.get32());
            globalVar = new IRGlobalVar(irIntArrayType, name, constantIntArray, true);
            symbolConst.setValue(globalVar);
        } else {
            IRConstantInt constantInt = new IRConstantInt(symbolConst.getValueInt(), IRIntegerType.get32());
            globalVar = new IRGlobalVar(IRIntegerType.get32(), name, constantInt, true);
            symbolConst.setValue(globalVar);
        }
        return globalVar;
    }

    // const的情况
    public static void setConstInit(Symbol symbol, ParserTreeNode constInitVal, SymbolTable symbolTable) {
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
            setVarInit(symbolVar, varDef.getLastChild(), this.symbolTable);
        } else {
            // 没有初始化
            setVarInit(symbolVar, null, this.symbolTable);
        }
        this.symbolTable.addSymbol(symbolVar);

        IRGlobalVar globalVar;
        String name = "@GlobalVar_" + IRGlobalVarNameCnt.getCount();


        if (isChar && varDef.hasLbrack()) {
            int size = varDef.getArraySize(this.symbolTable);
            ArrayList<Integer> initVal = symbolVar.getInitValArray();
            ArrayList<IRConstantInt> constantInts = new ArrayList<>();
            if (initVal.size() > 0) {
                for (int i = 0; i < size; i++) {
                    if (i < initVal.size()) {
                        constantInts.add(new IRConstantInt(initVal.get(i), IRIntegerType.get8()));
                    } else {
                        constantInts.add(new IRConstantInt(0, IRIntegerType.get8()));
                    }
                }
            }
            IRIntArrayType irIntArrayType = new IRIntArrayType(IRIntegerType.get8(), size);
            IRConstantIntArray constantIntArray = new IRConstantIntArray(constantInts, size, irIntArrayType);
            globalVar = new IRGlobalVar(irIntArrayType, name, constantIntArray, false);
            symbolVar.setValue(globalVar);
        } else if (isChar) {
            IRConstantInt constantInt = new IRConstantInt(symbolVar.getInitVal(), IRIntegerType.get8());
            globalVar = new IRGlobalVar(IRIntegerType.get8(), name, constantInt, false);
            symbolVar.setValue(globalVar);
        } else if (varDef.hasLbrack()) {
            int size = varDef.getArraySize(this.symbolTable);
            ArrayList<Integer> initVal = symbolVar.getInitValArray();
            ArrayList<IRConstantInt> constantInts = new ArrayList<>();
            if (initVal.size() > 0) {
                for (int i = 0; i < size; i++) {
                    if (i < initVal.size()) {
                        constantInts.add(new IRConstantInt(initVal.get(i), IRIntegerType.get32()));
                    } else {
                        constantInts.add(new IRConstantInt(0, IRIntegerType.get32()));
                    }
                }
            }
            IRIntArrayType irIntArrayType = new IRIntArrayType(IRIntegerType.get32(), size);
            IRConstantIntArray constantIntArray = new IRConstantIntArray(constantInts, size, irIntArrayType);
            globalVar = new IRGlobalVar(irIntArrayType, name, constantIntArray, false);
            symbolVar.setValue(globalVar);
        } else {
            IRConstantInt constantInt = new IRConstantInt(symbolVar.getInitVal(), IRIntegerType.get32());
            globalVar = new IRGlobalVar(IRIntegerType.get32(), name, constantInt, false);
            symbolVar.setValue(globalVar);
        }
        return globalVar;
    }

    // var的情况
    public static void setVarInit(SymbolVar symbolVar, ParserTreeNode initVal, SymbolTable symbolTable) {
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
                symbolVar.setInitVal(initVal.getFirstChild().calIntInitVal(symbolTable));
            }
        } else {
            // 没有初始化,全部初始化为0
            if (symbolVar.isArray()) {
                ArrayList<Integer> initVals = new ArrayList<>();
                symbolVar.setInitValArray(initVals);
                symbolVar.setAll0();
            } else {
                symbolVar.setInitVal(0);
            }
        }
    }
}
