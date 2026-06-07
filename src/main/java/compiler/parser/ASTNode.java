package compiler.parser;

import compiler.lexer.Token;
import java.util.List;

public abstract class ASTNode {

    public static abstract class Stmt extends ASTNode {}

    public static abstract class Expr extends ASTNode {}

    // --- Expressions ---
    public static class Binary extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;
        public Binary(Expr left, Token op, Expr right) {
            this.left = left; this.op = op; this.right = right;
        }
    }

    public static class Unary extends Expr {
        public final Token op;
        public final Expr right;
        public Unary(Token op, Expr right) {
            this.op = op; this.right = right;
        }
    }

    public static class Literal extends Expr {
        public final Object value;
        public Literal(Object value) { this.value = value; }
    }

    public static class Variable extends Expr {
        public final Token name;
        public Variable(Token name) { this.name = name; }
    }

    public static class Assign extends Expr {
        public final Token name;
        public final Expr value;
        public Assign(Token name, Expr value) { this.name = name; this.value = value; }
    }

    public static class Logical extends Expr {
        public final Expr left;
        public final Token op;
        public final Expr right;
        public Logical(Expr left, Token op, Expr right) {
            this.left = left; this.op = op; this.right = right;
        }
    }

    public static class Call extends Expr {
        public final Expr callee;
        public final Token paren;
        public final List<Expr> arguments;
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee; this.paren = paren; this.arguments = arguments;
        }
    }

    public static class Grouping extends Expr {
        public final Expr expression;
        public Grouping(Expr expression) { this.expression = expression; }
    }

    // --- Statements ---
    public static class ExpressionStmt extends Stmt {
        public final Expr expression;
        public ExpressionStmt(Expr expression) { this.expression = expression; }
    }

    public static class PrintStmt extends Stmt {
        public final Expr expression;
        public PrintStmt(Expr expression) { this.expression = expression; }
    }

    public static class VarDecl extends Stmt {
        public final Token name;
        public final Expr initializer;
        public VarDecl(Token name, Expr initializer) { this.name = name; this.initializer = initializer; }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;
        public Block(List<Stmt> statements) { this.statements = statements; }
    }

    public static class IfStmt extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;
        public IfStmt(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition; this.thenBranch = thenBranch; this.elseBranch = elseBranch;
        }
    }

    public static class WhileStmt extends Stmt {
        public final Expr condition;
        public final Stmt body;
        public WhileStmt(Expr condition, Stmt body) { this.condition = condition; this.body = body; }
    }

    public static class FunctionDecl extends Stmt {
        public final Token name;
        public final List<Token> parameters;
        public final Block body;
        public FunctionDecl(Token name, List<Token> parameters, Block body) {
            this.name = name; this.parameters = parameters; this.body = body;
        }
    }

    public static class ReturnStmt extends Stmt {
        public final Token keyword;
        public final Expr value;
        public ReturnStmt(Token keyword, Expr value) { this.keyword = keyword; this.value = value; }
    }
}
