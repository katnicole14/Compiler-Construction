package translation;

import java.io.File;

import symbol_table.SemanticAnalyzer;
import symbol_table.TypeChecker;
//import symbol_table.TypeChecker;
import parser.Node;
import parser.Parser;
public class Main {
    public static void main(String[] args) {
        try {
            Parser.parseInit();

            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            File xmlFile = new File("parser/syntax_tree.xml"); // Input XML file
            analyzer.analyze(xmlFile);
            
            // Step 4: Perform type checking
            TypeChecker typeChecker = new TypeChecker(xmlFile,analyzer.getVtable() ,analyzer.getFtable());
            typeChecker.typeCheckers(xmlFile);
            analyzer.printSymbolTables();


            // Step 5: Generate code
            Translator translator = new Translator(analyzer.getVtable(), analyzer.getFtable());
            Node syntaxTree = Parser.getTree();
            if (syntaxTree == null) {
                System.err.println("Error: Syntax tree is null.");
                return;
            }
            translator.translate(syntaxTree, "translation/intermediateCode.txt");
            
            System.out.println("Intermediate code generated successfully. View intermediateCode.txt for the output.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
