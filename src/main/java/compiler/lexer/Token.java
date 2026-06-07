package compiler.lexer;

/**
 * 词法单元（Token）
 *
 * 词法分析器（Lexer）的输出单位，包含：
 * - type: token 类型（见 TokenType）
 * - lexeme: 原始词素字符串
 * - literal: 字面量的运行时值（如数字的 Double 值、字符串的 String 值）
 * - line: 源代码行号，用于错误定位
 */
public class Token {
    public final TokenType type;
    public final String lexeme;
    public final Object literal;
    public final int line;

    public Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    @Override
    public String toString() {
        return type + "(" + lexeme + ")";
    }
}
