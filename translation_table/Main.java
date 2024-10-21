package translation_table;

import java.io.File;

import symbol_table.SemanticAnalyzer;
import symbol_table.TypeChecker;

public class Main {
    public static void main(String[] args) {
        //translate from syntax tree to non-executable intermediate code
            //need to read in the syntax_tree.xml file
            //send it to the translater for further processing
            //need to output the intermediate code to the console
        File xmlFile = new File("syntax_tree.xml"); // Input XML file
        Translator translator = new Translator();
        String intermediateCode = translator.translate(xmlFile);

        // Output the result
        // System.out.println(intermediateCode);
    }
}


// public static void main(String[] args) {
//     Translator translator = new Translator();
//     translator.loadSymbolTable("path/to/symbol_table.txt");
//     translator.translate(new File("syntax_tree.xml"), "ntermediate_code.txt");
// }
