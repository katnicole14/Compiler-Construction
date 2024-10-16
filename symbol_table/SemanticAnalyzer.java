package symbol_table;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class SemanticAnalyzer {
    // HashMap to represent the symbol table
    private Map<String, Symbol> symbolTable = new HashMap<>();
    // Stack to manage scopes
    private Stack<String> scopeStack = new Stack<>();

    // List to store semantic errors
    private List<String> errors = new ArrayList<>();

    // Method to analyze the syntax tree
    public void analyze(File xmlFile) {
        try {
            // Parse the XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Start analyzing from the <ROOT> node
            Element root = (Element) doc.getElementsByTagName("ROOT").item(0);
            traverseNode(root);

            // After traversal, print the symbol table
            printSymbolTable();
            printErrors();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive function to traverse the tree and enforce semantic rules
    private void traverseNode(Node node) {
        String nodeName = node.getNodeName().trim();
        String nodeText = node.getTextContent().trim();
        
       
     

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

    // Handle ROOT node
    private void handleRootNode(Node node) {
        // Push the root scope (main program scope) onto the stack
        scopeStack.push("MAIN");
        String startSymbol = getTextContent(node, "SYMB");

        // Get the CHILDREN IDs from the ROOT node
        NodeList childIDs = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        
        // Get the INNERNODES element (this will hold all <IN> elements)
        NodeList innerNodes = ((Element) node.getOwnerDocument().getElementsByTagName("INNERNODES").item(0)).getElementsByTagName("IN");
        NodeList leafNodes = ((Element) node.getOwnerDocument().getElementsByTagName("LEAFNODES").item(0)).getElementsByTagName("LEAF");
        
        // Loop through all CHILDREN IDs
        for (int i = 0; i < childIDs.getLength(); i++) {
            if (childIDs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String childID = childIDs.item(i).getTextContent().trim();  // the children of the root
            
                // Find the corresponding <IN> node with the matching <UNID> as the childID
                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Element innerNode = (Element) innerNodes.item(j);
                    String unid = getTextContent(innerNode, "UNID");
                    
                   
                    if (unid.equals(childID)) {
                       
                        // Found in INNERNODES, process as an inner node
                        traverseNode(innerNode); // Recurse through this inner node
                        
                    }
                }
                
                // If not found in INNERNODES, check in LEAFNODES
                for (int j = 0; j < leafNodes.getLength(); j++) {
                    Element leafNode = (Element) leafNodes.item(j);
                    String unid = getTextContent(leafNode, "UNID");
                
                    if (unid.equals(childID)) {
                        // Found in LEAFNODES, process as a leaf node
                        traverseNode(leafNode); // Pass the leaf node for leaf-specific processing
                         // Exit the loop after finding and processing the node
                    }
                }
                
            }
        }
    
        // Pop the root scope after traversal
        scopeStack.pop();
    }

   
    // Handle IN node (function or inner scope)
    private void handleInNode(Node node) {
       

    String nodeName = node.getNodeName();
    
    String symbol= getTextContent(node, "SYMB");
      System.out.println(symbol);


      String parent = getTextContent(node, "PARENT");
      String unid = getTextContent(node, "UNID");
      String nonTerminal = getTextContent(node, "SYMB");
  

        // Check if the non-terminal represents a function
        if (isFunction(nonTerminal)) {
            // Function scope handling
            if (scopeStack.contains(nonTerminal)) {
                throwError("Function name conflict in scope", unid);
            }
            scopeStack.push(nonTerminal);
            symbolTable.put(unid, new Symbol(nonTerminal, "function", scopeStack.peek()));
        }
    
        // Handle child nodes under this IN node
        // Get the CHILDREN IDs from the ROOT node
        NodeList childIDs = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        
        // Get the INNERNODES element (this will hold all <IN> elements)
        NodeList innerNodes = ((Element) node.getOwnerDocument().getElementsByTagName("INNERNODES").item(0)).getElementsByTagName("IN");
        NodeList leafNodes = ((Element) node.getOwnerDocument().getElementsByTagName("LEAFNODES").item(0)).getElementsByTagName("LEAF");
        
        // Loop through all CHILDREN IDs
        for (int i = 0; i < childIDs.getLength(); i++) {
            if (childIDs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String childID = childIDs.item(i).getTextContent().trim();  // the children of the root
                
                // Find the corresponding <IN> node with the matching <UNID> as the childID
                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Element innerNode = (Element) innerNodes.item(j);
                    String id = getTextContent(innerNode, "UNID");
                    
    
                    if (id.equals(childID)) {
                        // Found in INNERNODES, process as an inner node
                        traverseNode(innerNode); // Recurse through this inner node
                        // Exit the loop after finding and processing the node
                    }
                }
                
                // If not found in INNERNODES, check in LEAFNODES
                for (int j = 0; j < leafNodes.getLength(); j++) {
                    Element leafNode = (Element) leafNodes.item(j);
                    String id = getTextContent(leafNode, "UNID");
                
                    if (id.equals(childID)) {
                        // Found in LEAFNODES, process as a leaf node
                        traverseNode(leafNode); // Pass the leaf node for leaf-specific processing
                        // Exit the loop after finding and processing the node
                    }
                }
                
            }
        }
    
        // After processing the IN node, pop the function scope if applicable
        if (isFunction(nonTerminal)) {
            scopeStack.pop();
        }
    }

    // Handle LEAFNODES (variables or terminal symbols)
    private void handleLeafNodes(Node node) {
       
        String terminal= getTextContent(node, "TERMINAL");

    

       String parent = getTextContent(node, "PARENT");
       String unid = getTextContent(node, "UNID");
   
   
       System.out.println(terminal);
       System.out.println(terminal + " "+ "parent :" + parent);

       // Handle terminal nodes (tokens from the lexer)
       if (isToken(terminal)) {
           symbolTable.put(unid, new Symbol(terminal, "token", parent)); // Store token in the symbol table
       }
    
        // Iterate through each LEAF element
    
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
        return terminal.matches("\\b(main|begin|end|skip|halt|print|input|num|if|then|void|else|not|sqrt|or|and|eq|grt|add|sub|mul|div)\\b");
    }

    // Helper method to check if a string is a valid token
    private boolean isToken(String terminal) {
        return isKeyword(terminal) || isVariable(terminal);
    }

    // Method to throw a semantic error
    private void throwError(String message, String unid) {
        errors.add("Error at node UNID " + unid + ": " + message);
    }

    // Method to print the symbol table
    public void printSymbolTable() {
        System.out.println("Symbol Table:");
        
        // Print the header
        System.out.printf("%-20s %-20s %-15s %-10s%n", "ID", "Symbol", "Type", "Scope");
        System.out.println("--------------------------------------------------------------");
        
        // Check if the symbol table is empty
        if (symbolTable.isEmpty()) {
            System.out.println("No symbols found.");
        } else {
            // Print each symbol in the table
            for (Map.Entry<String, Symbol> entry : symbolTable.entrySet()) {
                Symbol symbol = entry.getValue();
                System.out.printf("%-20s %-20s %-15s %-10s%n", entry.getKey(), symbol.name, symbol.type, symbol.scope);
            }
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
}
