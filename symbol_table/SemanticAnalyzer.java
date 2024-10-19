package symbol_table;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.util.*;


public class SemanticAnalyzer {
    // HashMap to represent the symbol table
    private Map<String, Symbol> vtable = new HashMap<>();
    private Map<String, Symbol> ftable = new HashMap<>();

    // Stack to manage scopes
    private Stack<String> scopeStack = new Stack<>();
    // List to store semantic errors
    private List<String> errors = new ArrayList<>();
    // Counter for generating unique names
    private int uniqueCounterVariable = 0;
    private int uniqueCounterFunction = 0;
    private static File xmlfile ;

    // Method to analyze the syntax tree
    public void analyze(File xmlFile) {
        try {
            this.xmlfile = xmlFile;
            // Parse the XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Start analyzing from the <ROOT> node
            Element root = (Element) doc.getElementsByTagName("ROOT").item(0);
            traverseNode(root);

            // After traversal, print the symbol table
            printSymbolTables();
            printErrors();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive function to traverse the tree and enforce semantic rules
    private void traverseNode(Node node) {
        String nodeName = node.getNodeName().trim();

        switch (nodeName) {
            case "ROOT":
                handleRootNode(node);
                break;

            case "IN":
                handleInNode(node);
                break;

            case "LEAF":
                handleLeafNodes(node);
                break;
        }
    }

public Map<String, Symbol> getVtable(){
    return vtable;
}
public Map<String, Symbol> getFtable(){
    return ftable;
}
    // Handle ROOT node
    private void handleRootNode(Node node) {
        // Push the root scope (main program scope) onto the stack
        scopeStack.push("GLOBAL");
        processChildren(node);
        // Pop the root scope after traversal
        scopeStack.pop();
    }

    // Handle IN node (function or inner scope)
    private void handleInNode(Node node) {
        // String unid = getTextContent(node, "UNID");
        // String nonTerminal = getTextContent(node, "SYMB");
            processChildren(node);
        
    }

    // Handle LEAF nodes (variables or terminal symbols)
    private void handleLeafNodes(Node node) {
        String terminal = getTextContent(node, "TERMINAL");
        //String parent = getTextContent(node, "PARENT");
        String unid = getTextContent(node, "UNID");
    
        // Handle terminal nodes (tokens from the lexer)

        String parent =null;
        String grandparent = null;
        String grandSymb  = null;
        List<String> childList = new ArrayList<>();  
        List<String> children = new ArrayList<>();   
        List<String> temp  = null;
        String types = "";
        if (isFunction(terminal)) {
            // Function scope handling
            try {
                parent = getParent(unid);
                grandparent = getParent(parent);
                grandSymb = getSymbolByUNID(grandparent);
                childList = getChildren(grandparent);
                 
                for (String child : childList) {
                    // Check your condition (e.g., if the child contains "special")
                    if (getSymbolByUNID(child).equals("VNAME")) {
                        // Add to childlist if condition is met
                        temp = getChildren(child);
                        children.add(getSymbolByUNID(temp.get(0)));
                    }

                    if (getSymbolByUNID(child).equals("FTYP")) {
                        // Add to childlist if condition is met
                        temp = getChildren(child);
                        types = getSymbolByUNID(temp.get(0));
                    }
                }

               
            } catch (Exception e) {
                e.printStackTrace(); // Handle any exceptions (can log or rethrow if necessary)
            }
        
         
            boolean inTable = findSymbolByName(terminal); // Check if the function is already in the symbol table
            boolean declaration = "HEADER".equals(grandSymb); // Check if the grandparent is 'HEADER'

           
        
            if (inTable && declaration) {
                throwError("Function name conflict in scope", unid); // Conflict: function already declared in this scope
            } else if (!inTable && declaration) {
                // Only add to the table if it's not already in the table and grandparent is 'HEADER'
                String uniqueName = generateUniqueName(terminal);
                String scope = scopeStack.peek();
                scopeStack.push(uniqueName); // Push the new unique function name onto the scope stack
                ftable.put(unid, new Symbol(uniqueName, types, scope, children,terminal)); // Add the function to the symbol table
            } else if (terminal.equals("main")) {
            ftable.put(unid, new Symbol(terminal, "main", scopeStack.peek(),children,terminal )); // Store 'main' in the symbol table
        } 
    }
    else if (isToken(terminal) && !isKeyword(terminal)) {
        try {
            // Get parent and grandparent nodes
            parent = getParent(unid);
            grandparent = getParent(parent);
            grandSymb = getSymbolByUNID(grandparent);
            childList = getChildren(grandparent);

            boolean local = grandSymb.equals("LOCVARS");
            boolean global = grandSymb.equals("GLOBVARS");
            
            boolean parameter = grandSymb.equals("HEADER");
            boolean sameScope = findSymbolByNameInScope(terminal, scopeStack.peek());

                 for (String child : childList) {
                // Check your condition (e.g., if the child contains "special")
             
            if (parameter) {
                temp = getChildren(child);
               
                
            }
             
            }

            if (sameScope && (local || global)) {
                // If sameScope is true and grandparent is either "LOCVARS" or "GLOBVARS", raise an error
                throwError("Variable name conflict in scope", unid); // Raise a variable name conflict
            } else {
                // If no conflict, add the new variable to the symbol table
                if (local || global || parameter) {
                String uniqueName = generateUniqueName(terminal);
             
                vtable.put(unid, new Symbol(uniqueName, "", scopeStack.peek(), terminal)); // Store variable in the symbol table
            }
        }
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace(); // Log or handle exceptions as necessary
        }





        

   
    }
     
    }
    
    
    // Helper method to process child nodes
    private void processChildren(Node node) {
        NodeList childIDs = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        NodeList innerNodes = ((Element) node.getOwnerDocument().getElementsByTagName("INNERNODES").item(0)).getElementsByTagName("IN");
        NodeList leafNodes = ((Element) node.getOwnerDocument().getElementsByTagName("LEAFNODES").item(0)).getElementsByTagName("LEAF");

        for (int i = 0; i < childIDs.getLength(); i++) {
            if (childIDs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String childID = childIDs.item(i).getTextContent().trim();

                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Element innerNode = (Element) innerNodes.item(j);
                    String id = getTextContent(innerNode, "UNID");
                    if (id.equals(childID)) {
                        traverseNode(innerNode);
                        break;
                    }
                }

                for (int j = 0; j < leafNodes.getLength(); j++) {
                    Element leafNode = (Element) leafNodes.item(j);
                    String id = getTextContent(leafNode, "UNID");
                    if (id.equals(childID)) {
                        traverseNode(leafNode);
                        break;
                    }
                }
            }
        }
    }

    

    // Helper method to generate unique names
    private String generateUniqueName(String baseName) {
       
        if (isFunction(baseName)){
            return "f" + (uniqueCounterFunction++);
        }
        else return "v" + (uniqueCounterVariable++);

      
    }

    // Helper method to get text content from an element by tag name
    private String getTextContent(Node node, String tagName) {
        return ((Element) node).getElementsByTagName(tagName).item(0).getTextContent();
    }

    // Helper method to check if a node represents a function
    private boolean isFunction(String symbol) {
        return symbol.matches("\\bF_[a-z][a-z0-9]*\\b");
    }

    // Helper method to check if a terminal represents a variable
    private boolean isVariable(String terminal) {
        return !isKeyword(terminal) && terminal.matches("\\bV_[a-z][a-z0-9]*\\b");
    }

    // Helper method to check if terminal is a keyword
    private boolean isKeyword(String terminal) {
        return terminal.matches(
                "\\b(begin|end|skip|halt|print|input|num|if|then|void|else|not|sqrt|or|and|eq|grt|add|sub|mul|div)\\b");
    }

    // Helper method to check if a string is a valid token
    private boolean isToken(String terminal) {
        return isKeyword(terminal) || isVariable(terminal);
    }

    // Helper method to throw an error
    private void throwError(String message, String unid) {
        errors.add("Error at " + unid + ": " + message);
    }

    // Method to print the symbol table in a table format
    private void printSymbolTables() {
        printVTable();
        System.out.println();
        printFTable();
    }

    // Method to print vtable (Variable Table)
private void printVTable() {
    System.out.println("Variable Table:");
    // Print the table headers for variables
    System.out.printf("%-10s %-20s %-15s %-10s %-15s%n", "ID", "Symbol", "Type", "Scope", "Unique Name");
    System.out.println("--------------------------------------------------------------------------");

    // Loop through the vtable and print each entry
    for (Map.Entry<String, Symbol> entry : vtable.entrySet()) {
        Symbol symbol = entry.getValue();
        System.out.printf("%-10s %-20s %-15s %-10s %-15s%n",
                entry.getKey(),         // ID
                symbol.getSymb(),       // Symbol
                symbol.getType(),       // Type
                symbol.getScope(),      // Scope
                symbol.getName());      // Unique Name
    }
}

// Method to print ftable (Function Table)
private void printFTable() {
    System.out.println("Function Table:");
    // Print the table headers for functions
    System.out.printf("%-10s %-20s %-15s %-10s %-15s %-15s %-15s%n", 
                      "ID", "Symbol", "Type", "Scope", "Unique Name", "Return Type", "Parameters");
    System.out.println("-------------------------------------------------------------------------------------");

    // Loop through the ftable and print each entry
    for (Map.Entry<String, Symbol> entry : ftable.entrySet()) {
        Symbol symbol = entry.getValue();
        
        // Get the return type and parameters
       // String returnType = symbol.getReturnType();  // Assuming getReturnType exists
        List<String> parameters = symbol.getParameters(); // Assuming getParameters returns a List<String>
        String returnType ="";
        // Join parameters into a single string separated by commas
        String parameterString = String.join(", ", parameters);

        // Print the function details including parameters as a single string
        System.out.printf("%-10s %-20s %-15s %-10s %-15s %-15s %-15s%n",
                entry.getKey(),         // ID
                symbol.getSymb(),       // Symbol
                symbol.getType(),       // Type
                symbol.getScope(),      // Scope
                symbol.getName(),       // Unique Name
                returnType,             // Return Type
                parameterString);       // Function Parameters (joined as a single string)
    }
}


    // Method to print errors
    private void printErrors() {
        if (errors.isEmpty()) {
            System.out.println("No semantic errors found.");
        } else {
            System.out.println("Semantic Errors:");
            for (String error : errors) {
                System.out.println(error);
            }
        }
    }

// Method to find a symbol by name in either vtable or ftable
public boolean findSymbolByName(String name) {
    // Search in vtable (variable table)
    if (findSymbolInTable(vtable, name)) {
        return true;
    }

    // Search in ftable (function table)
    return findSymbolInTable(ftable, name);
}

// Helper method to search a symbol by name in a specific table
private boolean findSymbolInTable(Map<String, Symbol> table, String name) {
    for (Map.Entry<String, Symbol> entry : table.entrySet()) {
        Symbol symbol = entry.getValue();
        if (symbol.getSymb().equals(name)) {
            return true; // Return true if the symbol matches
        }
    }
    return false; // Return false if no symbol with the name is found
}

// Method to find a symbol by name in a specific scope (for both vtable and ftable)
private boolean findSymbolByNameInScope(String name, String currentScope) {
    // Search in vtable (variable table)
    if (findSymbolInScopeInTable(vtable, name, currentScope)) {
        return true;
    }

    // Search in ftable (function table)
    return findSymbolInScopeInTable(ftable, name, currentScope);
}

// Helper method to search a symbol by name in a specific scope within a table
private boolean findSymbolInScopeInTable(Map<String, Symbol> table, String name, String currentScope) {
    for (Map.Entry<String, Symbol> entry : table.entrySet()) {
        Symbol symbol = entry.getValue();
        if (symbol.getSymb().equals(name) && symbol.getScope().equals(currentScope)) {
            return true; // Name conflict found in the same scope
        }
    }
    return false; // No conflict in the current scope
}

    private String getSymbolByUNID(String UNID) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlfile);

