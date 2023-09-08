package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Something akin to `using namespace std;` in C/C++
import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
    private final String source;    // Input/source string
    private final List<Token> tokens = new ArrayList<>();   // Parsed string
    private int start = 0;
    private int current = 0;
    private int line = 1;

    // Pass a line to Scanner and it assigns to its memeber `source` 
    // the passed string.
    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // RN: We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        // Reached the end, so append an `EOF` and move on.
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                      addToken(match('=') ? BANG_EQUAL : BANG); break;
            case '=':
                      addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
            case '<':
                      addToken(match('=') ? LESS_EQUAL : LESS); break;
            case '>':
                      addToken(match('=') ? GREATER_EQUAL : GREATER); break;
            // `/` needs special handling since comments in Lox are
            // also handled by the `//` token, which starts with `/`
            case '/':
                      if (match('/')) {
                          // RN: A comment goes until the end of the line
                          while (peek() != '\n' && !isAtEnd()) advance();
                          // Here's how inline-comments are made, basically
                      } else {
                          addToken(SLASH);
                      }
                      break;
            case ' ':
            case '\r':  // Since this is ignored, doesn't this partially
                        // contribute to running on Windows?
            case '\t':
                      // ignore whitespace
                      break;
            case '\n':
                      line++;
                      break;

            // Time for literals
            case '"': string(); break;

            default:
                // See Section 4.6.2 on why numbers can't start or end with
                // a "bare" . - i.e., no 1234. or .1234
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    // RN: this reports ***each*** unaccounted-for character, such 
                    // as "@" or "#" or "^", etc. for ***each*** occurance.
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }   // End of scanToken

    private boolean isAlpha(char c) {
        return  (c >= 'a' && c <= 'z')  ||
                (c >= 'A' && c <= 'Z')  ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        // RN: If it's a keyword, use the IDENTIFIER type, otherwise it's a
        // regular user-defined identifier.
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        // Consume all the digits before a possible '.'
        while (isDigit(peek())) advance();

        // RN: Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
            // RN: Consume the "."
            // Consumes all the digits after the '.'
            advance();

            while (isDigit(peek())) advance();
        }

        // Using Java's own parsing method for reading a double.
        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            // Support for multi-line strings.
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated string.");
            return;
        }

        advance();  // RN: The closing ".

        // RN: Trim the surroundign quotes.
        // As in, the actual string part of the string are the characters
        // between the quotation marks, excluding those.
        String value = source.substring(start + 1, current - 1);
        // RN: If Lox supported escape sequences like `\n`, we'd unescape those
        // here.

        addToken(STRING, value);
    }

    // Looks to check if the current character is matches some expected one
    // RN: "IT's like a conditional `advance()`. We only consume the current
    // character if its what we're looking for.
    // Note: the "advancing" required already happened, meaning "current" here
    // is already the new ~~map~~ character.
    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    // It only looks one character ahead. More could be added for longer lexemes
    // at the cost of slower runtime for `scanner`.
    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    // Relies on Java's comparisons
    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    // RN: Consumes the next character in the source file and returns it. Input.
    private char advance() {
        return source.charAt(current++);
    }

    // RN: Grabs the text of the current lexeme and creates a new token for it.
    // `advance`, but for output.
    private void addToken(TokenType type) {
        addToken(type, null);
    }

    // RN: Used for tokens with literal values.
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and",     AND);
        keywords.put("class",   CLASS);
        keywords.put("else",    ELSE);
        keywords.put("false",   FALSE);
        keywords.put("for",     FOR);
        keywords.put("fun",     FUN);
        keywords.put("if",      IF);
        keywords.put("nil",     NIL);
        keywords.put("or",      OR);
        keywords.put("print",   PRINT); // Building it right into the language
        keywords.put("return",  RETURN);
        keywords.put("super",   SUPER);
        keywords.put("this",    THIS);
        keywords.put("true",    TRUE);
        keywords.put("var",     VAR);
        keywords.put("while",   WHILE);
    }
}
