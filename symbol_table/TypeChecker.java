package symbol_table;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


public class TypeChecker {

    private Map<String, Symbol> vtable ;
    private Map<String, Symbol> ftable ;
    private static File xmlfile ;
    public TypeChecker( File xmlfile ,Map<String, Symbol> vtable ,Map<String, Symbol> ftable) {
        this.xmlfile = xmlfile;
        this.ftable = ftable;
        this.vtable = vtable;

    }
    
 
    public boolean typeChecker(Node node) {
        try {
            String unid = getTextContent(node, "UNID");
            List<String> children = getChildren(unid);
            String symbol = getSymbol(node);
    
            System.out.println(unid + ": " + symbol);
    
            List<Node> studentInnerNodes = getChildrenNodes(node);
    
            switch (symbol) {
                case "PROG":
                    System.out.println("Reached case 'PROG'");
                    boolean allChecksPass = true;
    
                    // Filter to get only inner nodes
                    List<Node> innerNodesOnly = studentInnerNodes.stream()
                                                                 .filter(this::isInnerNode)
                                                                 .collect(Collectors.toList());
    
                    // Print out innerNodesOnly
                    System.out.println("Inner nodes only:");
                    for (Node innerNode : innerNodesOnly) {
                        System.out.println("Node: " + innerNode);
                        System.out.println("Node name: " + innerNode.getNodeName());
                        System.out.println("Node value: " + innerNode.getNodeValue());
                        System.out.println("Node text content: " + innerNode.getTextContent());
                    }
    
                    // Debugging: Print the number of inner nodes
                    System.out.println("Number of inner nodes: " + innerNodesOnly.size());
    
                    boolean globVarsCheck = false;
                    boolean algoCheck = false;
                    boolean functionsCheck = false;
    
                    for (Node studentNode : innerNodesOnly) {
                        // Debugging: Print out the studentNode before fetching its symbol
                        System.out.println("Processing inner node: " + studentNode);
    
                        // Fetch and print the symbol for the current studentNode
                        String studentSymbol = getSymbol(studentNode);
    
                        // Debugging: Print the fetched symbol
                        System.out.println("Fetched symbol: " + studentSymbol);
    
                        // If the symbol is null or empty, print a warning
                        if (studentSymbol == null || studentSymbol.isEmpty()) {
                            System.out.println("Warning: Symbol is null or empty for node: " + studentNode);
                            continue;
                        }
    
                        // Check the type of the inner node
                        switch (studentSymbol) {
                            case "GLOBVARS":
                                globVarsCheck = typeChecker(studentNode);
                                break;
                            case "ALGO":
                                algoCheck = typeChecker(studentNode);
                                break;
                            case "FUNCTIONS":
                                functionsCheck = typeChecker(studentNode);
                                break;
                            default:
                                System.out.println("Unknown symbol: " + studentSymbol);
                                allChecksPass = false;
                                break;
                        }
    
                        if (!allChecksPass) {
                            break;
                        }
                    }
    
                    // Combine the results of the checks
                    allChecksPass = globVarsCheck && algoCheck && functionsCheck;
    
                    // Debugging: Final result of all checks
                    System.out.println("All checks pass: " + allChecksPass);
    
                    return allChecksPass;
    
                case "GLOBVARS":
                    // Base case: no children
                    if (studentInnerNodes.isEmpty()) {
                        return true;
                    }
    
                    // Recursive case: process GLOBVARS1
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
    
                        if ("VTYP".equals(childSymbol)) {
                            String type = getTextContentSafe(childNode, "VTYP");
                            String id = getTextContentSafe(childNode, "VNAME");
    
                            // Update the symbol table
                            updateSymbolTable(id, type);
    
                            // Recursively type check GLOBVARS2
                            return typeChecker(childNode);
                        }
                    }
                    return false;
    
                case "ALGO":
                    // Process the INSTRUC child node
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
    
                        if ("INSTRUC".equals(childSymbol)) {
                            return typeChecker(childNode);
                        }
                    }
                    return false;
    
                case "VTYPE": // come back and update 
                    // Determine the type of VTYP
                    String vtypContent = getTextContentSafe(node, "VTYP");
                    char typeChar = typeof(vtypContent);
                    System.out.println("Type of VTYP: " + typeChar);
                    return typeChar == 'n' || typeChar == 't';
    
                // Add other cases as needed
                case "INSTRUC":
                    return false;
    
                case "COMMAND":
                    return false;
    
                case "ATOMIC":
                    return false;
    
                case "ASSIGN":
                    return false;
    
                case "TERM":
                    return false;
    
                case "CALL":
                    return false;
    
                case "OP":
                    return false;
    
                case "ARG":
                    return false;
    
                case "UNOP":
                    return false;
    
                case "BINOP":
                    return false;
    
                case "BRANCH":
                    return false;
    
                case "COND":
                    return false;
    
                case "SIMPLE":
                    return false;
    
                case "COMPOSITE":
                    return false;
    
                case "FNAME":
                    return false;
    
                case "FUNCTIONS":
                    return false;
    
                case "DECL":
                    return false;
    
                case "HEADER":
                    return false;
    
                case "FTYP":
                    return false;
    
                case "BODY":
                    return false;
    
                case "PROLOG":
                    return false;
    
                case "EPILOG":
                    return false;
    
                case "LOCVARS":
                    return false;
    
                case "SUBFUNCS":
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return false;
    }
    
