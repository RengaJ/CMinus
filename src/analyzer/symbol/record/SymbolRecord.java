package analyzer.symbol.record;

import analyzer.symbol.SymbolItem;

/**
 * Class used to store information about an identifier in the symbol
 * table.
 */
public abstract class SymbolRecord extends SymbolItem
{
  /**
   * The memory location of the record where it was declared. Note
   * that if the symbol record is a parameter, it's actually the
   * parameter number
   */
  private int memoryLocation;

  /**
   * Flag identifying the record as a parameter or not
   */
  private boolean isParameter;

  /**
   * Variable identifying the number of the local identifier
   * (used in code generation)
   */
  private int id;

  /**
   * Full constructor for the SymbolRecord
   *
   * @param declaredLine   The line on which the record was declared
   * @param classType      The type of identifying being declared
   * @param memoryLocation The memory location of the identifier
   */
  SymbolRecord(final int declaredLine,
               final Class<?> classType,
               final int memoryLocation,
               final int id)
  {
    super(declaredLine, classType);

    this.memoryLocation = memoryLocation;

    this.id = id;
  }

  /**
   * Mark (or unmark) the record as a parameter
   *
   * @param parameterStatus Flag indicating this record as a parameter
   */
  public void makeParameter(final boolean parameterStatus)
  {
    isParameter = parameterStatus;
  }

  public boolean isParameter()
  {
    return isParameter;
  }

  /**
   * Retrieve the memory location of the record
   *
   * @return The memory location of the record
   */
  public int getMemoryLocation()
  {
    return memoryLocation;
  }

  /**
   * Determine if the record represents an array
   *
   * @return Flag indicating if the record represents an array
   */
  public abstract boolean isArray();

  public abstract int getSize();

  public int getId() { return id; }

  /**
   * Obtain the String representation of the record
   *
   * @return The String representation of the record
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder(super.toString());

    if (isParameter)
    {
      builder = builder.append(" Parameter Location: ");
    }
    else
    {
      builder = builder.append(" Memory Location: ");
    }
    builder.append(memoryLocation);

    return builder.toString();
  }
}
