import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The Lexer class is responsible for tokenizing input code and outputting tokens in XML format.
 */
public class Lexer {
    private String inputCode;
    private List<Token> tokens;
    private int currentPos;

    public Lexer(String inputCode) {
        this.inputCode = inputCode;
        this.tokens = new ArrayList<>();
        this.currentPos = 0;
    }

    /**
     * Tokenizes the input code based on predefined patterns.
     */
    public void tokenize() throws Exception {
        String[] patterns = {
            "\\b(main|begin|end|skip|halt|print|input|num|if|then|void|else|not|sqrt|or|and|eq|grt|add|sub|mul|div)\\b", // Keywords
            "\\bV_[a-z][a-z0-9]*\\b", // Variable names
            "\\bF_[a-z][a-z0-9]*\\b", // Function names
            "-?\\b0(\\.\\d+)?\\b", // Numbers starting with 0 or 0.x
            "-?\\b[1-9]\\d*(\\.\\d+)?\\b", // Positive and negative integers/real numbers
            "\\b[A-Z][a-z]{0,7}\\b", // String literals with 8 or fewer characters
            "[=<>(){};,]", // Symbols treated as keywords
            "\\s+", // Whitespace
            ".+" // Unknown (catch-all)
        };
    
        String[] tokenClasses = {
            "reserved_keyword", "V", "F", "N", "N", "T", "reserved_keyword", "whitespace", "unknown"
        };
    
        int tokenId = 1;
        while (currentPos < inputCode.length()) {
            boolean matchFound = false;
            for (int i = 0; i < patterns.length; i++) {
                Pattern pattern = Pattern.compile(patterns[i]);
                Matcher matcher = pattern.matcher(inputCode.substring(currentPos));
                if (matcher.lookingAt()) {
                    String tokenValue = matcher.group(0);
    
                    // Check if it's a string literal and longer than 8 characters
                    if (i == 5 && tokenValue.length() > 8) {
                        tokens.add(new Token(tokenId++, "unknown", tokenValue));
                    } else if (!tokenClasses[i].equals("whitespace")) {  // Ignore whitespace
                        tokens.add(new Token(tokenId++, tokenClasses[i], tokenValue));
                    }
    
                    currentPos += tokenValue.length();
                    matchFound = true;
    
                    // Break the loop to process the next token
                    break;
                }
            }
    
            if (!matchFound) {
                throw new Exception("Lexical error at position " + currentPos);
            }
        }
    }
    
    

    /**
     * Outputs the token list to an XML file.
     *
     * @param filename the path to the output XML file
     * @throws IOException if an I/O error occurs
     */
    public void writeTokensToXML(String filename) throws IOException {
        FileWriter fileWriter = new FileWriter(new File(filename));
        fileWriter.write("<TOKENSTREAM>\n");
        for (Token token : tokens) {
            fileWriter.write(token.toString() + "\n");
        }
        fileWriter.write("</TOKENSTREAM>");
        fileWriter.close();
    }
}
