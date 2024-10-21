package translation_table;
import java.util.function.Function;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.util.List;

//FUNCTION TO TRAVERSE THE TREE/XML FILE
//STRUCTURE TO STORE THE INTERMEDIATE CODE [txt file]
//TRANSLATION RULES FOR EACH NODE TYPE IN THE SYNTAX TREE
//SYMBOL TABLE
//ERROR HANDLING
public class Translator {

    private IntermediateCode intermediateCode = new IntermediateCode();
    private Map<String, String> symbolTable = new HashMap<>();

    private File xmlFile;

    // Main translation method
    public String translate(File xmlFile) {
        try {
            this.xmlfile = xmlFile;
            // Parse the XML document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Start translating from the <ROOT> node
            Element root = (Element) doc.getElementsByTagName("ROOT").item(0);
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
            case "GLOBVARS": //ignore
            case "VTYP": //ignore
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
            default:
                return "";
        }
    }
}


