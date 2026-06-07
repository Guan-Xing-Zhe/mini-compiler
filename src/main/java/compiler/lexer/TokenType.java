package compiler.lexer;

/**
 * 词法单元类型枚举（TokenType）
 *
 * 编译原理：词法分析阶段，将源代码字符流分割为有意义的词素（lexeme），
 * 每个词素对应一个词法单元（token），token 由其类型和值组成。
 *
 * 这里定义了 MiniLang 语言支持的所有 token 类型，包括：
 * - 单字符运算符：+ - * / ( ) { } = ; , ! < >
 * - 双字符运算符：== != <= >= && ||
 * - 字面量：数字、字符串
 * - 关键字：let if else while for function return print
 * - 标识符：变量名、函数名
 */
public enum TokenType {
    // 单字符运算符 / 分隔符
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, SEMICOLON,

    // 算术 / 逻辑运算符
    PLUS, MINUS, STAR, SLASH, PERCENT,
    BANG, BANG_EQUAL,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    AND, OR,

    // 字面量与标识符
    IDENTIFIER, NUMBER, STRING,

    // 关键字
    LET, IF, ELSE, WHILE, FOR, FUNCTION, RETURN, PRINT,
    TRUE, FALSE, NIL,

    // 文件结束标记
    EOF
}
