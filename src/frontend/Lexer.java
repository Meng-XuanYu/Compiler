package frontend;

import java.util.ArrayList;
import java.util.List;

public class Lexer {
    //词法分析只会产生一个错误
    private boolean error;
    private Character errorType;
    private boolean inMultiLineComment = false;

    public List<Token> tokenize(String line, int lineNum) {
        List<Token> tokens = new ArrayList<>();
        int length = line.length();
        int i = 0;
        this.error = false;

        while (i < length) {
            char currentChar = line.charAt(i);

            if (this.inMultiLineComment) {
                if (currentChar == '*' && i + 1 < length && line.charAt(i + 1) == '/') {
                    this.inMultiLineComment = false;
                    i += 2;
                } else {
                    i++;
                }
                continue;
            }

            if (Character.isWhitespace(currentChar)) {
                i++;
                continue;
            }

            // 单行注释
            if (currentChar == '/' && i + 1 < length && line.charAt(i + 1) == '/') {
                break; // 忽略单行注释后的内容
            }

            // 多行注释
            if (currentChar == '/' && i + 1 < length && line.charAt(i + 1) == '*') {
                this.inMultiLineComment = true;
                i += 2;
                continue;
            }

            if (Character.isDigit(currentChar)) {
                int start = i;
                while (i < length && Character.isDigit(line.charAt(i))) {
                    i++;
                }
                tokens.add(new Token(TokenType.INTCON, line.substring(start, i), lineNum));
                continue;
            }

            if (Character.isLetter(currentChar) || currentChar == '_') {
                int start = i;
                while (i < length && (Character.isLetterOrDigit(line.charAt(i)) || line.charAt(i) == '_')) {
                    i++;
                }
                String word = line.substring(start, i);
                TokenType type = getKeywordTokenType(word);
                tokens.add(new Token(type, word, lineNum));
                continue;
            }

            switch (currentChar) {
                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", lineNum));
                    break;
                case '-':
                    tokens.add(new Token(TokenType.MINU, "-", lineNum));
                    break;
                case '*':
                    tokens.add(new Token(TokenType.MULT, "*", lineNum));
                    break;
                case '/':
                    tokens.add(new Token(TokenType.DIV, "/", lineNum));
                    break;
                case '%':
                    tokens.add(new Token(TokenType.MOD, "%", lineNum));
                    break;
                case '<':
                    if (i + 1 < length && line.charAt(i + 1) == '=') {
                        tokens.add(new Token(TokenType.LEQ, "<=", lineNum));
                        i++;
                    } else {
                        tokens.add(new Token(TokenType.LSS, "<", lineNum));
                    }
                    break;
                case '>':
                    if (i + 1 < length && line.charAt(i + 1) == '=') {
                        tokens.add(new Token(TokenType.GEQ, ">=", lineNum));
                        i++;
                    } else {
                        tokens.add(new Token(TokenType.GRE, ">", lineNum));
                    }
                    break;
                case '=':
                    if (i + 1 < length && line.charAt(i + 1) == '=') {
                        tokens.add(new Token(TokenType.EQL, "==", lineNum));
                        i++;
                    } else {
                        tokens.add(new Token(TokenType.ASSIGN, "=", lineNum));
                    }
                    break;
                case '!':
                    if (i + 1 < length && line.charAt(i + 1) == '=') {
                        tokens.add(new Token(TokenType.NEQ, "!=", lineNum));
                        i++;
                    } else {
                        tokens.add(new Token(TokenType.NOT, "!", lineNum));
                    }
                    break;
                case '&':
                    tokens.add(new Token(TokenType.AND, "&&", lineNum));
                    if (i + 1 < length && line.charAt(i + 1) == '&') {
                        i++;
                    } else {
                        setError('a');
                        //继续扫描
                    }
                    break;
                case '|':
                    tokens.add(new Token(TokenType.OR, "||", lineNum));
                    if (i + 1 < length && line.charAt(i + 1) == '|') {
                        i++;
                    } else {
                        setError('a');
                        //继续扫描
                    }
                    break;
                case ';':
                    tokens.add(new Token(TokenType.SEMICN, ";", lineNum));
                    break;
                case ',':
                    tokens.add(new Token(TokenType.COMMA, ",", lineNum));
                    break;
                case '(':
                    tokens.add(new Token(TokenType.LPARENT, "(", lineNum));
                    break;
                case ')':
                    tokens.add(new Token(TokenType.RPARENT, ")", lineNum));
                    break;
                case '[':
                    tokens.add(new Token(TokenType.LBRACK, "[", lineNum));
                    break;
                case ']':
                    tokens.add(new Token(TokenType.RBRACK, "]", lineNum));
                    break;
                case '{':
                    tokens.add(new Token(TokenType.LBRACE, "{", lineNum));
                    break;
                case '}':
                    tokens.add(new Token(TokenType.RBRACE, "}", lineNum));
                    break;
                case '\'':
                    if (i + 3 < length && line.charAt(i + 1) == '\\' && line.charAt(i + 3) == '\'') {
                        String charConst = line.substring(i, i + 4);
                        if (isValidCharConst(charConst)) {
                            tokens.add(new Token(TokenType.CHRCON, charConst, lineNum));
                            i += 3;
                        } else {
                            //setError("INVALID_CHAR_CONST");
                        }
                    } else if (i + 2 < length && line.charAt(i + 2) == '\'') {
                        String charConst = line.substring(i, i + 3);
                        if (isValidCharConst(charConst)) {
                            tokens.add(new Token(TokenType.CHRCON, charConst, lineNum));
                            i += 2;
                        } else {
                            //setError("INVALID_CHAR_CONST");
                        }
                    } else {
                        //setError("INVALID_CHAR_CONST");
                    }
                    break;
                case '\"':
                    int start = i;
                    i++;
                    while (i < length && line.charAt(i) != '\"') {
                        if (line.charAt(i) == '\\' && i + 1 < length && line.charAt(i + 1) == 'n') {
                            i += 2;
                        } else {
                            i++;
                        }
                    }
                    if (i < length) {
                        tokens.add(new Token(TokenType.STRCON, line.substring(start, i + 1), lineNum));
                    } else {
                        //setError("INVALID_STRING_CONST");
                    }
                    break;
                default:
                    //setError("UNKNOWN");
            }
            i++;
        }

        return tokens;
    }

