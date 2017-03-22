package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 *
 */
public final class ArraySymbolRecord extends SymbolRecord
{
  private int size;

  public ArraySymbolRecord(final int declaredLine,
                           final Class<?> classType,
                           final int memoryLocation,
                           final int size)
  {
    super(declaredLine, classType, memoryLocation);

    this.size = size;
  }

  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD_ARRAY;
  }

  @Override
  public boolean isArray()
  {
    return true;
  }
}
