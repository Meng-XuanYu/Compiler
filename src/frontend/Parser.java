package frontend;

import java.util.List;
import java.util.Iterator;

public class Parser {
    private final List<Token> tokens;
    private final Iterator<Token> iterator;
    private Token currentToken;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.iterator = tokens.iterator();
        this.currentToken = iterator.hasNext() ? iterator.next() : null;
    }

    private void nextToken() {
        currentToken = iterator.hasNext() ? iterator.next() : null;
    }

    private boolean match(TokenType type) {
        if (currentToken != null && currentToken.type() == type) {
            nextToken();
            return true;
        }
        return false;
    }

    private void expect(TokenType type) {
        if (!match(type)) {
            throw new RuntimeException("Syntax error: expected " + type);
        }
    }

    public void parse() {
        parseCompUnit();
    }

    private void parseCompUnit() {
        while (currentToken != null && (currentToken.type() == TokenType.CONSTTK || currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK)) {
            if (currentToken.type() == TokenType.CONSTTK) {
                parseConstDecl();
            } else {
                parseVarDecl();
            }
        }
        while (currentToken != null && (currentToken.type() == TokenType.VOIDTK || currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK)) {
            parseFuncDef();
        }
        parseMainFuncDef();
    }

    private void parseConstDecl() {
        expect(TokenType.CONSTTK);
        parseBType();
        do {
            parseConstDef();
        } while (match(TokenType.COMMA));
        expect(TokenType.SEMICN);
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
    }

    private void parseConstExp() {
        parseAddExp();
    }

    private void parseAddExp() {
        parseMulExp();
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            nextToken();
            parseMulExp();
        }
    }

    private void parseMulExp() {
        parseUnaryExp();
        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            nextToken();
            parseUnaryExp();
        }
    }

    private void parseUnaryExp() {
        if (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU || currentToken.type() == TokenType.NOT)) {
            nextToken();
            parseUnaryExp();
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
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
    }

    private void parseFuncRParams() {
    }

    private void parsePrimaryExp() {
        if (match(TokenType.LPARENT)) {
            parseExp();
            expect(TokenType.RPARENT);
        } else if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            parseLVal();
        } else if (currentToken != null && currentToken.type() == TokenType.INTCON) {
            nextToken();
        } else if (currentToken != null && currentToken.type() == TokenType.CHRCON) {
            nextToken();
        } else {
            throw new RuntimeException("Syntax error: expected PrimaryExp");
        }
    }

    private void parseExp() {
        parseAddExp();
    }

    private void parseLVal() {
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            parseExp();
            expect(TokenType.RBRACK);
        }
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
        } else {
            parseConstExp();
        }
    }

    private void parseVarDecl() {
        parseBType();
        do {
            parseVarDef();
        } while (match(TokenType.COMMA));
        expect(TokenType.SEMICN);
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
        } else {
            parseExp();
        }
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
    }

    private void parseFuncType() {
        if (!(match(TokenType.VOIDTK) || match(TokenType.INTTK) || match(TokenType.CHARTK))) {
            throw new RuntimeException("Syntax error: expected FuncType");
        }
    }

    private void parseFuncFParams() {
        do {
            parseFuncFParam();
        } while (match(TokenType.COMMA));
    }

    private void parseFuncFParam() {
        parseBType();
        expect(TokenType.IDENFR);
        if (match(TokenType.LBRACK)) {
            expect(TokenType.RBRACK);
        }
    }

    private void parseBlock() {
        expect(TokenType.LBRACE);
        while (currentToken != null && (currentToken.type() == TokenType.CONSTTK || currentToken.type() == TokenType.INTTK || currentToken.type() == TokenType.CHARTK || currentToken.type() == TokenType.LBRACE || currentToken.type() == TokenType.IFTK || currentToken.type() == TokenType.FORTK || currentToken.type() == TokenType.BREAKTK || currentToken.type() == TokenType.CONTINUETK || currentToken.type() == TokenType.RETURNTK || currentToken.type() == TokenType.PRINTFTK || currentToken.type() == TokenType.IDENFR || currentToken.type() == TokenType.SEMICN)) {
            parseBlockItem();
        }
        expect(TokenType.RBRACE);
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

    private void parseStmt() {
        if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
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
                throw new RuntimeException("Syntax error: expected '='");
            }
        } else if (match(TokenType.SEMICN)) {
            // Empty statement
        } else if (match(TokenType.LBRACE)) {
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
            if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
                parseForStmt();
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() != TokenType.SEMICN) {
                parseCond();
            }
            expect(TokenType.SEMICN);
            if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
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
            parseExp();
            expect(TokenType.SEMICN);
        }
    }

    private void parseCond() {
        parseLOrExp();
    }

    private void parseLOrExp() {
        do {
            parseLAndExp();
        } while (match(TokenType.OR));
    }

    private void parseLAndExp() {
        do {
            parseEqExp();
        } while (match(TokenType.AND));
    }

    private void parseEqExp() {
        parseRelExp();
        while (currentToken != null && (currentToken.type() == TokenType.EQL || currentToken.type() == TokenType.NEQ)) {
            nextToken();
            parseRelExp();
        }
    }

    private void parseRelExp() {
        parseAddExp();
        while (currentToken != null && (currentToken.type() == TokenType.LSS || currentToken.type() == TokenType.GRE || currentToken.type() == TokenType.LEQ || currentToken.type() == TokenType.GEQ)) {
            nextToken();
            parseAddExp();
        }
    }

    private void parseForStmt() {
        parseLVal();
        expect(TokenType.ASSIGN);
        parseExp();
    }

    private void parseMainFuncDef() {
        expect(TokenType.INTTK);
        expect(TokenType.MAINTK);
        expect(TokenType.LPARENT);
        expect(TokenType.RPARENT);
        parseBlock();
    }

    public List<Token> getTokens() {
        return tokens;
    }
}