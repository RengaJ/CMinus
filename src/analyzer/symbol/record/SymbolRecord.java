package analyzer.symbol.record;

import analyzer.symbol.SymbolItem;

/**
 * Immutable class used to store information about an identifier in the symbol
 * table.
 */
public abstract class SymbolRecord extends SymbolItem
{
  private int memoryLocation;

  private boolean isParameter;

  public SymbolRecord(final int declaredLine,
                      final Class<?> classType,
                      final int memoryLocation)
  {
    super(declaredLine, classType);

    this.memoryLocation = memoryLocation;
  }

  public void setIsParameter(final boolean parameterStatus)
  {
    isParameter = parameterStatus;
  }

  public int getMemoryLocation()
  {
    return memoryLocation;
  }

  public abstract boolean isArray();

  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder(super.toString());

    if (isParameter)
    {
      builder = builder.append(" Parameter Location: ");
    }
    else
    {
      builder = builder.append(" Memory Location: ");
    }
    builder.append(memoryLocation);

    return builder.toString();
  }
}
