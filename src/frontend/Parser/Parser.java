package frontend.Parser;
import frontend.*;
import frontend.Symbol.Symbol;
import frontend.Symbol.SymbolFunc;
import frontend.Symbol.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final List<Token> tokens;
    private int index;
    private Token currentToken;
    private ErrorList errors;
    private SymbolTable symbolTable;
    private ParserTreeNode root;

    public Parser(List<Token> tokens, ErrorList errors, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.index = 0;
        this.currentToken = this.tokens.get(this.index);
        this.root = null;
        this.errors = errors;
        this.symbolTable = symbolTable;
    }

    // 获取后续token函数群
    private Token getLastToken() {
        return this.tokens.get(this.index - 1);
    }

    private Token getNextToken() {
        return index <= tokens.size() - 1 ? this.tokens.get(this.index + 1) : null;
    }

    private Token getNext2Token() {
        return index <= tokens.size() - 2 ? this.tokens.get(this.index + 2) : null;
    }

    private void nextToken() {
        this.index += 1;
        this.currentToken = index <= tokens.size() - 1 ? this.tokens.get(this.index) : null;
    }

    // 判断函数群，直接用match的都是课题组保证不会出错的地方
    private boolean match(TokenType type) {
        return this.currentToken != null && this.currentToken.type() == type;
    }

    private void expect(TokenType type) {
        if (!match(type)) {
            if (type == TokenType.SEMICN) {
                errors.addError(getLastToken().line(), 'i');
            } else if (type == TokenType.RPARENT) {
                errors.addError(getLastToken().line(), 'j');
            } else if (type == TokenType.RBRACK) {
                errors.addError(getLastToken().line(), 'k');
            } else {
                throw new RuntimeException("Syntax error: expected " + type + " at line " + this.currentToken.line() + " token: " + this.currentToken);
            }
        } else {
            nextToken();
        }
    }

    // 语法分析函数群
    public void parse() {
        this.root = parseCompUnit(root);
    }

    private ParserTreeNode parseCompUnit(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.CompUnit);
        node.setParent(parent);
        while ((currentToken.type() == TokenType.CONSTTK) ||
                (Objects.requireNonNull(getNextToken()).type() == TokenType.IDENFR &&
                        (currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK) &&
                        (Objects.requireNonNull(getNext2Token()).type() != TokenType.LPARENT))) {
            if (currentToken.type() == TokenType.CONSTTK) {
                node.addChild(parseConstDecl(node));
            } else {
                node.addChild(parseVarDecl(node));
            }
        }
        while (currentToken.type() == TokenType.VOIDTK || currentToken.type() == TokenType.CHARTK ||
                (currentToken.type() == TokenType.INTTK && Objects.requireNonNull(getNextToken()).type() != TokenType.MAINTK)) {
            node.addChild(parseFuncDef(node));
        }
        node.addChild(parseMainFuncDef(node));
        return node;
    }

    private ParserTreeNode parseConstDecl(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstDecl);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        expect(TokenType.CONSTTK); //确定是const
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType(node));
        node.addChild(parseConstDef(node,isChar));
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseConstDef(node,isChar));
        }
        if (match(TokenType.SEMICN)) {
            node.addTokenChild(currentToken);
        } else {
            node.addSemicnChild(getLastToken().line());
        }
        expect(TokenType.SEMICN);

        return node;
    }

    private ParserTreeNode parseBType(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.BType);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        nextToken();

        return node;
    }

    private ParserTreeNode parseConstDef(ParserTreeNode parent, boolean isChar) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstDef);
        node.setParent(parent);
        String constName = currentToken.value();
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);
            isArr = true;
            node.addChild(parseConstExp(node));
            if (match(TokenType.RBRACK)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRbrackChild(getLastToken().line());
            }
            expect(TokenType.RBRACK);
        }
        node.addTokenChild(currentToken);
        expect(TokenType.ASSIGN);
        node.addChild(parseConstInitVal(node));

        if (symbolTable.containsSymbolInCurrentScope(constName)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(constName, isArr && isChar ? SymbolType.ConstCharArray : isChar ? SymbolType.ConstChar : isArr ? SymbolType.ConstIntArray : SymbolType.ConstInt);
        }
        return node;
    }

    private ParserTreeNode parseConstExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstExp);
        node.setParent(parent);
        node.addChild(parseAddExp(node));
        return node;
    }

    private ParserTreeNode parseAddExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.AddExp);
        node.setParent(parent);
        node.addChild(parseMulExp(node));
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.AddExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseMulExp(node));
        }
        return node;
    }

    private ParserTreeNode parseMulExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.MulExp);
        node.setParent(parent);
        node.addChild(parseUnaryExp(node));

        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.MulExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseUnaryExp(node));
        }

        return node;
    }

    private ParserTreeNode parseUnaryExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.UnaryExp);
        node.setParent(parent);

        if (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU || currentToken.type() == TokenType.NOT)) {
            node.addChild(parseUnaryOp(node));
            node.addChild(parseUnaryExp(node));
        } else if (currentToken != null &&
                currentToken.type() == TokenType.IDENFR && Objects.requireNonNull(getNextToken()).type() == TokenType.LPARENT) {
            String functionName = currentToken.value();
            if (symbolTable.notContainsSymbol(functionName)) {
                this.errors.addError(currentToken.line(), 'c');
            }
            SymbolFunc functionSymbol = (SymbolFunc)symbolTable.getSymbol(functionName);
            node.addTokenChild(currentToken);
            expect(TokenType.IDENFR);
            if (match(TokenType.LPARENT)) {
                node.addTokenChild(currentToken);
                expect(TokenType.LPARENT);

                ArrayList<SymbolType> paramTypes = new ArrayList<>();
                if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                    int tempIndex = this.index;
                    Token tempToken = this.currentToken;
                    ErrorList tempErrors = this.errors;
                    SymbolTable tempSymbolTable = this.symbolTable;
                    try {
                        ParserTreeNode funcRParamsNode = parseFuncRParams(node);
                        node.addChild(funcRParamsNode);
                        paramTypes = getRParams(funcRParamsNode);
                    } catch (Exception e) {
                        this.index = tempIndex;
                        this.currentToken = tempToken;
                        this.errors = tempErrors;
                        this.symbolTable = tempSymbolTable;
                    }
                }
                if (match(TokenType.RPARENT)) {
                    node.addTokenChild(currentToken);
                } else {
                    node.addRparentChild(getLastToken().line());
                }
                expect(TokenType.RPARENT);

                // 检查参数个数和类型是否匹配
                int paramCount = paramTypes.size();
                if (functionSymbol != null) {
                    if (functionSymbol.getParamCount() != paramCount) {
                        errors.addError(getLastToken().line(), 'd');
                    } else {
                        if (!functionSymbol.paramCorrect(paramTypes)) {
                            errors.addError(getLastToken().line(), 'e');
                        }
                    }
                }
            } else {
                throw new RuntimeException("函数调用少括号" + currentToken);
            }
        } else {
            node.addChild(parsePrimaryExp(node));
        }
        return node;
    }

    private ArrayList<SymbolType> getRParams(ParserTreeNode funcRParamsNode) {
        ArrayList<SymbolType> params = new ArrayList<>();
        ArrayList<ParserTreeNode> children = funcRParamsNode.getChildren();
        for (int i = 0; i < children.size(); i+=2) {
            params.add(getSymbType(children.get(i)));
        }
        return params;
    }

    private SymbolType getSymbType(ParserTreeNode node) {
        if (node.getType() == SyntaxType.Exp) {
            return getSymbType(node.getChildren().get(0));
        } else if (node.getType() == SyntaxType.AddExp) {
            for (int i = 0; i < node.getChildren().size(); i+=2) {
                SymbolType type = getSymbType(node.getChildren().get(i));
                if (type == SymbolType.IntArray || type == SymbolType.CharArray) {
                    return type;
                }
            }
            return getSymbType(node.getChildren().get(0));
        } else if (node.getType() == SyntaxType.MulExp) {
            for (int i = 0; i < node.getChildren().size(); i+=2) {
                SymbolType type = getSymbType(node.getChildren().get(i));
                if (type == SymbolType.IntArray || type == SymbolType.CharArray) {
                    return type;
                }
            }
            return getSymbType(node.getChildren().get(0));
        } else if (node.getType() == SyntaxType.UnaryExp) {
            if (node.getChildren().get(0).getType() == SyntaxType.UnaryOp) {
                return getSymbType(node.getChildren().get(1));
            } else if (node.getChildren().get(0).getType() == SyntaxType.PrimaryExp) {
                return getSymbType(node.getChildren().get(0));
            } else {
                Token token = node.getChildren().get(0).getToken();
                return symbolTable.getSymbol(token.value()).type();
            }
        } else if (node.getType() == SyntaxType.PrimaryExp) {
            if (node.getChildren().get(0).getType() == SyntaxType.LVal) {
                if (symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.CharArray ||
                        symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.ConstCharArray) {
                    if (node.getChildren().get(0).getChildren().size() > 1 && node.getChildren().get(0).getChildren().get(1).getToken().type() == TokenType.LBRACK) {
                        return SymbolType.Char;
                    }
                }
                if (symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.IntArray ||
                        symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value()).type() == SymbolType.ConstIntArray) {
                    if (node.getChildren().get(0).getChildren().size() > 1 && node.getChildren().get(0).getChildren().get(1).getToken().type() == TokenType.LBRACK) {
                        return SymbolType.Int;
                    }
                }
                return symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value()).type();
            } else if (node.getChildren().get(0).getType() == SyntaxType.Number) {
                return SymbolType.Int;
            } else if (node.getChildren().get(0).getType() == SyntaxType.Character) {
                return SymbolType.Char;
            } else {
                return getSymbType(node.getChildren().get(1));
            }
        } else {
            return symbolTable.getSymbol(node.getChildren().get(0).getToken().value()).type();
        }
    }

    private ParserTreeNode parseFuncRParams(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncRParams);
        node.setParent(parent);
        node.addChild(parseExp(node));
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseExp(node));
        }
        return node;
    }

    private ParserTreeNode parsePrimaryExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.PrimaryExp);
        node.setParent(parent);

        if (match(TokenType.LPARENT)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);

            node.addChild(parseExp(node));

            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            node.addChild(parseLVal(node));
        } else if (currentToken != null && currentToken.type() == TokenType.INTCON) {
            node.addChild(parseNumber(node));
        } else if (currentToken != null && currentToken.type() == TokenType.CHRCON) {
            node.addChild(parseCharacter(node));
        } else {
            throw new RuntimeException("Syntax error: expected PrimaryExp " + currentToken);
        }
        return node;
    }

    private ParserTreeNode parseExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Exp);
        node.setParent(parent);
        node.addChild(parseAddExp(node));

        return node;
    }

    private ParserTreeNode parseLVal(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.LVal);
        node.setParent(parent);

        String name = currentToken.value();
        if (symbolTable.notContainsSymbol(name)) {
            this.errors.addError(currentToken.line(), 'c');
        }
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);

            node.addChild(parseExp(node));

            if (match(TokenType.RBRACK)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRbrackChild(getLastToken().line());
            }
            expect(TokenType.RBRACK);
        }

        return node;
    }

    private ParserTreeNode parseConstInitVal(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstInitVal);
        node.setParent(parent);

        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACE);
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                node.addChild(parseConstExp(node));
                while (match(TokenType.COMMA)) {
                    node.addTokenChild(currentToken);
                    expect(TokenType.COMMA);
                    node.addChild(parseConstExp(node));
                }
            }
            node.addTokenChild(currentToken);
            expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            node.addTokenChild(currentToken);
            expect(TokenType.STRCON);
        } else {
            node.addChild(parseConstExp(node));
        }
        return node;
    }

    private ParserTreeNode parseVarDecl(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.VarDecl);
        node.setParent(parent);
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType(node));
        node.addChild(parseVarDef(node,isChar));
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseVarDef(node,isChar));
        }
        if (match(TokenType.SEMICN)) {
            node.addTokenChild(currentToken);
        } else {
            node.addSemicnChild(getLastToken().line());
        }
        expect(TokenType.SEMICN);

        return node;
    }

    private ParserTreeNode parseVarDef(ParserTreeNode parent, boolean isChar) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.VarDef);
        node.setParent(parent);
        String name = currentToken.value();
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);
            isArr = true;
            node.addChild(parseConstExp(node));
            if (match(TokenType.RBRACK)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRbrackChild(getLastToken().line());
            }
            expect(TokenType.RBRACK);
        }
        if (match(TokenType.ASSIGN)) {
            node.addTokenChild(currentToken);
            expect(TokenType.ASSIGN);
            node.addChild(parseInitVal(node));
        }

        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, isArr && isChar ? SymbolType.CharArray : isChar ? SymbolType.Char : isArr ? SymbolType.IntArray : SymbolType.Int);
        }
        return node;
    }

    private ParserTreeNode parseInitVal(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.InitVal);
        node.setParent(parent);

        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACE);
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                node.addChild(parseExp(node));
                while (match(TokenType.COMMA)) {
                    node.addTokenChild(currentToken);
                    expect(TokenType.COMMA);
                    node.addChild(parseExp(node));
                }
            }
            node.addTokenChild(currentToken);
            expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            node.addTokenChild(currentToken);
            expect(TokenType.STRCON);
        } else {
            node.addChild(parseExp(node));
        }
        return node;
    }

    private ParserTreeNode parseFuncDef(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncDef);
        node.setParent(parent);
        TokenType tokenType = currentToken.type();
        node.addChild(parseFuncType(node));
        String name = currentToken.value();
        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, tokenType == TokenType.CHARTK ? SymbolType.CharFunc : tokenType == TokenType.INTTK ? SymbolType.IntFunc : SymbolType.VoidFunc);
        }
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        node.addTokenChild(currentToken);
        expect(TokenType.LPARENT);
        if (currentToken != null && currentToken.type() != TokenType.RPARENT && currentToken.type() != TokenType.LBRACE) {
            node.addChild(parseFuncFParams(node,name));
        }
        if (match(TokenType.RPARENT)) {
            node.addTokenChild(currentToken);
        } else {
            node.addRparentChild(getLastToken().line());
        }
        expect(TokenType.RPARENT);
        node.addChild(parseBlock(node));

        if (tokenType != TokenType.VOIDTK) {
            ParserTreeNode blockNode = node.getLastChild();
            if (blockNode.getChildren().size() > 2) {
                ParserTreeNode lastBlockItem = blockNode.getChildren().get(blockNode.getChildren().size() - 2);
                if (lastBlockItem.getChildren().get(0).getType() == SyntaxType.Stmt &&
                lastBlockItem.getChildren().get(0).getChildren().get(0).getToken() != null &&
                lastBlockItem.getChildren().get(0).getChildren().get(0).getToken().type() == TokenType.RETURNTK) {
                    SymbolFunc funcSymbol = (SymbolFunc) this.symbolTable.getSymbol(name);
                    funcSymbol.setReturn();
                }
            }
            SymbolFunc funcSymbol = (SymbolFunc) this.symbolTable.getSymbol(name);
            if (!funcSymbol.isReturn()) {
                errors.addError(getLastToken().line(), 'g');
            }
        }

        return node;
    }

    private ParserTreeNode parseFuncType(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncType);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        nextToken();

        return node;
    }

    private ParserTreeNode parseFuncFParams(ParserTreeNode parent, String name) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncFParams);
        node.setParent(parent);

        node.addChild(parseFuncFParam(node,name));
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseFuncFParam(node,name));
        }

        return node;
    }

    private ParserTreeNode parseFuncFParam(ParserTreeNode parent, String funcName) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncFParam);
        node.setParent(parent);
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType(node));
        String name = currentToken.value();
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);
            isArr = true;
            if (match(TokenType.RBRACK)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRbrackChild(getLastToken().line());
            }
            expect(TokenType.RBRACK);
        }

        if (symbolTable.containsSymbolFuncPara(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            Symbol paramSymbol = this.symbolTable.addSymbolFuncPara(name, isArr && isChar ? SymbolType.CharArray : isChar ? SymbolType.Char : isArr ? SymbolType.IntArray : SymbolType.Int);
            Symbol funcFParam = this.symbolTable.getSymbol(funcName);
            ((SymbolFunc)funcFParam).addParam(paramSymbol);
        }
        return node;
    }

    private ParserTreeNode parseBlock(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Block);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        expect(TokenType.LBRACE);

        this.symbolTable.enterScope();
        while (currentToken != null && (currentToken.typeSymbolizeDecl() || currentToken.typeSymbolizeStmt())) {
            node.addChild(parseBlockItem(node));
        }
        this.symbolTable.exitScope();
        node.addTokenChild(currentToken);
        expect(TokenType.RBRACE);

        return node;
    }

    private ParserTreeNode parseBlockItem(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.BlockItem);
        node.setParent(parent);
        if (currentToken != null && (currentToken.type() == TokenType.CONSTTK || currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK)) {
            node.addChild(parseDecl(node));
        } else {
            node.addChild(parseStmt(node));
        }
        return node;
    }

    private ParserTreeNode parseDecl(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Decl);
        node.setParent(parent);
        if (currentToken != null && currentToken.type() == TokenType.CONSTTK) {
            node.addChild(parseConstDecl(node));
        } else {
            node.addChild(parseVarDecl(node));
        }
        return node;
    }

    private ParserTreeNode parseStmt(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Stmt);
        node.setParent(parent);

        if (match(TokenType.SEMICN)) {
            node.addTokenChild(currentToken);
            expect(TokenType.SEMICN);
        } else if (match(TokenType.LBRACE)) {
            node.addChild(parseBlock(node));
        } else if (match(TokenType.IFTK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.IFTK);
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);
            node.addChild(parseCond(node));
            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
            node.addChild(parseStmt(node));
            if (match(TokenType.ELSETK)) {
                node.addTokenChild(currentToken);
                expect(TokenType.ELSETK);
                node.addChild(parseStmt(node));
            }
        } else if (match(TokenType.FORTK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.FORTK);
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);
            if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
                node.addChild(parseForStmt(node));
            }
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.typeSymbolizeBeginOfExp()) {
                node.addChild(parseCond(node));
            }
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
                node.addChild(parseForStmt(node));
            }
            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
            node.addChild(parseStmt(node));
        } else if (match(TokenType.BREAKTK)) {
            node.addTokenChild(currentToken);
            int line = currentToken.line();
            // 检查是否在循环内
            if (notInLoop(node)) {
                errors.addError(line, 'm');
            }

            expect(TokenType.BREAKTK);
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.CONTINUETK)) {
            node.addTokenChild(currentToken);
            int line = currentToken.line();
            // 检查是否在循环内
            if (notInLoop(node)) {
                errors.addError(line, 'm');
            }

            expect(TokenType.CONTINUETK);
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.RETURNTK)) {
            int line = currentToken.line();
            node.addTokenChild(currentToken);
            expect(TokenType.RETURNTK);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                int index_temp = this.index;
                try {
                    tryParseExp();
                    if (match(TokenType.SEMICN)) {
                        this.index = index_temp;
                        this.currentToken = this.tokens.get(this.index);
                        node.addChild(parseExp(node));
                        // 回溯到函数定义，看是不是void，如果是void则发生错误f
                        if (node.getCurrentFuncType().equals("void")) {
                            errors.addError(line, 'f');
                        }
                    } else if (match(TokenType.ASSIGN)) {
                        this.index = index_temp;
                        this.currentToken = this.tokens.get(this.index);
                    } else {
                        this.index = index_temp;
                        this.currentToken = this.tokens.get(this.index);
                        node.addChild(parseExp(node));
                        // 回溯到函数定义，看是不是void，如果是void则发生错误f
                        if (node.getCurrentFuncType().equals("void")) {
                            errors.addError(line, 'f');
                        }
                    }
                } catch (Exception e) {
                    this.index = index_temp;
                    this.currentToken = this.tokens.get(this.index);
                }
            }
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.PRINTFTK)) {
            node.addTokenChild(currentToken);
            int line = currentToken.line();
            expect(TokenType.PRINTFTK);
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);
            node.addTokenChild(currentToken);
            String formatString = currentToken.value();
            expect(TokenType.STRCON);

            int formatCount = countFormatSpecifiers(formatString);
            int expCount = 0;
            while (match(TokenType.COMMA)) {
                node.addTokenChild(currentToken);
                expect(TokenType.COMMA);
                node.addChild(parseExp(node));
                expCount++;
            }
            if (formatCount != expCount) {
                errors.addError(line, 'l');
            }

            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.IDENFR)) {
            int index_temp = this.index;
            while (currentToken != null && currentToken.type() != TokenType.SEMICN && currentToken.type() != TokenType.ASSIGN) {
                nextToken();
            }
            Token signToken = currentToken;
            this.index = index_temp;
            this.currentToken = this.tokens.get(this.index);
            if (signToken.type() == TokenType.ASSIGN) {
                tryParseLVal();
                if (match(TokenType.ASSIGN)) {
                    this.index = index_temp;
                    this.currentToken = this.tokens.get(this.index);
                    node.addChild(parseLVal(node));
                    Symbol symbol = symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value());
                    if (symbol != null && symbol.isConst()) {
                        errors.addError(getLastToken().line(), 'h');
                    }
                    node.addTokenChild(currentToken);
                    expect(TokenType.ASSIGN);
                    if (match(TokenType.GETINTTK)) {
                        node.addTokenChild(currentToken);
                        expect(TokenType.GETINTTK);
                        node.addTokenChild(currentToken);
                        expect(TokenType.LPARENT);
                        if (match(TokenType.RPARENT)) {
                            node.addTokenChild(currentToken);
                        } else {
                            node.addRparentChild(getLastToken().line());
                        }
                        expect(TokenType.RPARENT);
                        if (match(TokenType.SEMICN)) {
                            node.addTokenChild(currentToken);
                        } else {
                            node.addSemicnChild(getLastToken().line());
                        }
                        expect(TokenType.SEMICN);
                    } else if (match(TokenType.GETCHARTK)) {
                        node.addTokenChild(currentToken);
                        expect(TokenType.GETCHARTK);
                        node.addTokenChild(currentToken);
                        expect(TokenType.LPARENT);
                        if (match(TokenType.RPARENT)) {
                            node.addTokenChild(currentToken);
                        } else {
                            node.addRparentChild(getLastToken().line());
                        }
                        expect(TokenType.RPARENT);
                        if (match(TokenType.SEMICN)) {
                            node.addTokenChild(currentToken);
                        } else {
                            node.addSemicnChild(getLastToken().line());
                        }
                        expect(TokenType.SEMICN);
                    } else {
                        node.addChild(parseExp(node));
                        if (match(TokenType.SEMICN)) {
                            node.addTokenChild(currentToken);
                        } else {
                            node.addSemicnChild(getLastToken().line());
                        }
                        expect(TokenType.SEMICN);
                    }
                } else {
                    this.index = index_temp;
                    this.currentToken = this.tokens.get(this.index);
                    node.addChild(parseExp(node));
                    if (match(TokenType.SEMICN)) {
                        node.addTokenChild(currentToken);
                    } else {
                        node.addSemicnChild(getLastToken().line());
                    }
                    expect(TokenType.SEMICN);
                }
            } else {
                node.addChild(parseExp(node));
                if (match(TokenType.SEMICN)) {
                    node.addTokenChild(currentToken);
                } else {
                    node.addSemicnChild(getLastToken().line());
                }
                expect(TokenType.SEMICN);
            }
        } else {
            node.addChild(parseExp(node));
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        }

        return node;
    }

    private boolean notInLoop(ParserTreeNode node) {
        while (node != null) {
            if (!node.getChildren().isEmpty() && node.getChildren().get(0).getToken() != null && node.getChildren().get(0).getToken().type() == TokenType.FORTK) {
                return false;
            }
            node = node.getParent();
        }
        return true;
    }

    private int countFormatSpecifiers(String formatString) {
        int count = 0;
        for (int i = 0; i < formatString.length() - 1; i++) {
            if ((formatString.charAt(i) == '%' && formatString.charAt(i + 1) != 'd') ||
                (formatString.charAt(i) == '%' && formatString.charAt(i + 1) != 'c')) {
                count++;
            }
        }
        return count;
    }

    private void tryIdenfrToken() {
        if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            nextToken();
        } else {
            throw new RuntimeException("Syntax error: expected " + TokenType.IDENFR + " at line " + Objects.requireNonNull(this.currentToken).line() + " token: " + this.currentToken);
        }
    }

    private void tryParseLVal() {
        tryIdenfrToken();
        if (match(TokenType.LBRACK)) {
            nextToken();
            tryParseExp();
            if (match(TokenType.RBRACK)) {
                nextToken();
            }
        }
    }

    private void tryParseExp() {
        tryParseAddExp();
    }

    private void tryParseAddExp() {
        tryParseMulExp();
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            nextToken();
            tryParseMulExp();
        }
    }

    private void tryParseMulExp() {
        tryParseUnaryExp();
        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            nextToken();
            tryParseUnaryExp();
        }
    }

    private void tryParseUnaryExp() {
        if (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU || currentToken.type() == TokenType.NOT)) {
            nextToken();
            tryParseUnaryExp();
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR && Objects.requireNonNull(getNextToken()).type() == TokenType.LPARENT) {
            tryIdenfrToken();
            if (match(TokenType.LPARENT)) {
                nextToken();
                if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                    tryParseFuncRParams();
                }
                if (match(TokenType.RPARENT)) {
                    nextToken();
                }
            } else {
                throw new RuntimeException("函数调用少括号" + currentToken);
            }
        } else {
            tryParsePrimaryExp();
        }
    }

    private void tryParseFuncRParams() {
        tryParseExp();
        while (match(TokenType.COMMA)) {
            nextToken();
            tryParseExp();
        }
    }

    private void tryParsePrimaryExp() {
        if (match(TokenType.LPARENT)) {
            nextToken();
            tryParseExp();
            if (match(TokenType.RPARENT)) {
                nextToken();
            }
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            tryParseLVal();
        } else if (currentToken != null && currentToken.type() == TokenType.INTCON) {
            nextToken();
        } else if (currentToken != null && currentToken.type() == TokenType.CHRCON) {
            nextToken();
        } else {
            throw new RuntimeException("Syntax error: expected PrimaryExp " + currentToken);
        }
    }

    private ParserTreeNode parseCond(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Cond);
        node.setParent(parent);
        node.addChild(parseLOrExp(node));

        return node;
    }

    private ParserTreeNode parseLOrExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.LOrExp);
        node.setParent(parent);
        node.addChild(parseLAndExp(node));
        while (currentToken != null && currentToken.type() == TokenType.OR) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.LOrExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseLAndExp(node));
        }

        return node;
    }

    private ParserTreeNode parseLAndExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.LAndExp);
        node.setParent(parent);
        node.addChild(parseEqExp(node));

        while (currentToken != null && currentToken.type() == TokenType.AND) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.LAndExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseEqExp(node));
        }

        return node;
    }

    private ParserTreeNode parseEqExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.EqExp);
        node.setParent(parent);
        node.addChild(parseRelExp(node));
        while (currentToken != null && (currentToken.type() == TokenType.EQL || currentToken.type() == TokenType.NEQ)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.EqExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseRelExp(node));
        }

        return node;
    }

    private ParserTreeNode parseRelExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.RelExp);
        node.setParent(parent);
        node.addChild(parseAddExp(node));
        while (currentToken != null && (currentToken.type() == TokenType.LSS || currentToken.type() == TokenType.GRE || currentToken.type() == TokenType.LEQ || currentToken.type() == TokenType.GEQ)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.RelExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseAddExp(node));
        }

        return node;
    }

    private ParserTreeNode parseForStmt(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ForStmt);
        node.setParent(parent);
        node.addChild(parseLVal(node));
        node.addTokenChild(currentToken);
        expect(TokenType.ASSIGN);
        node.addChild(parseExp(node));

        return node;
    }

    private ParserTreeNode parseMainFuncDef(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.MainFuncDef);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        expect(TokenType.INTTK);
        node.addTokenChild(currentToken);
        expect(TokenType.MAINTK);
        node.addTokenChild(currentToken);
        if (symbolTable.containsSymbolInCurrentScope("main")) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol("main",SymbolType.IntFunc);
        }
        expect(TokenType.LPARENT);
        if (match(TokenType.RPARENT)) {
            node.addTokenChild(currentToken);
        } else {
            node.addRparentChild(getLastToken().line());
        }
        expect(TokenType.RPARENT);
        node.addChild(parseBlock(node));

        ParserTreeNode blockNode = node.getLastChild();
        if (blockNode.getChildren().size() > 2) {
            ParserTreeNode lastBlockItem = blockNode.getChildren().get(blockNode.getChildren().size() - 2);
            if (lastBlockItem.getChildren().get(0).getType() == SyntaxType.Stmt &&
                    lastBlockItem.getChildren().get(0).getChildren().get(0).getToken() != null &&
                    lastBlockItem.getChildren().get(0).getChildren().get(0).getToken().type() == TokenType.RETURNTK) {
                SymbolFunc funcSymbol = (SymbolFunc) this.symbolTable.getSymbol("main");
                funcSymbol.setReturn();
            }
        }
        SymbolFunc funcSymbol = (SymbolFunc) this.symbolTable.getSymbol("main");
        if (!funcSymbol.isReturn()) {
            errors.addError(getLastToken().line(), 'g');
        }

        return node;
    }

    private ParserTreeNode parseNumber(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Number);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        expect(TokenType.INTCON);

        return node;
    }

    private ParserTreeNode parseCharacter(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Character);
        node.setParent(parent);
        node.addTokenChild(currentToken);
        expect(TokenType.CHRCON);

        return node;
    }

    private ParserTreeNode parseUnaryOp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.UnaryOp);
        node.setParent(parent);

        if (!(match(TokenType.PLUS) || match(TokenType.MINU) || match(TokenType.NOT))) {
            throw new RuntimeException("Syntax error: expected UnaryOp");
        }

        node.addTokenChild(currentToken);
        nextToken();

        return node;
    }

    public ParserTreeNode getRoot() {
        return this.root;
    }
}