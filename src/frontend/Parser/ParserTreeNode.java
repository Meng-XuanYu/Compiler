package frontend.Parser;

import frontend.SymbolType;
import frontend.SyntaxType;
import frontend.Token;
import frontend.TokenType;

import java.util.ArrayList;

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
            result.add(node.token.type() + " " + node.token.value());
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

    public String getCurrentFuncName() {
        ParserTreeNode node = this;
        while (node != null) {
            if (node.type == SyntaxType.FuncDef ) {
                return node.getChildren().get(1).getToken().value();
            } else if (node.type == SyntaxType.MainFuncDef) {
                return "main";
            }
            node = node.getParent();
        }
        return null;
    }
}