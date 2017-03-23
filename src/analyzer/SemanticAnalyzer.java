package analyzer;

import analyzer.symbol.table.SymbolTableCode;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import globals.CompilerFlags;
import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.expression.IDExpressionNode;
import syntaxtree.statement.IfStatementNode;

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
   * Full constructor for the SemanticAnalyzer
   */
  public SemanticAnalyzer()
  {
    symbolTable         = null;
    errorOccurred       = false;
    anonymousScopeCount = 0;
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
    anonymousScopeCount = 0;
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

  private void processTree(final AbstractSyntaxTreeNode tree, final String scope)
  {
    AbstractSyntaxTreeNode currentNode = tree;
    while (currentNode != null)
    {
      processNode(currentNode, scope);
      currentNode = currentNode.getSibling();
    }
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
      // If the node is an array identifier ( ID[...] )...
      case EXPRESSION_ARRAY_IDENTIFIER:
      {
        // TODO: Implement Array Identifier Processing
        break;
      }
      // If the node is a function call ( ID (...) )...
      case EXPRESSION_CALL:
      {
        // TODO: Implement Function Call Processing
        break;
      }
      // If the node is a simple identifier ( ID )...
      case EXPRESSION_IDENTIFIER:
      {
        // TODO: Implement Simple Identifier Processing
        reportSemanticError(symbolTable.update(scope, node.getName(), node.getLineNumber()));
        break;
      }
      // If the node is an operation ( ID + ID )...
      case EXPRESSION_OPERATION:
      {
        // TODO: Implement Operator Processing
        break;
      }
      // If the node is an array parameter ( int ID[] )...
      case META_ARRAY_PARAMETER:
      {
        // TODO: Implement Array Parameter Processing
        break;
      }
      // If the node is a function declaration ( <type> ID( ... ) { ... } )...
      case META_FUNCTION:
      {
        processFunctionDeclaration(node, scope);
        break;
      }
      // If the node is a simple parameter ( int ID )...
      case META_PARAMETER:
      {
        processSimpleParameter(node, scope);
        break;
      }
      // If the node is a local array declaration ( int x[NUM] )...
      case STATEMENT_ARRAY_DECLARATION:
      {
        // TODO: Implement Local Array Declaration Processing
        break;
      }
      // If the node is an assignment ( ID = ... )...
      case STATEMENT_ASSIGN:
      {
        // TODO: Implement Assignment Processing
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
      // If the node is a local declaration ( int x )...
      case STATEMENT_VAR_DECLARATION:
      {
        // TODO: Implement Local Declaration Processing
        break;
      }
      // If the node is a while-statement...
      case STATEMENT_WHILE:
      {
        // TODO: Implement While-Statement Processing
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

  private void processSimpleParameter(final AbstractSyntaxTreeNode node,
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
      reportSemanticError(symbolTable.addRecord(scope, node, 0));
    }
  }

  private void processIfStatement(final AbstractSyntaxTreeNode node,
                                  final String scope)
  {
    if (node.getChild(0).getType() != Boolean.class)
    {
      reportSemanticError(SymbolTableCode.SEMANTIC_FAILURE);
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

  private void reportSemanticError(final SymbolTableCode errorCode)
  {
    if (errorCode != SymbolTableCode.OK)
    {
      System.err.printf("SEMANTIC ERROR - %s\n", errorCode.toString());
      errorOccurred = true;
    }
  }
}
