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
public class SymbolTable extends SymbolItem
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
   * Get the symbol type associated with this particular realization of SymbolItem
   *
   * @return The enumeration that identifies the symbol item
   */
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_TABLE_SCOPE;
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

    String currentScope = SymbolTableUtilities.GetCurrentScope(scope);
    String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

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

  /**
   * Add a record to the symbol table, given a particular scope. Note
   * that if the provided scope does not exist, the record will not
   * added to the symbol table.
   *
   * @param scope
   * @param identifier
   * @param record
   */
  public void addRecord(final String scope,
                        final String identifier,
                        final SymbolRecord record)
  {
  }

  public void addScope(final String scope,
                       final String identifier,
                       final SymbolTable symbolTable)
  {

  }
}
