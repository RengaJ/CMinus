package analyzer.symbol;

import java.util.HashMap;

/**
 * Class that represents a Symbol Table data structure.
 *
 * The Symbol Table contains the identifiers within a particular scoping level. It
 * is typically represented as a hash table used for rapid I/O operations. For this
 * implementation, the Symbol Table is a wrapper around a {@link java.util.HashMap}.
 * The map will contain both {@link SymbolRecord}s and other SymbolTables.
 *
 * Having the SymbolTable store other SymbolTables will allow for scope associations
 * to be made. For example, a function declaration would have it's own scope, and can
 * easily be identified through the use of another SymbolTable.
 */
public class SymbolTable implements SymbolItem
{
  /**
   * The internal HashMap being wrapped by the SymbolTable
   */
  private HashMap<String, SymbolItem> table;

  /**
   * The full constructor for the Symbol Table
   */
  public SymbolTable()
  {
    table = new HashMap<>();
  }

  /**
   * Add an identified scope and its table into this SymbolTable.
   *
   * @param scopeName  The name of the scope being inserted
   * @param scopeTable The completed SymbolTable containing the scoped identifiers
   */
  public void addScope(final String scopeName, final SymbolTable scopeTable)
  {
    table.put(scopeName, scopeTable);
  }

  public void addRecord(final String identifier, final SymbolRecord record)
  {
    if (!table.containsKey(identifier))
    {
      SymbolRecordList recordList = new SymbolRecordList();

      recordList.add(record);
      table.put(identifier, recordList);
    }
    else
    {
      SymbolItem item = table.get(identifier);
      if (item.getSymbolType() == SymbolItemType.SYMBOL_RECORD)
      {
        SymbolRecordList list = (SymbolRecordList)item;
        list.add(record);

        table.put(identifier, list);
      }
    }
  }

  //////////////////////////////// Interface Methods ////////////////////////////////
  /**
   * Get the symbol type associated with this particular realization of SymbolItem
   *
   * @return SYMBOL_TABLE
   */
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_TABLE;
  }
}
