package analyzer;

import analyzer.symbol.SymbolItem;
import analyzer.symbol.SymbolItemType;
import analyzer.symbol.table.SymbolTableCode;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import globals.CompilerFlags;
import syntaxtree.ASTNodeType;
import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.statement.IfStatementNode;
import syntaxtree.statement.WhileStatementNode;

/**
 *
 */
public final class SemanticAnalyzer
{
  /** The global scope string */
  private final String GLOBAL_SCOPE = "";

  /** Internally tracked symbol table (used during analysis procedure) */
  private SymbolTable symbolTable;

  /**
   * The current count of anonymous scopes (if, else and while) for unique scope
   * identification
   */
  private int anonymousScopeCount;

  /**
   * A boolean flag used to indicate if an error occurred during the semantic
   * analysis.
   */
  private boolean errorOccurred;

  /**
   * The current memory location for storing identifiers in the symbol table
   */
  private int memoryLocation;

  /**
   * Full constructor for the SemanticAnalyzer
   */
  public SemanticAnalyzer()
  {
    symbolTable         = null;
    errorOccurred       = false;
    anonymousScopeCount = 0;
    memoryLocation      = 0;
  }

  /**
   * Given an abstract syntax tree, perform semantic analysis to produce a symbol
   * table.
   *
   * @param tree The abstract syntax tree on which to perform the semantic analysis
   * @return The newly created symbol table
   */
  public SymbolTable analyze(final AbstractSyntaxTreeNode tree)
  {
    // Reset the anonymous scope count
    anonymousScopeCount = 0;

    // Reset the memory location
    memoryLocation = 0;

    // Create the global symbol table that will be used to keep
    // track of everything in the semantic analysis
    symbolTable = new SymbolTable(-1, null);

    // Add the input function call (assumes it's already defined)
    symbolTable.addScope("input",
        new FunctionSymbolTable(-1, Integer.class));

    // Add the output function call (assumes it's already defined)
    FunctionSymbolTable outputTable =
        new FunctionSymbolTable(-1, Void.class);
    // The output function takes 1 argument, so we'll add it here. This is a special
    // case of the addParameter function. This function should not be explicitly
    // used.
    outputTable.addParameter(false);
    symbolTable.addScope("output", outputTable);

    // Reset the errorOccurred flag from its previous value (hopefully false) to
    // false. This prevents accidental false failures
    errorOccurred = false;

    // Perform the actual processing
    processTree(tree, GLOBAL_SCOPE);

    // Remove all unused scopes from the symbol table (empty scopes usually appear
    // from the processing of if, else and while statements).
    symbolTable.removeAllEmpty();

    // Return the completed symbol table
    return symbolTable;
  }

  /**
   *
   * @return
   */
  public boolean errorOccurred()
  {
    return errorOccurred;
  }

  private int processTree(final AbstractSyntaxTreeNode tree, final String scope)
  {
    int processed = 0;
    AbstractSyntaxTreeNode currentNode = tree;
    while (currentNode != null)
    {
      processNode(currentNode, scope);
      currentNode = currentNode.getSibling();
      ++processed;
    }

    return processed;
  }

