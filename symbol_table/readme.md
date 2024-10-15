START SemanticAnalyzer

  DEFINE symbolTable as HashMap (ID -> Symbol)  // A map to hold symbols and their properties (name, type, scope)
  DEFINE scopeStack as Stack of String  // A stack to manage the scopes (e.g., MAIN, function names)
  DEFINE errors as List of String  // A list to collect errors during analysis

  FUNCTION analyze(xmlFile):
    TRY:
      // Parse the XML file and create a document object
      CREATE factory using DocumentBuilderFactory
      CREATE builder using factory.newDocumentBuilder()
      PARSE xmlFile into doc
      NORMALIZE doc

      // Get the root element (the top-most node in the XML)
      GET rootNode = doc.getElementsByTagName("ROOT").item(0)
      CALL traverseNode with rootNode

      // After traversal, print the symbol table and errors
      CALL printSymbolTable
      CALL printErrors
    EXCEPT Exception:
      PRINT error details

  FUNCTION traverseNode(node):
    // Get the name of the current node
    GET nodeName from node.getNodeName()

    IF nodeName is "ROOT":
      PUSH "MAIN" onto scopeStack  // Enter the main scope
      GET startSymbol = getTextContent(node, "SYMB")
      PRINT "Analyzing ROOT with start symbol: " + startSymbol

      // Traverse child nodes of the ROOT node
      FOR each child in node.getChildNodes():
        IF child is an element node:
          CALL traverseNode(child)  // Recurse through child node

      POP the MAIN scope from the stack

    IF nodeName is "IN":
      // Handle function or non-terminal symbols
      GET parent = getTextContent(node, "PARENT")
      GET unid = getTextContent(node, "UNID")
      GET nonTerminal = getTextContent(node, "SYMB")

      PRINT "Analyzing IN node: " + nonTerminal

      IF nonTerminal is a function:
        IF function name already exists in the current scope:
          CALL throwError("Function name conflict in scope", unid)
        ELSE:
          PUSH function name onto scopeStack
          ADD function symbol to symbolTable with UNID as key
          PRINT "Function declared: " + nonTerminal
      END IF

      // Traverse children of the IN node
      FOR each child in node.getChildNodes():
        IF child is an element node:
          CALL traverseNode(child)

      IF nonTerminal is a function:
        POP function scope from the stack

    IF nodeName is "LEAF":
      // Handle leaf nodes (variables or terminals)
      GET parent = getTextContent(node, "PARENT")
      GET unid = getTextContent(node, "UNID")
      GET terminal = getTextContent(node, "TERMINAL")

      PRINT "Analyzing LEAF node: " + terminal

      IF terminal is a variable:
        GET currentScope = scopeStack.peek()
        IF symbolTable contains the variable in the current scope:
          CALL throwError("Variable redeclaration in scope", unid)
        ELSE:
          ADD variable symbol to symbolTable with UNID as key
          PRINT "Variable declared: " + terminal
      END IF

  FUNCTION getTextContent(node, tagName):
    GET and return text content of the element with the specified tagName

  FUNCTION isFunction(symbol):
    CHECK if symbol matches the pattern for function names (e.g., "F_...")

  FUNCTION isVariable(terminal):
    CHECK if terminal is not a keyword and matches the pattern for variables (e.g., "V_...")

  FUNCTION throwError(message, unid):
    ADD "Error at node UNID " + unid + ": " + message to errors list

  FUNCTION printSymbolTable:
    IF symbolTable is empty:
      PRINT "No symbols found."
    ELSE:
      PRINT a formatted table of the symbols in symbolTable (ID, name, type, scope)

  FUNCTION printErrors:
    IF errors is empty:
      PRINT "No semantic errors found."
    ELSE:
      PRINT all the error messages in errors

END SemanticAnalyzer
