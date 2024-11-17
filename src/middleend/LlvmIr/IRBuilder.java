package middleend.LlvmIr;

import frontend.Parser.ParserTreeNode;
import frontend.SymbolParser.SymbolTableParser;

import java.util.ArrayList;

public class IRBuilder {
    private ParserTreeNode root;
    private SymbolTableParser symbolTableParser;
    private final IRModule module;

    public IRBuilder(ParserTreeNode root) {
        this.root = root;
        this.symbolTableParser = new SymbolTableParser();
        this.module = generateIRModule();
    }

    public IRModule generateIRModule() {
        IRModule module = new IRModule();

        // 全局变量部分
        ArrayList<ParserTreeNode> decls = root.getDecls();
        for (ParserTreeNode decl : decls) {
            IRGlobalVarBuilder
        }

        // 函数部分

        return module;
    }

    public IRModule getModule() {
        return this.module;
    }
}
