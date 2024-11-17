package frontend.Parser;
import frontend.SymbolParser.SymbolTableParser;
import frontend.SymbolParser.SymbolType;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;
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

    // 子节点是否有左括号
    public boolean hasLbrack() {
        for (ParserTreeNode child : this.getChildren()) {
            if (child.getToken() != null && child.getToken().type() == TokenType.LBRACK) {
                return true;
            }
        }
        return false;
    }
}