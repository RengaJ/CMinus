package codegen;

import analyzer.symbol.record.SymbolRecord;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import codegen.emitter.MIPSCodeEmitter;
import codegen.emitter.MIPSRegister;
import codegen.table.LocalTable;
import codegen.table.RegisterRecord;
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

  private LocalTable localTable;

  private FunctionSymbolTable functionTable;

  public CodeGenerator()
  {
    emitter       = null;
    localTable    = null;
    functionTable = null;
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
      System.out.println("Function: main");

      functionTable =
          (FunctionSymbolTable)symbolTable.getSymbolItem("", pair.name, true);
      processFunction();
      emitter.emitSystemExit();
      System.out.println("");
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
      System.out.println("Function: " + function.name);
      functionTable =
          (FunctionSymbolTable)symbolTable.getSymbolItem("", function.name, true);
      processFunction();
      System.out.println("");
    }
  }

  private void processFunction()
  {
    localTable = new LocalTable();

    AbstractSyntaxTreeNode functionRoot = functionTable.getNode();

    ArrayList<IdentifierPair> parameters = functionTable.getParameters();
    for (final IdentifierPair idPair : parameters)
    {
      SymbolRecord symbolRecord =
          (SymbolRecord)functionTable.getSymbolItem("", idPair.name, false);

      MIPSRegister register = MIPSRegister.valueOf(String.format("A%d", symbolRecord.getMemoryLocation()));

      RegisterRecord record = new RegisterRecord(register, 0,idPair.size * 4);

      localTable.addRecord(idPair.name, record);
    }
    while (functionRoot != null)
    {
      processNode(functionRoot);

      functionRoot = functionRoot.getSibling();
    }

    localTable.printTable();
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
        MIPSRegister register = MIPSRegister.valueOf(String.format("S%d", 0));
        RegisterRecord record = new RegisterRecord(register, 0, 4);
        localTable.addRecord(node.getName(), record);
        break;
      }
      case STATEMENT_ARRAY_DECLARATION:
      {
        int size = node.getChild(0).getValue() * 4;
        MIPSRegister register = MIPSRegister.valueOf(String.format("S%d", 0));
        RegisterRecord record = new RegisterRecord(register, 0, size);
        localTable.addRecord(node.getName(), record);
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
