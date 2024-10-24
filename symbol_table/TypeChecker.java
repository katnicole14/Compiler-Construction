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
    private Stack<String> scopeStack=new Stack<>();
    public TypeChecker( File xmlfile ,Map<String, Symbol> vtable ,Map<String, Symbol> ftable) {
        this.xmlfile = xmlfile;
        this.ftable = ftable;
        this.vtable = vtable;

    }
    
    public boolean typeChecker(Node node) {
        try {
            String unid = getTextContent(node, "UNID"); // id of the current symbol
            String symbol = getSymbol(node); // symbol of the node
    
            List<Node> studentInnerNodes = getChildrenNodes(node);  // children of the passed-in node
    
            List<Node> innerNodesOnly = studentInnerNodes.stream()
                    .filter(this::isInnerNode)
                    .collect(Collectors.toList());   // non-terminals
            List<Node> leafNodesOnly = studentInnerNodes.stream()
                    .filter(this::isLeafNode) 
                    .collect(Collectors.toList()); // terminals
    
            switch (symbol) {

                case "PROG":
                   
    
                    boolean allChecksPass = true;
    
                    boolean globVarsCheck = false;
                    boolean algoCheck = false;
                    boolean functionsCheck = false;
    
                    for (Node studentNode : innerNodesOnly) {
                        String studentSymbol = getSymbol(studentNode);
    
                        if (studentSymbol == null || studentSymbol.isEmpty()) {
                            continue;
                        }
    
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
                                allChecksPass = false;
                                break;
                        }
    
                        if (!allChecksPass) {
                            break;
                        }
                    }
                    System.out.println();
                    System.out.println();
                    System.out.println("TYPE CHECKING");
                    allChecksPass = globVarsCheck && algoCheck && functionsCheck;
                    System.out.println("[DEBUG] Final check results: globVarsCheck=" + globVarsCheck +
                            ", algoCheck=" + algoCheck + ", functionsCheck=" + functionsCheck );
                    System.out.println("allChecksPass=" + allChecksPass);
    
                    return allChecksPass;
    
                case "GLOBVARS":
    
                    if (studentInnerNodes.isEmpty()) {
                        return true;
                    }
    
                    String id = "";
                    char type1 = ' ';
    
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
    
                        if ("VTYP".equals(childSymbol)) {
                            List<Node> kid = getChildrenNodes(childNode);
                            type1 = typeof(getSymbol(kid.get(0)));  // Let T := typeof VTYP
                        }
    
                        if ("VNAME".equals(childSymbol)) {
                            id = getTextContentSafe(childNode, "ID");
                        }
                    }
    
    
                    if (type1 != ' ' && !id.isEmpty()) {
                        //updateSymbolTable(id, type1);
                        
                    }
                    return typeChecker(studentInnerNodes.get(3));
    
                case "ALGO":
    
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
    
                        if ("INSTRUC".equals(childSymbol)) {
                            return typeChecker(childNode);
                        }
                    }
    
                    return false;
    
                case "INSTRUC":
                    if (studentInnerNodes.isEmpty()) {
                        return true;
                    }
                            boolean commandCheck = typeChecker(studentInnerNodes.get(0));
                            boolean instruc2Check = typeChecker(studentInnerNodes.get(2)); // Assuming INSTRUC2 is the next sibling
                            return commandCheck && instruc2Check;   
                
    
                            case "COMMAND":
                        
                            for (Node childNode : studentInnerNodes) {
                                String childSymbol = getSymbol(childNode);
                              
                        
                                switch (childSymbol) {
                                    case "skip":
                                    
                                        return true;
                                    case "halt":
                                      
                                        return true;
                                    case "print":
                                    
                                        Node atomicNode = studentInnerNodes.get(1);
                                        if (atomicNode != null) {
                                            char atomicType = typeof(atomicNode);
      
                                            return atomicType == 'n' || atomicType == 't';
                                        }
                                        return false;
                                    case "return":
                                        String scope = currentScope();

                        
                                        String scopeId = getIdBySymbolName(scope);
                                     
                        
                                        String functionType = getTypeFromSymbolTable(scopeId);
                               
                        
                                        Node returnAtomicNode = studentInnerNodes.get(1);
                                        if (returnAtomicNode != null) {
                                            char atomicType = typeof(returnAtomicNode);

                                            return atomicType == typeof(functionType) || atomicType == typeof(functionType);
                                        }
                             
                                        return false;
                                    case "ASSIGN":
                             
                                        return typeChecker(childNode);
                                    case "CALL":
                                        String callNodeId = getTextContent(childNode, "UNID");

                                        return typeof(childNode) == 'v';
                                    case "BRANCH":

                                        return typeChecker(childNode);
                                    default:

                                        return false;
                                }
                            }

                            return false;
                        
    
                            case "ASSIGN":


                        
                            boolean inputCheck = false;
                            boolean termCheck = false;
                        
                            for (Node childNode : studentInnerNodes) {
                                String childSymbol = getSymbol(childNode);
                           
                        
                                if ("input".equals(childSymbol)) {
                                    inputCheck = typeof(studentInnerNodes.get(0)) == 'n';

                                }
                        
                                if ("TERM".equals(childSymbol)) {
                                    termCheck = typeof(studentInnerNodes.get(0)) == typeof(childNode);
                                
                                }
                            }
                        
                   
                            return inputCheck || termCheck;
                        
    
                case "BRANCH":
    
                    Node condNode = getChildNodeByTagName(node, "COND");
                    Node algo1Node = studentInnerNodes.get(1);
                    System.out.println();
                    System.out.println(getSymbol(algo1Node));
                    
                    Node algo2Node = studentInnerNodes.get(2);
                    System.out.println(getSymbol(algo2Node));
    
                    if (condNode != null && algo1Node != null && algo2Node != null) {
                        char condType = typeof(condNode);
                        if (condType == 'b') {
                            return typeChecker(algo1Node) && typeChecker(algo2Node);
                        }
                    }
    
                    return false;

                    case "HEADER":
                    // Determine the function type
                    char ftyp = typeof(studentInnerNodes.get(0));
                    
                    // Get children nodes of the function
                    List<Node> funNode = getChildrenNodes(studentInnerNodes.get(1));
                    String FuncId = getIdBySymbolName(getSymbol(funNode.get(0)));
                    enterScope(getSymbol(funNode.get(0)));
                    // Update the symbol table with the function ID and type
                    //updateSymbolTable(FuncId, ftyp);

                    List<Node> var1 = getChildrenNodes(studentInnerNodes.get(2));
                    List<Node> var2 = getChildrenNodes(studentInnerNodes.get(3));
                    List<Node> var3 = getChildrenNodes(studentInnerNodes.get(4));
                    String var1Id = getIdBySymbolName(getSymbol(var1.get(0)));
                    String var2Id = getIdBySymbolName(getSymbol(var2.get(0)));
                    String var3Id = getIdBySymbolName(getSymbol(var3.get(0)));
                    char paramtype = 'n';

                
                    // Update the symbol table with the function ID and type
                    updateSymbolTable(var1Id, "num");
                    updateSymbolTable(var2Id, "num");
                    updateSymbolTable(var3Id, "num");
                    
                    // Check types of specific nodes
                    char type3 = typeof(studentInnerNodes.get(2));
                    char type5 = typeof(studentInnerNodes.get(3));
                    char type7 = typeof(studentInnerNodes.get(4));
                    
                
                    // Check if all specified types are 'n'
                    if (type3 == 'n' && type5 == 'n' && type7 == 'n') {
                        return true;
                    } else {
                        return false;
                    }
                
               
                    case "DECL":
                    List<Node> declarechildren = getChildrenNodes(node);
                    if (declarechildren.size() < 2) {
                        return false; // Adjust return value as needed
                    }
                    boolean header = typeChecker(declarechildren.get(0));
                    boolean body = typeChecker(declarechildren.get(1));
                    return header && body;
                case "PROLOG":
                    return true;
                case "EPILOG":
                
                    return true;
                    case "LOCVARS":
                
                    String id1 = " ";
                    String id2 = " ";
                    String id3 = " ";
                     type1 = ' ';
                    char type2 = ' ';
                     type3 = ' ';

                     List<Node> v1 = getChildrenNodes(studentInnerNodes.get(1));
                     List<Node> v2 = getChildrenNodes(studentInnerNodes.get(3));           
                     List<Node> v3 = getChildrenNodes(studentInnerNodes.get(5));
                     id1 =getIdBySymbolName(getSymbol(v1.get(0)));
                     id2 =getIdBySymbolName(getSymbol(v2.get(0)));
                     id3 =getIdBySymbolName(getSymbol(v3.get(0)));
                     List<Node> kid1 = getChildrenNodes(studentInnerNodes.get(0));
                     List<Node> kid2 = getChildrenNodes(studentInnerNodes.get(2)); 
                     List<Node> kid3 = getChildrenNodes(studentInnerNodes.get(4));

                     type1 = typeof(getSymbol(kid1.get(0)));
                     type2 = typeof(getSymbol(kid2.get(0)));
                     type3 = typeof(getSymbol(kid3.get(0)));   
                
                
                return true;

                case "BODY":
                    boolean prolog = typeChecker(studentInnerNodes.get(0));
                    boolean locvar = typeChecker(studentInnerNodes.get(1)); // Corrected index
                    boolean algo = typeChecker(studentInnerNodes.get(2)); // Corrected index
                    boolean epilog = typeChecker(studentInnerNodes.get(3)); // Corrected index
                    boolean subfun = typeChecker(studentInnerNodes.get(4)); // Corrected index

                    return prolog && locvar && algo && epilog && subfun;
                
                case "FUNCTIONS":
                    
                    if (studentInnerNodes.isEmpty()) {
                        return true;
                    }
                    functionsCheck = typeChecker(studentInnerNodes.get(0));
                    return functionsCheck;
                
                case "SUBFUNCS":
                    List<Node> FuncNode = getChildrenNodes(node);
                    
                    if (FuncNode.isEmpty()) {
                        return false; // Adjust return value as needed
                    }
                    
                    boolean subFuncCheck = typeChecker(FuncNode.get(0));
                    return subFuncCheck;
                
    
                default:
                    return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private Node getChildNodeByTagName(Node node, String tagName) {
        if (node == null) {
            return null;
        }
        Element element = (Element) node;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return null;
        }
        return nodeList.item(0);
    }

    
    private void updateSymbolTable(String id, String type) {

        // Check if the id exists in vtable or ftable
        if (vtable.containsKey(id)) {
            // Update the existing variable entry
            Symbol existingVarSymbol = vtable.get(id);
            existingVarSymbol.setType(type); // Assuming there's a setType method in Symbol
            return; // Early exit after updating
        } else if (ftable.containsKey(id)) {
            // Update the existing function entry
            Symbol existingFuncSymbol = ftable.get(id);
            existingFuncSymbol.setType(type); // Assuming there's a setType method in Symbol
            return; // Early exit after updating
        }
    }
    
    private char typeof(Node node) {
        try {
            String unid = getTextContent(node, "UNID"); 
            List<Node> children = getChildrenNodes(node);
            String symbol = getSymbol(node);
            
            // Debugging output for the current node
            
            switch (symbol) {
                case "ATOMIC":
                List<Node> atomicChild = getChildrenNodes(node);
                return typeof(atomicChild.get(0));

                case "VNAME":
                String VarName = getIdBySymbolName(getSymbol(children.get(0)));
                String typ = getTypeFromSymbolTable(VarName);
                return typeof(typ);
                

                case "FNAME":
                    String funcName = getIdBySymbolName(getSymbol(children.get(0)));
                    String fy = getTypeFromSymbolTable(funcName);
                    return typeof(fy);
    
                    case "TERM":

             
                
                    // Loop through the children
                    for (Node child : children) {
                        String childSymbol = getSymbol(child);
                    
                
                        // Check if child symbol is "ATOMIC"
                        if ("ATOMIC".equals(childSymbol)) {
                          
                            List<Node> atomic = getChildrenNodes(child);
                          
                
                            char atomicType = typeof(atomic.get(0));
                         
                
                            return atomicType;
                        }
                    
                  
                
    
                        // Check if child symbol is "CALL"
                        if ("CALL".equals(childSymbol)) {
                            return typeof(child);
                        }
    
                        if ("OP".equals(childSymbol)) {
                           
                            char callType = typeof(child);
                            return callType;
                        }
                    }
                    break;
    
                case "CALL":
    
                    // Get the children of the CALL node
                    List<Node> childrenNode = getChildrenNodes(node);
            
    
                    Node fnameNode = childrenNode.get(0); // FNAME
                    Node atomic1Node = childrenNode.get(1); // ATOMIC1
                    Node atomic2Node = childrenNode.get(2); // ATOMIC2
                    Node atomic3Node = childrenNode.get(3); // ATOMIC3
    
                    char funtype = typeof(fnameNode); // Type of function name
                    char atomic1Type = typeof(getChildrenNodes(atomic1Node).get(0));
                    char atomic2Type = typeof(getChildrenNodes(atomic2Node).get(0));
                    char atomic3Type = typeof(getChildrenNodes(atomic3Node).get(0));
    
    
                    if (atomic1Type == 'n' && atomic2Type == 'n' && atomic3Type == 'n') {
                        return funtype;  // typeof(CALL) = typeof(FNAME)
                    } else {
                        return 'u';  // typeof(CALL) = 'u'
                    }
    
                    case "OP":
                    List<Node> opchildren = getChildrenNodes(node); // binop or unop
                    String operation = getSymbol(getChildrenNodes(opchildren.get(0)).get(0));
                
                    if (getSymbol(children.get(0)).equals("UNOP")) {
                        System.out.println("Detected UNOP");
                
                       
                
                        char unopType = typeof(operation);
                        char argType = typeof(children.get(1));
                
                      
                
                        if (unopType == 'b' && argType == 'b') {
        
                            return 'b';  // typeof(OP) = 'b'
                        } else if (unopType == 'n' && argType == 'n') {
                        
                            return 'n';  // typeof(OP) = 'n'
                        } else {
                      
                            return 'u';  // typeof(OP) = 'u'
                        }
                    } else if (getSymbol(children.get(0)).equals("BINOP")) {
                    
                        

                      
                        char binopType = typeof(operation);
                        char argType1 = typeof(children.get(1));
                        char argType2 = typeof(children.get(2));
                
                    
                
                        if (binopType == 'b' && argType1 == 'b' && argType2 == 'b') {
                          
                            return 'b'; // bool-type
                        } else if (binopType == 'n' && argType1 == 'n' && argType2 == 'n') {
                        
                            return 'n'; // numeric type
                        } else if (binopType == 'c' && argType1 == 'n' && argType2 == 'n') {
                          
                            return 'b'; // comparison-type, yields a boolean result
                        } else {
                        
                            return 'u'; // undefined type
                        }
                    }
                
                
                case "ARG":
    
                    if (children.size() < 1) {
                        return 'u';  // Return 'u' if no children
                    }
    
                    Node argChild = children.get(0);
                    char argType = typeof(argChild);
    
                    if (getSymbol(argChild).equals("ATOMIC")) {
                        return argType;
                    } else if (getSymbol(argChild).equals("OP")) {
                        return typeof(argChild);
                    }
                    break;
    
                case "CONST":
                List<Node> constValueNode = getChildrenNodes(node);
                     if (isNumber(getSymbol((constValueNode.get(0))))){
                        return 'n';
                     }
                     else return 't';
                    
                case "FTYP":
                    List<Node> ftypChildren = getChildrenNodes(node);
                    if (ftypChildren.size() < 1) {
                        return 'u';  // Return 'u' if no children
                    }
    
                    Node ftypChild = ftypChildren.get(0); // num or void
                    String ftypChildSymbol = getSymbol(ftypChild);
    
                    if ("num".equals(ftypChildSymbol)) {
                        return 'n';
                    } else if ("void".equals(ftypChildSymbol)) {
                        return 'v';
                    } else {
                        return 'u';  // Undefined type
                    }
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    
        return 'x';  // Default return value if no conditions matched
    }
        

    private char typeof(String content) {
        switch (content) {
            case "num":
                return 'n'; // Numeric type
            case "text":
                return 't'; // Text type
            case "not":
                return 'b'; // Boolean type (unary operator)
            case "sqrt":
                return 'n'; // Numeric type (unary operator)
            case "or":
            case "and":
                return 'b'; // Boolean type (binary operator)
            case "eq":
            case "grt":
                return 'c'; // Comparison type (binary operator)
            case "add":
            case "sub":
            case "mul":
            case "div":
                return 'n'; // Numeric type (binary operator)

            case "void":
                return 'v'; //void functions return
          
            default:
                return 'u'; // Undefined type
        }
    }
    
    
    private String getTextContentSafe(Node node, String tagName) {
        if (node == null) {
            return "";
        }
        Element element = (Element) node;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return "";
        }
        Node item = nodeList.item(0);
        if (item == null) {
            return "";
        }
        return item.getTextContent().trim();
    }

    
    private char findFunctionType(Node node) {
        // Traverse up the tree to find the function scope
        Node currentNode = node;
        while (currentNode != null) {
            String symbol = getSymbol(currentNode);
            if ("FUNCTION".equals(symbol) || "DECL".equals(symbol)) {
                // Found the function declaration, now find the FTYP node
                Node ftypNode = getChildNodeByTagName(currentNode, "FTYP");
                if (ftypNode != null) {
                    return typeof(getTextContentSafe(ftypNode, "FTYP"));
                }
            }
            currentNode = currentNode.getParentNode();
        }
        return 'u'; // Return 'u' for undefined if not found
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
          exitScope();


        } catch (Exception e) {
            e.printStackTrace();
        }
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
    

    // Helper method to get text content from an element by tag name
    private String getTextContent(Node node, String tagName) {
        return ((Element) node).getElementsByTagName(tagName).item(0).getTextContent();
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

    private Node getChildNode(Node parentNode, String tagName) {
        if (parentNode == null) {
            return null;
        }
        Element element = (Element) parentNode;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            return null;
        }
        return nodeList.item(0);
    }
    
    public boolean isNumber(String input) {
        // Regular expression for a valid number (integer or decimal)
        String numberPattern = "^-?\\d+(\\.\\d+)?$";
        
        // Return true if the input matches the pattern, false otherwise
        return input.matches(numberPattern);
    }

    private String getTypeFromSymbolTable(String name) {
        // Check in the variable table
        if (vtable.containsKey(name)) {

            return vtable.get(name).getType();
        }
    
        // Check in the function table
        if (ftable.containsKey(name)) {
            return ftable.get(name).getType();
        }
    
        return " ";
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

    public String getIdBySymbolName(String symbolName) {
        // Search in vtable
    
        if (vtable != null) {
            for (Map.Entry<String, Symbol> entry : vtable.entrySet()) {
                if (entry.getValue().getSymb().equals(symbolName)) {
                    return entry.getKey(); // Return the ID (key) associated with the symbol
                }
            }
        } else {
        }
    
        // Optionally search in ftable if needed
        if (ftable != null) {
            for (Map.Entry<String, Symbol> entry : ftable.entrySet()) {
                if (entry.getValue().getSymb().equals(symbolName)) {
                    return entry.getKey(); // Return the ID (key) associated with the symbol
                }
            }
        } else {
        }
    
        return null; // Return null if not found in both tables
    }
    
    private void enterScope(String scopeName) {
        scopeStack.push(scopeName); // Push the new scope onto the stack

       
    }

    private void exitScope() {
        if (scopeStack.size() == 0) {
            return;
        }
        String exitedScope = scopeStack.pop(); // Remove the current scope


    }

    private String currentScope() {
        if (scopeStack.size() == 0) {
            return " ";
        }
        return scopeStack.peek(); // Get the current scope from the stack
    }
    

    private static Document parseXML(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }


}