    // private void updateSymbolTable(String id, String type) {
    //     // Implement the logic to update the symbol table with the given id and type
    //     System.out.println("Updating symbol table: " + id + " -> " + type);
    // }
    
    // private String getTextContentSafe(Node node, String tagName) {
    //     if (node == null) {
    //         System.out.println("Warning: Node is null for tag: " + tagName);
    //         return "";
    //     }
    //     Element element = (Element) node;
    //     NodeList nodeList = element.getElementsByTagName(tagName);
    //     if (nodeList.getLength() == 0) {
    //         System.out.println("Warning: No elements found for tag: " + tagName);
    //         return "";
    //     }
    //     Node item = nodeList.item(0);
    //     if (item == null) {
    //         System.out.println("Warning: Item is null for tag: " + tagName);
    //         return "";
    //     }
    //     return item.getTextContent().trim();
    // }
    
    private char typeof(String vtypContent) {
        switch (vtypContent) {
            case "num":
                return 'n';
            case "text":
                return 't';
            default:
                System.out.println("Unknown VTYP content: " + vtypContent);
                return '?';
        }
    }
    
    
    
    // private void updateSymbolTable(String id, String type) {
    //     // Implement the logic to update the symbol table with the given id and type
    //     System.out.println("Updating symbol table: " + id + " -> " + type);
    // }
    
