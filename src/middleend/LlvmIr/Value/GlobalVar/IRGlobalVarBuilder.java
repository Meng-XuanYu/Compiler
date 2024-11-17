package middleend.LlvmIr.Value.GlobalVar;

import frontend.Parser.ParserTreeNode;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;
import frontend.SymbolParser.SymbolParser;
import frontend.SymbolParser.SymbolTableParser;
import frontend.SymbolParser.SymbolType;
import middleend.Symbol.Symbol;

import java.util.ArrayList;

public class IRGlobalVarBuilder {
    private final SymbolTableParser symbolTableParser;
    private final ParserTreeNode decl;

    public IRGlobalVarBuilder(SymbolTableParser symbolTableParser, ParserTreeNode decl) {
        this.symbolTableParser = symbolTableParser;
        this.decl = decl;
    }

    public ArrayList<IRGlobalVar> genIrGlobalVar() {
        ArrayList<IRGlobalVar> globalVars = new ArrayList<>(); // 因为一个声明可能对应多个全局变量

        ParserTreeNode declElement = decl.getChildren().get(0);
        if (declElement.getType() == SyntaxType.ConstDecl) {
            ParserTreeNode constDecl = declElement;
            boolean ischar = constDecl.getChildren().get(1).getChildren().get(0).getToken().type() == TokenType.CHARTK;
            globalVars.add(globalVar);
        } else if (declElement.getType() == SyntaxType.VarDecl) {
            ParserTreeNode varDecl = declElement;

            globalVars.add(globalVar);
        } else {
            System.err.println("Error in IRGlobalVarBuilder: unexpected decl element type");
        }
        return globalVars;
    }

    // constVar生成
    private IRGlobalVar generateGlobalVar(ParserTreeNode constDef, boolean isChar) {
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
        Symbol symbol = new Symbol(ident.value(), symbolType);

        IRGlobalVar globalVar = new IRGlobalVar();
        return globalVar;
    }
}
