package analyzer.symbol.table;

import analyzer.symbol.SymbolItem;
import analyzer.symbol.SymbolItemType;
import analyzer.symbol.record.SymbolRecord;

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
public abstract class SymbolTable extends SymbolItem
{
  /**
   * The internal HashMap being wrapped by the SymbolTable
   */
  private HashMap<String, SymbolItem> table;

  /**
   * The full constructor for the Symbol Table
   */
  public SymbolTable(final int declaredLine, final Class<?> type)
  {
    super(declaredLine, type);

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

  /**
   * Attempt to locate the most-scoped version of an identifier based upon a given
   * scope string
   *
   * @param scope       The scope string used to identify how far to look into the
   *                    symbol table. A scope string consists of multiple scopes
   *                    separated by a period ('.'). Example:
   *                        "scope1.scope2.scope3" indicates three scopes to examine
   * @param identifier  The identifier to look for.
   *
   * @return The most scoped SymbolItem that was able to be located, or null if no
   *         such identifier exists in the symbol table
   */
  public SymbolItem getSymbolItem(String scope, String identifier)
  {
    // Base case:
    // If no scope is provided, simply attempt to retrieve the
    // identifier from the current symbol table.
    if (scope.isEmpty())
    {
      return table.get(identifier);
    }

    // Recursive case:
    // Look for remaining scopes and continue the descent from there
    SymbolItem item = null;
    String currentScope;
    String remainingScope;
    int firstScopeEnd = scope.indexOf('.');

    // Check to see if there is only one scope remaining in the list of scopes
    if (firstScopeEnd == -1)
    {
      // If so, the current scope is the entire scope string, and the remaining
      // scope is the empty string (useful for the base-case)
      currentScope = scope;
      remainingScope = "";
    }
    // Otherwise multiple scopes remain in the list of scopes
    else
    {
      // Split the first and remaining scopes from each other
      currentScope = scope.substring(0, firstScopeEnd);
      remainingScope = scope.substring(firstScopeEnd + 1);
    }

    // Get the current scope from the symbol table (hopefully it exists!)
    SymbolItem scopeItem = table.get(currentScope);

    // If the scope does exist...
    if (scopeItem != null)
    {
      // Make sure the scope item is a symbol table
      // (otherwise we may have a problem)
      if (scopeItem.getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION ||
          scopeItem.getSymbolType() == SymbolItemType.SYMBOL_TABLE_SCOPE)
      {
        // Perform a recursive descent into the new symbol table, using the
        // remaining scope as the current scope level
        SymbolTable symbolTable = (SymbolTable) scopeItem;
        item = symbolTable.getSymbolItem(remainingScope, identifier);
      }
    }

    // If, after the recursive descent, nothing has been found, attempt to retrieve
    // the identifier at the current scope level
    if (item == null)
    {
      item = table.get(identifier);
    }

    // At this point, the item may actually still be null if the identifier was
    // unable to be located.
    return item;
  }

  public void addRecord(final String identifier, final SymbolRecord record)
  {
//    if (!table.containsKey(identifier))
//    {
//      SymbolRecordList recordList = new SymbolRecordList();
//
//      recordList.add(record);
//      table.put(identifier, recordList);
//    }
//    else
//    {
//      SymbolItem item = table.get(identifier);
//      if (item.getSymbolType() == SymbolItemType.SYMBOL_RECORD)
//      {
//        SymbolRecordList list = (SymbolRecordList)item;
//        list.add(record);
//
//        table.put(identifier, list);
//      }
//    }
  }
}
