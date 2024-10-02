package parser;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            // Step 1: Parse the XML file to get tokens
            // Lexer.lexer1();

            // Step 2: Parse the tokens to generate a syntax tree
            Parser.parseInit();
            // Step 3: Analyze the syntax tree for semantic correctness

            // If no exceptions were thrown, the compilation is successful
            System.out.println("Compilation successful!");

        } catch (Exception e) {
            // Handle any exceptions that occur during parsing or semantic analysis
            System.err.println("Compilation failed: " + e.getMessage());
        }
    }
}

