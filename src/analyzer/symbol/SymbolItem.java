package analyzer.symbol;

import java.util.ArrayList;

/**
 * Abstract class used to group symbol tables and symbol records together.
 * This class contains the fields and functions that are common to both
 * the SymbolTable and the SymbolRecord
 */
public abstract class SymbolItem
{
  /**
   * A list of the lines that indicate where the current symbol item
   * is being referenced. Does not include the line where the symbol
   * item is declared.
   */
  protected ArrayList<Integer> lines;

  /**
   * The line on which the symbol item is declared.
   */
  protected int declared;

  /**
   * The type of SymbolItem. For SymbolTables this is the return type of the
   * function (or null if this is a non-function SymbolTable) or the actual
   * object type of a SymbolRecord. Note that this will not identify if the
   * current SymbolRecord is an array.
   */
  protected Class<?> type;

  /**
   * Full constructor for the SymbolItem
   */
  public SymbolItem(final int declaredLine, final Class<?> classType)
  {
    lines = new ArrayList<>();

    declared = declaredLine;

    type = classType;
  }

  /**
   * Add a line of reference to the SymbolItem.
   *
   * @param line The line of reference to store within this SymbolItem
   */
  public void addLine(final int line)
  {
    lines.add(line);
  }

  /**
   * Retrieve the list of lines on which this SymbolItem is referenced
   *
   * @return An ArrayList of Integers that contains the line on which the
   *         SymbolItem is referenced
   */
  public final ArrayList<Integer> getLines()
  {
    return lines;
  }

  /**
   * Get the line number on which this SymbolItem was declared
   *
   * @return The initial line of declaration for this SymbolItem
   */
  public final int getDeclaredLine()
  {
    return declared;
  }

  /**
   * Retrieve the type of object the SymbolItem represents.
   *
   * @return The class type of the object that this SymbolItem represents
   */
  public final Class<?> getClassType()
  {
    return type;
  }

  /**
   * Get the symbol type associated with this particular realization of SymbolItem
   *
   * @return The enumeration that identifies the symbol item
   */
  public abstract SymbolItemType getSymbolType();

  /**
   * Obtain the String representation of the symbol item
   *
   * @return The String representation of the symbol item
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder("");

    builder.append(String.format(" - %s - ", type.getSimpleName()));

    builder.append("Lines: [");
    for (int i = 0; i < lines.size(); ++i)
    {
      builder.append(String.format("%d", lines.get(i)));
      if (i + 1 < lines.size())
      {
        builder.append(", ");
      }
    }

    builder.append(String.format("] Declared: %d", declared));

    return builder.toString();
  }
}
