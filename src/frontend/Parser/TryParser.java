package frontend.Parser;
import frontend.Token;
import frontend.TokenType;
import java.util.List;
import java.util.Objects;

public class TryParser {
    private final List<Token> tokens;
    private int index;
    private Token currentToken;

    public TryParser(List<Token> tokens, int index) {
        this.tokens = tokens;
        this.index = index;
        this.currentToken = tokens.get(index);
    }

    public boolean match(TokenType type) {
        return this.currentToken != null && this.currentToken.type() == type;
    }

    private void nextToken() {
        this.index += 1;
        this.currentToken = index <= tokens.size() - 1 ? this.tokens.get(this.index) : null;
    }

    private Token getNextToken() {
        return index <= tokens.size() - 1 ? this.tokens.get(this.index + 1) : null;
    }

    public void tryIdenfrToken() {
        if (currentToken != null && currentToken.type() == TokenType.IDENFR) {
            nextToken();
        } else {
            throw new RuntimeException("Syntax error: expected " + TokenType.IDENFR + " at line " + Objects.requireNonNull(this.currentToken).line() + " token: " + this.currentToken);
        }
    }

    public void tryParseLVal() {
        tryIdenfrToken();
        if (match(TokenType.LBRACK)) {
            nextToken();
            tryParseExp();
            if (match(TokenType.RBRACK)) {
                nextToken();
            }
        }
    }

    public void tryParseExp() {
        tryParseAddExp();
    }

    public void tryParseAddExp() {
        tryParseMulExp();
        while (currentToken != null && (currentToken.type() == TokenType.PLUS || currentToken.type() == TokenType.MINU)) {
            nextToken();
            tryParseMulExp();
        }
    }

    public void tryParseMulExp() {
        tryParseUnaryExp();
        while (currentToken != null && (currentToken.type() == TokenType.MULT || currentToken.type() == TokenType.DIV || currentToken.type() == TokenType.MOD)) {
            nextToken();
            tryParseUnaryExp();
        }
    }

    public void tryParseUnaryExp() {
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

    public void tryParseFuncRParams() {
        tryParseExp();
        while (match(TokenType.COMMA)) {
            nextToken();
            tryParseExp();
        }
    }

    public void tryParsePrimaryExp() {
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
}
