package codegen;

import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import codegen.emitter.MIPSCodeEmitter;
import globals.pair.IdentifierPair;
import syntaxtree.AbstractSyntaxTreeNode;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Main code generation class
 */
public final class CodeGenerator
{
  private MIPSCodeEmitter emitter;

  public CodeGenerator()
  {
    emitter = null;
  }

  public void generate(final AbstractSyntaxTreeNode root,
                       final SymbolTable symbolTable,
                       final String filename) throws IOException
  {
    if (emitter != null)
    {
      emitter.close();
    }

    emitter = new MIPSCodeEmitter(filename);
    emitter.emitHeader(symbolTable.getLocalIdentifiers());

    ArrayList<IdentifierPair> functions = symbolTable.getFunctionDefinitions();

    for (final IdentifierPair pair : functions)
    {
      if (!pair.name.equals("main"))
      {
        continue;
      }
      emitter.emitLabel(pair.name);
      processFunction(
          (FunctionSymbolTable)symbolTable.getSymbolItem("", pair.name, true));
      emitter.emitSystemExit();
    }

    for (final IdentifierPair function : functions)
    {
      if (function.name.equals("main") ||
          function.name.equals("input") ||
          function.name.equals("output"))
      {
        continue;
      }

      emitter.emitLabel(function.name);
      processFunction(
          (FunctionSymbolTable)symbolTable.getSymbolItem(
              "",
              function.name,
              true));
    }
  }

  private void processFunction(final FunctionSymbolTable functionTable)
  {
    AbstractSyntaxTreeNode functionRoot = functionTable.getNode();

    while (functionRoot != null)
    {
      processNode(functionRoot);

      functionRoot = functionRoot.getSibling();
    }
  }

  private void processNode(final AbstractSyntaxTreeNode node)
  {
    switch (node.getNodeType())
    {
      case STATEMENT_IF:
      {
        break;
      }
      case STATEMENT_RETURN:
      {
        break;
      }
      case STATEMENT_WHILE:
      {
        break;
      }
      case STATEMENT_VAR_DECLARATION:
      {
        break;
      }
      case STATEMENT_ARRAY_DECLARATION:
      {
        break;
      }
      case STATEMENT_ASSIGN:
      {
        break;
      }
      case EXPRESSION_ARRAY_IDENTIFIER:
      {
        break;
      }
      case EXPRESSION_IDENTIFIER:
      {
        break;
      }
      case EXPRESSION_CALL:
      {
        break;
      }
      case EXPRESSION_NUMBER:
      {
        break;
      }
      case EXPRESSION_OPERATION:
      {
        break;
      }
      default:
      {
        break;
      }
    }
  }
}
