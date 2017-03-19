package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 *
 */
public final class ArraySymbolRecord extends SymbolRecord
{
  public ArraySymbolRecord(int declaredLine, Class<?> classType)
  {
    super(declaredLine, classType);
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
