package frontend.Parser;
import frontend.*;
import middleend.Symbol.Symbol;
import middleend.Symbol.SymbolFunc;
import middleend.Symbol.SymbolTable;
import middleend.Symbol.SymbolType;
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

    private Token getLastToken() {
        return this.tokens.get(this.index - 1);
    }

    public ParserTreeNode getRoot() {
        return this.root;
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

    private void addCurrentTokenAndNext(ParserTreeNode node) {
        node.addTokenChild(currentToken);
        nextToken();
    }

    private void addCorrectTokenAndNext(ParserTreeNode node, TokenType type) {
        if (match(type)) {
            node.addTokenChild(currentToken);
        } else {
            if (type.equals(TokenType.SEMICN)) {
                node.addSemicnChild(getLastToken().line());
            } else if (type.equals(TokenType.RPARENT)) {
                node.addRparentChild(getLastToken().line());
            } else { //if (type.equals(TokenType.RBRACK))
                node.addRbrackChild(getLastToken().line());
            }
        }
        expect(type);
    }

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
                throw new RuntimeException("未定义错误: expected " + type + " at line " + this.currentToken.line() + " token: " + this.currentToken);
            }
        } else {
            nextToken();
        }
    }

    private ArrayList<SymbolType> getRParams(ParserTreeNode funcRParamsNode) {
        ArrayList<SymbolType> params = new ArrayList<>();
        ArrayList<ParserTreeNode> children = funcRParamsNode.getChildren();
        for (int i = 0; i < children.size(); i+=2) {
            params.add(children.get(i).getSymbType(symbolTable));
        }
        return params;
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
        addCurrentTokenAndNext(node);//expect(TokenType.CONSTTK); //确定是const
        boolean isChar = currentToken.type() == TokenType.CHARTK;
        node.addChild(parseBType(node));
        node.addChild(parseConstDef(node,isChar));
        while (match(TokenType.COMMA)) {
            addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
            node.addChild(parseConstDef(node,isChar));
        }
        addCorrectTokenAndNext(node, TokenType.SEMICN);
        return node;
    }

    private ParserTreeNode parseBType(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.BType);
        node.setParent(parent);
        addCurrentTokenAndNext(node);
        return node;
    }

    private ParserTreeNode parseConstDef(ParserTreeNode parent, boolean isChar) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstDef);
        node.setParent(parent);
        String constName = currentToken.value();
        addCurrentTokenAndNext(node);//expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            addCurrentTokenAndNext(node);//expect(TokenType.LBRACK);
            isArr = true;
            node.addChild(parseConstExp(node));
            addCorrectTokenAndNext(node, TokenType.RBRACK);
        }
        addCurrentTokenAndNext(node);//expect(TokenType.ASSIGN);
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
            addCurrentTokenAndNext(node);
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
            addCurrentTokenAndNext(node);
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
            addCurrentTokenAndNext(node);//expect(TokenType.IDENFR);
            if (match(TokenType.LPARENT)) {
                addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
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
                addCorrectTokenAndNext(node, TokenType.RPARENT);
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

    private ParserTreeNode parseFuncRParams(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncRParams);
        node.setParent(parent);
        node.addChild(parseExp(node));
        while (match(TokenType.COMMA)) {
            addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
            node.addChild(parseExp(node));
        }
        return node;
    }

    private ParserTreeNode parsePrimaryExp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.PrimaryExp);
        node.setParent(parent);

        if (match(TokenType.LPARENT)) {
            addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
            node.addChild(parseExp(node));
            addCorrectTokenAndNext(node, TokenType.RPARENT);
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
        addCurrentTokenAndNext(node);//expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            addCurrentTokenAndNext(node);//expect(TokenType.LBRACK);
            node.addChild(parseExp(node));
            addCorrectTokenAndNext(node, TokenType.RBRACK);
        }

        return node;
    }

    private ParserTreeNode parseConstInitVal(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ConstInitVal);
        node.setParent(parent);

        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            addCurrentTokenAndNext(node);//expect(TokenType.LBRACE);
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                node.addChild(parseConstExp(node));
                while (match(TokenType.COMMA)) {
                    addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
                    node.addChild(parseConstExp(node));
                }
            }
            addCurrentTokenAndNext(node);//expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            addCurrentTokenAndNext(node);//expect(TokenType.STRCON);
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
            addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
            node.addChild(parseVarDef(node,isChar));
        }
        addCorrectTokenAndNext(node, TokenType.SEMICN);
        return node;
    }

    private ParserTreeNode parseVarDef(ParserTreeNode parent, boolean isChar) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.VarDef);
        node.setParent(parent);
        String name = currentToken.value();
        addCurrentTokenAndNext(node);//expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            addCurrentTokenAndNext(node);//expect(TokenType.LBRACK);
            isArr = true;
            node.addChild(parseConstExp(node));
            addCorrectTokenAndNext(node, TokenType.RBRACK);
        }
        if (match(TokenType.ASSIGN)) {
            addCurrentTokenAndNext(node);//expect(TokenType.ASSIGN);
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
            addCurrentTokenAndNext(node);//expect(TokenType.LBRACE);
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                node.addChild(parseExp(node));
                while (match(TokenType.COMMA)) {
                    addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
                    node.addChild(parseExp(node));
                }
            }
            addCurrentTokenAndNext(node);//expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            addCurrentTokenAndNext(node);//expect(TokenType.STRCON);
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
        addCurrentTokenAndNext(node);//expect(TokenType.IDENFR);
        addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
        if (currentToken != null && currentToken.type() != TokenType.RPARENT && currentToken.type() != TokenType.LBRACE) {
            node.addChild(parseFuncFParams(node,name));
        }
        addCorrectTokenAndNext(node, TokenType.RPARENT);
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
        addCurrentTokenAndNext(node);
        return node;
    }

    private ParserTreeNode parseFuncFParams(ParserTreeNode parent, String name) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.FuncFParams);
        node.setParent(parent);

        node.addChild(parseFuncFParam(node,name));
        while (match(TokenType.COMMA)) {
            addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
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
        addCurrentTokenAndNext(node);//expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            addCurrentTokenAndNext(node);//expect(TokenType.LBRACK);
            isArr = true;
            addCorrectTokenAndNext(node, TokenType.RBRACK);
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
        addCurrentTokenAndNext(node);//expect(TokenType.LBRACE);
        this.symbolTable.enterScope();
        while (currentToken != null && (currentToken.typeSymbolizeDecl() || currentToken.typeSymbolizeStmt())) {
            node.addChild(parseBlockItem(node));
        }
        this.symbolTable.exitScope();
        addCurrentTokenAndNext(node);//expect(TokenType.RBRACE);
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
            addCurrentTokenAndNext(node);//expect(TokenType.SEMICN);这个可以因为已经match了不会报错
        } else if (match(TokenType.LBRACE)) {
            node.addChild(parseBlock(node));
        } else if (match(TokenType.IFTK)) {
            addCurrentTokenAndNext(node);//expect(TokenType.IFTK);
            addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
            node.addChild(parseCond(node));
            addCorrectTokenAndNext(node, TokenType.RPARENT);
            node.addChild(parseStmt(node));
            if (match(TokenType.ELSETK)) {
                addCurrentTokenAndNext(node);//expect(TokenType.ELSETK);
                node.addChild(parseStmt(node));
            }
        } else if (match(TokenType.FORTK)) {
            addCurrentTokenAndNext(node);//expect(TokenType.FORTK);
            addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
            if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
                node.addChild(parseForStmt(node));
            }
            addCorrectTokenAndNext(node, TokenType.SEMICN);
            if (currentToken != null && currentToken.typeSymbolizeBeginOfExp()) {
                node.addChild(parseCond(node));
            }
            addCorrectTokenAndNext(node, TokenType.SEMICN);
            if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
                node.addChild(parseForStmt(node));
            }
            addCorrectTokenAndNext(node, TokenType.RPARENT);
            node.addChild(parseStmt(node));
        } else if (match(TokenType.BREAKTK)) {
            int line = currentToken.line();
            // 检查是否在循环内
            if (notInLoop(node)) {
                errors.addError(line, 'm');
            }
            addCurrentTokenAndNext(node);//expect(TokenType.BREAKTK);
            addCorrectTokenAndNext(node, TokenType.SEMICN);
        } else if (match(TokenType.CONTINUETK)) {
            int line = currentToken.line();
            // 检查是否在循环内
            if (notInLoop(node)) {
                errors.addError(line, 'm');
            }
            addCurrentTokenAndNext(node);//expect(TokenType.CONTINUETK);
            addCorrectTokenAndNext(node, TokenType.SEMICN);
        } else if (match(TokenType.RETURNTK)) {
            int line = currentToken.line();
            addCurrentTokenAndNext(node);//expect(TokenType.RETURNTK);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                try {
                    TryParser tryParser = new TryParser(tokens,index);
                    tryParser.tryParseExp();
                    if (tryParser.match(TokenType.SEMICN)) {
                        node.addChild(parseExp(node));
                        // 回溯到函数定义，看是不是void，如果是void则发生错误f
                        if (node.getCurrentFuncType().equals("void")) {
                            errors.addError(line, 'f');
                        }
                    } else if (tryParser.match(TokenType.ASSIGN)) {
                    } else {
                        node.addChild(parseExp(node));
                        // 回溯到函数定义，看是不是void，如果是void则发生错误f
                        if (node.getCurrentFuncType().equals("void")) {
                            errors.addError(line, 'f');
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            addCorrectTokenAndNext(node, TokenType.SEMICN);
        } else if (match(TokenType.PRINTFTK)) {
            int line = currentToken.line();
            addCurrentTokenAndNext(node);//expect(TokenType.PRINTFTK);
            addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
            String formatString = currentToken.value();
            addCurrentTokenAndNext(node);//expect(TokenType.STRCON);
            int formatCount = countFormatSpecifiers(formatString);
            int expCount = 0;
            while (match(TokenType.COMMA)) {
                addCurrentTokenAndNext(node);//expect(TokenType.COMMA);
                node.addChild(parseExp(node));
                expCount++;
            }
            if (formatCount != expCount) {
                System.out.println("formatCount: " + formatCount + " expCount: " + expCount);
                errors.addError(line, 'l');
            }
            addCorrectTokenAndNext(node, TokenType.RPARENT);
            addCorrectTokenAndNext(node, TokenType.SEMICN);
        } else if (match(TokenType.IDENFR)) {
            int index_temp = this.index;
            while (currentToken != null && currentToken.type() != TokenType.SEMICN && currentToken.type() != TokenType.ASSIGN) {
                nextToken();
            }
            Token signToken = currentToken;
            this.index = index_temp;
            this.currentToken = this.tokens.get(this.index);
            if (signToken.type() == TokenType.ASSIGN) {
                TryParser tryParser = new TryParser(tokens,index);
                tryParser.tryParseLVal();
                if (tryParser.match(TokenType.ASSIGN)) {
                    node.addChild(parseLVal(node));
                    Symbol symbol = symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value());
                    if (symbol != null && symbol.isConst()) {
                        errors.addError(getLastToken().line(), 'h');
                    }
                    addCurrentTokenAndNext(node);//expect(TokenType.ASSIGN);
                    if (match(TokenType.GETINTTK)) {
                        addCurrentTokenAndNext(node);//expect(TokenType.GETINTTK);
                        addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
                        addCorrectTokenAndNext(node, TokenType.RPARENT);
                        addCorrectTokenAndNext(node, TokenType.SEMICN);
                    } else if (match(TokenType.GETCHARTK)) {
                        addCurrentTokenAndNext(node);//expect(TokenType.GETCHARTK);
                        addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
                        addCorrectTokenAndNext(node, TokenType.RPARENT);
                        addCorrectTokenAndNext(node, TokenType.SEMICN);
                    } else {
                        node.addChild(parseExp(node));
                        addCorrectTokenAndNext(node, TokenType.SEMICN);
                    }
                } else {
                    node.addChild(parseExp(node));
                    addCorrectTokenAndNext(node, TokenType.SEMICN);
                }
            } else {
                node.addChild(parseExp(node));
                addCorrectTokenAndNext(node, TokenType.SEMICN);
            }
        } else {
            node.addChild(parseExp(node));
            addCorrectTokenAndNext(node, TokenType.SEMICN);
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
            if ((formatString.charAt(i) == '%' && formatString.charAt(i + 1) == 'd') ||
                (formatString.charAt(i) == '%' && formatString.charAt(i + 1) == 'c')) {
                count++;
            }
        }
        return count;
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
            addCurrentTokenAndNext(node);
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
            addCurrentTokenAndNext(node);
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
            addCurrentTokenAndNext(node);
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
            addCurrentTokenAndNext(node);
            node.addChild(parseAddExp(node));
        }
        return node;
    }

    private ParserTreeNode parseForStmt(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.ForStmt);
        node.setParent(parent);
        node.addChild(parseLVal(node));
        Symbol symbol = symbolTable.getSymbol(node.getChildren().get(0).getChildren().get(0).getToken().value());
        if (symbol != null && symbol.isConst()) {
            errors.addError(getLastToken().line(), 'h');
        }
        addCurrentTokenAndNext(node);//expect(TokenType.ASSIGN);
        node.addChild(parseExp(node));
        return node;
    }

    private ParserTreeNode parseMainFuncDef(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.MainFuncDef);
        node.setParent(parent);
        addCurrentTokenAndNext(node);//expect(TokenType.INTTK);
        addCurrentTokenAndNext(node);//expect(TokenType.MAINTK);
        addCurrentTokenAndNext(node);//expect(TokenType.LPARENT);
        if (symbolTable.containsSymbolInCurrentScope("main")) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol("main",SymbolType.IntFunc);
        }
        addCorrectTokenAndNext(node, TokenType.RPARENT);
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
        addCurrentTokenAndNext(node);//expect(TokenType.INTCON);
        return node;
    }

    private ParserTreeNode parseCharacter(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.Character);
        node.setParent(parent);
        addCurrentTokenAndNext(node);//expect(TokenType.CHRCON);
        return node;
    }

    private ParserTreeNode parseUnaryOp(ParserTreeNode parent) {
        ParserTreeNode node = new ParserTreeNode(SyntaxType.UnaryOp);
        node.setParent(parent);
        addCurrentTokenAndNext(node);
        return node;
    }
}