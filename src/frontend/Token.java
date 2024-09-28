package frontend;

public record Token(TokenType type, String value, int line) {
    public String toString() {
        return type + " " + value + " " + line;
    }

    public boolean typeSymbolizeDecl() {
        return type == TokenType.INTTK
                || type == TokenType.CHARTK
                || type == TokenType.CONSTTK;
    }

    public boolean typeSymbolizeStmt() {
        return type == TokenType.IDENFR
                || type == TokenType.LBRACE
                || type == TokenType.IFTK
                || type == TokenType.ELSETK
                || type == TokenType.BREAKTK
                || type == TokenType.CONTINUETK
                || type == TokenType.RETURNTK
                || type == TokenType.PRINTFTK
                || type == TokenType.SEMICN
                || type == TokenType.FORTK
                || typeSymbolizeBeginOfExp();
    }

    public boolean typeSymbolizeBeginOfExp() {
        return type == TokenType.LPARENT
                || type == TokenType.IDENFR
                || type == TokenType.INTCON
                || type == TokenType.NOT
                || type == TokenType.PLUS
                || type == TokenType.MINU;
    }
}