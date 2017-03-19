package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 *
 */
public final class ArraySymbolRecord extends SymbolRecord
{
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD_ARRAY;
  }
}
