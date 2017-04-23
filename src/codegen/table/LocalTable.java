package codegen.table;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Class for storing a ID/Register mapping
 */
public final class LocalTable
{
  /**
   * Register map
   */
  private HashMap<String, RegisterRecord> table;

  /**
   * Full constructor for the LocalTable
   */
  public LocalTable()
  {
    table = new LinkedHashMap<>();
  }

  /**
   * Add a record to the local table
   * @param id     The identifier name
   * @param record The associated RegisterRecord
   */
  public void addRecord(final String id, final RegisterRecord record)
  {
    table.put(id, record);
  }

  /**
   * Obtain a RegisterRecord
   */
  public RegisterRecord getRecord(final String id)
  {
    return table.get(id);
  }

  /**
   * Obtain a copy of the this table
   * @return A copy of the table
   */
  public LocalTable copy()
  {
    LocalTable localTable = new LocalTable();

    Object clonedTable = table.clone();
    if (clonedTable instanceof LinkedHashMap)
    {
      localTable.table = (LinkedHashMap) clonedTable;
    }
    return localTable;
  }

  /**
   * Check if the ID exists
   * @param id The id in question
   * @return T/F if the id exists in the table
   */
  public boolean idExists(final String id)
  {
    return table.containsKey(id);
  }

  /**
   * Check if the register exists in the table
   *
   * @param register The register to check
   * @return T/F if the register exists in the table
   */
  public boolean registerExists(final String register)
  {
    for (final String id : table.keySet())
    {
      if (table.get(id).getLabel().equals(register))
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Print the the table to the console
   */
  public void printTable()
  {
    System.out.println(" ID     | Label  | Offset | Size");
    System.out.println("--------|--------|--------|-----");

    if (table.isEmpty())
    {
      System.out.println(" <NONE> | <NONE> | <NONE> | <NONE>");
      return;
    }
    for (final String id : table.keySet())
    {
      final RegisterRecord record = table.get(id);

      System.out.println(
          String.format(
              " %-5s  | %-5s  | %-6d | %-4d",
              id,
              record.getLabel(),
              record.getOffset(),
              record.getSize()));
    }
  }

}
