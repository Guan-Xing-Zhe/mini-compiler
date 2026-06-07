package compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 词法分析器（Lexer / Scanner）
 *
 * 编译原理第一阶段：将源代码字符串逐个字符扫描，识别并生成 token 序列。
 *
 * 实现要点：
 * - 双指针（start / current）维护当前词素的起止位置
 * - 最长匹配原则：识别最长的合法词素
 * - 字符串字面量（"..."）：遇到 " 开始，匹配到下一个 " 结束
 * - 数字字面量：支持整数和浮点数
 * - 单行注释（//）：跳过直到行尾
 * - 关键字优先于标识符：先查关键字表，匹配不上则作为标识符
 * - 空白字符（空格、制表符、回车）被直接跳过
 */
public class Lexer {
    private static final Map<String, TokenType> KEYWORDS = new HashMap<>();

    static {
        KEYWORDS.put("let", TokenType.LET);
        KEYWORDS.put("if", TokenType.IF);
        KEYWORDS.put("else", TokenType.ELSE);
        KEYWORDS.put("while", TokenType.WHILE);
        KEYWORDS.put("for", TokenType.FOR);
        KEYWORDS.put("function", TokenType.FUNCTION);
        KEYWORDS.put("return", TokenType.RETURN);
        KEYWORDS.put("print", TokenType.PRINT);
        KEYWORDS.put("true", TokenType.TRUE);
        KEYWORDS.put("false", TokenType.FALSE);
        KEYWORDS.put("nil", TokenType.NIL);
        KEYWORDS.put("and", TokenType.AND);
        KEYWORDS.put("or", TokenType.OR);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;   // 当前词素在 source 中的起始位置
    private int current = 0; // 当前扫描指针位置
    private int line = 1;    // 当前行号

    public Lexer(String source) {
        this.source = source;
    }

    /**
     * 扫描全部字符，返回完整 token 列表。
     * 逐字符扫描直到文件末尾，然后添加 EOF 标记。
     */
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * 扫描一个 token。
     * 根据当前字符的类型决定如何识别 token。
     * 运算符和分隔符直接匹配单字符；复杂 token 调用专用方法。
     */
    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case ',': addToken(TokenType.COMMA); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '*': addToken(TokenType.STAR); break;
            case '%': addToken(TokenType.PERCENT); break;
            case '/':
                // 如果是 // 则是单行注释，跳过整行
                if (match('/')) {
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(TokenType.SLASH);
                }
                break;
            // 双字符运算符：检查下一个字符是否 =
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            // 空白字符直接跳过
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

    /** 扫描字符串字面量："..." */
    private void scanString() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }
        if (isAtEnd()) {
            System.err.println("Lexer error: unterminated string at line " + line);
            return;
        }
        advance(); // 跳过结束的 "
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    /** 扫描数字字面量：支持整数和浮点数 */
    private void scanNumber() {
        while (isDigit(peek())) advance();
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // 跳过小数点
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /** 扫描标识符或关键字 */
    private void scanIdentifier() {
        while (isAlphaNumeric(peek())) advance();
        String text = source.substring(start, current);
        TokenType type = KEYWORDS.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    /** 条件性前进：当下一个字符符合预期时才消费它 */
    private boolean match(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    /** 窥探当前字符，不消费 */
    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    /** 窥探下一个字符 */
    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }
    private boolean isAlphaNumeric(char c) { return isAlpha(c) || isDigit(c); }
    private boolean isDigit(char c) { return c >= '0' && c <= '9'; }
    private boolean isAtEnd() { return current >= source.length(); }
    private char advance() { return source.charAt(current++); }
    private void addToken(TokenType type) { addToken(type, null); }
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }
}
