package parser;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

class Parser extends JFrame {
    private int currIndex = 0; // Current token index
    private static int curr = 0; // Current unique ID
    private List<Token> tok; // List of tokens
    private int lastValidPosition;
    private int endcases = 0; //

    public Parser(List<Token> tokens) { // Constructor
        this.tok = tokens;
    }

    private Token getCurrentToken() { // Get current token from token stream
        if (currIndex < tok.size()) {
            return tok.get(currIndex);
        }
        return null;
    }

    private void consumeToken() { // Move to next token in the token stream
        currIndex++;
    }

    public static int generateUniqueId() { // Generate unique ID for each node
        return curr++;
    }

    public static void parseInit() throws Exception {
        List<Token> tokens = XMLconvert("lexer/output.xml"); // Parse XML file to get tokens
                        Parser parser = new Parser(tokens); // Create parser object
        Node syntaxTree = parser.parse(); // Parse tokens to generate syntax tree
        SwingUtilities.invokeLater(() -> { // Display syntax tree
            TreeVisualizer frame = new TreeVisualizer(syntaxTree); // Create TreeVisualizer object
            frame.setVisible(true); // Set frame visibility
        });
        syntaxTree.print(""); // Print syntax tree
        String xml = TreeToXML(syntaxTree); // Convert syntax tree to XML
        
         writeXMLToFile(xml, "syntax_tree.xml"); // Write syntax tree to XML file
    }

    public static void main(String[] args) throws Exception {
        parseInit(); // Initialize parsing
    }
    //region: XML conversion
    public static String TreeToXML(Node root) { // Convert syntax tree to XML for semantic table
        StringBuilder xml = new StringBuilder(); // Create StringBuilder object
        xml.append("<SYNTREE>\n"); // Append SYNTREE tag

        appendRootNodeXML(root, xml, 1); // Append root node to XML

        // Collect inner nodes and leaf nodes separately
        StringBuilder innerNodesXML = new StringBuilder();
        StringBuilder leafNodesXML = new StringBuilder();

        innerNodesXML.append(indent("<INNERNODES>\n", 1)); // Start INNERNODES tag
        leafNodesXML.append(indent("<LEAFNODES>\n", 1)); // Start LEAFNODES tag

        for (Node child : root.getChildren()) {
            collectInnerNodesXML(child, innerNodesXML, 2); // Collect inner nodes
            collectLeafNodesXML(child, leafNodesXML, 2); // Collect leaf nodes
        }

        innerNodesXML.append(indent("</INNERNODES>\n", 1)); // End INNERNODES tag
        leafNodesXML.append(indent("</LEAFNODES>\n", 1)); // End LEAFNODES tag

        // Append collected inner nodes and leaf nodes
        xml.append(innerNodesXML.toString());
        xml.append(leafNodesXML.toString());

        xml.append("</SYNTREE>\n"); // Append closing SYNTREE tag

        return xml.toString(); // Return XML string
    }

    private static void appendRootNodeXML(Node root, StringBuilder xml, int level) { // Append root node to XML (PROG)
        if (root == null) return; // Check for null root

        xml.append(indent("<ROOT>\n", level)); // Append ROOT tag
        xml.append(indent("<UNID>" + root.getId() + "</UNID>\n", level + 1)); // Append UNID tag
        xml.append(indent("<SYMB>" + root.getName() + "</SYMB>\n", level + 1)); // Append SYMB tag
        xml.append(indent("<CHILDREN>\n", level + 1)); // Append CHILDREN tag

        for (Node child : root.getChildren()) { // Iterate through children
            xml.append(indent("<ID>" + child.getId() + "</ID>\n", level + 2)); // Append ID tag
        } // End iteration

        xml.append(indent("</CHILDREN>\n", level + 1)); // Append closing CHILDREN tag
        xml.append(indent("</ROOT>\n", level)); // Append closing ROOT tag
    }

    private static void collectInnerNodesXML(Node node, StringBuilder xml, int level) { // Collect inner nodes to XML
        if (node == null || (node.isTerminal() && !node.getName().matches("[A-Z]+"))) return; // Check for null or terminal node

        appendInnerNodeDetailsXML(node, xml, level); // Append inner node details to XML
    }