    // 判断字符常量是否合法 （包括转义字符）
    // 但是根据文法，不会出现这类错误 = =
    private boolean isValidCharConst(String charConst) {
        if (charConst.length() == 3) {
            char c = charConst.charAt(1);
            return (c >= 32 && c <= 126) && c != '\\';
        } else if (charConst.length() == 4 && charConst.charAt(1) == '\\') {
            char c = charConst.charAt(2);
            return c == 'a' || c == 'b' || c == 't' || c == 'n' || c == 'v' || c == 'f' || c == '\"' || c == '\'' || c == '\\' || c == '0';
        }
        return false;
    }

    private TokenType getKeywordTokenType(String word) {
        return switch (word) {
            case "else" -> TokenType.ELSETK;
            case "void" -> TokenType.VOIDTK;
            case "main" -> TokenType.MAINTK;
            case "for" -> TokenType.FORTK;
            case "const" -> TokenType.CONSTTK;
            case "getint" -> TokenType.GETINTTK;
            case "getchar" -> TokenType.GETCHARTK;
            case "printf" -> TokenType.PRINTFTK;
            case "if" -> TokenType.IFTK;
            case "break" -> TokenType.BREAKTK;
            case "return" -> TokenType.RETURNTK;
            case "continue" -> TokenType.CONTINUETK;
            case "int" -> TokenType.INTTK;
            case "char" -> TokenType.CHARTK;
            default -> TokenType.IDENFR;
        };
    }

    private void setError(Character errorType) {
        this.error = true;
        this.errorType = errorType;
    }

    public boolean hasError() {
        return error;
    }

    public Character getErrorType() {
        return errorType;
    }
}