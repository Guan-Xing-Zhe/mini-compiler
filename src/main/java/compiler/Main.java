package compiler;

import compiler.lexer.Lexer;
import compiler.lexer.Token;
import compiler.parser.Parser;
import compiler.parser.ASTNode.Stmt;
import compiler.interpreter.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import compiler.ai.AIErrorAssistant;
import java.util.Scanner;

public class Main {
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            runFile(args[0]);
        } else {
            runREPL();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes));
        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runREPL() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("MiniLang REPL (type 'exit' to quit)");
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            if (line == null || line.equals("exit")) break;
            run(line);
            hadError = false;
        }
    }

        private static void run(String source) {
        AIErrorAssistant aiAssistant = new AIErrorAssistant();
        Lexer lexer = new Lexer(source);
        List<Token> tokens = lexer.scanTokens();
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();
        if (hadError) return;
        interpreter.interpret(statements);
    }

        if (hadError) {
            String aiHelp = aiAssistant.explainError("Parse error in source", source, 1);
            System.out.println(aiHelp);
        }

    static boolean hadError = false;
    static boolean hadRuntimeError = false;
}