    private static void appendInnerNodeDetailsXML(Node node, StringBuilder xml, int level) { // Append inner node details to XML (NON-TERMINALS)
        if (node == null || (node.isTerminal() && !node.getName().matches("[A-Z]+"))) return; // Check for null or terminal node

        xml.append(indent("<IN>\n", level)); // Append IN tag
        xml.append(indent("<PARENT>" + node.getParent().getId() + "</PARENT>\n", level + 1)); // Append PARENT tag
        xml.append(indent("<UNID>" + node.getId() + "</UNID>\n", level + 1)); // Append UNID tag
        xml.append(indent("<SYMB>" + node.getName() + "</SYMB>\n", level + 1)); // Append SYMB tag
        xml.append(indent("<CHILDREN>\n", level + 1)); // Append CHILDREN tag

        for (Node child : node.getChildren()) { // Iterate through children
            xml.append(indent("<ID>" + child.getId() + "</ID>\n", level + 2)); // Append ID tag
        } // End iteration

        xml.append(indent("</CHILDREN>\n", level + 1)); // Append closing CHILDREN tag
        xml.append(indent("</IN>\n", level)); // Append closing IN tag

        for (Node child : node.getChildren()) { // Iterate through children
            appendInnerNodeDetailsXML(child, xml, level + 1); // Append inner node details to XML
        } // End iteration
    }

    private static void collectLeafNodesXML(Node node, StringBuilder xml, int level) { // Collect leaf nodes to XML (TERMINALS)
        if (node == null) return; // Check for null node

        if (node.isTerminal() && !node.getName().matches("[A-Z]+")) { // If node is terminal and not a non-terminal symbol
            appendLeafNodeDetailsXML(node, xml, level); // Append leaf node details to XML
        } else { // If node is not terminal or is a non-terminal symbol
            for (Node child : node.getChildren()) { // Iterate through children
                collectLeafNodesXML(child, xml, level + 1); // Collect leaf nodes to XML
            } // End iteration
        }
    }

    private static void appendLeafNodeDetailsXML(Node node, StringBuilder xml, int level) { // Append leaf node details to XML
        if (node == null) return; // Check for null node

        xml.append(indent("<LEAF>\n", level)); // Append LEAF tag
        xml.append(indent("<PARENT>" + node.getParent().getId() + "</PARENT>\n", level + 1)); // Append PARENT tag
        xml.append(indent("<UNID>" + node.getId() + "</UNID>\n", level + 1)); // Append UNID tag
        xml.append(indent("<TERMINAL>" + node.getName() + "</TERMINAL>\n", level + 1)); // Append TERMINAL tag
        xml.append(indent("</LEAF>\n", level)); // Append closing LEAF tag
    }

