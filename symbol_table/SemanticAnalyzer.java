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

            // After traversal, print the symbol table and errors
            printSymbolTable();
            printErrors();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive function to traverse the tree and enforce semantic rules
    private void traverseNode(Node node) {
        String nodeName = node.getNodeName();

        System.out.println("Currently analyzing node: " + nodeName); // Debugging output

        switch (nodeName) {
            case "ROOT":
                handleRootNode(node);
                break;

            case "INNERNODES":
                handleInnerNodes(node);
                break;

            case "LEAFNODES":
                handleLeafNodes(node);
                break;

            default:
                System.out.println("Skipping unknown node: " + nodeName);
        }
    }

    // Handle ROOT node
    private void handleRootNode(Node node) {
        // Push the root scope (main program scope) onto the stack
        scopeStack.push("MAIN");
        String startSymbol = getTextContent(node, "SYMB");
        System.out.println("Analyzing ROOT with start symbol: " + startSymbol);

        // Traverse child nodes of the root
        NodeList children = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                traverseNode(children.item(i)); // Recurse through child nodes
            }
        }

        // Pop the root scope after traversal
        scopeStack.pop();
        System.out.println("Popped ROOT scope (MAIN)");
    }

    // Handle INNERNODES (function or inner scopes)
    private void handleInnerNodes(Node node) {
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
    
        // Debug output
        System.out.println("Analyzing IN node: " + nonTerminal + " with UNID: " + unid + " and parent: " + parent);
    
        // Check if the non-terminal represents a function
        if (isFunction(nonTerminal)) {
            // Function scope handling
            if (scopeStack.contains(nonTerminal)) {
                throwError("Function name conflict in scope", unid);
            }
            scopeStack.push(nonTerminal);
            symbolTable.put(unid, new Symbol(nonTerminal, "function", scopeStack.peek()));
            System.out.println("Function declared: " + nonTerminal + " in scope: " + nonTerminal);
        }
    
        // Handle child nodes under this IN node
        NodeList children = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node childNode = children.item(i);
    
            // Only process ID nodes (the child UNID references)
            if (childNode.getNodeType() == Node.ELEMENT_NODE && "ID".equals(childNode.getNodeName())) {
                String childUnid = childNode.getTextContent();
                System.out.println("Child node UNID: " + childUnid);
            }
        }
    
        // After processing the IN node, pop the function scope if applicable
        if (isFunction(nonTerminal)) {
            scopeStack.pop();
            System.out.println("Popped function scope: " + nonTerminal);
        }
    }
    

    // Handle LEAFNODES (variables or terminal symbols)
    private void handleLeafNodes(Node node) {
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
    
        // Debug output
        System.out.println("Analyzing LEAF node: " + terminal + " with UNID: " + unid + " and parent: " + parent);
    
        // Handle terminal nodes (tokens from the lexer)
        // If needed, process the token as a part of your lexical analysis
        if (isToken(terminal)) {
            System.out.println("Token recognized: " + terminal + " with UNID: " + unid);
            symbolTable.put(unid, new Symbol(terminal, "token", parent)); // Store token in the symbol table
        } else {
            System.out.println("Unknown terminal: " + terminal);
        }
    }
    

    // Helper method to get text content from an element by tag name
    private String getTextContent(Node node, String tagName) {
        return ((Element) node).getElementsByTagName(tagName).item(0).getTextContent();
    }

    // Helper method to check if a node represents a function
    private boolean isFunction(String symbol) {
        // Check if the symbol matches the function name pattern
        return symbol.matches("\\bF_[a-z][a-z0-9]*\\b");
    }

    // Helper method to check if a terminal represents a variable
    private boolean isVariable(String terminal) {
        // Check if it's not a keyword and follows the variable pattern
        return !isKeyword(terminal) && terminal.matches("\\bV_[a-z][a-z0-9]*\\b");
    }

    // Helper method to check if terminal is a keyword
    private boolean isKeyword(String terminal) {
        // Check if terminal is one of the predefined keywords
        return terminal.matches("\\b(main|begin|end|skip|halt|print|input|num|if|then|void|else|not|sqrt|or|and|eq|grt|add|sub|mul|div)\\b");
    }

    // Helper method to check if a string is a valid token
private boolean isToken(String terminal) {
    // Assuming you have methods to check if a terminal is a keyword or variable
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
