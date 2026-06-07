# MiniLang Compiler

A from-scratch compiler for a custom mini programming language (MiniLang), implementing the full compilation pipeline:

- **Lexer** — character-by-character tokenization
- **Parser** — recursive descent AST construction
- **Interpreter** — tree-walking evaluation
- **REPL** — interactive read-eval-print loop

Built to demonstrate compiler theory fundamentals: lexical analysis, syntax analysis, AST representation, and runtime interpretation.

## Language Features

```
// Variables and arithmetic
let x = 10;
let y = x * 2 + 5;

// Conditionals
if (x > 5) {
    print("greater");
} else {
    print("less or equal");
}

// While loops
while (x > 0) {
    x = x - 1;
}

// Functions
function fib(n) {
    if (n <= 1) return n;
    return fib(n - 1) + fib(n - 2);
}

print(fib(10));
```

## Build & Run

```bash
# Compile
javac -d out src/main/java/compiler/**/*.java

# Run REPL
java -cp out compiler.Main

# Run a .ml file
java -cp out compiler.Main examples/fib.ml
```

## Project Structure

```
src/main/java/compiler/
├── Main.java              # Entry point & REPL
├── lexer/
│   ├── Token.java         # Token representation
│   ├── TokenType.java     # Token type enum
│   └── Lexer.java         # Character-by-character lexer
├── parser/
│   ├── ASTNode.java       # AST node definitions
│   └── Parser.java        # Recursive descent parser
└── interpreter/
    └── Interpreter.java   # Tree-walking interpreter
```