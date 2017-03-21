package analyzer;

import analyzer.symbol.SymbolTableCode;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import globals.CompilerFlags;
import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.expression.IDExpressionNode;

/**
 *
 */
public final class SemanticAnalyzer
{
  private SymbolTable symbolTable;

  private final String GLOBAL_SCOPE = "";

  private int anonymousScopeCount;

  private boolean errorOccurred;

  public SemanticAnalyzer()
  {
    symbolTable = null;

    errorOccurred = false;

    anonymousScopeCount = 0;
  }

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
    // The output function takes 1 argument, so we'll add it here
    outputTable.addParameter(false);
    symbolTable.addScope("output", outputTable);

    errorOccurred = false;

    processTree(tree, GLOBAL_SCOPE);

    symbolTable.removeAllEmpty();

    return symbolTable;
  }

  public boolean didErrorOccur()
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
        // TODO: Implement Function Declaration Processing
        processFunctionDeclaration(node, scope);
        break;
      }
      // If the node is a simple parameter ( int ID )...
      case META_PARAMETER:
      {
        // TODO: Implement Simple Parameter Processing
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
        // TODO: Implement If-Statement Processing
        processIfStatement(node, scope);
        break;
      }
      // If the node is a return statement...
      case STATEMENT_RETURN:
      {
        // TODO: Implement Return Statement Processing
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
    symbolTable.updateScope(scope, node, true);

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
      reportSemanticError(symbolTable.updateRecord(scope, node, false));
    }
  }

  private void processIfStatement(final AbstractSyntaxTreeNode node,
                                  final String scope)
  {
    if (node.getChild(0).getType() != Boolean.class)
    {
      reportSemanticError(SymbolTableCode.UPDATE_SEMANTIC_FAILURE);
    }

    processNode(node.getChild(0), scope);

    String ifScope = String.format("if_%d", ++anonymousScopeCount);

    AbstractSyntaxTreeNode ifNode = new IDExpressionNode();
    ifNode.setName(ifScope);

    symbolTable.updateScope(scope, ifNode, false);

    String newScope = String.format("%s.%s", scope, ifScope);

    processNode(node.getChild(1), newScope);

    if (node.getChild(2) == null)
    {
      return;
    }

    AbstractSyntaxTreeNode elseNode = new IDExpressionNode();
    String elseScope = String.format("else_%d", anonymousScopeCount);
    elseNode.setName(elseScope);

    symbolTable.updateScope(scope, elseNode, false);

    newScope = String.format("%s.%s", scope, elseScope);

    processNode(node.getChild(2), newScope);
  }

  private void reportSemanticError(final SymbolTableCode errorCode)
  {
    if (errorCode != SymbolTableCode.UPDATE_OK)
    {
      System.err.printf("SEMANTIC ERROR - %s\n", errorCode.toString());
      errorOccurred = true;
    }
  }
}
