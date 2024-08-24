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
            "\\b(if|else|while)\\b",       // keyword
            "\\b[a-zA-Z_]\\w*\\b",         // identifier
            "\\b\\d+(\\.\\d+)?\\b",        // number
            "[+\\-*/]",                    // operator
            "\\s+",                        // whitespace
            ".+"                           // unknown (catch-all)
        };
        String[] tokenClasses = {
            "keyword", "identifier", "number", "operator", "whitespace", "unknown"
        };

        int tokenId = 1;
        while (currentPos < inputCode.length()) {
            boolean matchFound = false;
            for (int i = 0; i < patterns.length; i++) {
                Pattern pattern = Pattern.compile(patterns[i]);
                Matcher matcher = pattern.matcher(inputCode.substring(currentPos));
                if (matcher.lookingAt()) {
                    String tokenValue = matcher.group(0);
                    if (!tokenClasses[i].equals("whitespace")) {
                        tokens.add(new Token(tokenId++, tokenClasses[i], tokenValue));
                    }
                    currentPos += tokenValue.length();
                    matchFound = true;
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