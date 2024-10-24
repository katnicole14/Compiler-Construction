package translation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import parser.Node;

import symbol_table.Symbol;

public class Translator {
    private Map<String, Symbol> VTable;
    private Map<String, Symbol> FTable;
    private int lineNumber = 10; // Starting line number for BASIC code
    private int stackSize = 30; // Size of the runtime stack

    public Translator(Map<String, Symbol> VTable, Map<String, Symbol> FTable) {
        this.VTable = VTable;
        this.FTable = FTable;
    }

    public String translate(Node syntaxTree, String outputFile) {
        if (syntaxTree == null) {
            return "Error: Syntax tree is null.";
        }
        
        StringBuilder code = new StringBuilder();
        translateProg(syntaxTree, code);
        writeToFile(code.toString(), outputFile);
        return "Translation complete";
    }

    private String translateVname(Node node) {
        String vname = node.getName();

        if (VTable == null) {
            System.err.println("Error: VTable is null.");
            return "Error: VTable is null.";
        }

        if (VTable.isEmpty()) {
            System.err.println("Error: VTable is empty.");
            return "Error: VTable is empty.";
        }

        for (Symbol symbol : VTable.values()) {
            String symb = symbol.getSymb();
            if (symb.equals(vname)) {
                vname = symbol.getName();
                return vname;
            }
        }
        return vname;
        
    }

    private void translateProg(Node node, StringBuilder code) {
        StringBuilder aCode = new StringBuilder();
        StringBuilder fCode = new StringBuilder();
        Node aNode;
        Node fNode;

        List<Node> children = node.getChildren();
        if (children.size() < 3) {
            System.err.println("Error in PROG");
            return;
        }

        for (Node childNode : children) {
            //System.out.println("Child node: " + childNode);
    
            String symbValue = childNode.getName();
            if (symbValue != null) {
                //System.out.println("Symb value: " + symbValue);
                if (symbValue.equals("ALGO")) {
                    aNode = children.get(2);
                    translateAlgo(aNode, aCode);
                } else if (symbValue.equals("FUNCTIONS")) {
                    fNode = children.get(3);
                    //translateFunctions(fNode, fCode);
                }
            }
        }

        code.append(aCode).append(" STOP ").append(fCode);
    }

    private void translateAlgo(Node node, StringBuilder code) {
        List<Node> children = node.getChildren();
        Node instrNode;
        if (children.isEmpty()) {
            System.err.println("Error in ALGO");
            return;
        }

        for (Node childNode : children) {
            System.out.println("Child node: " + childNode);
    
            String symbValue = childNode.getName();
            if (symbValue != null) {
                System.out.println("Symb value: " + symbValue);
                if (symbValue.equals("INSTRUC")) {
                    instrNode = children.get(1);
                    translateInstruc(instrNode, code);
                }
            }
        }        
    }

    private void translateInstruc(Node node, StringBuilder code) {
        List<Node> children = node.getChildren();
        Node cmdNode; 
        Node instrNode;

        if (children.isEmpty()) {
            code.append(" REM END \n");
        } else {
            if (children.size() < 2) {
                System.err.println("Error in INSTRUC");
                return;
            }

            for (Node childNode : children) {
                System.out.println("Child node: " + childNode);
        
                String symbValue = childNode.getName();
                if (symbValue != null) {
                    System.out.println("Symb value: " + symbValue);
                    if (symbValue.equals("COMMAND")) {
                        cmdNode = children.get(0);
                        translateCommand(cmdNode, code);
                        code.append(" ; \n");
                    } else if (symbValue.equals("INSTRUC")) {
                        instrNode = children.get(2);
                        translateInstruc(instrNode, code);
                    }
                }
            }
        }
    }

