package analyzer.symbol.record;

import analyzer.symbol.SymbolItem;

/**
 * Immutable class used to store information about an identifier in the symbol
 * table.
 */
public abstract class SymbolRecord extends SymbolItem
{
  private int memoryLocation;

  public SymbolRecord(final int declaredLine,
                      final Class<?> classType,
                      final int memoryLocation)
  {
    super(declaredLine, classType);

    this.memoryLocation = memoryLocation;
  }

  public int getMemoryLocation()
  {
    return memoryLocation;
  }

  public abstract boolean isArray();
}
