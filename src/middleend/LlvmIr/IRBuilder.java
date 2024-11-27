package middleend.LlvmIr;

import frontend.Parser.ParserTreeNode;
import middleend.LlvmIr.Value.Function.IRFunctionBuilder;
import middleend.LlvmIr.Value.GlobalVar.IRGlobalVar;
import middleend.LlvmIr.Value.GlobalVar.IRGlobalVarBuilder;
import middleend.Symbol.SymbolTable;

import java.util.ArrayList;

public class IRBuilder {
    private ParserTreeNode root;
    private SymbolTable symbolTable;
    private IRModule module;

    public IRBuilder(ParserTreeNode root) {
        this.root = root;
        this.symbolTable = new SymbolTable();
        this.module = new IRModule();
    }

    public IRModule generateIRModule() {
        // 全局变量部分
        ArrayList<ParserTreeNode> decls = root.getDecls();
        for (ParserTreeNode decl : decls) {
            IRGlobalVarBuilder globalVarBuilder = new IRGlobalVarBuilder(symbolTable, decl);
            ArrayList<IRGlobalVar> globalVars = globalVarBuilder.genIrGlobalVar();
            for (IRGlobalVar globalVar : globalVars) {
                if (this.module != null) {
                    this.module.addGlobalVar(globalVar);
                }
            }
        }
        for (ParserTreeNode funcDef : root.getFuncDefs()) {
            SymbolTable table = new SymbolTable(symbolTable);
            IRFunctionBuilder functionBuilder = new IRFunctionBuilder(table, funcDef, this.module);
            this.module.addFunction(functionBuilder.generateIRFunction());
        }
        SymbolTable table = new SymbolTable(symbolTable);
        IRFunctionBuilder functionBuilder = new IRFunctionBuilder(table, root.getLastChild(), this.module);
        this.module.addFunction(functionBuilder.generateIRFunction());
        return module;
    }
}