  private void processNode(final AbstractSyntaxTreeNode node, final String scope)
  {
    if (node == null)
    {
      return;
    }
    // Perform different operations based on the type of the node
    // being processed
    if (CompilerFlags.TraceAnalyzer)
    {
      System.out.println("Analyzing " + node.getName() +
                         "\tScope = \"" + scope + "\"" +
                         "\tNode Type = \"" + node.getNodeType().toString() + "\"");
    }

    switch (node.getNodeType())
    {
      // If the node is a function call ( ID (...) )...
      case EXPRESSION_CALL:
      {
        processFunctionCall(node, scope);
        break;
      }
      // If the node is an array identifier ( ID[...] )...
      case EXPRESSION_ARRAY_IDENTIFIER:
      {
        reportSemanticError(
            symbolTable.update(scope, node.getName(), node.getLineNumber()),
            node.getLineNumber());

        final AbstractSyntaxTreeNode child = node.getChild(0);
        reportSemanticError(
            symbolTable.update(scope, child.getName(), child.getLineNumber()),
            child.getLineNumber());
        break;
      }
      // If the node is a simple identifier ( ID )...
      case EXPRESSION_IDENTIFIER:
      {
        reportSemanticError(
            symbolTable.update(scope, node.getName(), node.getLineNumber()),
            node.getLineNumber());
        break;
      }
      // If the node is an assignment ( ID = ... )...
      case STATEMENT_ASSIGN:
      // If the node is an operation ( ID + ID )...
      case EXPRESSION_OPERATION:
      {
        processOperator(node, scope);
        break;
      }
      // If the node is an array parameter ( int ID[] )...
      case META_ARRAY_PARAMETER:
      // If the node is a simple parameter ( int ID )...
      case META_PARAMETER:
      {
        processParameter(node, scope);
        break;
      }
      // If the node is a local array declaration ( int x[NUM] )...
      case STATEMENT_ARRAY_DECLARATION:
      {
        SymbolTableCode result = symbolTable.addRecord(scope, node, memoryLocation);
        if (result == SymbolTableCode.OK)
        {
          memoryLocation += node.getChild(0).getValue();
        }
        else
        {
          reportSemanticError(result, node.getLineNumber());
        }
        break;
      }
      // If the node is a local declaration ( int x )...
      case STATEMENT_VAR_DECLARATION:
      {
        SymbolTableCode result = symbolTable.addRecord(scope, node, memoryLocation);
        if (result == SymbolTableCode.OK)
        {
          ++memoryLocation;
        }
        else
        {
          reportSemanticError(result, node.getLineNumber());
        }
        break;
      }
      // If the node is a function declaration ( <type> ID( ... ) { ... } )...
      case META_FUNCTION:
      {
        processFunctionDeclaration(node, scope);
        break;
      }
      // If the node is an if-statement...
      case STATEMENT_IF:
      {
        processIfStatement(node, scope);
        break;
      }
      // If the node is a return statement...
      case STATEMENT_RETURN:
      {
        processNode(node.getChild(0), scope);
        break;
      }

      // If the node is a while-statement...
      case STATEMENT_WHILE:
      {
        processWhileStatement(node, scope);
        break;
      }
      // If the node is a constant value ( NUM ) or is unrecognized...
      default:
      {
        // Do nothing
        break;
      }
    }
  }

  private void processFunctionDeclaration(final AbstractSyntaxTreeNode node,
                                          final String scope)
  {
    symbolTable.addScope(scope, node);

    String newScope;
    if (scope.equals(GLOBAL_SCOPE))
    {
      newScope = node.getName();
    }
    else
    {
      newScope = String.format("%s.%s", scope, node.getName());
    }

    // Process parameters
    processTree(node.getChild(0), newScope);

    // Process the function body
    processTree(node.getChild(1), newScope);
  }

  private void processParameter(final AbstractSyntaxTreeNode node,
                                final String scope)
  {
    if (node.getType() == Void.class)
    {
      if (node.hasSibling())
      {
        System.err.println("ERROR - Function definitions with 'void' cannot have multiple arguments.");
        errorOccurred = true;
      }
    }
    else
    {
      reportSemanticError(
          symbolTable.addRecord(scope, node, 0),
          node.getLineNumber());
    }
  }

  private void processIfStatement(final AbstractSyntaxTreeNode node,
                                  final String scope)
  {
    if (node.getChild(0).getType() != Boolean.class)
    {
      reportSemanticError(
          SymbolTableCode.SEMANTIC_FAILURE,
          node.getLineNumber());
    }

    processNode(node.getChild(0), scope);

    String ifScope = String.format("if_%d", ++anonymousScopeCount);

    AbstractSyntaxTreeNode ifNode = new IfStatementNode();
    ifNode.setName(ifScope);
    ifNode.setLineNumber(node.getChild(1).getLineNumber());

    symbolTable.addScope(scope, ifNode);

    String newScope = String.format("%s.%s", scope, ifScope);

    processTree(node.getChild(1), newScope);

    if (node.getChild(2) == null)
    {
      return;
    }

    AbstractSyntaxTreeNode elseNode = new IfStatementNode();
    String elseScope = String.format("else_%d", anonymousScopeCount);
    elseNode.setName(elseScope);
    elseNode.setLineNumber(node.getChild(2).getLineNumber());

    symbolTable.addScope(scope, elseNode);

    newScope = String.format("%s.%s", scope, elseScope);

    processTree(node.getChild(2), newScope);
  }

