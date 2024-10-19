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
    
                    boolean globVarsCheck = false;
                    boolean algoCheck = false;
                    boolean functionsCheck = false;
    
                    for (Node studentNode : innerNodesOnly) {
          
    
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
    
                case "INSTRUC":
                    // Base case: no children
                    if (studentInnerNodes.isEmpty()) {
                        return true;
                    }
    
                    // Recursive case: process COMMAND and INSTRUC2
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
    
                        if ("COMMAND".equals(childSymbol)) {
                            boolean commandCheck = typeChecker(childNode);
                            boolean instruc2Check = typeChecker(childNode); // Assuming INSTRUC2 is the next sibling
                            return commandCheck && instruc2Check;
                        }
                    }
                    return false;
    
                case "COMMAND":
                    // Handle different COMMAND cases
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
    
                        switch (childSymbol) {
                            case "skip":
                            case "halt":
                                return true;
    
                            case "print":
                                Node atomicNode = getChildNodeByTagName(childNode, "ATOMIC");
                                if (atomicNode != null) {
                                    char atomicType = typeof(getTextContentSafe(atomicNode, "ATOMIC"));
                                    return atomicType == 'n' || atomicType == 't';
                                }
                                return false;
    
                            case "return":
                                Node returnAtomicNode = getChildNodeByTagName(childNode, "ATOMIC");
                                if (returnAtomicNode != null) {
                                    char returnType = typeof(getTextContentSafe(returnAtomicNode, "ATOMIC"));
                                    char functionType = findFunctionType(childNode);
                                   return returnType == functionType && functionType == 'n';
                                }
                                return false;
    
                                case "ASSIGN":
                                // Handle ASSIGN cases
                                for (Node child : studentInnerNodes) {
                                    childSymbol = getSymbol(childNode);
                            
                                    if ("VNAME".equals(childSymbol)) {
                                       String vnameType = getTypeFromSymbolTable(getTextContentSafe(childNode, "VNAME"));
                            
                                        // Check for "< input" assignment
                                        if ("<".equals(getTextContentSafe(child, "ASSIGN"))) {
                                            return vnameType.equals("n");
                                        }
                            
                                        // Check for "=" assignment
                                        if ("=".equals(getTextContentSafe(child, "ASSIGN"))) {
                                            Node termNode = getChildNodeByTagName(child, "TERM");
                                            if (termNode != null) {
                                               String termType = getTypeFromSymbolTable(getTextContentSafe(termNode, "TERM"));
                                               return vnameType.equals(termType);
                                            }
                                        }
                                    }
                                }
                                return false;
                            
    
                                case "CALL":
                                // Handle CALL case
                                Node fnameNode = getChildNodeByTagName(node, "FNAME");
                                Node atomic1Node = getChildNodeByTagName(node, "ATOMIC1");
                                Node atomic2Node = getChildNodeByTagName(node, "ATOMIC2");
                                Node atomic3Node = getChildNodeByTagName(node, "ATOMIC3");
                            
                                if (fnameNode != null && atomic1Node != null && atomic2Node != null && atomic3Node != null) {
                                    char atomic1Type = typeof(getTextContentSafe(atomic1Node, "ATOMIC1"));
                                    char atomic2Type = typeof(getTextContentSafe(atomic2Node, "ATOMIC2"));
                                    char atomic3Type = typeof(getTextContentSafe(atomic3Node, "ATOMIC3"));
                            
                                    if (atomic1Type == 'n' && atomic2Type == 'n' && atomic3Type == 'n') {
                                       String fnameType = getTypeFromSymbolTable(getTextContentSafe(fnameNode, "FNAME"));
                                        return typeof(fnameNode) == fnameType.charAt(0);
                                    } else {
                                        return false;
                                    }
                                }
                                return false;
                            
    
                            case "BRANCH":
                                return typeChecker(childNode);
    
                            default:
                                System.out.println("Unknown COMMAND symbol: " + childSymbol);
                                return false;
                        }
                    }
                    return false;
    
      
                case "VTYPE":
                    // Determine the type of VTYP
                    String vtypContent = getTextContentSafe(node, "VTYP");
                    char typeChar = typeof(vtypContent);
                    System.out.println("Type of VTYP: " + typeChar);
                    return typeChar == 'n' || typeChar == 't';
    
                case "ATOMIC":
                    // Handle ATOMIC case if needed
                    return true;
    
                // Add other cases as needed
            
                case "ASSIGN":
                    return false;
    
                
                    case "TERM":
                    // Determine the type of TERM
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                
                        if ("ATOMIC".equals(childSymbol)) {
                            return typeof(getTextContentSafe(childNode, "ATOMIC")) == typeof(getTextContentSafe(childNode, "ATOMIC"));
                        } else if ("CALL".equals(childSymbol)) {
                            return typeof(getTextContentSafe(childNode, "CALL")) == typeof(getTextContentSafe(childNode, "CALL"));
                        } else if ("OP".equals(childSymbol)) {
                            return typeof(getTextContentSafe(childNode, "OP")) == typeof(getTextContentSafe(childNode, "OP"));
                        }
                    }
                    return false;
                
    
                    case "CALL":
                    // Handle CALL case
                    Node fnameNode = getChildNodeByTagName(node, "FNAME");
                    Node atomic1Node = getChildNodeByTagName(node, "ATOMIC1");
                    Node atomic2Node = getChildNodeByTagName(node, "ATOMIC2");
                    Node atomic3Node = getChildNodeByTagName(node, "ATOMIC3");
                
                    if (fnameNode != null && atomic1Node != null && atomic2Node != null && atomic3Node != null) {
                        char atomic1Type = typeof(getTextContentSafe(atomic1Node, "ATOMIC1"));
                        char atomic2Type = typeof(getTextContentSafe(atomic2Node, "ATOMIC2"));
                        char atomic3Type = typeof(getTextContentSafe(atomic3Node, "ATOMIC3"));
                
                        if (atomic1Type == 'n' && atomic2Type == 'n' && atomic3Type == 'n') {
                            String fnameType = getTypeFromSymbolTable(getTextContentSafe(fnameNode, "FNAME"));
                           return typeof(fnameType) == 'n'; // Assuming functions return 'n'
                        } else {
                            return false;
                        }
                    }
                    return false;
                
            
    
                case "OP":
                // Handle OP case
                Node unopNode = getChildNodeByTagName(node, "UNOP");
                Node argNode = getChildNodeByTagName(node, "ARG");
            
                if (unopNode != null && argNode != null) {
                    char unopType = typeof(getTextContentSafe(unopNode, "UNOP"));
                    char argType = typeof(getTextContentSafe(argNode, "ARG"));
            
                    if (unopType == argType) {
                        if (unopType == 'b' || unopType == 'n') {
                            return true;
                        }
                    }
                    return false;
                }
            
                Node binopNode = getChildNodeByTagName(node, "BINOP");
                Node arg1Node = getChildNodeByTagName(node, "ARG1");
                Node arg2Node = getChildNodeByTagName(node, "ARG2");
            
                if (binopNode != null && arg1Node != null && arg2Node != null) {
                    char binopType = typeof(getTextContentSafe(binopNode, "BINOP"));
                    char arg1Type = typeof(getTextContentSafe(arg1Node, "ARG1"));
                    char arg2Type = typeof(getTextContentSafe(arg2Node, "ARG2"));
            
                    if (binopType == arg1Type && arg1Type == arg2Type) {
                        if (binopType == 'b' || binopType == 'n') {
                            return true;
                        } else if (binopType == 'c' && arg1Type == 'n' && arg2Type == 'n') {
                            return true;
                        }
                    }
                    return false;
                }
              
            
    
                case "ARG":
                // Handle ARG case
                for (Node childNode : studentInnerNodes) {
                    String childSymbol = getSymbol(childNode);
            
                    if ("ATOMIC".equals(childSymbol)) {
                        return typeof(getTextContentSafe(childNode, "ATOMIC")) == typeof(getTextContentSafe(childNode, "ATOMIC"));
                    } else if ("OP".equals(childSymbol)) {
                        return typeof(getTextContentSafe(childNode, "OP")) == typeof(getTextContentSafe(childNode, "OP"));
                    }
                }
                return false;
            
    
                case "UNOP":
                // Handle UNOP case
                String unopContent = getTextContentSafe(node, "UNOP");
                char unopType = typeof(unopContent);
                return unopType == 'b' || unopType == 'n';
            
    
                case "BINOP":
                // Handle BINOP case
                String binopContent = getTextContentSafe(node, "BINOP");
                char binopType = typeof(binopContent);
                return binopType == 'b' || binopType == 'n' || binopType == 'c';

    
                case "BRANCH":
                // Handle BRANCH case
                Node condNode = getChildNodeByTagName(node, "COND");
                Node algo1Node = getChildNodeByTagName(node, "ALGO1");
                Node algo2Node = getChildNodeByTagName(node, "ALGO2");

                if (condNode != null && algo1Node != null && algo2Node != null) {
                    char condType = typeof(getTextContentSafe(condNode, "COND"));

                    if (condType == 'b') {
                        return typeChecker(algo1Node) && typeChecker(algo2Node);
                    }
                    return false;
                }
                return false;

    
                case "COND":
                // Handle COND case
                for (Node childNode : studentInnerNodes) {
                    String childSymbol = getSymbol(childNode);
            
                    if ("SIMPLE".equals(childSymbol)) {

                        
                        return typeof(childNode) == typeof(getTextContentSafe(childNode, "SIMPLE"));
                    } else if ("COMPOSITE".equals(childSymbol)) {
                        return typeof(childNode) == typeof(getTextContentSafe(childNode, "COMPOSITE"));
                    }
                }
                return false;
            
    
                case "SIMPLE":
    // Handle SIMPLE case
                    binopNode = getChildNodeByTagName(node, "BINOP");
                   atomic1Node = getChildNodeByTagName(node, "ATOMIC1");
                     atomic2Node = getChildNodeByTagName(node, "ATOMIC2");

                if (binopNode != null && atomic1Node != null && atomic2Node != null) {
                     binopType = typeof(getTextContentSafe(binopNode, "BINOP"));
                    char atomic1Type = typeof(getTextContentSafe(atomic1Node, "ATOMIC1"));
                    char atomic2Type = typeof(getTextContentSafe(atomic2Node, "ATOMIC2"));

                    if (binopType == 'b' && atomic1Type == 'b' && atomic2Type == 'b') {
                        return true;
                    } else if (binopType == 'c' && atomic1Type == 'n' && atomic2Type == 'n') {
                        return true;
                    }
                    return false;
                }
                return false;

    
                case "COMPOSITE":
                // Handle COMPOSITE case
                binopNode = getChildNodeByTagName(node, "BINOP");
                Node simple1Node = getChildNodeByTagName(node, "SIMPLE1");
                Node simple2Node = getChildNodeByTagName(node, "SIMPLE2");

                if (binopNode != null && simple1Node != null && simple2Node != null) {
                    binopType = typeof(getTextContentSafe(binopNode, "BINOP"));
                    char simple1Type = typeof(getTextContentSafe(simple1Node, "SIMPLE1"));
                    char simple2Type = typeof(getTextContentSafe(simple2Node, "SIMPLE2"));

                    if (binopType == 'b' && simple1Type == 'b' && simple2Type == 'b') {
                        return true;
                    }
                    return false;
                }

                 unopNode = getChildNodeByTagName(node, "UNOP");
                Node simpleNode = getChildNodeByTagName(node, "SIMPLE");

                if (unopNode != null && simpleNode != null) {
                    unopType = typeof(getTextContentSafe(unopNode, "UNOP"));
                    char simpleType = typeof(getTextContentSafe(simpleNode, "SIMPLE"));

                    if (unopType == 'b' && simpleType == 'b') {
                        return true;
                    }
                    return false;
                }
                return false;

    
                case "FNAME":
                    return false;
    
                    case "FUNCTIONS":
                    // Base case: no children
                    if (studentInnerNodes.isEmpty()) {
                        return true;
                    }
                
                    // Recursive case: process DECL and FUNCTIONS2
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                
                        if ("DECL".equals(childSymbol)) {
                            boolean declCheck = typeChecker(childNode);
                            boolean functions2Check = typeChecker(childNode); // Assuming FUNCTIONS2 is the next sibling
                            return declCheck && functions2Check;
                        }
                    }
                    return false;
                
    
                    case "DECL":
                    // Process HEADER and BODY
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                
                        if ("HEADER".equals(childSymbol)) {
                            boolean headerCheck = typeChecker(childNode);
                            boolean bodyCheck = typeChecker(childNode); // Assuming BODY is the next sibling
                            return headerCheck && bodyCheck;
                        }
                    }
                    return false;
                
    
                    case "HEADER":
                    // Process FTYP, FNAME, and VNAMEs
                    Node ftypNode = getChildNodeByTagName(node, "FTYP");
                     fnameNode = getChildNodeByTagName(node, "FNAME");
                    Node vname1Node = getChildNodeByTagName(node, "VNAME1");
                    Node vname2Node = getChildNodeByTagName(node, "VNAME2");
                    Node vname3Node = getChildNodeByTagName(node, "VNAME3");
                
                    if (ftypNode != null && fnameNode != null && vname1Node != null && vname2Node != null && vname3Node != null) {
                        char ftypType = typeof(getTextContentSafe(ftypNode, "FTYP"));
                        String fname = getTextContentSafe(fnameNode, "FNAME");
                
                        // Update the symbol table
                        updateSymbolTable(fname, String.valueOf(ftypType));
                
                        char vname1Type = typeof(getTextContentSafe(vname1Node, "VNAME1"));
                        char vname2Type = typeof(getTextContentSafe(vname2Node, "VNAME2"));
                        char vname3Type = typeof(getTextContentSafe(vname3Node, "VNAME3"));
                
                        return ftypType == typeof(fname) && vname1Type == 'n' && vname2Type == 'n' && vname3Type == 'n';
                    }
                    return false;
                
                case "FTYP":
                    return false;
    
                    case "BODY":
                    // Process PROLOG, LOCVARS, ALGO, EPILOG, and SUBFUNCS
                    Node prologNode = getChildNodeByTagName(node, "PROLOG");
                    Node locvarsNode = getChildNodeByTagName(node, "LOCVARS");
                    Node algoNode = getChildNodeByTagName(node, "ALGO");
                    Node epilogNode = getChildNodeByTagName(node, "EPILOG");
                    Node subfuncsNode = getChildNodeByTagName(node, "SUBFUNCS");
                
                    if (prologNode != null && locvarsNode != null && algoNode != null && epilogNode != null && subfuncsNode != null) {
                        boolean prologCheck = typeChecker(prologNode);
                        boolean locvarsCheck = typeChecker(locvarsNode);
                         algoCheck = typeChecker(algoNode);
                        boolean epilogCheck = typeChecker(epilogNode);
                        boolean subfuncsCheck = typeChecker(subfuncsNode);
                
                        return prologCheck && locvarsCheck && algoCheck && epilogCheck && subfuncsCheck;
                    }
                    return false;
                
    
                case "PROLOG":
                    return false;
    
                case "EPILOG":
                    return false;
    
                    case "LOCVARS":
                    // Process VTYP and VNAME pairs
                    Node vtyp1Node = getChildNodeByTagName(node, "VTYP1");
                     vname1Node = getChildNodeByTagName(node, "VNAME1");
                    Node vtyp2Node = getChildNodeByTagName(node, "VTYP2");
                     vname2Node = getChildNodeByTagName(node, "VNAME2");
                    Node vtyp3Node = getChildNodeByTagName(node, "VTYP3");
                    vname3Node = getChildNodeByTagName(node, "VNAME3");
                
                    if (vtyp1Node != null && vname1Node != null && vtyp2Node != null && vname2Node != null && vtyp3Node != null && vname3Node != null) {
                        char vtyp1Type = typeof(getTextContentSafe(vtyp1Node, "VTYP1"));
                        String vname1 = getTextContentSafe(vname1Node, "VNAME1");
                        updateSymbolTable(vname1, String.valueOf(vtyp1Type));
                
                        char vtyp2Type = typeof(getTextContentSafe(vtyp2Node, "VTYP2"));
                        String vname2 = getTextContentSafe(vname2Node, "VNAME2");
                        updateSymbolTable(vname2, String.valueOf(vtyp2Type));
                
                        char vtyp3Type = typeof(getTextContentSafe(vtyp3Node, "VTYP3"));
                        String vname3 = getTextContentSafe(vname3Node, "VNAME3");
                        updateSymbolTable(vname3, String.valueOf(vtyp3Type));
                
                        return vtyp1Type == typeof(vname1) && vtyp2Type == typeof(vname2) && vtyp3Type == typeof(vname3);
                    }
                    return false;
                
    
                    case "SUBFUNCS":
                    // Process FUNCTIONS
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                
                        if ("FUNCTIONS".equals(childSymbol)) {
                            return typeChecker(childNode);
                        }
                    }
                    return false;
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    
        return false;
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
        // Determine if the id is a variable or a function
        if (id.startsWith("V_")) {
            // It's a variable, update the variable table
            Symbol symbol = new Symbol(id, type, "GLOBAL", null, "v" + vtable.size());
            vtable.put(id, symbol);
            System.out.println("Updated variable table: " + id + " -> " + type);
        } else if (id.startsWith("F_")) {
            // It's a function, update the function table
            Symbol symbol = new Symbol(id, type, "GLOBAL", new ArrayList<>(), "f" + ftable.size());
            ftable.put(id, symbol);
            System.out.println("Updated function table: " + id + " -> " + type);
        } else {
            System.out.println("Warning: Unknown id type for " + id);
        }
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
            default:
                System.out.println("Unknown content: " + content);
                return 'u'; // Undefined type
        }
    }
    
    
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

    private String getTypeFromSymbolTable(String name) {
        // Check in the variable table
        if (vtable.containsKey(name)) {
            return vtable.get(name).getType();
        }
    
        // Check in the function table
        if (ftable.containsKey(name)) {
            return ftable.get(name).getType();
        }
    
        System.out.println("Warning: " + name + " not found in the symbol table.");
        return null;
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
