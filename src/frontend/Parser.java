package frontend;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final List<Token> tokens;
    private int index;
    private Token currentToken;
    private final ErrorList errors;
    private final SymbolTable symbolTable;
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
        this.root = parseCompUnit();
    }

    private ParserTreeNode parseCompUnit() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.CompUnit);
        while ((currentToken.type() == TokenType.CONSTTK) ||
                (Objects.requireNonNull(getNextToken()).type() == TokenType.IDENFR &&
                        (currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK) &&
                        (Objects.requireNonNull(getNext2Token()).type() != TokenType.LPARENT))) {
            if (currentToken.type() == TokenType.CONSTTK) {
                node.addChild(parseConstDecl());
            } else {
                node.addChild(parseVarDecl());
            }
        }
        while (currentToken.type() == TokenType.VOIDTK || currentToken.type() == TokenType.CHARTK ||
                (currentToken.type() == TokenType.INTTK && Objects.requireNonNull(getNextToken()).type() != TokenType.MAINTK)) {
            node.addChild(parseFuncDef());
        }
        node.addChild(parseMainFuncDef());
        return node;
    }

    private ParserTreeNode parseConstDecl() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstDecl);
        node.addTokenChild(currentToken);
        expect(TokenType.CONSTTK); //确定是const
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType());
        node.addChild(parseConstDef(isChar));
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseConstDef(isChar));
        }
        if (match(TokenType.SEMICN)) {
            node.addTokenChild(currentToken);
        } else {
            node.addSemicnChild(getLastToken().line());
        }
        expect(TokenType.SEMICN);

        return node;
    }

    private ParserTreeNode parseBType() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.BType);
        node.addTokenChild(currentToken);
        nextToken();

        return node;
    }

    private ParserTreeNode parseConstDef(boolean isChar) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstDef);
        String name = currentToken.value();
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);
            isArr = true;
            node.addChild(parseConstExp());
            if (match(TokenType.RBRACK)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRbrackChild(getLastToken().line());
            }
            expect(TokenType.RBRACK);
        }
        node.addTokenChild(currentToken);
        expect(TokenType.ASSIGN);
        node.addChild(parseConstInitVal());

        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, isArr && isChar ? SymbolType.ConstCharArray : isChar ? SymbolType.ConstChar : isArr ? SymbolType.ConstIntArray : SymbolType.ConstInt);
        }
        return node;
    }

    private ParserTreeNode parseConstExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstExp);
        node.addChild(parseAddExp());
        return node;
    }

    private ParserTreeNode parseAddExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.AddExp);
        node.addChild(parseMulExp());
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.AddExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseMulExp());
        }
        return node;
    }

    private ParserTreeNode parseMulExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.MulExp);
        node.addChild(parseUnaryExp());

        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.MulExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseUnaryExp());
        }

        return node;
    }

    private ParserTreeNode parseUnaryExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.UnaryExp);

        if (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU || currentToken.type() == TokenType.NOT)) {
            node.addChild(parseUnaryOp());
            node.addChild(parseUnaryExp());
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

                int paramCount = 0;
                ArrayList<SymbolType> paramTypes = new ArrayList<>();
                //todo 更新paramTypes和paramCount
                if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                    node.addChild(parseFuncRParams());
                }
                if (match(TokenType.RPARENT)) {
                    node.addTokenChild(currentToken);
                } else {
                    node.addRparentChild(getLastToken().line());
                }
                expect(TokenType.RPARENT);

                // 检查参数个数和类型是否匹配
