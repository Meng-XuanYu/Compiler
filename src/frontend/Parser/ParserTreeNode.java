package frontend.Parser;
import frontend.SymbolParser.SymbolTableParser;
import frontend.SymbolParser.SymbolType;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;
import middleend.Symbol.Symbol;
import middleend.Symbol.SymbolConst;
import middleend.Symbol.SymbolTable;
import middleend.Symbol.SymbolVar;

import java.util.ArrayList;
import java.util.Objects;

public class ParserTreeNode {
    private ParserTreeNode parent;
    private final SyntaxType type;
    private final ArrayList<ParserTreeNode> children;
    private final Token token;

    public ParserTreeNode(SyntaxType type) {
        this.parent = null;
        this.type = type;
        this.children = new ArrayList<>();
        this.token = null;
    }

    // Token 作为叶子节点
    public ParserTreeNode(Token token) {
        this.parent = null;
        this.type = SyntaxType.Token;
        this.children = new ArrayList<>();
        this.token = token;
    }

    public void addChild(ParserTreeNode child) {
        children.add(child);
        child.setParent(this);
    }

    public void setParent(ParserTreeNode parent) {
        this.parent = parent;
    }

    public ParserTreeNode getParent() {
        return parent;
    }

    public SyntaxType getType() {
        return type;
    }

    public ArrayList<ParserTreeNode> getChildren() {
        return children;
    }

    public ParserTreeNode getLastChild() {
        return children.get(children.size() - 1);
    }

    public ParserTreeNode getFirstChild() {
        return children.get(0);
    }

    public void addTokenChild(Token token) {
        children.add(new ParserTreeNode(token));
    }

    public void addSemicnChild(int line) {
        children.add(new ParserTreeNode(new Token(TokenType.SEMICN, ";",line)));
    }

    public void addRbrackChild(int line) {
        children.add(new ParserTreeNode(new Token(TokenType.RBRACK, "]",line)));
    }

    public void addRparentChild(int line) {
        children.add(new ParserTreeNode(new Token(TokenType.RPARENT, ")",line)));
    }

    // 用于输出语法树,语法分析部分
    @Deprecated
    public ArrayList<String> printTree() {
        ArrayList<String> result = new ArrayList<>();
        postOrderTraversal(this, result);
        return result;
    }

    private void postOrderTraversal(ParserTreeNode node, ArrayList<String> result) {
        if (node == null) {
            return;
        }
        for (ParserTreeNode child : node.getChildren()) {
            postOrderTraversal(child, result);
        }
        if (node.type == SyntaxType.Token) {
            result.add(Objects.requireNonNull(node.token).type() + " " + node.token.value());
        } else {
            if (node.type != SyntaxType.BType && node.type != SyntaxType.BlockItem && node.type != SyntaxType.Decl) {
                result.add('<' + node.toString() + '>');
            }
        }
    }

    @Override
    public String toString() {
        if (this.token != null) {
            return this.token.toString();
        } else {
            return this.type.toString();
        }
    }

    public Token getToken() {
        return this.token;
    }

    // 语法分析功能函数部分

    public String getCurrentFuncType() {
        ParserTreeNode node = this;
        while (node != null) {
            if (node.type == SyntaxType.FuncDef ) {
                return node.getChildren().get(0).getChildren().get(0).getToken().value();
            } else if (node.type == SyntaxType.MainFuncDef) {
                return "int";
            }
            node = node.getParent();
        }
        return null;
    }

