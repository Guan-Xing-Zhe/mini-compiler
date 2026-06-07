package compiler.interpreter;

import compiler.lexer.Token;
import compiler.lexer.TokenType;
import compiler.parser.ASTNode.*;
import java.util.*;

public class Interpreter {
    private static class ReturnSignal extends RuntimeException {
        final Object value;
        ReturnSignal(Object value) { this.value = value; }
    }

    private static class RuntimeErr extends RuntimeException {
        RuntimeErr(Token token, String msg) {
            super("Runtime error at line " + token.line + ": " + msg);
        }
    }

    public interface Callable {
        int arity();
        Object call(Interpreter interpreter, List<Object> arguments);
    }

    static class MiniLangFunction implements Callable {
        final FunctionDecl declaration;
        final Environment closure;
        MiniLangFunction(FunctionDecl declaration, Environment closure) {
            this.declaration = declaration;
            this.closure = closure;
        }
        @Override
        public int arity() { return declaration.parameters.size(); }
        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Environment env = new Environment(closure);
            for (int i = 0; i < declaration.parameters.size(); i++) {
                env.define(declaration.parameters.get(i).lexeme, arguments.get(i));
            }
            try {
                interpreter.executeBlock(declaration.body.statements, env);
            } catch (ReturnSignal r) {
                return r.value;
            }
            return null;
        }
        @Override
        public String toString() { return "<fn " + declaration.name.lexeme + ">"; }
    }

    private Environment environment = new Environment();

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } catch (RuntimeErr e) {
            System.err.println(e.getMessage());
        }
    }

    private void execute(Stmt stmt) {
        if (stmt instanceof ExpressionStmt) {
            evaluate(((ExpressionStmt) stmt).expression);
        } else if (stmt instanceof PrintStmt) {
            PrintStmt p = (PrintStmt) stmt;
            Object val = evaluate(p.expression);
            System.out.println(stringify(val));
        } else if (stmt instanceof VarDecl) {
            VarDecl v = (VarDecl) stmt;
            Object value = null;
            if (v.initializer != null) value = evaluate(v.initializer);
            environment.define(v.name.lexeme, value);
        } else if (stmt instanceof Block) {
            Block b = (Block) stmt;
            executeBlock(b.statements, new Environment(environment));
        } else if (stmt instanceof IfStmt) {
            IfStmt i = (IfStmt) stmt;
            if (isTruthy(evaluate(i.condition))) {
                execute(i.thenBranch);
            } else if (i.elseBranch != null) {
                execute(i.elseBranch);
            }
        } else if (stmt instanceof WhileStmt) {
            WhileStmt w = (WhileStmt) stmt;
            while (isTruthy(evaluate(w.condition))) {
                execute(w.body);
            }
        } else if (stmt instanceof FunctionDecl) {
            FunctionDecl f = (FunctionDecl) stmt;
            MiniLangFunction fn = new MiniLangFunction(f, environment);
            environment.define(f.name.lexeme, fn);
        } else if (stmt instanceof ReturnStmt) {
            ReturnStmt r = (ReturnStmt) stmt;
            Object value = null;
            if (r.value != null) value = evaluate(r.value);
            throw new ReturnSignal(value);
        }
    }

    void executeBlock(List<Stmt> statements, Environment env) {
        Environment previous = this.environment;
        try {
            this.environment = env;
            for (Stmt stmt : statements) {
                execute(stmt);
            }
        } finally {
            this.environment = previous;
        }
    }

    private Object evaluate(Expr expr) {
        if (expr instanceof Literal) return ((Literal) expr).value;
        if (expr instanceof Grouping) return evaluate(((Grouping) expr).expression);
        if (expr instanceof Variable) return environment.get(((Variable) expr).name);
        if (expr instanceof Unary) {
            Unary u = (Unary) expr;
            Object right = evaluate(u.right);
            switch (u.op.type) {
                case MINUS: return -(double) right;
                case BANG: return !isTruthy(right);
            }
            return null;
        }
        if (expr instanceof Binary) {
            Binary b = (Binary) expr;
            Object left = evaluate(b.left);
            Object right = evaluate(b.right);
            switch (b.op.type) {
                case PLUS:
                    if (left instanceof Double && right instanceof Double) return (Double) left + (Double) right;
                    if (left instanceof String && right instanceof String) return (String) left + (String) right;
                    throw new RuntimeErr(b.op, "Operands must be two numbers or two strings.");
                case MINUS: return (Double) left - (Double) right;
                case STAR: return (Double) left * (Double) right;
                case SLASH: return (Double) left / (Double) right;
                case PERCENT: return (Double) left % (Double) right;
                case GREATER: return (Double) left > (Double) right;
                case GREATER_EQUAL: return (Double) left >= (Double) right;
                case LESS: return (Double) left < (Double) right;
                case LESS_EQUAL: return (Double) left <= (Double) right;
                case BANG_EQUAL: return !isEqual(left, right);
                case EQUAL_EQUAL: return isEqual(left, right);
            }
            return null;
        }
        if (expr instanceof Logical) {
            Logical l = (Logical) expr;
            Object left = evaluate(l.left);
            if (l.op.type == TokenType.OR) {
                if (isTruthy(left)) return left;
            } else {
                if (!isTruthy(left)) return left;
            }
            return evaluate(l.right);
        }
        if (expr instanceof Assign) {
            Assign a = (Assign) expr;
            Object value = evaluate(a.value);
            environment.assign(a.name, value);
            return value;
        }
        if (expr instanceof Call) {
            Call c = (Call) expr;
            Object callee = evaluate(c.callee);
            List<Object> arguments = new ArrayList<>();
            for (Expr arg : c.arguments) arguments.add(evaluate(arg));
            if (!(callee instanceof Callable)) {
                throw new RuntimeErr(c.paren, "Can only call functions.");
            }
            Callable fn = (Callable) callee;
            if (arguments.size() != fn.arity()) {
                throw new RuntimeErr(c.paren, "Expected " + fn.arity() + " arguments but got " + arguments.size() + ".");
            }
            return fn.call(this, arguments);
        }
        return null;
    }

    private boolean isTruthy(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean) o;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object o) {
        if (o == null) return "nil";
        if (o instanceof Double) {
            double d = (Double) o;
            if (d == Math.floor(d)) return String.valueOf((long) d);
            return String.valueOf(d);
        }
        return o.toString();
    }

    static class Environment {
        final Environment enclosing;
        final Map<String, Object> values = new HashMap<>();

        Environment() { this.enclosing = null; }
        Environment(Environment enclosing) { this.enclosing = enclosing; }

        void define(String name, Object value) { values.put(name, value); }

        Object get(Token name) {
            if (values.containsKey(name.lexeme)) return values.get(name.lexeme);
            if (enclosing != null) return enclosing.get(name);
            throw new RuntimeErr(name, "Undefined variable '" + name.lexeme + "'.");
        }

        void assign(Token name, Object value) {
            if (values.containsKey(name.lexeme)) {
                values.put(name.lexeme, value);
                return;
            }
            if (enclosing != null) {
                enclosing.assign(name, value);
                return;
            }
            throw new RuntimeErr(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }
}
