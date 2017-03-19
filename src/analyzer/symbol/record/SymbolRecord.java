package analyzer.symbol.record;

import analyzer.symbol.SymbolItem;

/**
 * Immutable class used to store information about an identifier in the symbol
 * table.
 */
public abstract class SymbolRecord extends SymbolItem
{
  public SymbolRecord(final int declaredLine, final Class<?> classType)
  {
    super(declaredLine, classType);
  }

  public abstract boolean isArray();
}