        // Check in Inner Nodes (IN)
        NodeList innerNodes = doc.getElementsByTagName("IN");
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Element node = (Element) innerNodes.item(i);
            String nodeUNID = node.getElementsByTagName("UNID").item(0).getTextContent();
            if (nodeUNID.equals(UNID)) {
                return node.getElementsByTagName("SYMB").item(0).getTextContent();
            }
        }

        // Check in Leaf Nodes (LEAF)
        NodeList leafNodes = doc.getElementsByTagName("LEAF");
        for (int i = 0; i < leafNodes.getLength(); i++) {
            Element node = (Element) leafNodes.item(i);
            String nodeUNID = node.getElementsByTagName("UNID").item(0).getTextContent();
            if (nodeUNID.equals(UNID)) {
                return node.getElementsByTagName("TERMINAL").item(0).getTextContent();
            }
        }

        return null; // Return null if no match is found
    }

      // Function to get the parent UNID for a given node UNID
      public static String getParent(String UNID) throws Exception {
        Document doc = parseXML(xmlfile);

        // Check in Inner Nodes
        NodeList innerNodes = doc.getElementsByTagName("IN");
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Element node = (Element) innerNodes.item(i);
            String nodeUNID = node.getElementsByTagName("UNID").item(0).getTextContent();
            if (nodeUNID.equals(UNID)) {
                return node.getElementsByTagName("PARENT").item(0).getTextContent();
            }
        }

        // Check in Leaf Nodes
        NodeList leafNodes = doc.getElementsByTagName("LEAF");
        for (int i = 0; i < leafNodes.getLength(); i++) {
            Element node = (Element) leafNodes.item(i);
            String nodeUNID = node.getElementsByTagName("UNID").item(0).getTextContent();
            if (nodeUNID.equals(UNID)) {
                return node.getElementsByTagName("PARENT").item(0).getTextContent();
            }
        }

        return null; // Return null if no parent is found
    }

    // Function to get the children UNIDs for a given node UNID
    public static List<String> getChildren(String UNID) throws Exception {
        Document doc = parseXML(xmlfile);
        List<String> children = new ArrayList<>();

        // Check in Root
        Node rootNode = doc.getElementsByTagName("ROOT").item(0);
        NodeList childrenNodes = ((Element) rootNode).getElementsByTagName("CHILDREN");
        for (int i = 0; i < childrenNodes.getLength(); i++) {
            Element childElement = (Element) childrenNodes.item(i);
            NodeList childIDs = childElement.getElementsByTagName("ID");
            for (int j = 0; j < childIDs.getLength(); j++) {
                String childUNID = childIDs.item(j).getTextContent();
                if (childUNID.equals(UNID)) {
                    for (int k = 0; k < childIDs.getLength(); k++) {
                        children.add(childIDs.item(k).getTextContent());
                    }
                }
            }
        }

        // Check in Inner Nodes
        NodeList innerNodes = doc.getElementsByTagName("IN");
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Element node = (Element) innerNodes.item(i);
            String nodeUNID = node.getElementsByTagName("UNID").item(0).getTextContent();
            if (nodeUNID.equals(UNID)) {
                NodeList childElements = node.getElementsByTagName("CHILDREN");
                for (int j = 0; j < childElements.getLength(); j++) {
                    Element childElement = (Element) childElements.item(j);
                    NodeList childIDs = childElement.getElementsByTagName("ID");
                    for (int k = 0; k < childIDs.getLength(); k++) {
                        children.add(childIDs.item(k).getTextContent());
                    }
                }
            }
        }

        // Check in Leaf Nodes
        NodeList leafNodes = doc.getElementsByTagName("LEAF");
        for (int i = 0; i < leafNodes.getLength(); i++) {
            Element node = (Element) leafNodes.item(i);
            String nodeUNID = node.getElementsByTagName("UNID").item(0).getTextContent();
            if (nodeUNID.equals(UNID)) {
                NodeList childElements = node.getElementsByTagName("CHILDREN");
                for (int j = 0; j < childElements.getLength(); j++) {
                    Element childElement = (Element) childElements.item(j);
                    NodeList childIDs = childElement.getElementsByTagName("ID");
                    for (int k = 0; k < childIDs.getLength(); k++) {
                        children.add(childIDs.item(k).getTextContent());
                    }
                }
            }
        }

        return children; // Return list of children UNIDs
    }

    private static Document parseXML(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }

    // public static void main(String[] args) {
    //     SemanticAnalyzer analyzer = new SemanticAnalyzer();
    //     File xmlFile = new File("syntax_tree.xml"); // Replace with your XML file path
    //     analyzer.analyze(xmlFile);
    // }
}
