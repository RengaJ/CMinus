package codegen.emitter;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Class that maintains and emits MIPS code for handling the stack
 */
public final class MemoryStack
{
  private MIPSCodeEmitter emitter;

  private Deque<DataType> currentStack;

  private int argumentCount;

  private int localCount;

  private boolean saveFP;

  public MemoryStack(final MIPSCodeEmitter mipsEmitter)
  {
    emitter = mipsEmitter;
    argumentCount = 0;
    localCount = 0;
    currentStack = new ArrayDeque<>();
    saveFP = false;

    currentStack.push(DataType.RETURN_ADDRESS);
  }

  public void addArgument()
  {
    currentStack.push(DataType.ARGUMENT);
    ++argumentCount;
  }

  public void addLocal()
  {
    currentStack.push(DataType.LOCAL);
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

  }

  public void emitStackPop()
  {
    int stackSize = 4 * (argumentCount + localCount + 1);

    if (stackSize % 8 != 0)
    {
      stackSize += 4;
    }

    emitter.emitStackPop(stackSize);

  }

  private enum DataType
  {
    ARGUMENT,
    LOCAL,
    RETURN_ADDRESS
  }
}
