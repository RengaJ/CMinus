package codegen.emitter;

/**
 * Class that maintains and emits MIPS code for handling the stack
 */
public final class MemoryStack
{
  /** A reference to the MIPSCodeEmitter to emit MIPS code */
  private MIPSCodeEmitter emitter;

  /** The number of arguments in the stack */
  private int argumentCount;

  /** The number of local variables in the stack */
  private int localCount;

  /**
   * Full constructor for the MemoryStack
   *
   * @param mipsEmitter The MIPSCodeEmitter that is to be associated
   *                    with this stack
   */
  public MemoryStack(final MIPSCodeEmitter mipsEmitter)
  {
    emitter = mipsEmitter;
    argumentCount = 0;
    localCount = 0;
  }

  /**
   * Add an argument to the stack.
   */
  public void addArgument()
  {
    ++argumentCount;
  }

  /**
   * Add a local variable to the stack.
   */
  public void addLocal()
  {
    ++localCount;
  }

  /**
   * Emit the MIPS code to push all of the provided items onto the stack.
   */
  public void emitStackPush()
  {
    // Calculate the stack size:
    // (# arguments + # locals + 1) * 4
    //   (1 is used for the return address)
    int stackSize = 4 * (argumentCount + localCount + 1);

    // Check to see if we need a pad (by convention, MIPS stack frames are
    // to be on 8-byte boundaries
    if (stackSize % 8 != 0)
    {
      stackSize += 4;
    }

    // Create the stack frame
    emitter.emitStackPush(stackSize);
    String register;
    // Push the arguments to the stack
    for (int i = 0; i < argumentCount; i++)
    {
      register = String.format("$a%d", i);
      emitter.emitStackSave(register, i * 4);
    }

    // Push the local variables to the stack
    for (int i = 0; i < localCount; i++)
    {
      register = String.format("$s%d", i);
      emitter.emitStackSave(register, (argumentCount * 4) + (i * 4));
    }

    // Save the return address (stored in $ra) to the stack
    emitter.emitStackSave("$ra", stackSize - 4);

  }

  /**
   * Emit the MIPS code to pop all of the provided items from the stack.
   */
  public void emitStackPop()
  {
    // Calculate the stack size:
    // (# arguments + # locals + 1) * 4
    //   (1 is used for the return address)
    int stackSize = 4 * (argumentCount + localCount + 1);

    // Check to see if we need a pad (by convention, MIPS stack frames are
    // to be on 8-byte boundaries
    if (stackSize % 8 != 0)
    {
      stackSize += 4;
    }

    // Retrieve the return address (stored in $ra) from the stack
    emitter.emitStackRetrieve("$ra", stackSize - 4);

    String register;
    // Pop the local variables from the stack
    for (int i = localCount-1; i >= 0; --i)
    {
      register = String.format("$s%d", i);
      emitter.emitStackRetrieve(register, (argumentCount * 4) + (i * 4));
    }

    // Pop the arguments from the stack
    for (int i = argumentCount - 1; i >= 0; --i)
    {
      register = String.format("$a%d", i);
      emitter.emitStackRetrieve(register, i * 4);
    }

    // Pop the stack frame
    emitter.emitStackPop(stackSize);
  }
}
