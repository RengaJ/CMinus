package codegen;

import analyzer.symbol.record.SymbolRecord;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import codegen.emitter.MIPSCodeEmitter;
import codegen.emitter.MIPSRegister;
import codegen.table.LocalTable;
import codegen.table.RegisterRecord;
import globals.pair.IdentifierPair;
import syntaxtree.ASTNodeType;
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

    ArrayList<IdentifierPair> globals = symbolTable.getLocalIdentifiers();
    LocalTable globalTable = new LocalTable();
    for (final IdentifierPair global : globals)
    {
      SymbolRecord symbolRecord   = (SymbolRecord)symbolTable.getSymbolItem("", global.name, false);
      RegisterRecord globalRecord = new RegisterRecord(null, symbolRecord.getMemoryLocation(), 4 * symbolRecord.getSize());
      globalRecord.setLabel(global.name);

      globalTable.addRecord(global.name, globalRecord);
    }

    globalTable.printTable();
    System.out.println("");

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
      localTable = globalTable.copy();
      processFunction(true);
      emitter.emitSeparator();
      System.out.println("");
      break;
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
      localTable = globalTable.copy();
      processFunction(false);
      emitter.emitSeparator();
      System.out.println("\n");
    }

    emitter.emitInputFunction();
    emitter.emitSeparator();

    emitter.emitOutputFunction();
    emitter.emitSeparator();
  }

  private void processFunction(final boolean terminate)
  {
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
      processNode(functionRoot, false);

      functionRoot = functionRoot.getSibling();
    }

    if (terminate)
    {
      emitter.emitSyscall(10, null, false);
    }
    else
    {
      emitter.emitFunctionExit();
    }

    localTable.printTable();
  }

  private String processNode(final AbstractSyntaxTreeNode node,
                             final boolean isLeft)
  {
    switch (node.getNodeType())
    {
      case STATEMENT_IF:
      {
        final boolean haveElse = node.getChild(2) != null;
        processOperator(node.getChild(0), false, node.getName(), haveElse);
        processNode(node.getChild(1), false);

        if (haveElse)
        {
          emitter.emitJump(node.getName() + "_end");
          emitter.emitLabel(node.getName() + "_else");
          processNode(node.getChild(2), false);
        }
        emitter.emitLabel(node.getName() + "_end");
        if (node.getSibling() == null)
        {
          emitter.emitNoop();
        }
        break;
      }
      case STATEMENT_RETURN:
      {
        if (node.getChild(0) != null)
        {
          processNode(node.getChild(0), false);
        }
        emitter.emitReturn();
        break;
      }
      case STATEMENT_WHILE:
      {
        emitter.emitLabel(node.getName() + "_start");
        processOperator(node.getChild(0), false, node.getName(), false);
        processNode(node.getChild(1), false);
        emitter.emitJump(node.getName() + "_start");
        emitter.emitLabel(node.getName() + "_end");
        break;
      }
      case STATEMENT_VAR_DECLARATION:
      {
        MIPSRegister register = MIPSRegister.valueOf(String.format("S%d", 0));
        RegisterRecord record = new RegisterRecord(register, 0, 4);
        emitter.emitRType("addi", "$0", "$0", register.getRegister());
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
        String register = processNode(node.getChild(0), false);
        String valueReg = processNode(node.getChild(1), true);

        if (node.getChild(0).getNodeType() == ASTNodeType.EXPRESSION_ARRAY_IDENTIFIER)
        {

        }
        else
        {
          emitter.emitDataSave(register, valueReg);
        }
        break;
      }
      case EXPRESSION_ARRAY_IDENTIFIER:
      {
        break;
      }
      case EXPRESSION_IDENTIFIER:
      {
        RegisterRecord record = localTable.getRecord(node.getName());
        return record.getLabel();
      }
      case EXPRESSION_CALL:
      {
        if (node.getName().equals("input"))
        {
          processInput();
        }
        return "$v0";
      }
      case EXPRESSION_NUMBER:
      {
        final String register = (isLeft) ? "$t0" : "$t1";
        final String value = Integer.toString(node.getValue());
        // addi <register>, $0, <value>
        emitter.emitRType("addi", "$0", value, register);
        return register;
      }
      case EXPRESSION_OPERATION:
      {
        return processOperator(node, isLeft, "", false);
      }
      default:
      {
        break;
      }
    }

    return "";
  }

  private String processOperator(final AbstractSyntaxTreeNode node,
                                 final boolean isLeft,
                                 final String branchRoot,
                                 final boolean hasElse)
  {
    // All operators are to be processed in the following order:
    // 1. Recursively process the left child (each mathematical operator has
    //    left-associativity that needs to be preserved) (should be $t0)
    // 2. Push $t0 on the stack (create a new stack to preserve the value of
    //    $t0 in-case it gets used in the next processing step)
    // 3. Recursively process the right child (should be $t1)
    // 4. Restore the value of $t0 from the stack (popping the stack frame)
    // 5. Determine the correct operation that should be used
    // 6. Print out the operation

    // STEP 1:
    final String register1 = processNode(node.getChild(0), false);

    // STEP 2:
    if (register1.equals("$t0"))
    {
      emitter.emitStackPush(4);
      emitter.emitStackSave(register1, 0);
    }
    // STEP 3:
    final String register2 = processNode(node.getChild(1), true);

    // STEP 4:
    if (register1.equals("$t0"))
    {
      emitter.emitStackRetrieve(register1, 0);
      emitter.emitStackPop(4);
    }

    String opcode;
    boolean isCondition = false;
    switch (node.getTokenType())
    {
      case SPECIAL_GREATER_THAN:
      {
        opcode = "bgt";
        isCondition = true;
        break;
      }
      case SPECIAL_GTE:
      {
        opcode = "bge";
        isCondition = true;
        break;
      }
      case SPECIAL_LESS_THAN:
      {
        opcode = "blt";
        isCondition = true;
        break;
      }
      case SPECIAL_LTE:
      {
        opcode = "ble";
        isCondition = true;
        break;
      }
      case SPECIAL_EQUAL:
      {
        opcode = "beq";
        isCondition = true;
        break;
      }
      case SPECIAL_NOT_EQUAL:
      {
        opcode = "bne";
        isCondition = true;
        break;
      }
      case SPECIAL_PLUS:
      {
        opcode = "add";
        break;
      }
      case SPECIAL_MINUS:
      {
        opcode = "sub";
        break;
      }
      case SPECIAL_TIMES:
      {
        opcode = "mul";
        break;
      }
      case SPECIAL_DIVIDE:
      {
        opcode = "div";
        break;
      }
      default:
      {
        return "";
      }
    }

    // If we're looking at a relational operator...
    if (isCondition)
    {
      final String trueBranch = String.format("%s_body", branchRoot);
      String elseBranch;
      if (hasElse)
      {
        elseBranch = String.format("%s_else", branchRoot);
      }
      else
      {
        elseBranch = String.format("%s_end", branchRoot);
      }

      emitter.emitBranch(opcode, register1, register2, trueBranch, elseBranch);
    }
    // We're looking at a math operator
    else
    {
      final String dest = (isLeft) ? "$t0" : "$t1";
      emitter.emitRType(opcode, register1, register2, dest);
    }

    return "";
  }

  private void processInput()
  {
    emitter.emitStackPush(8);
    emitter.emitStackSave("$a0", 0);
    emitter.emitStackSave("$ra", 4);
    emitter.emitFunctionCall("__input");
    emitter.emitStackRetrieve("$ra", 4);
    emitter.emitStackRetrieve("$a0", 0);
    emitter.emitStackPop(8);
  }
}
