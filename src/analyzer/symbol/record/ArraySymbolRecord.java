package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 *
 */
public class ArraySymbolRecord extends SymbolRecord
{
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD_ARRAY;
  }
}
