import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * The Main class serves as the entry point for the Lexer application.
 * It initializes the Lexer, processes input files, and handles exceptions.
 */
public class Main {

    public static void main(String[] args) {
        //Ensure that the user provides the required arguments
        // if (args.length != 2) {
        //     System.err.println("Usage: java Main <input_file> <output_file>");
        //     System.exit(1);
        // }

        String inputFilePath = "input/main_006.txt";
        String outputFilePath = "lexer/output.xml";

        // Create a File object for the input file
        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);

        try {
            // Read the entire file as a String
            String inputCode = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            
            // Create an instance of Lexer
            Lexer lexer = new Lexer(inputCode);

            // Tokenize the input file and get the list of tokens
            lexer.tokenize();

            // Write the tokens to the output XML file
            lexer.writeTokensToXML(outputFilePath);

            System.out.println("Tokenization complete. Output written to: " + outputFilePath);

        } catch (IOException e) {
            System.err.println("An error occurred while reading or writing files: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
          
            e.printStackTrace();
        }
    }
}