    private static String indent(String text, int level) { // Indent text for XML formatting
        StringBuilder indentedText = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indentedText.append("    "); // 4 spaces for each level
        }
        indentedText.append(text);
        return indentedText.toString();
    }

    private static void writeXMLToFile(String xml, String filename) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            
            writer.write(xml); // Write XML to file
        }
    }
    //endregion

    //region: read in XML tokens stream
    private static List<Token> XMLconvert(String filename) throws Exception { // Convert XML to tokens
        Document doc = parseXMLFile(filename); // Parse XML file
        NodeList tokenNodes = doc.getElementsByTagName("TOK"); // Get token nodes
        return convertTokenNodes(tokenNodes); // Convert token nodes
    }

    private static Document parseXMLFile(String filename) throws Exception { // Parse XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); // Create DocumentBuilderFactory object
        DocumentBuilder builder = factory.newDocumentBuilder(); // Create DocumentBuilder object
        return builder.parse(new File(filename)); // Parse XML file
    }

    private static List<Token> convertTokenNodes(NodeList tokenNodes) { // Convert token nodes
        List<Token> tokens = new ArrayList<>(); // Create list of tokens
        for (int i = 0; i < tokenNodes.getLength(); i++) { // Iterate through token nodes
            Element tokenElement = (Element) tokenNodes.item(i); // Get token element
            tokens.add(convertTokenNode(tokenElement)); // Convert token node
        }
        return tokens;
    }

    private static Token convertTokenNode(Element tokenElement) { // Convert token node
        int id = Integer.parseInt(tokenElement.getElementsByTagName("ID").item(0).getTextContent()); // Get token ID
        String tokenClass = tokenElement.getElementsByTagName("CLASS").item(0).getTextContent(); // Get token class
        String word = tokenElement.getElementsByTagName("WORD").item(0).getTextContent(); // Get token word
        return new Token(id, tokenClass, word); // Return token object
    }
    //endregion
    
    //region: Parser
    private void expect(String tokenClass) throws Exception {
        if (getCurrentToken() == null) {        // Check if current token is null
            throw new Exception("Expected token class: " + tokenClass + " but found: null");        // Throw exception
        }
        if (!getCurrentToken().getTokenClass().equals(tokenClass)) {        // Check if current token class is not equal to expected token class
            throw new Exception("Expected token class: " + tokenClass + " but found: " + getCurrentToken().getTokenClass());        // Throw exception
        }
        consumeToken();     // if token class exists and is expected, consume token
    }

    public Node parse() throws Exception {
        return parsePROG();     // Parse PROG
       //);
    }

    private Node parsePROG() throws Exception {     // Parse PROG -> main GLOBVARS ALGO FUNCTIONS
        Node progNode = new Node("PROG");       // Create PROG node
        Token token = getCurrentToken();        // Get current token
        if (token != null) {        // Check if token is not null
            switch (token.getWord()) {
                case ("main"):      // Check if token is 'main'
                    progNode.addChild(new Node("main"));        // Add 'main' to PROG node
                    consumeToken();    // Consume token
                    break;
                default:
                    throw new Exception("Expected 'main' but found: " + (token != null ? token.getWord() : "null"));        // Throw exception
            }
        }
        progNode.addChild(parseGLOBVARS());     // Add GLOBVARS to PROG node
                progNode.addChild(parseALGO());     // Add ALGO to PROG node
              progNode.addChild(parseFUNCTIONS());        // Add FUNCTIONS to PROG node
       
        return progNode;
    }

    private Node parseGLOBVARS() throws Exception {         // Parse GLOBVAR -> ε, GLOBVARS -> VTYP VNAME , GLOBVARS
        Node globvarsNode = new Node("GLOBVARS");           // Create GLOBVARS node
        Token currentToken = getCurrentToken();     // Get current token
        
        if (currentToken != null) {
            switch (currentToken.getWord()) {       
                case "num":
                case "text":
                    globvarsNode.addChild(parseVTYP());     // Add VTYP to GLOBVARS node
                    globvarsNode.addChild(parseVNAME());    // Add VNAME to GLOBVARS node
                    currentToken = getCurrentToken();
                    if (currentToken != null && currentToken.getTokenClass().equals("comma")) {     // Check if current token is comma
                        globvarsNode.addChild(new Node(","));       // Add comma to GLOBVARS node
                        consumeToken();     // Consume token
                        globvarsNode.addChild(parseGLOBVARS());    // Add GLOBVARS to GLOBVARS node
                    }
                    break;
                default:
                    // Do nothing, epsilon production
                    break;
            }
        }
        return globvarsNode;
    }

    private Node parseVTYP() throws Exception {     // Parse VTYP -> num, VTYP -> text
        Node vtypNode = new Node("VTYP");       // Create VTYP node
        Token token = getCurrentToken();        // Get current token
        
        if (token != null) {
            switch (token.getWord()) {
                case "num":
                case "text":
                    vtypNode.addChild(new Node(token.getWord()));       // Add token to VTYP node
                    consumeToken();     // Consume token
                    break;
                default:
                    throw new Exception("Expected VTYP but found: " + token.getWord());     // Throw exception
            }
        } else {
            throw new Exception("Expected VTYP but found: null");
        }
        
        return vtypNode;    
    }
    private Node parseVNAME() throws Exception {    // Parse VNAME -> a token of Token-class V from the lexer
        Node vNameNode = new Node("VNAME"); 
        Token token = getCurrentToken();
        if (token != null) {
            vNameNode.addChild(new Node(token.getWord()));
            consumeToken();
        } else {
            throw new Exception("Expected VNAME but found: " + (token != null ? token.getWord() : "null"));
        }
        return vNameNode;
    }

    private Node parseALGO() throws Exception {
                // Parse ALGO -> begin INSTRUC end
        Node algoNode = new Node("ALGO");
        Token token = getCurrentToken();
    
        // Check for 'begin' token
        if (token != null && "begin".equals(token.getWord())) {
                        algoNode.addChild(new Node("begin"));  // Add 'begin' to ALGO node
            consumeToken();  // Move to the next token
        } else {
            throw new Exception("Expected 'begin' but found: " + (token != null ? token.getWord() : "null"));
        }
    
        // Parse INSTRUC part
                algoNode.addChild(parseINSTRUC());  // Parse INSTRUC and add it to ALGO node
                 // Check for 'end' token
        token = getCurrentToken();
                if (token != null && "end".equals(token.getWord())) {

            algoNode.addChild(new Node("end"));  // Add 'end' to ALGO node
                                             if(tok.size() == currIndex + 1){
                         //  consumeToken();

           }
           else{
            consumeToken();
           }
        
              // Move to the next token
                    } else {
            throw new Exception("Expected 'end' but found: " + (token != null ? token.getWord() : "null"));
        }
    
                return algoNode;
    }
    

    private Node parseINSTRUC() throws Exception {      // Parse INSTRUC -> ε, INSTRUC -> COMMAND ; INSTRUC
        Node instrucNode = new Node("INSTRUC");
                Token currentToken = getCurrentToken();     
    
        if (currentToken != null) {
            switch (currentToken.getWord()) {
                case "end":
                                    // Do nothing, return empty INSTRUC node, INSTRUC -> ε
                    
                    break;
                default:
                                    instrucNode.addChild(parseCOMMAND());
                    currentToken = getCurrentToken();
                    if (currentToken != null && currentToken.getTokenClass().equals("semicolon")) {
                        instrucNode.addChild(new Node(";"));
                        consumeToken();
                        instrucNode.addChild(parseINSTRUC());
                    }
                    break;
            }
        }
    
        return instrucNode;
    }

    private Node parseCOMMAND() throws Exception {      // Parse COMMAND -> skip, COMMAND -> halt, COMMAND -> print ATOMIC, COMMAND -> ASSIGN, COMMAND -> CALL, COMMAND -> BRANCH
        Node commandNode = new Node("COMMAND");
        Token token = getCurrentToken();
        if (token != null) {
            switch (token.getWord()) {
                case "skip":        // COMMAND -> skip
                case "halt":        // COMMAND -> halt
                    commandNode.addChild(new Node(token.getWord()));
                    consumeToken();
                    break;
                case "print":       // COMMAND -> print ATOMIC
                    commandNode.addChild(new Node("print"));
                    consumeToken();
                    commandNode.addChild(parseATOMIC());
                    break;
                case "return":      // COMMAND -> return ATOMIC
                    commandNode.addChild(new Node("return"));
                    consumeToken();
                    commandNode.addChild(parseATOMIC());
                    break;
                case "if":          // COMMAND -> BRANCH
                    commandNode.addChild(parseBRANCH());
                    break;
                default:
                    switch (token.getTokenClass()) {
                        case "V":       // COMMAND -> ASSIGN
                                                    commandNode.addChild(parseASSIGN());
                            break;
                        case "F":       // COMMAND -> CALL
                            commandNode.addChild(parseCALL());
                            break;
                        default:
                            throw new Exception("Expected COMMAND but found: " + token.getWord());
                    }
            }
        }
        return commandNode;
    }


    private Node parseATOMIC() throws Exception {           
        System.out.println("Entering parseATOMIC...");  // Debugging
        
        Node atomicNode = new Node("ATOMIC");
        Token token = getCurrentToken();
    
        if (token != null) {
            System.out.println("Current token in parseATOMIC: " + token.getWord());  // Debugging
            System.out.println("Token class: " + token.getTokenClass());  // Debugging
        } else {
            System.out.println("Current token is null in parseATOMIC");  // Debugging
        }
    
        if (token != null) {
            switch (token.getTokenClass()) {
                case "V":       // ATOMIC -> VNAME
                    System.out.println("Token is a variable (V), parsing VNAME...");  // Debugging
                    atomicNode.addChild(parseVNAME());
                    break;
                case "N":
                case "T":       // ATOMIC -> CONST
                    System.out.println("Token is a constant (N or T), parsing CONST...");  // Debugging
                    atomicNode.addChild(parseCONST());
                    break;
                default:
                    String errorMessage = "Expected ATOMIC but found: " + token.getWord();
                    System.out.println(errorMessage);  // Debugging
                    throw new Exception(errorMessage);
            }
        } else {
            System.out.println("Error: Token is null in parseATOMIC");  // Debugging
        }
    
        System.out.println("Exiting parseATOMIC...");  // Debugging
        return atomicNode;
    }
    


    private Node parseCONST() throws Exception {    // Parse CONST -> a token of Token-class N from the lexer, CONST -> a token of Token-class T from the lexer
        Node constNode = new Node("CONST");
        Token token = getCurrentToken();
        if (token != null) {
            constNode.addChild(new Node(token.getWord()));
            consumeToken();
        } else {
            throw new Exception("Expected constNode but found: " + (token != null ? token.getWord() : "null"));
        }
        return constNode;
    }


    private Node parseASSIGN() throws Exception {       // Parse ASSIGN -> VNAME < input, ASSIGN -> VNAME = TERM
        Node assignNode = new Node("ASSIGN");
        assignNode.addChild(parseVNAME());
        Token token = getCurrentToken();
                if (token != null) {
            switch (token.getWord()) {
                case "< input":     // ASSIGN -> VNAME < input
                    consumeToken();
                    assignNode.addChild(new Node("< input"));
                    break;
                case "=":           // ASSIGN -> VNAME = TERM
                    assignNode.addChild(new Node("="));
                    consumeToken();
                    assignNode.addChild(parseTERM());
                    break;
                default:
                    throw new Exception("Expected < input or = but found: " + token.getWord());
            }
        } else {
            throw new Exception("Expected < input or = but found: null");
        }
        
        return assignNode;
    }

    private Node parseCALL() throws Exception {     // Parse CALL -> FNAME ( ATOMIC , ATOMIC , ATOMIC )
        Node callNode = new Node("CALL");
        callNode.addChild(parseFNAME());    // Add FNAME to CALL node
        expect("lparen");       // Add ( to CALL node
        callNode.addChild(parseATOMIC());   // Add ATOMIC to CALL node
        expect("comma");        // Add , to CALL node
        callNode.addChild(parseATOMIC());   // Add ATOMIC to CALL node
        expect("comma");        // Add , to CALL node   
        callNode.addChild(parseATOMIC());   // Add ATOMIC to CALL node
        expect("rparen");       // Add ) to CALL node
        return callNode;
    }

    private Node parseBRANCH() throws Exception {       // Parse BRANCH -> if COND then ALGO else ALGO
        Node branchNode = new Node("BRANCH");
        expect("reserved_keyword"); // if
        branchNode.addChild(parseCOND());
                expect("reserved_keyword"); // then
                branchNode.addChild(parseALGO());
                expect("reserved_keyword"); // else
        branchNode.addChild(parseALGO());
        return branchNode;
    }

    private Node parseTERM() throws Exception {     // Parse TERM -> ATOMIC, TERM -> CALL, TERM -> OP
                Node termNode = new Node("TERM");
        Token token = getCurrentToken();
        
        if (token != null) {
            switch (token.getTokenClass()) {
                case "V":
                case "N":
                case "T":       // TERM -> ATOMIC
                    termNode.addChild(parseATOMIC());
                    break;
                case "F":       // TERM -> CALL
                    termNode.addChild(parseCALL());
                    break;
                default:    // TERM -> OP
                                    termNode.addChild(parseOP());
                    break;
            }
        }
        
        return termNode;
    }

    private Node parseOP() throws Exception {       // Parse OP -> UNOP ( ARG ), OP -> BINOP ( ARG , ARG )
        Node opNode = new Node("OP");
        Token token = getCurrentToken();
        if (token != null) {
            switch (token.getWord()) {
                case "not":
                case "sqrt":    // OP -> UNOP ( ARG )
                    opNode.addChild(parseUNOP());
                    break;
                default:       // OP -> BINOP ( ARG , ARG )
                    opNode.addChild(parseBINOP());
                    expect("lparen");
                    opNode.addChild(parseARG());
                    expect("comma");
                    opNode.addChild(parseARG());
                    expect("rparen");
                    break;
            }
        }
        
        return opNode;
    }


    private Node parseARG() throws Exception {      // Parse ARG -> ATOMIC, ARG -> OP
        Node argNode = new Node("ARG");
        Token token = getCurrentToken();
        
        if (token != null) {
            switch (token.getTokenClass()) {
                case "V":
                case "N":
                case "T":    // ARG -> ATOMIC
                    argNode.addChild(parseATOMIC());
                    break;
                default:     // ARG -> OP
                    argNode.addChild(parseOP());
                    break;
            }
        }
        
        return argNode;
    }

    //TODO: make unamibiguous
