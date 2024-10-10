package frontend;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final List<Token> tokens;
    private int index;
    private Token currentToken;
    private final List<String> parserOutput;
    private final ErrorList errors;
    private final SymbolTable symbolTable;

    public Parser(List<Token> tokens, List<String> parserOutput, ErrorList errors, SymbolTable symbolTable) {
        this.tokens = tokens;
        this.index = 0;
        this.currentToken = this.tokens.get(this.index);
        this.parserOutput = parserOutput;
        this.errors = errors;
        this.symbolTable = symbolTable;
    }

    // 语法分析输出函数群
    private void addTokenOutput(Token token) {
        if (token != null) {
            this.parserOutput.add(token.type() + " " + token.value());
        }
    }

    private void deleteTokenOutput() {
        this.parserOutput.remove(this.parserOutput.size() - 1);
    }

    private void addSyntaxOutput(SyntaxType syntax) {
        this.parserOutput.add('<' + syntax.toString() + '>');
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

    // 前进后退函数群
    private void nextToken() {
        addTokenOutput(this.currentToken);
        this.index += 1;
        this.currentToken = index <= tokens.size() - 1 ? this.tokens.get(this.index) : null;
    }

    private void prevToken() {
        this.index -= 1;
        this.currentToken = this.tokens.get(this.index);
        deleteTokenOutput();
    }

    // 判断函数群，直接用match的都是课题组保证不会出错的地方
    private boolean match(TokenType type) {
        if (this.currentToken != null && this.currentToken.type() == type) {
            nextToken();
            return true;
        } else {
            return false;
        }
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
        }
    }

    // 语法分析函数群
    public void parse() {
        parseCompUnit();
    }

    private void parseCompUnit() {
        while ((currentToken.type() == TokenType.CONSTTK) ||
                (Objects.requireNonNull(getNextToken()).type() == TokenType.IDENFR &&
                        (currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK) &&
                        (Objects.requireNonNull(getNext2Token()).type() != TokenType.LPARENT))) {
            if (currentToken.type() == TokenType.CONSTTK) {
                parseConstDecl();
            } else {
                parseVarDecl();
            }
        }
        while (currentToken.type() == TokenType.VOIDTK || currentToken.type() == TokenType.CHARTK ||
                (currentToken.type() == TokenType.INTTK && Objects.requireNonNull(getNextToken()).type() != TokenType.MAINTK)) {
            parseFuncDef();
        }
        parseMainFuncDef();
        addSyntaxOutput(SyntaxType.CompUnit);
    }

    private void parseConstDecl() {
        expect(TokenType.CONSTTK);
        boolean isChar = parseBType();
        do {
            parseConstDef(isChar);
        } while (match(TokenType.COMMA));
        expect(TokenType.SEMICN);

        addSyntaxOutput(SyntaxType.ConstDecl);
    }

    private boolean parseBType() {
        if (match(TokenType.INTTK)) {
            return false;
        } else if (match(TokenType.CHARTK)) {
            return true;
        } else {
            throw new RuntimeException("Syntax error: expected BType");
        }
    }

    private void parseConstDef(boolean isChar) {
        String name = currentToken.value();
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            isArr = true;
            parseConstExp();
            expect(TokenType.RBRACK);
        }
        expect(TokenType.ASSIGN);
        parseConstInitVal();

        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, isArr && isChar ? SymbolType.ConstCharArray : isChar ? SymbolType.ConstChar : isArr ? SymbolType.ConstIntArray : SymbolType.ConstInt);
        }
        addSyntaxOutput(SyntaxType.ConstDef);
    }

    private void parseConstExp() {
        parseAddExp();

        addSyntaxOutput(SyntaxType.ConstExp);
    }

    private SymbolType parseAddExp() {
        SymbolType type = parseMulExp();
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            addSyntaxOutput(SyntaxType.AddExp);
            nextToken();
            parseMulExp();
        }

        addSyntaxOutput(SyntaxType.AddExp);
        return type;
    }

    private SymbolType parseMulExp() {
        SymbolType type = parseUnaryExp();
        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            addSyntaxOutput(SyntaxType.MulExp);
            nextToken();
            parseUnaryExp();
        }

        addSyntaxOutput(SyntaxType.MulExp);
        return type;
    }

    private SymbolType parseUnaryExp() {
        SymbolType type;
        if (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU || currentToken.type() == TokenType.NOT)) {
            parseUnaryOp();
            type = parseUnaryExp();
        } else if (currentToken != null &&
                currentToken.type() == TokenType.IDENFR && Objects.requireNonNull(getNextToken()).type() == TokenType.LPARENT) {
            String functionName = currentToken.value();
            if (symbolTable.notContainsSymbol(functionName)) {
                this.errors.addError(currentToken.line(), 'c');
            }
            SymbolFunc functionSymbol = (SymbolFunc)symbolTable.getSymbol(functionName);
            type = functionSymbol.type();
            nextToken();
            if (match(TokenType.LPARENT)) {
                int paramCount = 0;
                ArrayList<SymbolType> paramTypes = new ArrayList<>();
                if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                    paramTypes = parseFuncRParams();
                    paramCount = paramTypes.size();
                }
                expect(TokenType.RPARENT);

                // 检查参数个数和类型是否匹配
                if (functionSymbol.getParamCount() != paramCount) {
                    errors.addError(getLastToken().line(), 'd');
                } else {
                    if (!functionSymbol.paramCorrect(paramTypes)) {
                        errors.addError(getLastToken().line(), 'e');
                    }
                }
            }
        } else {
            type = parsePrimaryExp();
        }

        addSyntaxOutput(SyntaxType.UnaryExp);
        return type;
    }

    private ArrayList<SymbolType> parseFuncRParams() {
        ArrayList<SymbolType> paramTypes = new ArrayList<>();
        do {
            paramTypes.add(parseExp());
        } while (match(TokenType.COMMA));

        addSyntaxOutput(SyntaxType.FuncRParams);
        return paramTypes;
    }

    private SymbolType parsePrimaryExp() {
        SymbolType type;
        if (match(TokenType.LPARENT)) {
            type = parseExp();
            expect(TokenType.RPARENT);
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            type = parseLVal();
        } else if (currentToken != null && currentToken.type() == TokenType.INTCON) {
            parseNumber();
            type = SymbolType.Int;
        } else if (currentToken != null && currentToken.type() == TokenType.CHRCON) {
            parseCharacter();
            type = SymbolType.Char;
        } else {
            throw new RuntimeException("Syntax error: expected PrimaryExp");
        }

        addSyntaxOutput(SyntaxType.PrimaryExp);
        return type;
    }

    private SymbolType parseExp() {
        SymbolType type = parseAddExp();

        addSyntaxOutput(SyntaxType.Exp);
        return type;
    }

    private SymbolType parseLVal() {
        String name = currentToken.value();
        if (symbolTable.notContainsSymbol(name)) {
            this.errors.addError(currentToken.line(), 'c');
        }
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            parseExp();
            expect(TokenType.RBRACK);
        }

        addSyntaxOutput(SyntaxType.LVal);
        return symbolTable.getSymbol(name).type();
    }

    private void parseConstInitVal() {
        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            nextToken();
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                do {
                    parseConstExp();
                } while (match(TokenType.COMMA));
            }
            expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            nextToken();
        } else {
            parseConstExp();
        }

        addSyntaxOutput(SyntaxType.ConstInitVal);
    }

    private void parseVarDecl() {
        boolean isChar = parseBType();
        do {
            parseVarDef(isChar);
        } while (match(TokenType.COMMA));
        expect(TokenType.SEMICN);

        addSyntaxOutput(SyntaxType.VarDecl);
    }

    private void parseVarDef(boolean isChar) {
        String name = currentToken.value();
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            isArr = true;
            parseConstExp();
            expect(TokenType.RBRACK);
        }
        if (match(TokenType.ASSIGN)) {
            parseInitVal();
        }

        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, isArr && isChar ? SymbolType.CharArray : isChar ? SymbolType.Char : isArr ? SymbolType.IntArray : SymbolType.Int);
        }
        addSyntaxOutput(SyntaxType.VarDef);
    }

    private void parseInitVal() {
        if (currentToken != null && currentToken.type() == TokenType.LBRACE) {
            nextToken();
            if (currentToken != null && currentToken.type() != TokenType.RBRACE) {
                do {
                    parseExp();
                } while (match(TokenType.COMMA));
            }
            expect(TokenType.RBRACE);
        } else if (currentToken != null && currentToken.type() == TokenType.STRCON) {
            nextToken();
        } else {
            parseExp();
        }

        addSyntaxOutput(SyntaxType.InitVal);
    }

    private void parseFuncDef() {
        TokenType tokenType = parseFuncType();
        String name = currentToken.value();
        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbol(name, tokenType == TokenType.CHARTK ? SymbolType.CharFunc : tokenType == TokenType.INTTK ? SymbolType.IntFunc : SymbolType.VoidFunc);
        }
        expect(TokenType.IDENFR);
        expect(TokenType.LPARENT);
        if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
            ArrayList<Symbol> params = parseFuncFParams();
            SymbolFunc symbolFunc = (SymbolFunc)this.symbolTable.getSymbol(name);
            symbolFunc.setParams(params);
        }
        expect(TokenType.RPARENT);
        parseBlock();

        addSyntaxOutput(SyntaxType.FuncDef);
    }

    private TokenType parseFuncType() {
        addSyntaxOutput(SyntaxType.FuncType);
        if(match(TokenType.VOIDTK)) {
            return TokenType.VOIDTK;
        } else if (match(TokenType.INTTK)) {
            return TokenType.INTTK;
        } else if (match(TokenType.CHARTK)) {
            return TokenType.CHARTK;
        } else {
            throw new RuntimeException("Syntax error: expected FuncType");
        }
    }

    private ArrayList<Symbol> parseFuncFParams() {
        ArrayList<Symbol> params = new ArrayList<>();
        do {
            params.add(parseFuncFParam());
        } while (match(TokenType.COMMA));

        addSyntaxOutput(SyntaxType.FuncFParams);
        return params;
    }

    private Symbol parseFuncFParam() {
        boolean isChar = parseBType();
        String name = currentToken.value();
        expect(TokenType.IDENFR);
        boolean isArr = false;
        if (match(TokenType.LBRACK)) {
            isArr = true;
            expect(TokenType.RBRACK);
        }

        if (symbolTable.containsSymbolInCurrentScope(name)) {
            errors.addError(currentToken.line(), 'b');
        } else {
            this.symbolTable.addSymbolFuncPara(name, isArr && isChar ? SymbolType.CharArray : isChar ? SymbolType.Char : isArr ? SymbolType.IntArray : SymbolType.Int);
        }
        addSyntaxOutput(SyntaxType.FuncFParam);
        return this.symbolTable.getSymbol(name);
    }

    private void parseBlock() {
        expect(TokenType.LBRACE);
        this.symbolTable.enterScope();
        while (currentToken != null && (currentToken.typeSymbolizeDecl() || currentToken.typeSymbolizeStmt())) {
            parseBlockItem();
        }
        this.symbolTable.exitScope();
        expect(TokenType.RBRACE);

        addSyntaxOutput(SyntaxType.Block);
    }

    private void parseBlockItem() {
        if (currentToken != null && (currentToken.type() == TokenType.CONSTTK || currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK)) {
            parseDecl();
        } else {
            parseStmt();
        }
    }

    private void parseDecl() {
        if (currentToken != null && currentToken.type() == TokenType.CONSTTK) {
            parseConstDecl();
        } else {
            parseVarDecl();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    private void parseStmt() {
        if (match(TokenType.SEMICN)) {
            // Empty statement
        } else if (match(TokenType.LBRACE)) {
            prevToken();
            parseBlock();
        } else if (match(TokenType.IFTK)) {
            expect(TokenType.LPARENT);
            parseCond();
            expect(TokenType.RPARENT);
            parseStmt();
            if (match(TokenType.ELSETK)) {
                parseStmt();
            }
        } else if (match(TokenType.FORTK)) {
            expect(TokenType.LPARENT);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                parseForStmt();
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                parseCond();
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                parseForStmt();
            }
            expect(TokenType.RPARENT);
            parseStmt();
        } else if (match(TokenType.BREAKTK)) {
            expect(TokenType.SEMICN);
        } else if (match(TokenType.CONTINUETK)) {
            expect(TokenType.SEMICN);
        } else if (match(TokenType.RETURNTK)) {
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                parseExp();
            }
            expect(TokenType.SEMICN);
        } else if (match(TokenType.PRINTFTK)) {
            expect(TokenType.LPARENT);
            expect(TokenType.STRCON);
            while (match(TokenType.COMMA)) {
                parseExp();
            }
            expect(TokenType.RPARENT);
            expect(TokenType.SEMICN);
        } else {
            int index_temp = this.index;
            while (currentToken != null && currentToken.type() != TokenType.SEMICN && currentToken.type() != TokenType.ASSIGN) {
                nextToken();
            }
            Token signToken = currentToken;
            while (this.index > index_temp) {
                prevToken();
            }
            if (signToken.type() == TokenType.ASSIGN) {
                List<String> parserOutputTemp = List.copyOf(this.parserOutput);
                parseLVal();
                if (match(TokenType.ASSIGN)) {
                    if (match(TokenType.GETINTTK)) {
                        expect(TokenType.LPARENT);
                        expect(TokenType.RPARENT);
                        expect(TokenType.SEMICN);
                    } else if (match(TokenType.GETCHARTK)) {
                        expect(TokenType.LPARENT);
                        expect(TokenType.RPARENT);
                        expect(TokenType.SEMICN);
                    } else {
                        parseExp();
                        expect(TokenType.SEMICN);
                    }
                } else {
                    this.parserOutput.clear();
                    this.parserOutput.addAll(parserOutputTemp);
                    this.index = index_temp;
                    this.currentToken = this.tokens.get(this.index);
                    parseExp();
                    expect(TokenType.SEMICN);
                }
            } else {
                parseExp();
                expect(TokenType.SEMICN);
            }
        }

        addSyntaxOutput(SyntaxType.Stmt);
    }

    private void parseCond() {
        parseLOrExp();

        addSyntaxOutput(SyntaxType.Cond);
    }

    private void parseLOrExp() {
        parseLAndExp();
        while (currentToken != null && currentToken.type() == TokenType.OR) {
            addSyntaxOutput(SyntaxType.LOrExp);
            nextToken();
            parseLAndExp();
        }

        addSyntaxOutput(SyntaxType.LOrExp);
    }

    private void parseLAndExp() {
        parseEqExp();
        while (currentToken != null && currentToken.type() == TokenType.AND) {
            addSyntaxOutput(SyntaxType.LAndExp);
            nextToken();
            parseEqExp();
        }

        addSyntaxOutput(SyntaxType.LAndExp);
    }

    private void parseEqExp() {
        parseRelExp();
        while (currentToken != null && (currentToken.type() == TokenType.EQL || currentToken.type() == TokenType.NEQ)) {
            addSyntaxOutput(SyntaxType.EqExp);
            nextToken();
            parseRelExp();
        }

        addSyntaxOutput(SyntaxType.EqExp);
    }

    private void parseRelExp() {
        parseAddExp();
        while (currentToken != null && (currentToken.type() == TokenType.LSS || currentToken.type() == TokenType.GRE || currentToken.type() == TokenType.LEQ || currentToken.type() == TokenType.GEQ)) {
            addSyntaxOutput(SyntaxType.RelExp);
            nextToken();
            parseAddExp();
        }

        addSyntaxOutput(SyntaxType.RelExp);
    }

    private void parseForStmt() {
        parseLVal();
        expect(TokenType.ASSIGN);
        parseExp();

        addSyntaxOutput(SyntaxType.ForStmt);
    }

    private void parseMainFuncDef() {
        expect(TokenType.INTTK);
        expect(TokenType.MAINTK);
        expect(TokenType.LPARENT);
        expect(TokenType.RPARENT);
        parseBlock();

        addSyntaxOutput(SyntaxType.MainFuncDef);
    }

    private void parseNumber() {
        expect(TokenType.INTCON);

        addSyntaxOutput(SyntaxType.Number);
    }

    private void parseCharacter() {
        expect(TokenType.CHRCON);

        addSyntaxOutput(SyntaxType.Character);
    }

    private void parseUnaryOp() {
        if (!(match(TokenType.PLUS) || match(TokenType.MINU) || match(TokenType.NOT))) {
            throw new RuntimeException("Syntax error: expected UnaryOp");
        }

        addSyntaxOutput(SyntaxType.UnaryOp);
    }
}