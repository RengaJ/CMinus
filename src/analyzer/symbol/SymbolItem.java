package analyzer.symbol;

/**
 * Interface used to group symbol tables and symbol records together
 */
public interface SymbolItem
{
  /**
   * Get the symbol type associated with this particular realization of SymbolItem
   *
   * @return The enumeration that identifies the symbol item
   */
  SymbolItemType getSymbolType();
}
