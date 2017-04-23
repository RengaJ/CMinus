package codegen;

import analyzer.symbol.record.SymbolRecord;
import analyzer.symbol.table.FunctionSymbolTable;
import analyzer.symbol.table.SymbolTable;
import codegen.emitter.MIPSCodeEmitter;
import codegen.emitter.MemoryStack;
import codegen.table.LocalTable;
import codegen.table.RegisterRecord;
import globals.CompilerFlags;
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
  /**
   * Code emitter used to emit MIPS code
   */
  private MIPSCodeEmitter emitter;

  /**
   * Main LocalTable used for register lookup
   */
  private LocalTable localTable;

  /**
   * Temporary LocalTable used for scope traversal
   */
  private LocalTable tempTable;

  /**
   * Name of the current function
   */
  private String currentFunctionName;

  /**
   * The SymbolTable for the current scope or function
   */
  private SymbolTable scopeTable;

  /**
   * A symbol table used for reserving the current scope table
   * when traversing into additional scopes
   */
  private SymbolTable currentTable;

  /**
   * The full constructor for the CodeGenerator class
   */
  public CodeGenerator()
  {
    emitter = null;
    tempTable = null;
    localTable = null;
    scopeTable = null;
    currentTable = null;
    currentFunctionName = "";
  }

  /**
   * The main code generation function. This function will take a root symbol
   * table and a filename and produce the MIPS assembly code that will properly
   * execute the compiled code. The produced code will reside in [filename].asm
   *
   * @param symbolTable  The root SymbolTable for code generation.
   * @param filename     The name of the file (without an extension)
   * @throws IOException Thrown if there is an issue with code emission
   */
  public void generate(final SymbolTable symbolTable,
                       final String filename) throws IOException
  {
    // Check to see if this is not the first time this function is called.
    if (emitter != null)
    {
      // Close the emitter if it's still open
      emitter.close();
    }

    // Reset the object state
    tempTable = null;
    localTable = null;
    scopeTable = null;
    currentTable = null;
    currentFunctionName = "";

    // Create a MIPSCodeEmitter with the provided file name
    emitter = new MIPSCodeEmitter(filename);

    // Emit the assembly header (allocating space for the global identifiers)
    emitter.emitHeader(symbolTable.getLocalIdentifiers());

    // Obtain the global identifiers to store in the local table
    final ArrayList<IdentifierPair> globals = symbolTable.getLocalIdentifiers();
    LocalTable globalTable = new LocalTable();
    for (final IdentifierPair global : globals)
    {
      // Create internal records for the LocalTable.
      // First, obtain the SymbolRecord in the SymbolTable corresponding to the
      // desired identifier.
      SymbolRecord symbolRecord =
          (SymbolRecord) symbolTable.getSymbolItem("", global.name, false);
      // Create a RegisterRecord to add to the local table.
      RegisterRecord globalRecord =
          new RegisterRecord(global.name,
                             symbolRecord.getMemoryLocation(),
                             4 * symbolRecord.getSize());

      // Add the record to the LocalTable object for the global identifier
      globalTable.addRecord(global.name, globalRecord);
    }

    // Print out the global table if tracing is enabled.
    if (CompilerFlags.TraceGenerator)
    {
      globalTable.printTable();
    }
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

      FunctionSymbolTable functionTable =
          (FunctionSymbolTable) symbolTable.getSymbolItem("", pair.name, true);
      localTable = globalTable.copy();
      currentFunctionName = pair.name;
      processFunction(functionTable, true);
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
      FunctionSymbolTable functionTable =
          (FunctionSymbolTable) symbolTable.getSymbolItem("", function.name, true);
      localTable = globalTable.copy();
      currentFunctionName = function.name;
      processFunction(functionTable, false);
      emitter.emitSeparator();
      System.out.println("\n");
    }

    emitter.emitInputFunction();
    emitter.emitSeparator();

    emitter.emitOutputFunction();
    emitter.emitSeparator();
  }

  private void processFunction(final FunctionSymbolTable functionTable,
                               final boolean terminate)
  {
    currentTable = functionTable;

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

    emitter.emitLabel(currentFunctionName + "_cleanup");
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

        tempTable = localTable.copy();

        SymbolTable symbolTable =
            (SymbolTable) currentTable.getSymbolItem("", node.getName(), true);
        boolean foundReturn = processScope(node.getChild(1), symbolTable);

        localTable = tempTable;

        if (haveElse)
        {
          if (!foundReturn)
          {
            emitter.emitJump(node.getName() + "_end");
          }
          emitter.emitLabel(node.getName() + "_else");

          tempTable = localTable.copy();

          String symbolName = node.getName().replace("if", "else");

          symbolTable =
              (SymbolTable) currentTable.getSymbolItem("", symbolName, true);
          foundReturn = processScope(node.getChild(2), symbolTable);

          localTable = tempTable;
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
        emitter.emitJump(currentFunctionName + "_cleanup");
        break;
      }
      case STATEMENT_WHILE:
      {
        emitter.emitLabel(node.getName() + "_start");
        processOperator(node.getChild(0), false, node.getName(), false);

        tempTable = localTable.copy();

        SymbolTable symbolTable =
            (SymbolTable) currentTable.getSymbolItem("", node.getName(), true);
        processScope(node.getChild(1), symbolTable);

        localTable = tempTable;

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
            (SymbolRecord) currentTable.getSymbolItem("", node.getName(), false);
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

        //TODO

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
        RegisterRecord record = localTable.getRecord(node.getName());

        String offsetRecord = processNode(node.getChild(0), false);

        emitter.emitShift(offsetRecord, "$t7");
        emitter.emitLoadWord("$t6", record.getLabel(), "$t7");

        return "$t6";
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
        tempTable = localTable.copy();

        SymbolTable symbolTable =
            (SymbolTable) currentTable.getSymbolItem("", node.getName(), true);
        processScope(node.getChild(0), symbolTable);

        localTable = tempTable;

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
    boolean needsStack = scopeTable != null;
    ArrayList<String> registerStack = new ArrayList<>();
    int stackSize = 0;
    if (needsStack)
    {
      int registerCount = 0;
      ArrayList<IdentifierPair> locals = scopeTable.getLocalIdentifiers();
      for (final IdentifierPair localId : locals)
      {
        if (!localTable.idExists(localId.name))
        {
          String fullRegister = String.format("$s%d", registerCount);
          while (localTable.registerExists(fullRegister))
          {
            ++registerCount;
            fullRegister = String.format("$s%d", registerCount);
          }
          RegisterRecord record = new RegisterRecord(fullRegister, 0, 4);
          localTable.addRecord(localId.name, record);
          registerStack.add(fullRegister);
        }

        this.scopeTable = currentTable;
        currentTable = scopeTable;
      }

      stackSize = registerStack.size() * 4;

      emitter.emitStackPush(stackSize);
      for (int i = 0; i < registerStack.size(); ++i)
      {
        emitter.emitStackSave(registerStack.get(i), i * 4);
      }
    }

    boolean endsWithReturn = false;

    AbstractSyntaxTreeNode statementNode = node;
    while (statementNode != null)
    {
      if (statementNode.getNodeType() == ASTNodeType.STATEMENT_RETURN)
      {
        endsWithReturn = true;
      }
      processNode(statementNode, false);
      statementNode = statementNode.getSibling();
    }

    if (needsStack)
    {
      currentTable = this.scopeTable;

      for (int i = registerStack.size()-1; i >= 0; --i)
      {
        emitter.emitStackRetrieve(registerStack.get(i), i * 4);
      }

      emitter.emitStackPop(stackSize);
    }

    return endsWithReturn;
  }

  /**
   * Write the MIPS code that will call the input__ function
   */
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

  /**
   * Write the MIPS code that will call the output__ function
   */
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
