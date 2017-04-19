package codegen.emitter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class that maintains and emits MIPS code for handling the stack
 */
public final class MemoryStack
{
  private MIPSCodeEmitter emitter;

  private int argumentCount;

  private int localCount;

  private boolean saveFP;

  public MemoryStack(final MIPSCodeEmitter mipsEmitter)
  {
    emitter = mipsEmitter;
    argumentCount = 0;
    localCount = 0;
    saveFP = false;
  }

  public void addArgument()
  {
    ++argumentCount;
  }

  public void addLocal()
  {
    ++localCount;
  }

  public void emitStackPush()
  {
    int stackSize = 4 * (argumentCount + localCount + 1);

    if (stackSize % 8 != 0)
    {
      stackSize += 4;
    }

    emitter.emitStackPush(stackSize);
    String register;
    for (int i = 0; i < argumentCount; i++)
    {
      register = String.format("$a%d", i);
      emitter.emitStackSave(register, i * 4);
    }

    for (int i = 0; i < localCount; i++)
    {
      register = String.format("$s%d", i);
      emitter.emitStackSave(register, (argumentCount * 4) + (i * 4));
    }

    emitter.emitStackSave("$ra", stackSize - 4);

  }

  public void emitStackPop()
  {
    int stackSize = 4 * (argumentCount + localCount + 1);

    if (stackSize % 8 != 0)
    {
      stackSize += 4;
    }

    emitter.emitStackRetrieve("$ra", stackSize - 4);

    String register;

    for (int i = localCount-1; i >= 0; --i)
    {
      register = String.format("$s%d", i);
      emitter.emitStackRetrieve(register, (argumentCount * 4) + (i * 4));
    }

    for (int i = argumentCount - 1; i >= 0; --i)
    {
      register = String.format("$a%d", i);
      emitter.emitStackRetrieve(register, i * 4);
    }

    emitter.emitStackPop(stackSize);

  }
}
