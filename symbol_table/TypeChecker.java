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
    
            System.out.println("[DEBUG] Processing node with UNID: " + unid + ", Symbol: " + symbol);
            List<Node> studentInnerNodes = getChildrenNodes(node);  // children of the passed-in node
    
            System.out.println("[DEBUG] Number of child nodes: " + studentInnerNodes.size());
            List<Node> innerNodesOnly = studentInnerNodes.stream()
                    .filter(this::isInnerNode)
                    .collect(Collectors.toList());   // non-terminals
            List<Node> leafNodesOnly = studentInnerNodes.stream()
                    .filter(this::isLeafNode) 
                    .collect(Collectors.toList()); // terminals
    
            switch (symbol) {

                case "PROG":
                   
                    System.out.println("[DEBUG] Entered 'PROG' case");
    
                    boolean allChecksPass = true;
                    System.out.println("[DEBUG] Number of inner nodes: " + innerNodesOnly.size());
    
                    boolean globVarsCheck = false;
                    boolean algoCheck = false;
                    boolean functionsCheck = false;
    
                    for (Node studentNode : innerNodesOnly) {
                        String studentSymbol = getSymbol(studentNode);
                        System.out.println("[DEBUG] Processing inner node with Symbol: " + studentSymbol);
    
                        if (studentSymbol == null || studentSymbol.isEmpty()) {
                            System.out.println("[WARNING] Inner node symbol is null or empty");
                            continue;
                        }
    
                        switch (studentSymbol) {
                            case "GLOBVARS":
                                System.out.println("[DEBUG] Entering 'GLOBVARS' case for symbol: " + studentSymbol);
                                globVarsCheck = typeChecker(studentNode);
                                break;
                            case "ALGO":
                                System.out.println("[DEBUG] Entering 'ALGO' case for symbol: " + studentSymbol);
                                algoCheck = typeChecker(studentNode);
                                break;
                            case "FUNCTIONS":
                                System.out.println("[DEBUG] Entering 'FUNCTIONS' case for symbol: " + studentSymbol);
                                functionsCheck = typeChecker(studentNode);
                                break;
                            default:
                                System.out.println("[ERROR] Unknown symbol encountered: " + studentSymbol);
                                allChecksPass = false;
                                break;
                        }
    
                        if (!allChecksPass) {
                            System.out.println("[DEBUG] Exiting loop as a check failed for symbol: " + studentSymbol);
                            break;
                        }
                    }
    
                    allChecksPass = globVarsCheck && algoCheck && functionsCheck;
                    System.out.println("[DEBUG] Final check results: globVarsCheck=" + globVarsCheck +
                            ", algoCheck=" + algoCheck + ", functionsCheck=" + functionsCheck +
                            ", allChecksPass=" + allChecksPass);
    
                    return allChecksPass;
    
                case "GLOBVARS":
                    System.out.println("[DEBUG] Entered 'GLOBVARS' case");
    
                    if (studentInnerNodes.isEmpty()) {
                        System.out.println("[DEBUG] No child nodes under 'GLOBVARS'");
                        return true;
                    }
    
                    String id = "";
                    char type1 = ' ';
    
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                        System.out.println("[DEBUG] Processing child node in 'GLOBVARS' with Symbol: " + childSymbol);
    
                        if ("VTYP".equals(childSymbol)) {
                            List<Node> kid = getChildrenNodes(childNode);
                            System.out.println("[DEBUG] Processing first child of VTYP: " + getSymbol(kid.get(0)));
                            type1 = typeof(getSymbol(kid.get(0)));  // Let T := typeof VTYP
                        }
    
                        if ("VNAME".equals(childSymbol)) {
                            id = getTextContentSafe(childNode, "ID");
                        }
                    }
    
                    System.out.println("[DEBUG] VNAME detected with id: " + id);
    
                    if (type1 != ' ' && !id.isEmpty()) {
                        updateSymbolTable(id, type1);
                        System.out.println("[DEBUG] Symbol table updated with id=" + id + ", type=" + type1);
                        
                    }
                    return typeChecker(studentInnerNodes.get(3));
    
                case "ALGO":
                    System.out.println("[DEBUG] Entered 'ALGO' case");
    
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                        System.out.println("[DEBUG] Processing child node in 'ALGO' with Symbol: " + childSymbol);
    
                        if ("INSTRUC".equals(childSymbol)) {
                            System.out.println("[DEBUG] 'INSTRUC' found, recursively calling typeChecker");
                            return typeChecker(childNode);
                        }
                    }
    
                    System.out.println("[DEBUG] No 'INSTRUC' found in child nodes of 'ALGO'");
                    return false;
    
                case "INSTRUC":
                    if (studentInnerNodes.isEmpty()) {
                        System.out.println("[DEBUG] No children found in 'ALGO'");
                        return true;
                    }
    
                        System.out.println("[DEBUG] Processing child node with Symbol: " + getSymbol(studentInnerNodes.get(0)));
    
                            System.out.println("[DEBUG] 'COMMAND' detected, processing command node.");
                            boolean commandCheck = typeChecker(studentInnerNodes.get(0));
                            System.out.println("[DEBUG] Result of commandCheck: " + commandCheck);
                            System.out.println("[DEBUG] Processing child node with Symbol: " + getSymbol(studentInnerNodes.get(2)));
                            boolean instruc2Check = typeChecker(studentInnerNodes.get(2)); // Assuming INSTRUC2 is the next sibling
                           System.out.println("instruc symbol " + getSymbol(studentInnerNodes.get(2)) +"with ID " + getTextContent(studentInnerNodes.get(2),"UNID"));
                            System.out.println("[DEBUG] this " + instruc2Check);
    
                            return commandCheck && instruc2Check;   
                
    
                case "COMMAND":
                    System.out.println("[DEBUG] Entered 'COMMAND' case");
    
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                        System.out.println("[DEBUG Command] Processing child node with Symbol: " + childSymbol);
    
                        switch (childSymbol) {
                            case "skip":
                                System.out.println("[DEBUG] 'skip' command detected");
                                return true;
                            case "halt":
                                System.out.println("[DEBUG] 'halt' command detected");
                                return true;
                            case "print":
                                System.out.println("[DEBUG] 'print' command detected");
                               Node atomicNode = studentInnerNodes.get(1);
                                System.out.println("thee atomic node" + getSymbol(atomicNode));
                                if (atomicNode != null) {
                                    char atomicType = typeof(atomicNode);
                                    System.out.println("[DEBUG] Atomic type for 'print': " + atomicType);
                                    return atomicType == 'n' || atomicType == 't';
                                }
                                System.out.println("[DEBUG] No ATOMIC node found for 'print' command");
                                return false;
                            case "return":
                            System.out.println("[DEBUG] case 'return' command detected");
                                String scope = currentScope();
                             
                                String scopeId = getIdBySymbolName(scope);
                              
                                char functionType  = getTypeFromSymbolTable(scopeId);
                                System.out.println("scope :" + scope + ";scopeid: " + scopeId + " ,function type: " + functionType);

                                System.out.println("[DEBUG] 'return' command detected");
                                Node returnAtomicNode =studentInnerNodes.get(1);
                             

                                if (returnAtomicNode != null) {
                                    char atomicType = typeof(returnAtomicNode);
                                    System.out.println("[DEBUG] Atomic type for 'print': " + atomicType);
                                    return atomicType == functionType || atomicType == functionType;
                                }
                                System.out.println("[DEBUG] No ATOMIC node found for 'return' command");
                                return false;

                              
                            case "ASSIGN":
                                System.out.println("[DEBUG] 'ASSIGN' detected, processing assignment");
                                return typeChecker(childNode);
                            case "CALL":
                                System.out.println("[DEBUG] 'CALL' detected");
                               System.out.println("childnode value "+ getSymbol(childNode));
                               String callNodeId = getTextContent(childNode, "UNID"); // Replace "ID" with the actual identifier key used in your node structure
                               System.out.println("[DEBUG] CALL node UNID: " + callNodeId);
                               System.out.println("child symbol " + getSymbol(childNode) );
                                return typeof(childNode) == 'v';
                            case "BRANCH":
                                System.out.println("[DEBUG] 'BRANCH' detected, processing branch");
                                return typeChecker(childNode);
                            default:
                                System.out.println("[ERROR] Unknown COMMAND symbol: " + childSymbol);
                                return false;
                        }
                    }
                    return false;
    
                case "ASSIGN":
                    System.out.println("[DEBUG] Entered 'ASSIGN' case");
    
                    boolean inputCheck = false;
                    boolean termCheck = false;
    
                    for (Node childNode : studentInnerNodes) {
                        String childSymbol = getSymbol(childNode);
                        System.out.println("[DEBUG] Processing child node with Symbol: " + childSymbol);
    
                        if ("input".equals(childSymbol)) {
                            System.out.println("[DEBUG] 'input' command detected");
                            inputCheck = typeof(studentInnerNodes.get(0)) == 'n';
                        }
    
                        if ("TERM".equals(childSymbol)) {
                            termCheck = typeof(studentInnerNodes.get(0)) == typeof(childNode);
                        }
                    }
    
                    return inputCheck || termCheck;
    
                case "BRANCH":
                    System.out.println("[DEBUG] Entered 'BRANCH' case");
    
                    Node condNode = getChildNodeByTagName(node, "COND");
                    Node algo1Node = studentInnerNodes.get(3);
                    Node algo2Node = studentInnerNodes.get(5);
    
                    if (condNode != null && algo1Node != null && algo2Node != null) {
                        char condType = typeof(condNode);
                        if (condType == 'b') {
                            return typeChecker(algo1Node) && typeChecker(algo2Node);
                        }
                    }
    
                    System.out.println("[DEBUG] No valid 'COND', 'ALGO1', or 'ALGO2' found in 'BRANCH'");
                    return false;

                    case "HEADER":
                    // Determine the function type
                    char ftyp = typeof(studentInnerNodes.get(0));
                    System.out.println("[DEBUG] Function type (ftyp): " + ftyp);
                    
                    // Get children nodes of the function
                    List<Node> funNode = getChildrenNodes(studentInnerNodes.get(1));
                    String FuncId = getIdBySymbolName(getSymbol(funNode.get(0)));
                    System.out.println("[DEBUG] Function ID: " + FuncId);
                    enterScope(getSymbol(funNode.get(0)));
                    // Update the symbol table with the function ID and type
                    updateSymbolTable(FuncId, ftyp);
                    System.out.println("[DEBUG] Updated symbol table with FuncId: " + FuncId + " and ftyp: " + ftyp);

                    List<Node> var1 = getChildrenNodes(studentInnerNodes.get(2));
                    List<Node> var2 = getChildrenNodes(studentInnerNodes.get(3));
                    List<Node> var3 = getChildrenNodes(studentInnerNodes.get(4));
                    String var1Id = getIdBySymbolName(getSymbol(var1.get(0)));
                    String var2Id = getIdBySymbolName(getSymbol(var2.get(0)));
                    String var3Id = getIdBySymbolName(getSymbol(var3.get(0)));
                    char paramtype = 'n';
                    // Update the symbol table with the function ID and type
                    updateSymbolTable(var1Id, paramtype);
                    updateSymbolTable(var2Id, paramtype);
                    updateSymbolTable(var3Id, paramtype);
                    
                    // Check types of specific nodes
                    char type3 = typeof(studentInnerNodes.get(2));
                    char type5 = typeof(studentInnerNodes.get(3));
                    char type7 = typeof(studentInnerNodes.get(4));
                    
                    System.out.println("[DEBUG] Types of nodes: "
                        + "Type of node 3: " + type3 + ", "
                        + "Type of node 5: " + type5 + ", "
                        + "Type of node 7: " + type7);
                
                    // Check if all specified types are 'n'
                    if (type3 == 'n' && type5 == 'n' && type7 == 'n') {
                        System.out.println("[DEBUG] All types are 'n'. Returning true.");
                        return true;
                    } else {
                        System.out.println("[DEBUG] At least one type is not 'n'. Returning false.");
                        return false;
                    }
                
               
                    case "DECL":
                    System.out.println("[DEBUG] Entered 'DECL' case");
                    List<Node> declarechildren = getChildrenNodes(node);
                    if (declarechildren.size() < 2) {
                        System.out.println("[ERROR] Not enough children for DECL node. Expected at least 2, got: " + declarechildren.size());
                        return false; // Adjust return value as needed
                    }
                    boolean header = typeChecker(declarechildren.get(0));
                    boolean body = typeChecker(declarechildren.get(1));
                    System.out.println("[DEBUG] Header type check: " + header + ", Body type check: " + body);
                    return header && body;
                case "PROLOG":
                    return true;
                case "EPILOG":
                
                    return true;
                    case "LOCVARS":
                    System.out.println("[DEBUG] Entered 'LOCALVARS' case");
                
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
                     System.out.println( " the 3 symbols "+id1+ " " + id2 + " " + id3);
                     List<Node> kid1 = getChildrenNodes(studentInnerNodes.get(0));
                     List<Node> kid2 = getChildrenNodes(studentInnerNodes.get(2)); 
                     List<Node> kid3 = getChildrenNodes(studentInnerNodes.get(4));

                     type1 = typeof(getSymbol(kid1.get(0)));
                     type2 = typeof(getSymbol(kid2.get(0)));
                     type3 = typeof(getSymbol(kid3.get(0)));   
                
                    if (type1 != ' ' && !id1.isEmpty()) {
                        updateSymbolTable(id1, type1);
                        System.out.println("[DEBUG] Symbol table updated with id1=" + id1 + ", type=" + type1);
                    } else {
                        System.out.println("[DEBUG] Skipped updating symbol table for id1. Type: " + type1 + ", ID: " + id1);
                    }     
                
                    if (type2 != ' ' && !id2.isEmpty()) {
                        updateSymbolTable(id2, type2);
                        System.out.println("[DEBUG] Symbol table updated with id2=" + id2 + ", type=" + type2);
                    } else {
                        System.out.println("[DEBUG] Skipped updating symbol table for id2. Type: " + type2 + ", ID: " + id2);
                    }
                             
                    if (type3 != ' ' && !id3.isEmpty()) {
                        updateSymbolTable(id3, type3);
                        System.out.println("[DEBUG] Symbol table updated with id3=" + id3 + ", type=" + type3);
                    } else {
                        System.out.println("[DEBUG] Skipped updating symbol table for id3. Type: " + type3 + ", ID: " + id3);
                    }
                
                return true;

                case "BODY":
                    System.out.println("[DEBUG] Entered 'BODY' case");
                    boolean prolog = typeChecker(studentInnerNodes.get(0));
                    boolean locvar = typeChecker(studentInnerNodes.get(1)); // Corrected index
                    boolean algo = typeChecker(studentInnerNodes.get(2)); // Corrected index
                    boolean epilog = typeChecker(studentInnerNodes.get(3)); // Corrected index
                    boolean subfun = typeChecker(studentInnerNodes.get(4)); // Corrected index
                    System.out.println("[DEBUG] Prolog: " + prolog + ", Local Variable: " + locvar + ", Algorithm: " + algo +
                                       ", Epilog: " + epilog + ", Subfunctions: " + subfun);
                                       exitScope();
                    return prolog && locvar && algo && epilog && subfun;
                
                case "FUNCTIONS":
                    System.out.println("[DEBUG] Entered 'FUNCTIONS' case");
                    
                    if (studentInnerNodes.isEmpty()) {
                        System.out.println("[DEBUG] No child nodes under 'FUNCTIONS'");
                        return true;
                    }
                    functionsCheck = typeChecker(studentInnerNodes.get(0));
                    System.out.println("[DEBUG] Functions type check result: " + functionsCheck);
                    return functionsCheck;
                
                case "SUBFUNCS":
                    System.out.println("[DEBUG] Entered 'SUBFUNCS' case");
                    List<Node> FuncNode = getChildrenNodes(node);
                    
                    if (FuncNode.isEmpty()) {
                        System.out.println("[ERROR] No child nodes for SUBFUNCS.");
                        return false; // Adjust return value as needed
                    }
                    
                    boolean subFuncCheck = typeChecker(FuncNode.get(0));
                    System.out.println("[DEBUG] Subfunction type check result: " + subFuncCheck);
                    return subFuncCheck;
                
    
                default:
                    System.out.println("[ERROR] Unsupported node symbol: " + symbol);
                    return false;
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Exception in typeChecker: " + e.getMessage());
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
    private void updateSymbolTable(String id, char type) {
        System.out.println("Updating symbol for id: " + id);

        // Check if the id exists in vtable or ftable
        if (vtable.containsKey(id)) {
            // Update the existing variable entry
            Symbol existingVarSymbol = vtable.get(id);
            existingVarSymbol.setType(type); // Assuming there's a setType method in Symbol
            System.out.println("Updated variable table: " + id + " -> " + type);
            return; // Early exit after updating
        } else if (ftable.containsKey(id)) {
            // Update the existing function entry
            Symbol existingFuncSymbol = ftable.get(id);
            existingFuncSymbol.setType(type); // Assuming there's a setType method in Symbol
            System.out.println("Updated function table: " + id + " -> " + type);
            return; // Early exit after updating
        }
    }
    
    private char typeof(Node node) {
        try {
            String unid = getTextContent(node, "UNID"); 
            List<Node> children = getChildrenNodes(node);
            String symbol = getSymbol(node);
            
            // Debugging output for the current node
            System.out.println("[DEBUG] Processing node typeof with UNID: " + unid + ", Symbol: " + symbol);
            
            switch (symbol) {
                case "ATOMIC":
                List<Node> atomicChild = getChildrenNodes(node);
                return typeof(atomicChild.get(0));

                case "VNAME":
                String VarName = getIdBySymbolName(getSymbol(children.get(0)));
                System.out.println("[DEBUG] Function ID: " + VarName);
                char typ = getTypeFromSymbolTable(VarName);
                return typ;

                case "FNAME":
                    String funcName = getIdBySymbolName(getSymbol(children.get(0)));
                    System.out.println("[DEBUG] Function ID: " + funcName);
                    char ty = getTypeFromSymbolTable(funcName);
                    System.out.println("returning the function type " + ty);
                    return ty;
    
                case "TERM":
                    System.out.println("[DEBUG] Node is of type TERM");
    
                    // Loop through the children
                    for (Node child : children) {
                        String childSymbol = getSymbol(child);
                        System.out.println("[DEBUG] Processing child node with symbol: " + childSymbol);
    
                        // Check if child symbol is "ATOMIC"
                        if ("ATOMIC".equals(childSymbol)) {
                            List<Node> atomic = getChildrenNodes(child);
                            char atomicType = typeof(atomic.get(0));
                            System.out.println("[DEBUG] Atomic type: " + atomicType);
                            return atomicType;
                        }
    
                        // Check if child symbol is "CALL"
                        if ("CALL".equals(childSymbol)) {
                            List<Node> call = getChildrenNodes(child);
                            System.out.println("call node" + getSymbol(call.get(0)));
                            char callType = typeof(call.get(0));
                            System.out.println("[DEBUG] CALL type: " + callType);
                            return callType;
                        }
    
                        if ("OP".equals(childSymbol)) {
                            char callType = typeof(child);
                            System.out.println("[DEBUG] OP type: " + callType);
                            return callType;
                        }
                    }
                    break;
    
                case "CALL":
                    System.out.println("[DEBUG] Node is of type CALL");
    
                    // Get the children of the CALL node
                    List<Node> childrenNode = getChildrenNodes(node);
                    System.out.println("[DEBUG] CALL Children size: " + childrenNode.size());
            
    
                    Node fnameNode = childrenNode.get(0); // FNAME
                    Node atomic1Node = childrenNode.get(1); // ATOMIC1
                    Node atomic2Node = childrenNode.get(2); // ATOMIC2
                    Node atomic3Node = childrenNode.get(3); // ATOMIC3
    
                    char funtype = typeof(fnameNode); // Type of function name
                    char atomic1Type = typeof(getChildrenNodes(atomic1Node).get(0));
                    char atomic2Type = typeof(getChildrenNodes(atomic2Node).get(0));
                    char atomic3Type = typeof(getChildrenNodes(atomic3Node).get(0));
    
                    System.out.println("[DEBUG] Types of parameters: "
                            + "FNAME: " + funtype + ", "
                            + "ATOMIC1: " + atomic1Type + ", "
                            + "ATOMIC2: " + atomic2Type + ", "
                            + "ATOMIC3: " + atomic3Type);
    
                    if (atomic1Type == 'n' && atomic2Type == 'n' && atomic3Type == 'n') {
                        return funtype;  // typeof(CALL) = typeof(FNAME)
                    } else {
                        System.out.println("[DEBUG] Not all parameters are numeric");
                        return 'u';  // typeof(CALL) = 'u'
                    }
    
                    case "OP":
                    System.out.println("[DEBUG] Node is of type OP");
                    List<Node> opchildren = getChildrenNodes(node);//binp or unop
                    String operation =getSymbol(getChildrenNodes(opchildren.get(0)).get(0));
                    System.out.println(getTextContentSafe(opchildren.get(0),"UNID"));
                    List<Node> ren = getChildrenNodes(opchildren.get(0)); //binop children

                    if (getSymbol(children.get(0)) .equals( "UNOP")) {
                        System.out.println("[DEBUG] Detected UNOP operator");
                
                        Node unopNode = opchildren.get(0); // UNOP
                        Node argNode = ren.get(1); // ARG
                
                        char unopType = typeof(operation);
                        char argType = typeof(argNode);
                
                        System.out.println("[DEBUG] Types: UNOP: " + unopType + ", ARG: " + argType);
                
                        if (unopType == 'b' && argType == 'b') {
                            System.out.println("[DEBUG] Both UNOP and ARG are boolean");
                            return 'b';  // typeof(OP) = 'b'
                        } else if (unopType == 'n' && argType == 'n') {
                            System.out.println("[DEBUG] Both UNOP and ARG are numeric");
                            return 'n';  // typeof(OP) = 'n'
                        } else {
                            System.out.println("[DEBUG] UNOP or ARG is undefined");
                            return 'u';  // typeof(OP) = 'u'
                        }
                    } else if (getSymbol(children.get(0)).equals( "BINOP")) {
                        System.out.println("[DEBUG] Detected BINOP operator");
                
                     
                        System.out.println("binop " +children.size());
                        Node argNode1 = ren.get(1); 
                        System.out.println(getSymbol(argNode1));
                        Node argNode2 = ren.get(2); 
                        System.out.println("arg2 ");
                
                        char binopType = typeof(operation);
                        char argType1 = typeof(argNode1);
                        char argType2 = typeof(argNode2);
                
                        System.out.println("[DEBUG] Types: BINOP: " + binopType + ", ARG1: " + argType1 + ", ARG2: " + argType2);
                
                        if (binopType == 'b' && argType1 == 'b' && argType2 == 'b') {
                            System.out.println("[DEBUG] BINOP and both arguments are boolean");
                            return 'b'; // bool-type
                        } else if (binopType == 'n' && argType1 == 'n' && argType2 == 'n') {
                            System.out.println("[DEBUG] BINOP and both arguments are numeric");
                            return 'n'; // numeric type
                        } else if (binopType == 'c' && argType1 == 'n' && argType2 == 'n') {
                            System.out.println("[DEBUG] BINOP is comparison-type, both arguments are numeric");
                            return 'b'; // comparison-type, yields a boolean result
                        } else {
                            System.out.println("[DEBUG] BINOP or arguments are undefined");
                            return 'u'; // undefined type
                        }
                    }
                
                case "ARG":
                    System.out.println("[DEBUG] Node is of type ARG");
    
                    if (children.size() < 1) {
                        System.out.println("[ERROR] No children for ARG node.");
                        return 'u';  // Return 'u' if no children
                    }
    
                    Node argChild = children.get(0);
                    System.out.println("argumennt symb "+getSymbol(argChild));
                    char argType = typeof(argChild);
                    System.out.println("[DEBUG] Type of ARG: " + argType);
    
                    if (getSymbol(argChild).equals("ATOMIC")) {
                        return argType;
                    } else if (getSymbol(argChild).equals("OP")) {
                        return typeof(argChild);
                    }
                    break;
    
                case "CONST":
                List<Node> constValueNode = getChildrenNodes(node);
                     System.out.println("the value of constant :" + getSymbol(constValueNode.get(0))); 
                     if (isNumber(getSymbol((constValueNode.get(0))))){
                        return 'n';
                     }
                     else return 't';
                    
                case "FTYP":
                    List<Node> ftypChildren = getChildrenNodes(node);
                    if (ftypChildren.size() < 1) {
                        System.out.println("[ERROR] No children for FTYP node.");
                        return 'u';  // Return 'u' if no children
                    }
    
                    Node ftypChild = ftypChildren.get(0); // num or void
                    String ftypChildSymbol = getSymbol(ftypChild);
                    System.out.println("[DEBUG] FTYP child symbol: " + ftypChildSymbol);
    
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
        System.out.println("Warning: Function type not found for node.");
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

    private Node getChildNode(Node parentNode, String tagName) {
        if (parentNode == null) {
            System.out.println("Warning: Parent node is null for tag: " + tagName);
            return null;
        }
        Element element = (Element) parentNode;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() == 0) {
            System.out.println("Warning: No elements found for tag: " + tagName);
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
    private char getTypeFromSymbolTable(String name) {
        System.out.println("name #####"+ name);
        // Check in the variable table
        if (vtable.containsKey(name)) {

            System.out.println("name #####"+ name);
            return vtable.get(name).getType();
        }
    
        // Check in the function table
        if (ftable.containsKey(name)) {
            return ftable.get(name).getType();
        }
    
        System.out.println("Warning: " + name + " not found in the symbol table.");
        return ' ';
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
        System.out.println("Searching for symbol name: " + symbolName);
    
        if (vtable != null) {
            for (Map.Entry<String, Symbol> entry : vtable.entrySet()) {
                System.out.println("Checking vtable entry: " + entry.getValue().getName());
                if (entry.getValue().getSymb().equals(symbolName)) {
                    System.out.println("Found in vtable: " + entry.getKey());
                    return entry.getKey(); // Return the ID (key) associated with the symbol
                }
            }
        } else {
            System.out.println("vtable is null.");
        }
    
        // Optionally search in ftable if needed
        if (ftable != null) {
            for (Map.Entry<String, Symbol> entry : ftable.entrySet()) {
                System.out.println("Checking ftable entry: " + entry.getValue().getName());
                if (entry.getValue().getSymb().equals(symbolName)) {
                    System.out.println("Found in ftable: " + entry.getKey());
                    return entry.getKey(); // Return the ID (key) associated with the symbol
                }
            }
        } else {
            System.out.println("ftable is null.");
        }
    
        System.out.println("Symbol name not found: " + symbolName);
        return null; // Return null if not found in both tables
    }
    
    private void enterScope(String scopeName) {
        scopeStack.push(scopeName); // Push the new scope onto the stack
        System.out.println("Entering scope : " + scopeName);

       
    }

    private void exitScope() {
        if (scopeStack.size() == 0) {
            return;
        }
        String exitedScope = scopeStack.pop(); // Remove the current scope

        System.out.println("xiting  scope : " + exitedScope);

    }

    private String currentScope() {
        if (scopeStack.size() == 0) {
            return " ";
        }
        System.out.println("getting current scope : " );
        return scopeStack.peek(); // Get the current scope from the stack
    }
    

    private static Document parseXML(File xmlFile) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xmlFile);
    }


}