    private void translateCommand(Node node, StringBuilder code) {
        List<Node> children = node.getChildren();
        for (Node childNode : children) {
            String childName = childNode.getName();

            switch (childName) {
                case "skip":
                    code.append(" REM DO NOTHING \n");
                    break;
                case "halt":
                    code.append(" STOP \n");
                    break;
                case "print":
                    Node atomNode = node.getChildren().get(1); // ATOMIC node
                    String atomCode = translateAtomic(atomNode);
                    code.append("PRINT ").append(atomCode).append("\n");
                    break;
                case "return":
                    code.append("RETURN\n");
                    break;
                case "ASSIGN":
                    Node assignNode = node.getChildren().get(0); // ASSIGN node
                    translateAssign(assignNode, code);
                    break;
                case "CALL":
                    Node callNode = node.getChildren().get(0); // CALL node
                    translateCall(callNode, code);
                    break;
                case "BRANCH":
                    Node branchNode = node.getChildren().get(0); // BRANCH node
                    translateBranch(branchNode, code);
                    break;
            }
        }
    }

    private String translateAtomic(Node node) {
        List<Node> children = node.getChildren();
        if (children.size() < 1) {
            System.err.println("Error in ATOMIC");
            return "";
        }

        for (Node childNode : children) { // For each child
            System.out.println("Child node: " + childNode);
    
            String childName = childNode.getName();
            System.out.println("Child name: " + childName);
            if (childName.equals("VNAME")) {
                return translateVname(childNode);
            } else if (childName.equals("CONST")) {
                return translateConst(childNode);
            }
        }
        return "Error in ATOMIC";
    }

    private String translateConst(Node node) {
        String constant = node.getChildren().get(0).getName();
        if (constant.matches("\\d+")) {
            return constant;
        } else if (constant.startsWith("\"") && constant.endsWith("\"")) {
            return constant;
        } else {
            return "Received node is neither a text or number";
        }
    }

    private void translateAssign(Node node, StringBuilder code) {
        List<Node> children = node.getChildren();
        Node vnNode;
        String vnCode;
        if (children.size() < 3) {
            System.err.println("Error in ASSIGN");
            return;
        }

        vnNode = children.get(0).getChildren().get(0);
        vnCode = translateVname(vnNode); 

        if (children.get(1).getName().equals("< input")) {
            code.append("INPUT ").append(vnCode);
        } else {
            Node tNode = children.get(2);
            String tCode = translateTerm(tNode);
            code.append(vnCode).append(":=").append(tCode);
        }
    }

    private String translateTerm(Node node) {
        Node childNode = node.getChildren().get(0); //should always be 1 child
        String childName = childNode.getName();

        if(childName.equals("ATOMIC"))
            return translateAtomic(childNode);
        else if(childName.equals("CALL"))
        {
            StringBuilder callCode = new StringBuilder();
            translateCall(childNode, callCode);
            return callCode.toString();
        } else if (childName.equals("OP"))
        {
            StringBuilder opCode = new StringBuilder();
            translateOp(childNode, opCode);
            return opCode.toString();
        } else {
            return "Incorrect structure for TERM";
        }
    }

    private void translateCall(Node node, StringBuilder code) {
        List<Node> children = node.getChildren();
        if (children.size() < 4) {
            System.err.println("Error in CALL");
            return;
        }

        Node fNode = children.get(0); 
        Node atomic1 = children.get(1);
        Node atomic2 = children.get(2); 
        Node atomic3 = children.get(3);

        String fName = translateFname(fNode.getChildren().get(0));
        String p1 = translateAtomic(atomic1);
        String p2 = translateAtomic(atomic2);
        String p3 = translateAtomic(atomic3);

        code.append("CALL_").append(fName).append("(").append(p1).append(",").append(p2).append(",").append(p3).append(")");
    }

    private void translateOp(Node node, StringBuilder code) {
        String opName = node.getChildren().get(0).getName();
        if(opName.equals("UNOP"))
        {
            Node aNode = node.getChildren().get(0);
            String aCode = translateArg(aNode);
            code.append("SQR(").append(aCode).append(")");
        }
        else if(opName.equals("BINOP"))
        {
            Node a1Node = node.getChildren().get(0).getChildren().get(1); // ARG1 node
            Node a2Node = node.getChildren().get(0).getChildren().get(2);; // ARG2 node
            String arg1Code = translateArg(a1Node);
            String arg2Code = translateArg(a2Node);
            String binop = translateBINOP(node.getChildren().get(0).getChildren().get(0).getName());
            code.append(arg1Code).append(" ").append(binop).append(" ").append(arg2Code);
            
        } else{
            System.err.println("Error in OP");
            return;
        }
    }

