package compiler.lexer;

/**
 * 词法分析器（Lexer）
 *
 * 编译原理第一阶段：将源代码字符串逐个字符扫描，识别并生成 token 序列。
 *
 * 实现要点：
 * - 双指针（start/current）维护当前词素的起止位置
 * - 采用"最长匹配"原则：识别最长的合法词素
 * - 支持字符串字面量（"..."）、数字字面量（整数/浮点数）
 * - 支持单行注释（//）
 * - 关键字优先于标识符匹配（先查表，再回退）
 * - 空白字符和换行符被跳过，换行用于更新行号
 */
\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case ' ': case '\r': case '\t': break;
            case '\n': line++; break;
            case '"': scanString(); break;
            default:
                if (isDigit(c)) {
                    scanNumber();
                } else if (isAlpha(c)) {
                    scanIdentifier();
                } else {
                    System.err.println("Lexer error: unexpected character '" + c + "' at line " + line);
                }
        }
    }

    private void scanString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            System.err.println("Lexer error: unterminated string at line " + line);
            return;
        }
        advance();
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void scanNumber() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanIdentifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
