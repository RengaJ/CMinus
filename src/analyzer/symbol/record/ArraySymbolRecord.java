package analyzer.symbol.record;

import analyzer.symbol.SymbolItemType;

/**
 * Class that represents a record used for containing array information.
 */
public final class ArraySymbolRecord extends SymbolRecord
{
  /**
   * The size of the array
   */
  private int size;

  /**
   * The full constructor for the ArraySymbolRecord
   * @param declaredLine   The line on which the array was declared
   * @param classType      The type of the array being declared
   * @param memoryLocation The memory location of the array being declared
   * @param size           The size of the array being declared
   */
  public ArraySymbolRecord(final int declaredLine,
                           final Class<?> classType,
                           final int memoryLocation,
                           final int size)
  {
    super(declaredLine, classType, memoryLocation);

    this.size = size;
  }

  /**
   * Return the SymbolItemType of the ArraySymbolRecord
   *
   * @return SYMBOL_RECORD_ARRAY
   */
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD_ARRAY;
  }

  /**
   * Determine if the record represents an array
   *
   * @return Flag indicating if the record represents an array
   */
  @Override
  public boolean isArray()
  {
    return true;
  }

  /**
   * Obtain the String representation of the record
   *
   * @return The String representation of the record
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder(super.toString());

    builder.append(String.format(" Array (Size: %d)", size));

    return builder.toString();
  }
}