    private String getTextContentSafe(Node node, String tagName) {
        if (node == null) {
            System.out.println("Warning: Node is null for tag: " + tagName);
            return "";
        }
        Element element = (Element) node;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            System.out.println("Warning: No elements found for tag: " + tagName);
            return "";
        }
        Node item = nodeList.item(0);
        if (item == null) {
            System.out.println("Warning: Item is null for tag: " + tagName);
            return "";
        }
        return item.getTextContent().trim();
    }
    
    private void updateSymbolTable(String id, String type) {
        // Implement the logic to update the symbol table with the given id and type
        System.out.println("Updating symbol table: " + id + " -> " + type);
    }
    
    
    
    public void typeCheckers(File xmlFile) {
        try {
            this.xmlfile = xmlFile;
            // Parse the XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Start analyzing from the <ROOT> node
            Element root = (Element) doc.getElementsByTagName("ROOT").item(0);
          boolean tree  = typeChecker(root);
          System.out.println(tree);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive function to traverse the tree and enforce semantic rules
    private boolean typeCheck(Node node) {
        String nodeName = node.getNodeName().trim();
     
        switch (nodeName) {
            case "ROOT":
            return typeCheckRootNode(node);

        case "IN":
            return typeCheckInNode(node);

        case "LEAF":
            return typeCheckLeafNode(node);
        }
        return false;
    }

    // Handle ROOT node
    private boolean typeCheckRootNode(Node node) {
        String nodeName = node.getNodeName().trim();
        String symb = getSymbol(node);
        // System.out.println(symb);

        NodeList childIDs = ((Element) node).getElementsByTagName("CHILDREN").item(0).getChildNodes();
        NodeList innerNodes = ((Element) node.getOwnerDocument().getElementsByTagName("INNERNODES").item(0)).getElementsByTagName("IN");

        for (int i = 0; i < childIDs.getLength(); i++) {
            if (childIDs.item(i).getNodeType() == Node.ELEMENT_NODE) {
                String childID = childIDs.item(i).getTextContent().trim();

                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Element innerNode = (Element) innerNodes.item(j);
                    String id = getTextContent(innerNode, "UNID");
                    if (id.equals(childID)) {
                       return typeCheck(innerNode);
                        
                    }
                }
            } 
    }

    return false;
    }


    private boolean isLeafNode(Node node) {
        Element element = (Element) node;
        NodeList childrenList = element.getElementsByTagName("CHILDREN");
        if (childrenList.getLength() == 0 || childrenList.item(0) == null) {
            return true; // No children element means it's a leaf node
        }
        NodeList children = childrenList.item(0).getChildNodes();
        return children.getLength() == 0;
    }
    
    private boolean isInnerNode(Node node) {
        return !isLeafNode(node);
    }
    
   
    
  
    // Handle IN node (function or inner scope)
    private boolean typeCheckInNode(Node node) {

           return processChildren(node);
        
    }

    private List<Node> getChildrenNodes(Node node) {
        List<Node> childrenNodes = new ArrayList<>();
    
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
                        childrenNodes.add(innerNode);
                        break;
                    }
                }
    
                for (int j = 0; j < leafNodes.getLength(); j++) {
                    Element leafNode = (Element) leafNodes.item(j);
                    String id = getTextContent(leafNode, "UNID");
                    if (id.equals(childID)) {
                        childrenNodes.add(leafNode);
                        break;
                    }
                }
            }
        }
    
        return childrenNodes;
    }
    
    
    private void addChildrenToList(List<Node> result, NodeList children) {
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                result.add(child);
            }
        }
    }


    // Handle LEAF nodes (variables or terminal symbols)
    private boolean typeCheckLeafNode(Node node) {
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

                 for (String child : childList) {
                // Check your condition (e.g., if the child contains "special")
             
            if (parameter) {
                temp = getChildren(child);
                types = "parameter";
                
            }
                if (getSymbolByUNID(child).equals("VNAME") ) {
                    // Add to childlist if condition is met
                    
                    temp = getChildren(child);
                    children.add(getSymbolByUNID(temp.get(0)));
                }

                if (getSymbolByUNID(child).equals("VTYP")) {
                    //System.out.println(getSymbolByUNID(child));
                    // Add to childlist if condition is met
                    temp = getChildren(child);
                    types = getSymbolByUNID(temp.get(0));
                }
            }
        
        } catch (Exception e) {
            // Handle any exceptions
            e.printStackTrace(); // Log or handle exceptions as necessary
        }

   
    }
    return false;
     
    }
    
    
    // Helper method to process child nodes
    private boolean processChildren(Node node) {
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
                       return typeCheck(innerNode);
                        
                    }
                }

                for (int j = 0; j < leafNodes.getLength(); j++) {
                    Element leafNode = (Element) leafNodes.item(j);
                    String id = getTextContent(leafNode, "UNID");
                    if (id.equals(childID)) {
                       return typeCheck(leafNode);
                    
                    }
                }
            }
        }
        return false;
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

    private String getSymbol(Node rootNode) {
        // Ensure the rootNode is an Element and it's the expected ROOT node
        if (rootNode.getNodeType() == Node.ELEMENT_NODE) {
            Element rootElement = (Element) rootNode;
            
            // Try to get the SYMB tag first
            NodeList symbNodes = rootElement.getElementsByTagName("SYMB");
            if (symbNodes.getLength() > 0) {
                return symbNodes.item(0).getTextContent().trim(); // Return the symbol (SYMB in this case)
            }
            
            // If SYMB is not found, try to get the TERMINAL tag
            NodeList terminalNodes = rootElement.getElementsByTagName("TERMINAL");
            if (terminalNodes.getLength() > 0) {
                return terminalNodes.item(0).getTextContent().trim(); // Return the symbol (TERMINAL in this case)
            }
        }
        return null; // Return null if neither SYMB nor TERMINAL is found
    }
    

    private static Document parseXML(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }


}
