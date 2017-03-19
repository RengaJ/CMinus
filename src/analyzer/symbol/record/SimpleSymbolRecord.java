package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 *
 */
public class SimpleSymbolRecord extends SymbolRecord
{
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD_SIMPLE;
  }
}
