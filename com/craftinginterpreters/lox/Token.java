package com.craftinginterpreters.lox;

class Token {
    // `final`: cannot extend/override/etc. this item. In inheiritance, `final`
    // objects cannot be "extended" (`extends`).
    // `final` != immutable, just not-override-able
    final TokenType type;   // The token's type
    final String lexeme;    // The lexeme
    final Object literal;   // The literal given
    final int line;         // The line number; needed anyways

    Token(TokenType type, String lexeme, Object literal, int line) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
    }

    public String toString() {
        return type + " " + lexeme + " " + literal;
    }
}
