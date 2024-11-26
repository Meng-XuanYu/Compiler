package middleend.LlvmIr.Value.Function;

import frontend.Parser.ParserTreeNode;
import frontend.SymbolParser.SymbolType;
import frontend.SyntaxType;
import frontend.TokenType;
import middleend.LlvmIr.IRModule;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.*;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.BasicBlock.IRBlockBuilder;
import middleend.Symbol.SymbolFunc;
import middleend.Symbol.SymbolTable;
import middleend.Symbol.SymbolVar;

import java.util.ArrayList;

public class IRFunctionBuilder {
    private ParserTreeNode funcDef; // root语法树节点
    private IRModule module; // 父Module
    private FunctionCnt functionCnt; // 函数变量名计数器，只属于某个函数
    private SymbolTable symbolTable;
    private ParserTreeNode mainFuncDef; // 主函数
    private SymbolFunc symbolFunc;

    public IRFunctionBuilder(SymbolTable symbolTable, ParserTreeNode funcDef, IRModule module) {
        if (funcDef.getType() == SyntaxType.FuncDef) {
            this.funcDef = funcDef;
            this.functionCnt = new FunctionCnt();
            this.mainFuncDef = null;
            this.symbolTable = symbolTable;
            this.module = module;
        } else {
            // 主函数
            this.symbolTable = symbolTable;
            this.mainFuncDef = funcDef;
            this.funcDef = null;
            this.module = module;
            this.functionCnt = new FunctionCnt();
        }
    }

    public IRFunction generateIRFunction() {
        addFuncSymbol(this.funcDef); // 把函数名加入父符号表，并且定义symbolFunc
        if (this.funcDef != null) {
            return IRFunctionStandard();
        } else {
            return IRFunctionMain();
        }
    }

    // 负责把函数名加入父符号表
    private void addFuncSymbol(ParserTreeNode funcDef) {
        SymbolType symbolType;
        if (funcDef.getChildren().get(0).getChildren().get(0).getToken() != null
                && funcDef.getChildren().get(0).getChildren().get(0).getToken().type() == TokenType.CHARTK) {
            symbolType = SymbolType.CharFunc;
        } else if (funcDef.getChildren().get(0).getChildren().get(0).getToken() != null
                && funcDef.getChildren().get(0).getChildren().get(0).getToken().type() == TokenType.INTTK) {
            symbolType = SymbolType.IntFunc; // main函数也是int类型
        } else {
            symbolType = SymbolType.VoidFunc;
        }

        String name = (funcDef.getType() == SyntaxType.MainFuncDef) ? "main" : funcDef.getChildren().get(1).getToken().value();
        SymbolFunc symbolFunc = new SymbolFunc(name, symbolType);
        this.symbolTable.getParent().addSymbol(symbolFunc);
        this.symbolFunc = symbolFunc;
    }

    // 处理普通函数
    private IRFunction IRFunctionStandard() {
        ArrayList<IRValueType> paramTypes = new ArrayList<>();
        ArrayList<IRValue> paramValues = new ArrayList<>();
        ParserTreeNode funcFParams = this.funcDef.getFuncFParams();
        if (funcFParams != null) {
            for (int i = 0; i < funcFParams.getChildren().size(); i+=2) {
                ParserTreeNode funcFParam = funcFParams.getChildren().get(i);
                paramTypes.add(getParamType(funcFParam));
                paramValues.add(addSymbol(funcFParam));
            }
        }
        IRValueType returnType = (this.funcDef.getFirstChild().getFirstChild().getToken().type() == TokenType.INTTK) ?
                IRIntegerType.get32() : (this.funcDef.getFirstChild().getFirstChild().getToken().type() == TokenType.CHARTK) ?
                IRIntegerType.get8() : IRVoidType.getVoidType();
        IRFunctionType functionType = new IRFunctionType(returnType, paramTypes);
        functionType.setParameters(paramValues);

        IRFunction function = new IRFunction(functionType, this.module, "@" + this.funcDef.getChildren().get(1).getToken().value(), this.functionCnt);

        this.symbolFunc.setValue(function);

        // Block
        ParserTreeNode block = this.funcDef.getLastChild();
        IRBlockBuilder blockBuilder = new IRBlockBuilder(block,this.symbolTable, this.functionCnt,null,null);
        function.addBlocks(blockBuilder.generateIRBlocks());
        return function;
    }

    // 添加参数符号,生成该变量在LLVM IR中的名字
    private IRValue addSymbol(ParserTreeNode funcFParam) {
        String name = "%LocalVariable" + this.functionCnt.getCnt();

        IRValue returnValue;
        SymbolVar symbolVar;
        if (funcFParam.hasLbrack()) {
            // 1维数组
            if (funcFParam.getFirstChild().getFirstChild().getToken() != null &&
                funcFParam.getFirstChild().getFirstChild().getToken().type() == TokenType.INTTK) {
                returnValue = new IRValue(name, IRIntegerType.get32(), true);
                symbolVar = new SymbolVar(name, SymbolType.IntArray, returnValue);
            } else {
                returnValue = new IRValue(name, IRIntegerType.get8(), true);
                symbolVar = new SymbolVar(name, SymbolType.CharArray, returnValue);
            }
        } else {
            // 普通变量
            if (funcFParam.getFirstChild().getToken() != null &&
                funcFParam.getFirstChild().getToken().type() == TokenType.INTTK) {
                returnValue = new IRValue(name, IRIntegerType.get32(), true);
                symbolVar = new SymbolVar(name, SymbolType.Int, returnValue);
            } else {
                returnValue = new IRValue(name, IRIntegerType.get8(), true);
                symbolVar = new SymbolVar(name, SymbolType.Char, returnValue);
            }
        }
        this.symbolFunc.addSymbol(symbolVar);
        this.symbolTable.addSymbol(symbolVar);

        return returnValue;
    }

    // 获取参数类型
    private IRValueType getParamType(ParserTreeNode funcFParam) {
        if (funcFParam.hasLbrack()) {
            if (funcFParam.getFirstChild().getFirstChild().getToken() != null &&
                funcFParam.getFirstChild().getFirstChild().getToken().type() == TokenType.INTTK) {
                return new IRIntArrayType(IRIntegerType.get32(), -1); // -1代表未知长度
            } else {
                return new IRIntArrayType(IRIntegerType.get8(), -1);
            }
        } else {
            if (funcFParam.getFirstChild().getToken() != null &&
                funcFParam.getFirstChild().getToken().type() == TokenType.INTTK) {
                return IRIntegerType.get32();
            } else {
                return IRIntegerType.get8();
            }
        }
    }

    // 处理主函数
    private IRFunction IRFunctionMain() {
        IRValueType returnType = IRIntegerType.get32();

        ArrayList<IRValueType> paramTypes = new ArrayList<>();
        IRFunctionType functionType = new IRFunctionType(returnType, paramTypes);

        IRFunction function = new IRFunction(functionType, this.module, "main");
        this.symbolFunc.setValue(function);

        // Block
        ParserTreeNode block = this.mainFuncDef.getLastChild();
        IRBlockBuilder blockBuilder = new IRBlockBuilder(block, this.symbolTable, this.functionCnt, null, null);
        function.addBlocks(blockBuilder.generateIRBlocks());
        return function;
    }
}
