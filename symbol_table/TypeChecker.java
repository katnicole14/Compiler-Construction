package symbol_table;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.io.File;
import java.util.*;


public class TypeChecker {

    private Map<String, Symbol> vtable ;
    private Map<String, Symbol> ftable ;
    private static File xmlfile ;
    public TypeChecker() {

    }
    
 
    public boolean typeCheck(String symbol){
        switch (symbol) {
            case "PROG":
                return typeCheck("GLOBVARS") && typeCheck("ALGO") && typeCheck("FUNCTIONS");
            
            case "GLOBVARS": //both cases
                return false;
            
            case "ALGO":
                return typeCheck("INSTRUC");
            case "INSTRUC": ///add both cases

                return false;

            case "COMMAND": //handle all the command 
                return false;

            case "ATOMIC": //handle atomic
                return false;

            case "ASSIGN":  //handle the implementation
                return false;

            case "TERM": // handle the cases 
                return false;

            case "CALL":
                return false;

            case "OP": //HANDLE BOTH unop
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
            case "SIMPLE" :
                return false;
            case "COMPOSITE":
                return false;
            
            case "FNAME":
            return false;

            case "FUNCTIONS":
            return false;

            case "DECL":
            return false ;

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
             return typeCheck("FUNCTIONS");        

        }
        return false;

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
            traverseNode(root);


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
        processChildren(node);
       
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

    private static Document parseXML(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }


}