private Node parseCOND() throws Exception {
        Node condNode = new Node("COND");
    Token token = getCurrentToken();
        switch (token.getWord()) {
        case "sqrt":
        case "not":
        condNode.addChild(parseCOMPOSIT());
      
            break;
        default:
                        // Attempt to parse as SIMPLE
            try {
                                condNode.addChild(parseSIMPLE());
            } catch (Exception e) {
                                // If parsing SIMPLE fails, we need to backtrack
                                // Reset the position to try COMPOSIT
                resetPosition();
                condNode.addChild(parseCOMPOSIT());
            }
    }
    
        return condNode;
}

// Method to reset the position to the last valid state
private void resetPosition() {
        this.currIndex = this.currIndex - (lastValidPosition+2); // lastValidPosition should be set before calling parseSIMPLE
    }

private Node parseSIMPLE() throws Exception {    // Parse SIMPLE -> BINOP
        Node simpleNode = new Node("SIMPLE");

            simpleNode.addChild(parseBINOP());
    expect("lparen");
    simpleNode.addChild(parseATOMIC());
    expect("comma");
    simpleNode.addChild(parseATOMIC());
    expect("rparen");
    return simpleNode;
}

private Node parseCOMPOSIT() throws Exception {     // Parse COMPOSIT -> BINOP, COMPOSIT -> UNOP
        Node compositNode = new Node("COMPOSIT");
    Token token = getCurrentToken();
        if (token != null) {
        switch (token.getWord()) {
            case "or":
            case "and":
            case "eq":
            case "grt":
            case "add":
            case "sub":
            case "mul":
            case "div":     // COMPOSIT -> BINOP 
                                compositNode.addChild(parseBINOP());
                expect("lparen");
                compositNode.addChild(parseSIMPLE());
                                expect("comma");
                                compositNode.addChild(parseSIMPLE());
                                expect("rparen");
                
                
                break;
            case "not":
            case "sqrt":     // COMPOSIT -> UNOP
                                compositNode.addChild(parseUNOP());
                expect("lparen");
                    compositNode.addChild(parseSIMPLE());
                    expect("rparen");
                break;
            default:
                                throw new Exception("Expected COMPOSIT but found: " + token.getWord());
        }
    } else {
                throw new Exception("Expected COMPOSIT but found: null");
    }
    
        return compositNode;
}

