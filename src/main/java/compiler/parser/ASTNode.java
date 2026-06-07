package compiler.parser;

import compiler.lexer.Token;
import java.util.List;

/**
 * 抽象语法树（AST）节点定义
 *
 * 编译原理第二阶段：语法分析器（Parser）将 token 序列转换为 AST。
 * AST 是源代码的树状中间表示，去除了括号、分号等语法细节，
 * 只保留程序的层次结构。
 *
 * 采用"组合模式"设计：
 * - Expr（表达式）：产生值的语法结构
 * - Stmt（语句）：执行动作的语法结构
 * - 复合节点（如 Block、IfStmt、FunctionDecl）包含子节点
 *
 * 每种节点类型对应一种语法产生式（grammar production）。
 */
public abstract class ASTNode {

    public static abstract class Stmt extends ASTNode {}
    public static abstract class Expr extends ASTNode {}

    // --- 表达式节点（对应文法中的 expression 层） ---

    /** 二元运算：left op right，如 a + b, x > y */
    public static class Binary extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;
        public Binary(Expr left, Token op, Expr right) {
            this.left = left; this.op = op; this.right = right;
        }
    }

    /** 一元运算：op right，如 !flag, -num */
    public static class Unary extends Expr {
        public final Token op;
        public final Expr right;
        public Unary(Token op, Expr right) {
            this.op = op; this.right = right;
        }
    }

    /** 字面量：数字、字符串、布尔值、nil */
    public static class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
    }

    /** 变量引用：读取变量值 */
    public static class Variable extends Expr {
        public final Token name;
        public Variable(Token name) { this.name = name; }
    }

    /** 赋值表达式：variable = value */
    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) { this.name = name; this.value = value; }
    }

    /** 逻辑运算：left && right 或 left || right（短路求值） */
    public static class Logical extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;
        public Logical(Expr left, Token op, Expr right) {
            this.left = left; this.op = op; this.right = right;
        }
    }

    /** 函数调用：callee(args) */
    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee; this.paren = paren; this.arguments = arguments;
        }
    }

    /** 括号分组：用于强制改变运算优先级 */
    public static class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Expr expression) { this.expression = expression; }
    }

    // --- 语句节点（对应文法中的 statement 层） ---

    /** 表达式语句：expression; 执行表达式并丢弃结果 */
    public static class ExpressionStmt extends Stmt {
        public final Expr expression;
        public ExpressionStmt(Expr expression) { this.expression = expression; }
    }

    /** 打印语句：print(expression); */
    public static class PrintStmt extends Stmt {
        public final Expr expression;
        public PrintStmt(Expr expression) { this.expression = expression; }
    }

    /** 变量声明：let name = initializer; */
    public static class VarDecl extends Stmt {
        public final Token name;
        public final Expr initializer;
        public VarDecl(Token name, Expr initializer) { this.name = name; this.initializer = initializer; }
    }

    /** 语句块：{ statements }，创建新的作用域 */
    public static class Block extends Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
    }

    /** if 语句：if (condition) thenBranch else elseBranch */
    public static class IfStmt extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
        }
    }

    /** while 循环：while (condition) body */
    public static class WhileStmt extends Stmt {
        public final Expr condition;
        public final Stmt body;
        public WhileStmt(Expr condition, Stmt body) { this.condition = condition; this.body = body; }
    }

    /** 函数声明：function name(params) body */
    public static class FunctionDecl extends Stmt {
        public final Token name;
        public final List<Token> parameters;
        public final Block body;
        public FunctionDecl(Token name, List<Token> parameters, Block body) {
            this.name = name; this.parameters = parameters; this.body = body;
        }
    }

    /** return 语句：return value; 从函数中返回值 */
    public static class ReturnStmt extends Stmt {
        public final Token keyword;
        public final Expr value;
        public ReturnStmt(Token keyword, Expr value) { this.keyword = keyword; this.value = value; }
    }
}
