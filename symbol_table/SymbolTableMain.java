package symbol_table;

import java.io.File;

public class SymbolTableMain {
    public static void main(String[] args) {
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        File xmlFile = new File("syntax_tree.xml"); // Input XML file
        analyzer.analyze(xmlFile);
        // analyzer.printSymbolTable();
        // TypeChecker typeChecker = new TypeChecker(xmlFile,analyzer.getVtable() ,analyzer.getFtable());
        // typeChecker.typeCheckers(xmlFile);
        //  analyzer.printSymbolTables();
       
    }
}