    private String translateArg(Node node) {
        Node childNode = node.getChildren().get(0);
        String childName = childNode.getName();

        if(childName.equals("ATOMIC"))
            return translateAtomic(childNode);
        else if(childName.equals("OP"))
        {
            StringBuilder opCode = new StringBuilder();
            translateOp(childNode, opCode);
            return opCode.toString();
        } else{
            return "Error in ARG";
        }
    }

    private String translateUnop(String unop) {
        switch (unop) {
            case "not":
                return "!";
            case "sqrt":
                return "SQR";
            default:
                return "";
        }
    }

    private String translateBINOP(String binop) {
        switch (binop) {
            case "or":
                return "||";
            case "and":
                return "&&";
            case "eq":
                return "=";
            case "grt":
                return ">";
            case "add":
                return "+";
            case "sub":
                return "-";
            case "mul":
                return "*";
            case "div":
                return "/";
            default:
                return "";
        }
    }

    private void translateBranch(Node node, StringBuilder code) {
        List<Node> children = node.getChildren();
        if (children.size() < 3) {
            System.err.println("Error in BRANCH");
            return;
        }

        Node condNode = children.get(0); 
        Node algo1Node = children.get(1); 
        Node algo2Node = children.get(2);

        String cCode = translateCond(condNode);
        StringBuilder a1Code = new StringBuilder();
        translateAlgo(algo1Node, a1Code);
        StringBuilder a2Code = new StringBuilder();
        translateAlgo(algo2Node, a2Code);

        code.append("IF ").append(cCode).append(" THEN ").append(a1Code).append("\n");
        code.append("GOTO ").append(a2Code).append("\n");
    }

    private String translateCond(Node node) {
        Node childNode = node.getChildren().get(0);
        String childName = childNode.getName();

        if(childName.equals("SIMPLE"))
            return translateSimple(childNode);
        else if(childName.equals("COMPOSIT"))
            return translateComposit(childNode);
        else
            return "Error in COND";
    }

    private String translateSimple(Node node) {
        List<Node> children = node.getChildren();
        if (children.size() < 3) {
            System.err.println("Error in SIMPLE");
            return "";
        }

        String binOp = translateBINOP(children.get(0).getName());
        String arg1 = translateAtomic(children.get(1));
        String arg2 = translateAtomic(children.get(2));

        return arg1 + " " + binOp + " " + arg2;
    }

    private String translateComposit(Node node) {
        List<Node> children = node.getChildren();
        if (children.size() < 3) {
            System.err.println("Error: COMPOSIT node does not have enough children.");
            return "";
        }

        Node childNode = children.get(0);
        String childName = childNode.getName();
        if(childName.equals("BINOP"))
        {
            String binOp = translateBINOP(childNode.getName());
            String s1 = translateSimple(children.get(1));
            String s2 = translateSimple(children.get(2));
            return s1 + " " + binOp + " " + s2;
        }
        else if(childName.equals("UNOP"))
        {
            Node simpleNode = children.get(1);
            String unOp = translateUnop(childNode.getName());
            String simple = translateSimple(simpleNode);
            return unOp + "(" + simple + ")";
        }
        else
            return "Error in COMPOSIT";
    }

    private String translateFname(Node node) {
        String fname = node.getName(); //F_sum
        String fUniqueName = "";
        for (Symbol symbol : FTable.values()) {
            if (symbol.getSymb().equals(fname)) {
                 fUniqueName = symbol.getName();
            }
        }

        if(fUniqueName != "")
            return fUniqueName;
        else
            return "Could not find function name in FTable";
    }

    private void writeToFile(String code, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(code);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}