//                if (functionSymbol.getParamCount() != paramCount) {
//                    errors.addError(getLastToken().line(), 'd');
//                } else {
//                    if (!functionSymbol.paramCorrect(paramTypes)) {
//                        errors.addError(getLastToken().line(), 'e');
//                    }
//                }
            } else {
                throw new RuntimeException("函数调用少括号" + currentToken);
            }
        } else {
            node.addChild(parsePrimaryExp());
        }
        return node;
    }

    private ParserTreeNode parseFuncRParams() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncRParams);
        node.addChild(parseExp());
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseExp());
        }
        return node;
    }

    private ParserTreeNode parsePrimaryExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.PrimaryExp);

        if (match(TokenType.LPARENT)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);

            node.addChild(parseExp());

            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            node.addChild(parseLVal());
        } else if (currentToken != null && currentToken.type() == TokenType.INTCON) {
            node.addChild(parseNumber());
        } else if (currentToken != null && currentToken.type() == TokenType.CHRCON) {
            node.addChild(parseCharacter());
        } else {
            throw new RuntimeException("Syntax error: expected PrimaryExp");
        }
        return node;
    }

    private ParserTreeNode parseExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Exp);
        node.addChild(parseAddExp());

        return node;
    }

    private ParserTreeNode parseLVal() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.LVal);

        String name = currentToken.value();
        if (symbolTable.notContainsSymbol(name)) {
            this.errors.addError(currentToken.line(), 'c');
        }
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);

            node.addChild(parseExp());

            if (match(TokenType.RBRACK)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRbrackChild(getLastToken().line());
            }
            expect(TokenType.RBRACK);
        }

        return node;
    }

    private ParserTreeNode parseConstInitVal() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstInitVal);

        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACE);
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                node.addChild(parseConstExp());
                while (match(TokenType.COMMA)) {
                    node.addTokenChild(currentToken);
                    expect(TokenType.COMMA);
                    node.addChild(parseConstExp());
                }
            }
            node.addTokenChild(currentToken);
            expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            node.addTokenChild(currentToken);
            expect(TokenType.STRCON);
        } else {
            node.addChild(parseConstExp());
        }
        return node;
    }

    private ParserTreeNode parseVarDecl() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.VarDecl);
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType());
        node.addChild(parseVarDef(isChar));
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseVarDef(isChar));
        }
        if (match(TokenType.SEMICN)) {
            node.addTokenChild(currentToken);
        } else {
            node.addSemicnChild(getLastToken().line());
        }
        expect(TokenType.SEMICN);

        return node;
    }

    private ParserTreeNode parseVarDef(boolean isChar) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.VarDef);

        String name = currentToken.value();
        node.addTokenChild(currentToken);
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACK);
            isArr = true;
            node.addChild(parseConstExp());
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
            node.addChild(parseInitVal());
        }

        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, isArr && isChar ? SymbolType.CharArray : isChar ? SymbolType.Char : isArr ? SymbolType.IntArray : SymbolType.Int);
        }
        return node;
    }

    private ParserTreeNode parseInitVal() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.InitVal);

        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            node.addTokenChild(currentToken);
            expect(TokenType.LBRACE);
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                node.addChild(parseExp());
                while (match(TokenType.COMMA)) {
                    node.addTokenChild(currentToken);
                    expect(TokenType.COMMA);
                    node.addChild(parseExp());
                }
            }
            node.addTokenChild(currentToken);
            expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            node.addTokenChild(currentToken);
            expect(TokenType.STRCON);
        } else {
            node.addChild(parseExp());
        }
        return node;
    }

    private ParserTreeNode parseFuncDef() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncDef);
        TokenType tokenType = currentToken.type();
        node.addChild(parseFuncType());
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
        if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
            node.addChild(parseFuncFParams());
