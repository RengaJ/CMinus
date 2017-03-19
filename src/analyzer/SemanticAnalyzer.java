package analyzer;

import analyzer.symbol.table.SymbolTable;
import syntaxtree.AbstractSyntaxTreeNode;

/**
 *
 */
public final class SemanticAnalyzer
{
  private SymbolTable symbolTable;

  public SemanticAnalyzer()
  {
    symbolTable = null;
  }

  public SymbolTable analyze(final AbstractSyntaxTreeNode tree)
  {
    symbolTable = new SymbolTable(-1, null);

    return symbolTable;
  }

  private void processNode(final AbstractSyntaxTreeNode node, final String scope)
  {
    // Perform different operations based on the type of the node
    // being processed
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
        break;
      }
      // If the node is a simple parameter ( int ID )...
      case META_PARAMETER:
      {
        // TODO: Implement Simple Parameter Processing
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
}