    public SymbolType getSymbType(SymbolTableParser symbolTableParser) {
        if (this.getType() == SyntaxType.Exp) {
            return this.getChildren().get(0).getSymbType(symbolTableParser);
        } else if (this.getType() == SyntaxType.AddExp) {
            for (int i = 0; i < this.getChildren().size(); i+=2) {
                SymbolType type = getChildren().get(i).getSymbType(symbolTableParser);
                if (type == SymbolType.IntArray || type == SymbolType.CharArray) {
                    return type;
                }
            }
            return getChildren().get(0).getSymbType(symbolTableParser);
        } else if (this.getType() == SyntaxType.MulExp) {
            for (int i = 0; i < this.getChildren().size(); i+=2) {
                SymbolType type = getChildren().get(i).getSymbType(symbolTableParser);
                if (type == SymbolType.IntArray || type == SymbolType.CharArray) {
                    return type;
                }
            }
            return getChildren().get(0).getSymbType(symbolTableParser);
        } else if (this.getType() == SyntaxType.UnaryExp) {
            if (this.getChildren().get(0).getType() == SyntaxType.UnaryOp) {
                return getChildren().get(1).getSymbType(symbolTableParser);
            } else if (this.getChildren().get(0).getType() == SyntaxType.PrimaryExp) {
                return getChildren().get(0).getSymbType(symbolTableParser);
            } else {
                Token token = this.getChildren().get(0).getToken();
                return symbolTableParser.getSymbol(token.value()).type();
            }
        } else if (this.getType() == SyntaxType.PrimaryExp) {
            if (this.getChildren().get(0).getType() == SyntaxType.LVal) {
                if (symbolTableParser.getSymbol(this.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.CharArray ||
                        symbolTableParser.getSymbol(this.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.ConstCharArray) {
                    if (this.getChildren().get(0).getChildren().size() > 1 && this.getChildren().get(0).getChildren().get(1).getToken().type() == TokenType.LBRACK) {
                        return SymbolType.Char;
                    }
                }
                if (symbolTableParser.getSymbol(this.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.IntArray ||
                        symbolTableParser.getSymbol(this.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.ConstIntArray) {
                    if (this.getChildren().get(0).getChildren().size() > 1 && this.getChildren().get(0).getChildren().get(1).getToken().type() == TokenType.LBRACK) {
                        return SymbolType.Int;
                    }
                }
                return symbolTableParser.getSymbol(this.getChildren().get(0).getChildren().get(0).getToken().value()).type();
            } else if (this.getChildren().get(0).getType() == SyntaxType.Number) {
                return SymbolType.Int;
            } else if (this.getChildren().get(0).getType() == SyntaxType.Character) {
                return SymbolType.Char;
            } else {
                return getChildren().get(1).getSymbType(symbolTableParser);
            }
        } else {
            return symbolTableParser.getSymbol(this.getChildren().get(0).getToken().value()).type();
        }
    }

    // 中间代码生成功能函数部分

    // compUnit 得到包含的 decls
    public ArrayList<ParserTreeNode> getDecls() {
        ArrayList<ParserTreeNode> decls = new ArrayList<>();
        for (ParserTreeNode child : this.getChildren()) {
            if (child.getType() == SyntaxType.Decl) {
                decls.add(child);
            }
        }
        return decls;
    }

    // compUnit 得到包含的 funcDef
    public ArrayList<ParserTreeNode> getFuncDefs() {
        ArrayList<ParserTreeNode> funcDefs = new ArrayList<>();
        for (ParserTreeNode child : this.getChildren()) {
            if (child.getType() == SyntaxType.FuncDef) {
                funcDefs.add(child);
            }
        }
        return funcDefs;
    }

    // funcDef 得到函数参数
    public ParserTreeNode getFuncFParams() {
        if (this.getChildren().get(3).getType() != SyntaxType.Token) {
            return this.getChildren().get(3);
        } else {
            return null; // 右括号
        }
    }

    // block 得到包含的 blockItems
    public ArrayList<ParserTreeNode> getBlockItems() {
        ArrayList<ParserTreeNode> blockItems = new ArrayList<>();
        for (ParserTreeNode child : this.getChildren()) {
            if (child.getType() == SyntaxType.BlockItem) {
                blockItems.add(child);
            }
        }
        return blockItems;
    }

    // 子节点是否有左括号
    public boolean hasLbrack() {
        for (ParserTreeNode child : this.getChildren()) {
            if (child.getToken() != null && child.getToken().type() == TokenType.LBRACK) {
                return true;
            }
        }
        return false;
    }

    // AddExp 得到第一个 mulExp
    public ParserTreeNode getFirstMulExp() {
        if (this.children.size() == 1) {
            return this.children.get(0);
        } else {
            return this.children.get(0).getFirstMulExp();
        }
    }

    // addExp 得到所有的 mulExp
    public ArrayList<ParserTreeNode> getOtherMulExps() {
        ArrayList<ParserTreeNode> mulExps = new ArrayList<>();
        if (this.children.size() > 1) {
            mulExps.add(this.children.get(2));
            mulExps.addAll(this.children.get(0).getOtherMulExps());
        }
        return mulExps;
    }

    // AddExp 得到所有的 operad
    public ArrayList<TokenType> getOperators() {
        ArrayList<TokenType> operands = new ArrayList<>();
        if (this.children.size() > 1) {
            operands.addAll(this.children.get(0).getOperators());
            operands.add(this.children.get(1).getToken().type());
        }
        return operands;
    }

    // MulExp 得到第一个 unaryExp
    public ParserTreeNode getFirstUnaryExp() {
        if (this.children.size() == 1) {
            return this.children.get(0);
        } else {
            return this.children.get(0).getFirstUnaryExp();
        }
    }

    // mulExp 得到所有的 unaryExp
    public ArrayList<ParserTreeNode> getOtherUnaryExps() {
        ArrayList<ParserTreeNode> unaryExps = new ArrayList<>();
        if (this.children.size() > 1) {
            unaryExps.addAll(this.children.get(0).getOtherUnaryExps());
            unaryExps.add(this.children.get(2));
        }
        return unaryExps;
    }


    // print 的 exp
    public ArrayList<ParserTreeNode> getOutputExps() {
        ArrayList<ParserTreeNode> exps = new ArrayList<>();
        for (ParserTreeNode child : this.getChildren()) {
            if (child.getType() == SyntaxType.Exp) {
                exps.add(child);
            }
        }
        return exps;
    }

    // LOrExp 得到所有 LAndExp
    public ArrayList<ParserTreeNode> getLAndExps() {
        ArrayList<ParserTreeNode> lAndExps = new ArrayList<>();
        if (this.getChildren().size() == 1) {
            lAndExps.add(this.getChildren().get(0));
        } else {
            lAndExps.add(this.getChildren().get(2));
            lAndExps.addAll(this.getChildren().get(0).getLAndExps());
        }
        return lAndExps;
    }

    // LAndExp 得到所有 EqExp
    public ArrayList<ParserTreeNode> getEqExps() {
        ArrayList<ParserTreeNode> eqExps = new ArrayList<>();
        if (this.getChildren().size() == 1) {
            eqExps.add(this.getChildren().get(0));
        } else {
            eqExps.add(this.getChildren().get(2));
            eqExps.addAll(this.getChildren().get(0).getEqExps());
        }
        return eqExps;
    }

    // EqExp 得到所有 RelExp
    public ArrayList<ParserTreeNode> getRelExps() {
        ArrayList<ParserTreeNode> relExps = new ArrayList<>();
        if (this.getChildren().size() == 1) {
            relExps.add(this.getChildren().get(0));
        } else {
            relExps.addAll(this.getChildren().get(0).getRelExps());
            relExps.add(this.getChildren().get(2));
        }
        return relExps;
    }

    // EqExp 得到所有 op
    public ArrayList<TokenType> getRelops() {
        ArrayList<TokenType> relops = new ArrayList<>();
        if (this.getChildren().size() > 1) {
            relops.addAll(this.getChildren().get(0).getRelops());
            relops.add(this.getChildren().get(1).getToken().type());
        }
        return relops;
    }

    // RelExp 得到所有 AddExp
    public ArrayList<ParserTreeNode> getAddExps() {
        ArrayList<ParserTreeNode> addExps = new ArrayList<>();
        if (this.getChildren().size() == 1) {
            addExps.add(this.getChildren().get(0));
        } else {
            addExps.addAll(this.getChildren().get(0).getAddExps());
            addExps.add(this.getChildren().get(2));
        }
        return addExps;
    }

    // RelExp 得到所有 op
    public ArrayList<TokenType> getAddops() {
        ArrayList<TokenType> addops = new ArrayList<>();
        if (this.getChildren().size() > 1) {
            addops.addAll(this.getChildren().get(0).getAddops());
            addops.add(this.getChildren().get(1).getToken().type());
        }
        return addops;
    }

    // for语句
    public ParserTreeNode getForInit() {
        if (this.children.get(2).getType() == SyntaxType.Token && this.children.get(2).getToken().type() == TokenType.SEMICN) {
            return null;
        } else {
            return this.children.get(2);
        }
    }

    public ParserTreeNode getForCond() {
        for (int i = 0; i < this.children.size(); i++) {
            ParserTreeNode child = this.children.get(i);
            if (child.getToken() != null && child.getToken().type() == TokenType.SEMICN) {
                return this.children.get(i + 1);
            }
        }
        System.err.println("Error in getForCond: no SEMICN");
        return null;
    }

    public ParserTreeNode getForStep() {
        for (int i = 0; i < this.children.size(); i++) {
            ParserTreeNode child = this.children.get(i);
            if (child.getToken() != null && child.getToken().type() == TokenType.RPARENT) {
                return this.children.get(i-1);
            }
        }
        System.err.println("Error in getForStep: no RPARENT");
        return null;
    }

    // 初始化的时候的计算
    // 这个是在定义全局const变量单int的情况
    public int calIntInitVal(SymbolTable symbolTable) {
        if (this.getType() == SyntaxType.ConstInitVal || this.getType() == SyntaxType.InitVal) {
            return this.getChildren().get(0).calIntInitVal(symbolTable);
        } else if (this.getType() == SyntaxType.ConstExp) {
            return this.getChildren().get(0).calIntInitVal(symbolTable);
        } else if (this.getType() == SyntaxType.AddExp) {
            int left = this.getChildren().get(0).calIntInitVal(symbolTable);
            if (this.getChildren().size() > 1) {
                int right = this.getChildren().get(2).calIntInitVal(symbolTable);
                if (this.getChildren().get(1).getToken().type() == TokenType.PLUS) {
                    return left + right;
                } else {
                    return left - right;
                }
            }
            return left;
        } else if (this.getType() == SyntaxType.MulExp) {
            int left = this.getChildren().get(0).calIntInitVal(symbolTable);
            if (this.getChildren().size() > 1) {
                int right = this.getChildren().get(2).calIntInitVal(symbolTable);
                if (this.getChildren().get(1).getToken().type() == TokenType.MULT) {
                    return left * right;
                } else if (this.getChildren().get(1).getToken().type() == TokenType.DIV) {
                    return left / right;
                } else {
                    return left % right;
                }
            }
            return left;
        } else if (this.getType() == SyntaxType.UnaryExp) {
            if (this.getChildren().get(0).getType() == SyntaxType.UnaryOp &&
                    (this.getFirstChild().getFirstChild().getToken().type() == TokenType.MINU ||
                            this.getFirstChild().getFirstChild().getToken().type() == TokenType.PLUS)) {
                return -this.getChildren().get(1).calIntInitVal(symbolTable);
            } else if (this.getChildren().get(0).getType() == SyntaxType.PrimaryExp) {
                return this.getChildren().get(0).calIntInitVal(symbolTable);
            } else {
                // 函数调用，因为是全局变量，所以不会有函数调用
                System.err.println("Error in calIntInitVal: unexpected UnaryExp type : FuncCall");
                return 0;
            }
        } else if (this.getType() == SyntaxType.PrimaryExp) {
            if (this.getChildren().get(0).getType() == SyntaxType.LVal) {
                return this.getChildren().get(0).calIntInitVal(symbolTable);
            } else if (this.getChildren().get(0).getType() == SyntaxType.Number) {
                return Integer.parseInt(this.getFirstChild().getFirstChild().getToken().value());
            } else if (this.getChildren().get(0).getType() == SyntaxType.Character) {
                return this.getFirstChild().getFirstChild().getToken().value().charAt(0);
            } else {
                // ( Exp )
                return this.getChildren().get(1).calIntInitVal(symbolTable);
            }
        } else if (this.getType() == SyntaxType.LVal) {
            Symbol symbol = symbolTable.getSymbol(this.getChildren().get(0).getToken().value());
            if (symbol instanceof SymbolConst) {
                return ((SymbolConst) symbol).getValueInt();
            } else {
                return ((SymbolVar) symbol).getInitVal();
            }
        } else if (this.getType() == SyntaxType.Exp) {
            return this.getChildren().get(0).calIntInitVal(symbolTable);
        } else if (this.getType() == SyntaxType.Token) {
            // StringConst 传进来的全是Token的node
            return this.getToken().value().charAt(1);
        } else {
            System.err.println("Error in calIntInitVal: unexpected type");
            return 0;
        }
    }

    // 数组初始化的时候,得到数组里面的值
    public ArrayList<ParserTreeNode> getInitValList() {
        if (this.getChildren().get(0).getType() == SyntaxType.Token &&
            this.getChildren().get(0).getToken().type() == TokenType.LBRACE) {
            // { initValList }
            ArrayList<ParserTreeNode> initValList = new ArrayList<>();
            for (int i = 1; i < this.getChildren().size(); i+=2) {
                initValList.add(this.getChildren().get(i));
            }
            return initValList;
        } else {
            // StringConst 传出去全是Token的node
            String str = this.getChildren().get(0).getToken().value().substring(1, this.getChildren().get(0).getToken().value().length() - 2); // 去掉前后的双引号
            ArrayList<ParserTreeNode> initValList = new ArrayList<>();
            for (int i = 0; i < str.length(); i++) {
                initValList.add(new ParserTreeNode(new Token(TokenType.CHRCON, "'" + str.charAt(i) + "'", this.getChildren().get(0).getToken().line())));
            }
            return initValList;
        }
    }

    // 变量赋值的时候看是否有赋值
    public boolean varDefHasAssign() {
        if (this.hasLbrack()) {
            return this.getChildren().size() > 4 &&
                    this.getChildren().get(this.getChildren().size() - 2).getToken().type() == TokenType.ASSIGN;
        } else {
            return this.getChildren().size() > 2 &&
                    this.getChildren().get(this.getChildren().size() - 2).getToken().type() == TokenType.ASSIGN;
        }
    }
}