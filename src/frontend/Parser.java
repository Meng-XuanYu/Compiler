package frontend;
import java.util.List;
import java.util.Objects;

public class Parser {
    private final List<Token> tokens;
    private int index;
    private Token currentToken;
    private final List<String> parserOutput;
    private final ErrorList errors;

    public Parser(List<Token> tokens, List<String> parserOutput, ErrorList errors) {
        this.tokens = tokens;
        this.index = 0;
        this.currentToken = this.tokens.get(this.index);
        this.parserOutput = parserOutput;
        this.errors = errors;
    }

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
        addTokenOutput(this.currentToken);
        this.index += 1;
        this.currentToken = index <= tokens.size() - 1 ? this.tokens.get(this.index) : null;
    }

    private void prevToken() {
        this.index -= 1;
        this.currentToken = this.tokens.get(this.index);
        deleteTokenOutput();
    }

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
                throw new RuntimeException("Syntax error: expected " + type + " at line " + this.currentToken.line() + "token:" + this.currentToken);
            }
        }
    }

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
        parseBType();
        do {
            parseConstDef();
        } while (match(TokenType.COMMA));
        expect(TokenType.SEMICN);

        addSyntaxOutput(SyntaxType.ConstDecl);
    }

    private void parseBType() {
        if (!(match(TokenType.INTTK) || match(TokenType.CHARTK))) {
            throw new RuntimeException("Syntax error: expected BType");
        }
    }

    private void parseConstDef() {
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            parseConstExp();
            expect(TokenType.RBRACK);
        }
        expect(TokenType.ASSIGN);
        parseConstInitVal();

        addSyntaxOutput(SyntaxType.ConstDef);
    }

    private void parseConstExp() {
        parseAddExp();

        addSyntaxOutput(SyntaxType.ConstExp);
    }

    private void parseAddExp() {
        parseMulExp();
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            nextToken();
            parseMulExp();
        }

        addSyntaxOutput(SyntaxType.AddExp);
    }

    private void parseMulExp() {
        parseUnaryExp();
        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            nextToken();
            parseUnaryExp();
        }

        addSyntaxOutput(SyntaxType.MulExp);
    }

    private void parseUnaryExp() {
        if (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU || currentToken.type() == TokenType.NOT)) {
            parseUnaryOp();
            parseUnaryExp();
        } else if (currentToken != null &&
                currentToken.type() == TokenType.IDENFR && Objects.requireNonNull(getNextToken()).type() == TokenType.LPARENT) {
            nextToken();
            if (match(TokenType.LPARENT)) {
                if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
                    parseFuncRParams();
                }
                expect(TokenType.RPARENT);
            }
        } else {
            parsePrimaryExp();
        }

        addSyntaxOutput(SyntaxType.UnaryExp);
    }

    private void parseFuncRParams() {
        do {
            parseExp();
        } while (match(TokenType.COMMA));

        addSyntaxOutput(SyntaxType.FuncRParams);
    }

    private void parsePrimaryExp() {
        if (match(TokenType.LPARENT)) {
            parseExp();
            expect(TokenType.RPARENT);
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            parseLVal();
        } else if (currentToken != null && currentToken.type() == TokenType.INTCON) {
            parseNumber();
        } else if (currentToken != null && currentToken.type() == TokenType.CHRCON) {
            parseCharacter();
        } else {
            throw new RuntimeException("Syntax error: expected PrimaryExp");
        }

        addSyntaxOutput(SyntaxType.PrimaryExp);
    }

    private void parseExp() {
        parseAddExp();

        addSyntaxOutput(SyntaxType.Exp);
    }

    private void parseLVal() {
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            parseExp();
            expect(TokenType.RBRACK);
        }

        addSyntaxOutput(SyntaxType.LVal);
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
        parseBType();
        do {
            parseVarDef();
        } while (match(TokenType.COMMA));
        expect(TokenType.SEMICN);

        addSyntaxOutput(SyntaxType.VarDecl);
    }

    private void parseVarDef() {
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            parseConstExp();
            expect(TokenType.RBRACK);
        }
        if (match(TokenType.ASSIGN)) {
            parseInitVal();
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
        parseFuncType();
        expect(TokenType.IDENFR);
        expect(TokenType.LPARENT);
        if (currentToken != null && currentToken.type() != TokenType.RPARENT) {
            parseFuncFParams();
        }
        expect(TokenType.RPARENT);
        parseBlock();

        addSyntaxOutput(SyntaxType.FuncDef);
    }

    private void parseFuncType() {
        if (!(match(TokenType.VOIDTK) || match(TokenType.INTTK) || match(TokenType.CHARTK))) {
            throw new RuntimeException("Syntax error: expected FuncType");
        }

        addSyntaxOutput(SyntaxType.FuncType);
    }

    private void parseFuncFParams() {
        do {
            parseFuncFParam();
        } while (match(TokenType.COMMA));

        addSyntaxOutput(SyntaxType.FuncFParams);
    }

    private void parseFuncFParam() {
        parseBType();
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            expect(TokenType.RBRACK);
        }

        addSyntaxOutput(SyntaxType.FuncFParam);
    }

    private void parseBlock() {
        expect(TokenType.LBRACE);
        while (currentToken != null && (currentToken.typeSymbolizeDecl() || currentToken.typeSymbolizeStmt())) {
            parseBlockItem();
        }
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
        do {
            parseLAndExp();
        } while (match(TokenType.OR));

        addSyntaxOutput(SyntaxType.LOrExp);
    }

    private void parseLAndExp() {
        do {
            parseEqExp();
        } while (match(TokenType.AND));

        addSyntaxOutput(SyntaxType.LAndExp);
    }

    private void parseEqExp() {
        parseRelExp();
        while (currentToken != null && (currentToken.type() == TokenType.EQL || currentToken.type() == TokenType.NEQ)) {
            nextToken();
            parseRelExp();
        }

        addSyntaxOutput(SyntaxType.EqExp);
    }

    private void parseRelExp() {
        parseAddExp();
        while (currentToken != null && (currentToken.type() == TokenType.LSS || currentToken.type() == TokenType.GRE || currentToken.type() == TokenType.LEQ || currentToken.type() == TokenType.GEQ)) {
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