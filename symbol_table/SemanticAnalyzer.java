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
    // Counter for generating unique names
    private int uniqueCounterVariable = 0;
    private int uniqueCounterFunction = 0;

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
        String parent = getTextContent(node, "PARENT");
        String unid = getTextContent(node, "UNID");
    
        // Handle terminal nodes (tokens from the lexer)
        if (isFunction(terminal)) {
            // Function scope handling
            if (scopeStack.contains(terminal)) {
                throwError("Function name conflict in scope", unid);
            }
            String uniqueName = generateUniqueName(terminal);
            scopeStack.push(uniqueName);
            symbolTable.put(unid, new Symbol(uniqueName, "function", scopeStack.peek(), terminal)); // Store function in the symbol table
        } else if (terminal.equals("main")) {
            symbolTable.put(unid, new Symbol(terminal, "main", parent, terminal)); // Store 'main' in the symbol table
        } else if (isToken(terminal) && !isKeyword(terminal)) {
            String uniqueName = generateUniqueName(terminal);
            symbolTable.put(unid, new Symbol(uniqueName, "variable", parent, terminal)); // Store other tokens in the symbol table
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
            return "f_" + (uniqueCounterFunction++);
        }
        else return "v_" + (uniqueCounterVariable++);

      
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
    private void printSymbolTable() {
        System.out.println("Symbol Table:");
        // Print the table headers with appropriate spacing
        System.out.printf("%-10s %-20s %-15s %-10s %-15s%n", "ID", "Symbol", "Type", "Scope", "Unique Name");
        System.out.println("--------------------------------------------------------------------------");

        // Loop through the symbol table and print each entry in a formatted manner
        for (Map.Entry<String, Symbol> entry : symbolTable.entrySet()) {
            Symbol symbol = entry.getValue();
            System.out.printf("%-10s %-20s %-15s %-10s %-15s%n",
                    entry.getKey(),         // ID
                    symbol.getSymb(),       // Symbol
                    symbol.getType(),       // Type
                    symbol.getScope(),      // Scope
                    symbol.getName());      // Unique Name
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

    public static void main(String[] args) {
        SemanticAnalyzer analyzer = new SemanticAnalyzer();
        File xmlFile = new File("your_xml_file_path.xml"); // Replace with your XML file path
        analyzer.analyze(xmlFile);
    }
}
