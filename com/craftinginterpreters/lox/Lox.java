package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // The commandline invocation on a file
    // Called a "thin wrapper" around the `run` function below
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // This looks like the start of a lambda, in concept - just stuff all the
        // things in there and let'er rip!
    
        // Indicates an error in the exit code.
        if (hadError) System.exit(65);
    }
    
    // The REPL.
    // Called a "thin wrapper" around the `run` function (below)
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
    
        for (;;) {
            System.out.print("8==D ");
            String line = reader.readLine();
            if (line == null) {
                System.out.println("");
                break;
            }
            run(line);
            hadError = false;   // If this flag wasn't here, the REPL would die 
                                // at the first error. Suicide Linux, anyone?
        }
    }
    
    // Does the actual running
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
    
        // For now, just print the tokens
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
    
    // The author makes the point that "it's good engineering practice to separate 
    // the code that *generates* the errors (read: data) from the code that 
    // *reports* them (read: plots/transforms that data)", parentheses mine.
    
    // When there's an error, whip this out
    static void error(int line, String message) {
        report(line, "", message);
    }
    
    // The actual function that reports the error.
    // This is the "helper", even though `error` above looks like a wrapper for this
    private static void report(int line, String where, String message) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}

