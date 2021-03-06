package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 * Class that represents a record used for containing simple
 * identifiers
 */
public final class SimpleSymbolRecord extends SymbolRecord
{
  /**
   * Full constructor for the SimpleSymbolRecord
   * @param declaredLine   The line on which the identifier was declared
   * @param classType      The type of the identifier being declared
   * @param memoryLocation The memory location of the identifier
   */
  public SimpleSymbolRecord(final int declaredLine,
                            final Class<?> classType,
                            final int memoryLocation,
                            final int id)
  {
    super(declaredLine, classType, memoryLocation, id);
  }

  /**
   * Return the SymbolItemType of the SimpleSymbolRecord
   *
   * @return SYMBOL_RECORD_SIMPLE
   */
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD_SIMPLE;
  }

  @Override
  public int getSize()
  {
    return 1;
  }

  /**
   * Determine if the record represents an array
   *
   * @return Flag indicating if the record represents an array
   */
  @Override
  public boolean isArray()
  {
    return false;
  }
}
