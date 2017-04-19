package codegen;

import analyzer.symbol.SymbolItem;
import analyzer.symbol.record.SymbolRecord;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import codegen.emitter.MIPSCodeEmitter;
import codegen.emitter.MemoryStack;
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

  private LocalTable tempTable;

  private FunctionSymbolTable functionTable;

  public CodeGenerator()
  {
    emitter = null;
    tempTable = null;
    localTable = null;
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
      SymbolRecord symbolRecord = (SymbolRecord) symbolTable.getSymbolItem("", global.name, false);
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
          (FunctionSymbolTable) symbolTable.getSymbolItem("", pair.name, true);
      localTable = globalTable.copy();
      processFunction(pair.name, true);
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
          (FunctionSymbolTable) symbolTable.getSymbolItem("", function.name, true);
      localTable = globalTable.copy();
      processFunction(function.name, false);
      emitter.emitSeparator();
      System.out.println("\n");
    }

    emitter.emitInputFunction();
    emitter.emitSeparator();

    emitter.emitOutputFunction();
    emitter.emitSeparator();
  }

  private void processFunction(final String name, final boolean terminate)
  {
    AbstractSyntaxTreeNode functionRoot = functionTable.getNode();

    ArrayList<IdentifierPair> parameters = functionTable.getParameters();
    ArrayList<IdentifierPair> locals     = functionTable.getLocalIdentifiers();

    MemoryStack stack = new MemoryStack(emitter);

    for (int i = 0; i < parameters.size(); ++i)
    {
      stack.addArgument();
      IdentifierPair idPair = parameters.get(i);

      String register = String.format("$a%d", i);
      RegisterRecord record = new RegisterRecord(register, 0, 4);
      localTable.addRecord(idPair.name, record);
    }
    for (int i = 0; i < locals.size(); ++i)
    {
      stack.addLocal();
      IdentifierPair idPair = locals.get(i);

      String register = String.format("$s%d", i);
      RegisterRecord record = new RegisterRecord(register, 0, 4);
      localTable.addRecord(idPair.name, record);
    }

    stack.emitStackPush();

    while (functionRoot != null)
    {
      processNode(functionRoot, false);

      functionRoot = functionRoot.getSibling();
    }

    emitter.emitLabel(name + "_cleanup");
    stack.emitStackPop();

    if (terminate)
    {
      emitter.emitSyscall(10, null, false);
    } else
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

        AbstractSyntaxTreeNode bodyNode = node.getChild(1);

        boolean foundReturn = false;

        while (bodyNode != null)
        {
          foundReturn = bodyNode.getNodeType() == ASTNodeType.STATEMENT_RETURN;
          processNode(bodyNode, false);
          bodyNode = bodyNode.getSibling();
        }

        if (haveElse)
        {
          if (!foundReturn)
          {
            emitter.emitJump(node.getName() + "_end");
          }
          emitter.emitLabel(node.getName() + "_else");

          bodyNode = node.getChild(2);

          while (bodyNode != null)
          {
            foundReturn = bodyNode.getNodeType() == ASTNodeType.STATEMENT_RETURN;
            processNode(bodyNode, false);
            bodyNode = bodyNode.getSibling();
          }
        }
        if (!foundReturn)
        {
          emitter.emitLabel(node.getName() + "_end");
        }
        else if (node.getSibling() == null)
        {
          emitter.emitNoop();
        }
        break;
      }
      case STATEMENT_RETURN:
      {
        if (node.getChild(0) != null)
        {
          final String register = processNode(node.getChild(0), false);
          emitter.emitDataSave("$v0", register);
        }
        emitter.emitFunctionExit();
        break;
      }
      case STATEMENT_WHILE:
      {
        emitter.emitLabel(node.getName() + "_start");
        processOperator(node.getChild(0), false, node.getName(), false);

        AbstractSyntaxTreeNode bodyNode = node.getChild(1);

        while (bodyNode != null)
        {
          processNode(bodyNode, false);
          bodyNode = bodyNode.getSibling();
        }
        emitter.emitJump(node.getName() + "_start");
        emitter.emitLabel(node.getName() + "_end");
        if (node.getSibling() == null)
        {
          emitter.emitNoop();
        }
        break;
      }
      case STATEMENT_VAR_DECLARATION:
      {
        SymbolRecord item =
            (SymbolRecord)functionTable.getSymbolItem("", node.getName(), false);
        // Produce register
        final String register = String.format("$s%d", item.getId());
        RegisterRecord record = new RegisterRecord(register, 0, 4);
        emitter.emitRType("add", "$0", "$0", register);
        localTable.addRecord(node.getName(), record);
        break;
      }
      case STATEMENT_ARRAY_DECLARATION:
      {
        int size = node.getChild(0).getValue() * 4;
        RegisterRecord record = new RegisterRecord(null, 0, size);
        localTable.addRecord(node.getName(), record);
        break;
      }
      case STATEMENT_ASSIGN:
      {
        String valueReg = processNode(node.getChild(1), false);

        if (node.getChild(0).getNodeType() == ASTNodeType.EXPRESSION_ARRAY_IDENTIFIER)
        {
          AbstractSyntaxTreeNode arrayNode = node.getChild(0);
          RegisterRecord record = localTable.getRecord(arrayNode.getName());
          String offsetRegister = processNode(arrayNode.getChild(0), true);

          emitter.emitShift(offsetRegister, "$t7");
          emitter.emitStoreWord(record.getLabel(), offsetRegister, valueReg);
        }
        else
        {
          String register = processNode(node.getChild(0), true);
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
        else if (node.getName().equals("output"))
        {
          final String register = processNode(node.getChild(0), false);
          emitter.emitDataSave("$a0", register);
          processOutput();
        }
        else
        {
          AbstractSyntaxTreeNode argNode = node.getChild(0);

          String register;
          int childCount = 0;
          while (argNode != null)
          {
            register = processNode(argNode, false);
            emitter.emitStackPush(4);
            emitter.emitStackSave(register, 0);
            argNode = argNode.getSibling();
            childCount++;
          }

          for (int i = childCount - 1; i >= 0; i--)
          {
            emitter.emitStackRetrieve("$a" + i, (childCount - (i + 1)) * 4);
          }
          emitter.emitStackPop(childCount * 4);
          emitter.emitStackPush(4);
          emitter.emitStackSave("$ra", 0);

          emitter.emitFunctionCall(node.getName());
          emitter.emitStackRetrieve("$ra", 0);
          emitter.emitStackPop(4);
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
      case META_ANONYMOUS_BLOCK:
      {
        break;
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
    final String register1 = processNode(node.getChild(0), true);

    // STEP 2:
    if (register1.equals("$t0"))
    {
      emitter.emitStackPush(4);
      emitter.emitStackSave(register1, 0);
    }
    // STEP 3:
    final String register2 = processNode(node.getChild(1), false);

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
    String dest = "";
    // If we're looking at a relational operator...
    if (isCondition)
    {
      final String trueBranch = String.format("%s_body", branchRoot);
      String elseBranch;
      if (hasElse)
      {
        elseBranch = String.format("%s_else", branchRoot);
      } else
      {
        elseBranch = String.format("%s_end", branchRoot);
      }

      emitter.emitBranch(opcode, register1, register2, trueBranch, elseBranch);
    }
    // We're looking at a math operator
    else
    {
      dest = (isLeft) ? "$t0" : "$t1";
      emitter.emitRType(opcode, register1, register2, dest);
    }

    return dest;
  }

  private boolean processScope(final AbstractSyntaxTreeNode node,
                               final SymbolTable scopeTable)
  {
    if (scopeTable != null)
    {
      ArrayList<IdentifierPair> locals = scopeTable.getLocalIdentifiers();
    }

    boolean endsWithReturn = false;



    return endsWithReturn;
  }

  private void processInput()
  {
    emitter.emitStackPush(8);
    emitter.emitStackSave("$a0", 0);
    emitter.emitStackSave("$ra", 4);
    emitter.emitFunctionCall("input");
    emitter.emitStackRetrieve("$ra", 4);
    emitter.emitStackRetrieve("$a0", 0);
    emitter.emitStackPop(8);
  }

  private void processOutput()
  {
    emitter.emitStackPush(8);
    emitter.emitStackSave("$a0", 0);
    emitter.emitStackSave("$ra", 4);
    emitter.emitFunctionCall("output");
    emitter.emitStackRetrieve("$ra", 4);
    emitter.emitStackRetrieve("$a0", 0);
    emitter.emitStackPop(8);
  }
}
