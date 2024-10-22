package translation_table;
import java.util.function.Function;
import org.w3c.dom.*;

import symbol_table.SemanticAnalyzer;
import symbol_table.Symbol;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

//FUNCTION TO TRAVERSE THE TREE/XML FILE
//STRUCTURE TO STORE THE INTERMEDIATE CODE [txt file]
//TRANSLATION RULES FOR EACH NODE TYPE IN THE SYNTAX TREE
//SYMBOL TABLE
//ERROR HANDLING
public class Translator {

    private IntermediateCode intermediateCode = new IntermediateCode();
    private Map<String, Symbol> vTable;
    private Document document;

    private File xmlFile;

    public Translator(SemanticAnalyzer analyzer) {
        this.vTable = analyzer.getVtable();
    }

    // Main translation method
    public String translate(File xmlFile) {
        try {
            this.xmlfile = xmlFile;
            // Parse the XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            // Start translating from the <ROOT> node
            Element root = (Element) document.getElementsByTagName("ROOT").item(0);
            String progCode = traverse_Node(root);

           // After traversal, write the intermediate code to a text file
           writeIntermediateCodeToFile("intermediate_code.txt", progCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Recursive function to traverse the tree and translate nodes
    private String translateNode(Node node) {
        String nodeName = node.getNodeName().trim();
        String symbValue = ((Element) node).getElementsByTagName("SYMB").item(0).getTextContent();

        switch (symbValue) {
            case "PROG":
                return translateProg(node);
            case "GLOBVARS":
            case "VTYP":
                // Ignore these nodes as per the rules
                return "";
            case "ALGO":
                return translateAlgo(node);
            case "INSTRUC":
                return translateInstruc(node);
            case "COMMAND":
                return translateCommand(node);
            case "ASSIGN":
                return translateAssign(node);
            case "TERM":
                return translateTerm(node);
            case "ATOMIC":
                return translateAtomic(node);
            case "CONST":
                return translateConst(node);
            case "CALL":
                return translateCall(node);
            case "OP":
                return translateOp(node);
            case "ARG":
                return translateArg(node);
            case "UNOP":
                return translateUnop(node);
            case "BINOP":
                return translateBinop(node);
            case "BRANCH":
                return translateBranch(node);
            case "COND":
                return translateCond(node);
            case "SIMPLE":
                return translateSimple(node);
            case "COMPOSIT":
                return translateComposit(node);
            case "FNAME":
                return translateFname(node);
            case "VNAME":
                return translateVname(node);
            default:
                return "";
        }
    }

    //VNAME
    private String translateVname(Node node) {
        String terminalValue = "";
        NodeList children = ((Element) node).getElementsByTagName("ID"); //get the children of the Vname node by ID TAG
        if (children.getLength() > 0) {
            for (int i = 0; i < children.getLength(); i++) { //for each child
                int childId = Integer.parseInt(children.item(i).getTextContent()); //get the ID of the child
                Node childNode = findNodeById(childId); //find the node by ID
                terminalValue = ((Element) childNode).getElementsByTagName("TERMINAL").item(i).getTextContent(); //get their terminal value
            }
        
        // Search the vTable using the terminal value to get the unique name
        Optional<Map.Entry<String, Symbol>> entry = vTable.entrySet().stream().filter(e -> e.getValue().getSymb().equals(terminalValue)).findFirst();

        if (entry.isPresent()) {
            return entry.get().getValue().getName(); // Return the unique name
        } else {
            return "No variable found"; // Return an empty string if the symbol is not found in the vTable
        }
    }

    //PROG
    private String translateProg(Node node) {
        String aCode = "";
        String fCode = "";

        NodeList children = ((Element) node).getElementsByTagName("ID"); //get the children of the Prog node by ID TAG
        for (int i = 0; i < children.getLength(); i++) { //for each child
            int childId = Integer.parseInt(children.item(i).getTextContent()); //get the ID of the child
            Node childNode = findNodeById(childId); //find the node by ID
            String symbValue = ((Element) childNode).getElementsByTagName("SYMB").item(0).getTextContent(); //get their symb value

            if (symbValue.equals("ALGO")) {
                aCode = translateNode(childNode);
            } else if (symbValue.equals("FUNCTIONS")) {
                fCode = translateNode(childNode);
            }
        }
        return aCode + " STOP " + fCode;
    }

    //ALGO
    private String translateAlgo(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        for (int i = 0; i < children.getLength(); i++) {
            int childId = Integer.parseInt(children.item(i).getTextContent());
            Node childNode = findNodeById(childId);
            NodeList symbNodes = ((Element) childNode).getElementsByTagName("SYMB");
            if (symbNodes.getLength() > 0) {
                String symbValue = symbNodes.item(0).getTextContent(); //won't work for terminals 
                if (symbValue.equals("INSTRUC")) {
                    return translateNode(childNode);
                }
            }
        }
        return "";
    }

    //INSTRUC
    private String translateInstruc(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID"); 
        if (children.getLength() == 0) {//if INSTRUC's children are empty (nullable production rule)
            return " REM END ";
        } 
        else {
            String commandCode = "";
            String instruc2Code = "";
            for (int i = 0; i < children.getLength(); i++) {
                int childId = Integer.parseInt(children.item(i).getTextContent());
                Node childNode = findNodeById(childId);
                NodeList symbNodes = ((Element) childNode).getElementsByTagName("SYMB");
                if (symbNodes.getLength() > 0) {
                    String symbValue = symbNodes.item(0).getTextContent(); //gets the value of the symb tag
                    if (symbValue.equals("COMMAND")) {
                        commandCode = translateNode(childNode);
                    } else if (symbValue.equals("INSTRUC")) {
                        instruc2Code = translateNode(childNode);
                    }
                }
            }
            return commandCode + instruc2Code;
        }
    }

    //COMMAND
    private String translateCommand(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        NodeList symbNodes = ((Element) node).getElementsByTagName("SYMB"); //will return null for terminals
        String symbValue;
    
        //check if the child is a terminal or not
        if (symbNodes.getLength() > 0) {
            symbValue = symbNodes.item(0).getTextContent();

            switch(symbValue)
            {
                case "ASSIGN":
                return translateAssign(node);
            case "CALL":
                return translateCall(node);
            case "BRANCH":
                return translateBranch(node);
            }
        } 
        else {
            NodeList terminalNodes = ((Element) node).getElementsByTagName("TERMINAL");
            if (terminalNodes.getLength() > 0) {
                symbValue = terminalNodes.item(0).getTextContent();

                if (symbValue.equals("skip")) {
                    return " REM DO NOTHING ";
                } else if (symbValue.equals("halt")) {
                    return " STOP ";
                } else if (children.getLength() == 2) {
                    Node atomicNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
                    return "PRINT " + " " + translateNode(atomicNode);
                }
            } else {
                return "No SYMB or TERMINAL tag found"; // No SYMB or TERMINAL tag found
            }
        }
    }

    //ASSIGN
    private String translateAssign(Node node) {
        // NodeList children = ((Element) node).getElementsByTagName("ID");
        // Node vnameNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
        // Node mathsymbol = findNodeById(Integer.parseInt(children.item(1).getTextContent()));
        // Node termNode = findNodeById(Integer.parseInt(children.item(2).getTextContent()));

        // if()
        // {
        //     String vnameCode = translateNode(vnameNode);
        //     return "INPUT" + " " + vnameCode;
        // }
        // else if()
        // {}

        
        // String termCode = translateNode(termNode);

        // return vnameCode + " := " + termCode;
    }

    //TERM
    private String translateTerm(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        Node childNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
        return translateNode(childNode);
    }

    //ATOMIC
    private String translateAtomic(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        Node childNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
        String symbValue = ((Element) childNode).getElementsByTagName("SYMB").item(0).getTextContent();

        if (symbValue.equals("VNAME")) {
            return translateAtomic(childNode);
        } else if (symbValue.equals("CONST")) {
            return translateConst(childNode);
        }
        return "";
    }

    //CONST
    private String translateConst(Node node) {
        // Get the child node
        NodeList children = ((Element) node).getElementsByTagName("ID");
        if (children.getLength() == 0) {
            return ""; // Handle the case where there is no child node
        }
        Node child = children.item(0);
    
        // Retrieve the terminal value
        String terminalValue = ((Element) child).getElementsByTagName("TERMINAL").item(0).getTextContent();
    
        // Return the translated value
        return terminalValue.matches("\\d+") ? terminalValue : "\"" + terminalValue + "\"";
    }

    //CALL
    private String translateCall(Node node) {
        // NodeList children = ((Element) node).getElementsByTagName("ID");
        // Node fnameNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
        // Node atomic1Node = findNodeById(Integer.parseInt(children.item(1).getTextContent()));
        // Node atomic2Node = findNodeById(Integer.parseInt(children.item(2).getTextContent()));
        // Node atomic3Node = findNodeById(Integer.parseInt(children.item(3).getTextContent()));

        // String fname = ((Element) fnameNode).getElementsByTagName("TERMINAL").item(0).getTextContent();
        // String newNameForFname = symbolTable.get(fname);

        // String p1 = translateNode(atomic1Node);
        // String p2 = translateNode(atomic2Node);
        // String p3 = translateNode(atomic3Node);

        // return "CALL_" + newNameForFname + "(" + p1 + "," + p2 + "," + p3 + ")";
    }

    //OP
    private String translateOp(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        Node unopNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
        Node argNode = findNodeById(Integer.parseInt(children.item(1).getTextContent()));

        String opName = translateNode(unopNode);
        String place1 = translateNode(argNode);

        return "place := " + opName + "(" + place1 + ")";



        // place = newvar();
        // code = TransExp(Exp, vtable, ftable,place);
        // op =transop(getopname(unop))
        // code ++ [place:= op place]
    }

    //ARG
    private String translateArg(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        Node childNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));
        return translateNode(childNode);
    }

    //UNOP
    private String translateUnop(Node node) {
        String terminalValue = ((Element) node).getElementsByTagName("TERMINAL").item(0).getTextContent();
        if (terminalValue.equals("not")) {
            return "!";
        } else if (terminalValue.equals("sqrt")) {
            return "SQR";
        }
        return "";
    }

    //BINOP
    private String translateBinop(Node node) {
        NodeList children = ((Element) node).getElementsByTagName("ID");
        Node childNode = findNodeById(Integer.parseInt(children.item(0).getTextContent()));  
        String terminalValue = ((Element) node).getElementsByTagName("TERMINAL").item(0).getTextContent();
        switch (terminalValue) {
            case "eq":
                return " = ";
            case "grt":
                return " > ";
            case "add":
                return " + ";
            case "sub":
                return " - ";
            case "mul":
                return " * ";
            case "div":
                return " / ";
            default:
                return "";
        }


        // arg  = newlabel();
        // code1 = Transcond(Cond1, arg, labelt, vtable, ftable);
        // code2 = Transcond(Cond2, labelt, labelf, vtable, ftable);
        // code1 ++ [LABEL arg] ++ code2
    }



    //Helper function
    private Node findNodeById(int id) {
        return findNodeByIdRecursive(document.getDocumentElement(), id);
    }

    private Node findNodeByIdRecursive(Node currentNode, int id) {
        if (currentNode == null) {
            return null;
        }

        // Check if the current node has the target UNID
        NodeList unidList = ((Element) currentNode).getElementsByTagName("UNID");
        if (unidList.getLength() > 0 && Integer.parseInt(unidList.item(0).getTextContent()) == id) {
            return currentNode;
        }

        // Recursively search in child nodes
        NodeList children = currentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node foundNode = findNodeByIdRecursive(children.item(i), id);
            if (foundNode != null) {
                return foundNode;
            }
        }

        return null;
    }

    // private String getTextContent(Node node, String tagName) {
    //     return ((Element) node).getElementsByTagName(tagName).item(0).getTextContent();
    // }

    private void writeIntermediateCodeToFile(String filePath, String code) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // //COMMAND
    // private String translate_COMMAND(Command command) {
    //     if (command instanceof Skip) { //command.name == "skip"
    //         return " REM DO NOTHING ";
    //     } 
    //     else if (command instanceof Halt) {
    //         return " STOP ";
    //     } 
    //     else if (command instanceof Print) {
    //         //private String translatePrint(Print printCmd) 
    //         String atomicCode = translateAtomic(((Print) command).atomic);
    //         return "PRINT" + " " + atomicCode;
    //     } 
    //     else if (command instanceof Assign) {
    //         return translate_ASSIGN((Assign) command);
    //     } 
    //     else if (command instanceof Call) {
    //         return translate_CALL((Call) command);
    //     } 
    //     else if (command instanceof Branch) {
    //         return translate_BRANCH((Branch) command);
    //     }
    //     return "";
    // }

    // //VNAME
    // private String translate_VNAME(VName vname) {
    //     return vname.name; // Assume the new name is already in the Symbol Table
    // }

    // //ASSIGN
    // private String translate_ASSIGN(Assign assign) {
    //     String varname = translate_VNAME(VName vname); // Assume the new name is already in the Symbol Table
    //     return "INPUT" + " " + varname;
    // }

    // //BRANCH
    // private String translate_BRANCH(Branch branch) {
    //     String condCode = translateCondition(branch.cond);
    //     String algo1Code = translate_ALGO(branch.algo1);
    //     String algo2Code = translate_ALGO(branch.algo2);
    //     return "IF " + condCode + " THEN " + algo1Code + " ELSE " + algo2Code;
    // }

    //CONST
    // private String translateConst(Const constant) {
    //     if (constant instanceof NumConst) {
    //         return " " + ((NumConst) constant).value + " ";
    //     } else if (constant instanceof TextConst) {
    //         return " \"" + ((TextConst) constant).value + "\" ";
    //     }
    //     return "";
    // }

    //  //CALL
    // private String translate_CALL(Call call) {
    //     String p1 = translateAtomic(call.atomic1);
    //     String p2 = translateAtomic(call.atomic2);
    //     String p3 = translateAtomic(call.atomic3);
    //     String newNameForFName = call.fname; // Assume the new name is already in the Symbol Table
    //     return "CALL_" + newNameForFName + "(" + p1 + "," + p2 + "," + p3 + ")";
    // }
}