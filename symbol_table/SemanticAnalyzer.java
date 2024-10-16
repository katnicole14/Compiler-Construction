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
        String nodeName = node.getNodeName();
        String nodeText = node.getTextContent().trim();
        //System.out.println(nodeName);

        switch (nodeName) {
            case "ROOT":
                handleRootNode(node);
                break;

            case "IN":
                handleInnerNodes(node);
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
        
        // Loop through all CHILDREN IDs
        for (int i = 0; i < childIDs.getLength(); i++) {
            if (childIDs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String childID = childIDs.item(i).getTextContent().trim();
    
                // Find the corresponding <IN> node with the matching <UNID> as the childID
                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Element innerNode = (Element) innerNodes.item(j);
                    String unid = getTextContent(innerNode, "UNID");
    
                    if (unid.equals(childID)) {
                        // Process the matching <IN> node (this is the corresponding child node)
                        traverseNode(innerNode); // Recurse through this inner node
                        break;
                    }
                }
            }
        }
    
        // Pop the root scope after traversal
        scopeStack.pop();
    }

    // Handle INNERNODES (function or inner scopes)
    private void handleInnerNodes(Node node) {
   
     String nodeName = node.getNodeName();

    // System.out.println( nodeName);
        // Get all IN elements within the INNERNODES element
        NodeList innerNodes = ((Element) node).getElementsByTagName("IN");
    
        // Iterate through each IN element
        for (int i = 0; i < innerNodes.getLength(); i++) {
            Node innerNode = innerNodes.item(i);
    
            // Ensure it's an element node
            if (innerNode.getNodeType() == Node.ELEMENT_NODE) {
                // Handle the IN node
                handleInNode(innerNode);
            }
        }
    }
    
    // Handle IN node (function or inner scope)
    private void handleInNode(Node node) {
        // Extract parent, UNID, and non-terminal symbol
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
        NodeList children = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
    
            // Only process ID nodes (the child UNID references)
            if (childNode.getNodeType() == Node.ELEMENT_NODE && "ID".equals(childNode.getNodeName())) {
                String childUnid = childNode.getTextContent();
            }
        }
    
        // After processing the IN node, pop the function scope if applicable
        if (isFunction(nonTerminal)) {
            scopeStack.pop();
        }
    }

    // Handle LEAFNODES (variables or terminal symbols)
    private void handleLeafNodes(Node node) {
        System.out.println( "here in the leaf node");
        // Get all LEAF elements within the LEAFNODES element
        NodeList leafNodes = ((Element) node).getElementsByTagName("LEAF");
    
        // Iterate through each LEAF element
        for (int i = 0; i < leafNodes.getLength(); i++) {
            Node leafNode = leafNodes.item(i);
    
            // Ensure it's an element node
            if (leafNode.getNodeType() == Node.ELEMENT_NODE) {
                // Handle the LEAF node
                handleLeafNode(leafNode);
            }
        }
    }
    
    private void handleLeafNode(Node node) {
        // Extract parent, UNID, and terminal symbol
        String parent = getTextContent(node, "PARENT");
        String unid = getTextContent(node, "UNID");
        String terminal = getTextContent(node, "TERMINAL");
    
        // Handle terminal nodes (tokens from the lexer)
        if (isToken(terminal)) {
            symbolTable.put(unid, new Symbol(terminal, "token", parent)); // Store token in the symbol table
        }
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