  private void processWhileStatement(final AbstractSyntaxTreeNode node,
                                     final String scope)
  {
    if (node.getChild(0).getType() != Boolean.class)
    {
      reportSemanticError(
          SymbolTableCode.SEMANTIC_FAILURE,
          node.getLineNumber());
    }

    processNode(node.getChild(0), scope);

    String whileScope = String.format("while_%d", ++anonymousScopeCount);

    AbstractSyntaxTreeNode ifNode = new WhileStatementNode();
    ifNode.setName(whileScope);
    ifNode.setLineNumber(node.getChild(1).getLineNumber());

    symbolTable.addScope(scope, ifNode);

    String newScope = String.format("%s.%s", scope, whileScope);

    processTree(node.getChild(1), newScope);
  }

  private void processOperator(final AbstractSyntaxTreeNode node,
                               final String scope)
  {
    processNode(node.getChild(0), scope);
    if (node.getChild(0).getType() != Integer.class)
    {
      reportSemanticError(SymbolTableCode.INVALID_LHS, node.getLineNumber());
    }
    processNode(node.getChild(1), scope);
    if (node.getChild(1).getType() != Integer.class)
    {
      reportSemanticError(SymbolTableCode.INVALID_RHS, node.getLineNumber());
    }
  }

  private void processFunctionCall(final AbstractSyntaxTreeNode node,
                                    final String scope)
  {
    final int lineNumber = node.getLineNumber();
    final String name    = node.getName();

    SymbolTableCode result = symbolTable.update(scope, name, lineNumber);

    if (result != SymbolTableCode.OK)
    {
      reportSemanticError(result, lineNumber);
      return;
    }

    SymbolItem function = symbolTable.getSymbolItem(scope, name);
    if (function.getSymbolType() != SymbolItemType.SYMBOL_TABLE_FUNCTION)
    {
      reportSemanticError(SymbolTableCode.SEMANTIC_FAILURE, lineNumber);
      return;
    }

    FunctionSymbolTable functionSymbolTable = (FunctionSymbolTable)function;
    node.setType(functionSymbolTable.getClassType());
    int processed = processTree(node.getChild(0), scope);

    if (processed != functionSymbolTable.getParameterCount())
    {
      reportSemanticError(SymbolTableCode.BAD_PARAM_COUNT, lineNumber);
    }

    int index = 0;
    AbstractSyntaxTreeNode arg = node.getChild(index);
    while (arg != null)
    {
      if (arg.getNodeType() == ASTNodeType.EXPRESSION_OPERATION ||
          arg.getNodeType() == ASTNodeType.EXPRESSION_NUMBER)
      {
        if (arg.getType() != Integer.class)
        {
          reportSemanticError(SymbolTableCode.INVALID_PTYPE, arg.getLineNumber());
        }
      }
      else
      {
        final SymbolItem argument = symbolTable.getSymbolItem(scope, arg.getName());
        final SymbolItemType type = argument.getSymbolType();
        if ((type == SymbolItemType.SYMBOL_RECORD_ARRAY &&
            !functionSymbolTable.isParameterArray(index)) ||
            (type != SymbolItemType.SYMBOL_RECORD_ARRAY &&
                functionSymbolTable.isParameterArray(index)))
        {
          reportSemanticError(SymbolTableCode.INVALID_PTYPE, lineNumber);
        }
        else if (type == SymbolItemType.SYMBOL_TABLE_FUNCTION &&
                 functionSymbolTable.isParameterArray(index))
        {
          reportSemanticError(SymbolTableCode.INVALID_PTYPE, lineNumber);
        }
      }
      arg = arg.getSibling();
    }
  }

  private void reportSemanticError(final SymbolTableCode errorCode,
                                   final int lineNumber)
  {
    if (errorCode != SymbolTableCode.OK)
    {
      System.err.printf("SEMANTIC ERROR - %s - Line %d\n",
          errorCode.toString(),
          lineNumber);
      errorOccurred = true;
    }
  }
}