private Node parseUNOP() throws Exception {     // Parse UNOP -> not, UNOP -> sqrt
        Node unopNode = new Node("UNOP");
    Token token = getCurrentToken();
        if (token != null) {
        switch (token.getWord()) {
            case "not":
            case "sqrt":    // UNOP -> not, UNOP -> sqrt
                unopNode.addChild(new Node(token.getWord()));
                consumeToken();
             
                break;
            default:
                throw new Exception("Expected UNOP but found: " + token.getWord());
        }
    } else {
        throw new Exception("Expected UNOP but found: null");
    }
            return unopNode;
}

private Node parseBINOP() throws Exception {
    System.out.println("Entering parseBINOP...");  // Debugging

    Node binopNode = new Node("BINOP");
    Token token = getCurrentToken();


    // Check if the current token is a valid binary operator
    if (token != null && Arrays.asList("or", "and", "eq", "grt", "add", "sub", "mul", "div").contains(token.getWord())) {
        System.out.println("Token is a valid BINOP operator: " + token.getWord());  // Debugging

        binopNode.addChild(new Node(token.getWord())); // Add the operator (eq, and, etc.)
        consumeToken();  // Move past the operator
                System.out.println("Consumed token, moving to the next...");  // Debugging


   
    } else {
        String errorMessage = "Expected BINOP but found: " + (token != null ? token.getWord() : "null");
        System.out.println(errorMessage);  // Debugging
        throw new Exception(errorMessage);
    }

    System.out.println("Exiting parseBINOP...");  // Debugging
    return binopNode;
}


    


    private Node parseFNAME() throws Exception {    // Parse FNAME -> a token of Token-class F from the lexer
        Node fnameNode = new Node("FNAME");
        Token token = getCurrentToken();
        
        if (token != null) {
            switch (token.getTokenClass()) {
                case "F":       // FNAME -> a token of Token-class F from the lexer
                    fnameNode.addChild(new Node(token.getWord()));
                    consumeToken();
                    break;
                default:
                    throw new Exception("Expected FNAME but found: " + token.getWord());
            }
        } else {
            throw new Exception("Expected FNAME but found: null");
        }
        
        return fnameNode;
    }
    private Node parseFUNCTIONS() throws Exception {        // Parse FUNCTIONS -> ε, FUNCTIONS -> DECL FUNCTIONS
        Node functionsNode = new Node("FUNCTIONS");
                Token token = getCurrentToken();
               if(getCurrentToken().getWord().equals( "end")){
                 consumeToken();
       }
      else{
        while (token != null) {
                        switch (token.getTokenClass()) {
                case "reserved_keyword":        // FUNCTIONS -> DECL FUNCTIONS
                    functionsNode.addChild(parseDECL());
                    break;
                default:        // FUNCTIONS -> ε
                                    return functionsNode;
            }
            token = getCurrentToken();
        }
    }
        return functionsNode;
    }

    private Node parseDECL() throws Exception {     // Parse DECL -> HEADER BODY
        Node declNode = new Node("DECL");
        declNode.addChild(parseHEADER());
        declNode.addChild(parseBODY());
        return declNode;
    }

    private Node parseHEADER() throws Exception {       // Parse HEADER -> FTYP FNAME ( VTYP , VTYP , VTYP )
        Node headerNode = new Node("HEADER");
        headerNode.addChild(parseFTYP());
        headerNode.addChild(parseFNAME());
        expect("lparen");
        headerNode.addChild(parseVNAME());
        expect("comma");
        headerNode.addChild(parseVNAME());
        expect("comma");
        headerNode.addChild(parseVNAME());
        expect("rparen");
        return headerNode;
    }

    private Node parseFTYP() throws Exception {     // Parse FTYP -> num, FTYP -> void
        Node ftypNode = new Node("FTYP");
        Token token = getCurrentToken();
        
        if (token != null) {
            switch (token.getWord()) {
                case "num":
                case "void":        // FTYP -> num, FTYP -> void
                    ftypNode.addChild(new Node(token.getWord()));
                    consumeToken();
                    break;
                default:
                    throw new Exception("Expected FTYP but found: " + token.getWord());
            }
        } else {
            throw new Exception("Expected FTYP but found: null");
        }
        
        return ftypNode;
    }
    private Node parseBODY() throws Exception {     // Parse BODY -> PROLOG LOCVARS ALGO EPILOG SUBFUNCS
        Node bodyNode = new Node("BODY");
        expect("lbrace");       // PROLOG
                bodyNode.addChild(parsePROLOG());
                bodyNode.addChild(parseLOCVARS());
                bodyNode.addChild(parseALGO());
                bodyNode.addChild(parseEPILOG());
                bodyNode.addChild(parseSUBFUNCS());
                // expect("rbrace");       // EPILOG
                return bodyNode;
    }

    //TODO: make parse {
    private Node parsePROLOG() throws Exception {       // Parse PROLOG -> {
        return new Node("PROLOG");
    }
    //TODO: make parse }
    private Node parseEPILOG() throws Exception {       // Parse EPILOG -> }
        return new Node("EPILOG");
    }

    private Node parseLOCVARS() throws Exception {      // Parse LOCVARS -> VTYP VNAME , VTYP VNAME , VTYP VNAME ,
        Node locvarsNode = new Node("LOCVARS");
        for (int i = 0; i < 3; i++) {
            locvarsNode.addChild(parseVTYP());
            locvarsNode.addChild(parseVNAME());
            if (i < 3) {
                expect("comma");
            }
        }
        return locvarsNode;
    }

    private Node parseSUBFUNCS() throws Exception {     // Parse SUBFUNCS -> FUNCTIONS
        return parseFUNCTIONS();
    }
    //endregion

    
}