//            SymbolFunc symbolFunc = (SymbolFunc)this.symbolTable.getSymbol(name);
//            symbolFunc.setParams(params);
        }
        if (match(TokenType.RPARENT)) {
            node.addTokenChild(currentToken);
        } else {
            node.addRparentChild(getLastToken().line());
        }
        expect(TokenType.RPARENT);
        node.addChild(parseBlock());

        return node;
    }

    private ParserTreeNode parseFuncType() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncType);
        node.addTokenChild(currentToken);
        nextToken();

        return node;
    }

    private ParserTreeNode parseFuncFParams() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncFParams);

        node.addChild(parseFuncFParam());
        while (match(TokenType.COMMA)) {
            node.addTokenChild(currentToken);
            expect(TokenType.COMMA);
            node.addChild(parseFuncFParam());
        }

        return node;
    }

    private ParserTreeNode parseFuncFParam() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncFParam);
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType());
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

        this.symbolTable.addSymbolFuncPara(name, isArr && isChar ? SymbolType.CharArray : isChar ? SymbolType.Char : isArr ? SymbolType.IntArray : SymbolType.Int);
        return node;
    }

    private ParserTreeNode parseBlock() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Block);
        node.addTokenChild(currentToken);
        expect(TokenType.LBRACE);

        this.symbolTable.enterScope();
        while (currentToken != null && (currentToken.typeSymbolizeDecl() || currentToken.typeSymbolizeStmt())) {
            node.addChild(parseBlockItem());
        }
        this.symbolTable.exitScope();
        node.addTokenChild(currentToken);
        expect(TokenType.RBRACE);

        return node;
    }

    private ParserTreeNode parseBlockItem() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.BlockItem);
        if (currentToken != null && (currentToken.type() == TokenType.CONSTTK || currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK)) {
            node.addChild(parseDecl());
        } else {
            node.addChild(parseStmt());
        }
        return node;
    }

    private ParserTreeNode parseDecl() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Decl);
        if (currentToken != null && currentToken.type() == TokenType.CONSTTK) {
            node.addChild(parseConstDecl());
        } else {
            node.addChild(parseVarDecl());
        }
        return node;
    }

    private ParserTreeNode parseStmt() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Stmt);

        if (match(TokenType.SEMICN)) {
            node.addTokenChild(currentToken);
            expect(TokenType.SEMICN);
        } else if (match(TokenType.LBRACE)) {
            node.addChild(parseBlock());
        } else if (match(TokenType.IFTK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.IFTK);
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);
            node.addChild(parseCond());
            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
            node.addChild(parseStmt());
            if (match(TokenType.ELSETK)) {
                node.addTokenChild(currentToken);
                expect(TokenType.ELSETK);
                node.addChild(parseStmt());
            }
        } else if (match(TokenType.FORTK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.FORTK);
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                node.addChild(parseForStmt());
            }
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                node.addChild(parseCond());
            }
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                node.addChild(parseForStmt());
            }
            if (match(TokenType.RPARENT)) {
                node.addTokenChild(currentToken);
            } else {
                node.addRparentChild(getLastToken().line());
            }
            expect(TokenType.RPARENT);
            node.addChild(parseStmt());
        } else if (match(TokenType.BREAKTK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.BREAKTK);
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.CONTINUETK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.CONTINUETK);
            if (match(TokenType.SEMICN)) {
                node.addTokenChild(currentToken);
            } else {
                node.addSemicnChild(getLastToken().line());
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.RETURNTK)) {
            node.addTokenChild(currentToken);
            expect(TokenType.RETURNTK);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                int index_temp = this.index;
                try {
                    tryParseExp();
                    if (match(TokenType.SEMICN)) {
                        this.index = index_temp;
                        this.currentToken = this.tokens.get(this.index);
                        node.addChild(parseExp());
                    } else if (match(TokenType.ASSIGN)) {
                        this.index = index_temp;
                        this.currentToken = this.tokens.get(this.index);
                    } else {
                        this.index = index_temp;
                        this.currentToken = this.tokens.get(this.index);
                        node.addChild(parseExp());
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
            expect(TokenType.PRINTFTK);
            node.addTokenChild(currentToken);
            expect(TokenType.LPARENT);
            node.addTokenChild(currentToken);
            expect(TokenType.STRCON);
            while (match(TokenType.COMMA)) {
                node.addTokenChild(currentToken);
                expect(TokenType.COMMA);
                node.addChild(parseExp());
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
        } else {
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
                    node.addChild(parseLVal());
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
                        node.addChild(parseExp());
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
                    node.addChild(parseExp());
                    if (match(TokenType.SEMICN)) {
                        node.addTokenChild(currentToken);
                    } else {
                        node.addSemicnChild(getLastToken().line());
                    }
                    expect(TokenType.SEMICN);
                }
            } else {
                node.addChild(parseExp());
                if (match(TokenType.SEMICN)) {
                    node.addTokenChild(currentToken);
                } else {
                    node.addSemicnChild(getLastToken().line());
                }
                expect(TokenType.SEMICN);
            }
        }

        return node;
    }

    private void tryToken(TokenType type) {
        if (currentToken != null && currentToken.type() == type) {
            nextToken();
        } else {
            throw new RuntimeException("Syntax error: expected " + type + " at line " + this.currentToken.line() + " token: " + this.currentToken);
        }
    }

    private void tryParseLVal() {
        tryToken(TokenType.IDENFR);
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
            tryToken(TokenType.IDENFR);
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

    private ParserTreeNode parseCond() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Cond);
        node.addChild(parseLOrExp());

        return node;
    }

    private ParserTreeNode parseLOrExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.LOrExp);
        node.addChild(parseLAndExp());
        while (currentToken != null && currentToken.type() == TokenType.OR) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.LOrExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseLAndExp());
        }

        return node;
    }

    private ParserTreeNode parseLAndExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.LAndExp);
        node.addChild(parseEqExp());

        while (currentToken != null && currentToken.type() == TokenType.AND) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.LAndExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseEqExp());
        }

        return node;
    }

    private ParserTreeNode parseEqExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.EqExp);
        node.addChild(parseRelExp());
        while (currentToken != null && (currentToken.type() == TokenType.EQL || currentToken.type() == TokenType.NEQ)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.EqExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseRelExp());
        }

        return node;
    }

    private ParserTreeNode parseRelExp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.RelExp);
        node.addChild(parseAddExp());
        while (currentToken != null && (currentToken.type() == TokenType.LSS || currentToken.type() == TokenType.GRE || currentToken.type() == TokenType.LEQ || currentToken.type() == TokenType.GEQ)) {
            ParserTreeNode newNode = new ParserTreeNode(SyntaxType.RelExp);
            newNode.addChild(node);
            node = newNode;
            node.addTokenChild(currentToken);
            nextToken();
            node.addChild(parseAddExp());
        }

        return node;
    }

    private ParserTreeNode parseForStmt() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ForStmt);
        node.addChild(parseLVal());
        node.addTokenChild(currentToken);
        expect(TokenType.ASSIGN);
        node.addChild(parseExp());

        return node;
    }

    private ParserTreeNode parseMainFuncDef() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.MainFuncDef);
        node.addTokenChild(currentToken);
        expect(TokenType.INTTK);
        node.addTokenChild(currentToken);
        expect(TokenType.MAINTK);
        node.addTokenChild(currentToken);
        expect(TokenType.LPARENT);
        if (match(TokenType.RPARENT)) {
            node.addTokenChild(currentToken);
        } else {
            node.addRparentChild(getLastToken().line());
        }
        expect(TokenType.RPARENT);
        node.addChild(parseBlock());

        return node;
    }

    private ParserTreeNode parseNumber() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Number);
        node.addTokenChild(currentToken);
        expect(TokenType.INTCON);

        return node;
    }

    private ParserTreeNode parseCharacter() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Character);
        node.addTokenChild(currentToken);
        expect(TokenType.CHRCON);

        return node;
    }

    private ParserTreeNode parseUnaryOp() {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.UnaryOp);

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