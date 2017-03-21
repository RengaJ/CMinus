package analyzer.symbol.table;

import analyzer.symbol.*;
import analyzer.symbol.record.*;
import syntaxtree.AbstractSyntaxTreeNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

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
   * Add or update a record to the symbol table, given a particular scope.
   * Note that if the provided scope does not exist, the record will not
   * added to the symbol table.
   *
   * @param scope       The scope to traverse down to
   * @param identifier  The AST node that will be used to create / update
   *                    the (potentially) new record
   * @param isArray     An indicator to determine if the AST node is an array
   *
   * @return An error code enumeration providing details on what occurred during
   *         attempt to update/add a record
   */
  public SymbolTableCode updateRecord(final String scope,
                                      final AbstractSyntaxTreeNode identifier,
                                      final boolean isArray)
  {
    SymbolItem record = table.get(identifier.getName());
    // Check to see if additional traversal is needed
    if (scope.isEmpty())
    {
      // If the identifier cannot be found, a new record should be made
      if (record == null)
      {
        // If the record to be made is an array type
        if (isArray)
        {
          // Add a new array symbol record
          table.put(identifier.getName(),
              new ArraySymbolRecord(identifier.getLineNumber(),
                                    identifier.getType()));
        }
        else
        {
          // Add a new simple symbol record
          table.put(identifier.getName(),
                    new SimpleSymbolRecord(identifier.getLineNumber(),
                                           identifier.getType()));
        }
        // The record was properly added
        return SymbolTableCode.UPDATE_OK;
      }
      // A record has previously been recorded. Time to make sure
      // the record matches the desired type
      else
      {
        // If either:
        //     1. We want an array and the record is an array record
        //                            OR
        //     2. We don't want an array and the record is a simple record
        // Add the current line to the record and update the table with the
        // new record contents.
        if ((isArray &&
            record.getSymbolType() == SymbolItemType.SYMBOL_RECORD_ARRAY) ||
            (!isArray &&
                record.getSymbolType() == SymbolItemType.SYMBOL_RECORD_SIMPLE))
        {
          record.addLine(identifier.getLineNumber());
          table.put(identifier.getName(), record);
          return SymbolTableCode.UPDATE_OK;
        }
      }

      return SymbolTableCode.UPDATE_NOT_FOUND;
    }

    String currentScope = SymbolTableUtilities.GetCurrentScope(scope);
    String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

    // If the scope item doesn't exist, exit early
    if (!table.containsKey(currentScope))
    {
      return SymbolTableCode.UPDATE_NO_SCOPE;
    }

    SymbolTableCode errorCode;
    SymbolItem scopeItem = table.get(currentScope);
    if ((scopeItem.getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION) ||
        (scopeItem.getSymbolType() == SymbolItemType.SYMBOL_TABLE_SCOPE))
    {
      errorCode = ((SymbolTable)scopeItem).updateRecord(remainingScope,
                                                        identifier,
                                                        isArray);
    }
    else
    {
      return SymbolTableCode.UPDATE_SEMANTIC_FAILURE;
    }

    if (errorCode != SymbolTableCode.UPDATE_OK && record != null)
    {
      // If either:
      //     1. We want an array and the record is an array record
      //                            OR
      //     2. We don't want an array and the record is a simple record
      // Add the current line to the record and update the table with the
      // new record contents.
      if ((isArray &&
          record.getSymbolType() == SymbolItemType.SYMBOL_RECORD_ARRAY) ||
          (!isArray &&
              record.getSymbolType() == SymbolItemType.SYMBOL_RECORD_SIMPLE))
      {
        record.addLine(identifier.getLineNumber());
        table.put(identifier.getName(), record);
        return SymbolTableCode.UPDATE_OK;
      }
    }

    // Only thing left to do is report a semantic failure
    return errorCode;
  }
  /**
   * Add or update a scope to the symbol table, given a particular scope.
   * Note that if the provided scope does not exist, the new scope will not
   * added to the symbol table.
   *
   * @param scope            The scope to traverse down to
   * @param scopeIdentifier  The AST node that will be used to create / update
   *                         the (potentially) new scope
   * @param isFunction       An indicator to determine if the scope to be created
   *                         is a functional scope or a plain scope
   *
   * @return An error code enumeration providing details on what occurred during
   *         attempt to update/add a scope
   */
  public SymbolTableCode updateScope(final String scope,
                                     final AbstractSyntaxTreeNode scopeIdentifier,
                                     final boolean isFunction)
  {
    SymbolItem scopeRecord = table.get(scopeIdentifier.getName());
    // Check to see if additional traversal is needed
    if (scope.isEmpty())
    {
      // If the identifier cannot be found, a new record should be made
      if (scopeRecord == null)
      {
        // If the record to be made is an array type
        if (isFunction)
        {
          // Add a new array symbol record
          table.put(scopeIdentifier.getName(),
              new FunctionSymbolTable(
                  scopeIdentifier.getLineNumber(),
                  scopeIdentifier.getType()));
        }
        else
        {
          // Add a new simple symbol record
          table.put(scopeIdentifier.getName(),
              new SymbolTable(
                  scopeIdentifier.getLineNumber(),
                  scopeIdentifier.getType()));
        }
        // The record was properly added
        return SymbolTableCode.UPDATE_OK;
      }
      // A record has previously been recorded. Time to make sure
      // the record matches the desired type
      else
      {
        // If either:
        //     1. We want an array and the record is an array record
        //                            OR
        //     2. We don't want an array and the record is a simple record
        // Add the current line to the record and update the table with the
        // new record contents.
        if ((isFunction &&
            scopeRecord.getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION) ||
            (!isFunction &&
                scopeRecord.getSymbolType() == SymbolItemType.SYMBOL_TABLE_SCOPE))
        {
          scopeRecord.addLine(scopeIdentifier.getLineNumber());
          table.put(scopeIdentifier.getName(), scopeRecord);
          return SymbolTableCode.UPDATE_OK;
        }
      }

      return SymbolTableCode.UPDATE_NOT_FOUND;
    }

    String currentScope = SymbolTableUtilities.GetCurrentScope(scope);
    String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

    // If the scope item doesn't exist, exit early
    if (!table.containsKey(currentScope))
    {
      return SymbolTableCode.UPDATE_NO_SCOPE;
    }

    SymbolTableCode errorCode;
    SymbolItem scopeItem = table.get(currentScope);
    if ((scopeItem.getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION) ||
        (scopeItem.getSymbolType() == SymbolItemType.SYMBOL_TABLE_SCOPE))
    {
      errorCode = ((SymbolTable)scopeItem).updateScope(remainingScope,
                                                       scopeIdentifier,
                                                       isFunction);
    }
    else
    {
      return SymbolTableCode.UPDATE_SEMANTIC_FAILURE;
    }

    if (errorCode != SymbolTableCode.UPDATE_OK && scopeRecord != null)
    {
      // If either:
      //     1. We want an array and the record is an array record
      //                            OR
      //     2. We don't want an array and the record is a simple record
      // Add the current line to the record and update the table with the
      // new record contents.
      if ((isFunction &&
          scopeRecord.getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION) ||
          (!isFunction &&
              scopeRecord.getSymbolType() == SymbolItemType.SYMBOL_TABLE_SCOPE))
      {
        scopeRecord.addLine(scopeIdentifier.getLineNumber());
        table.put(scopeIdentifier.getName(), scopeRecord);
        return SymbolTableCode.UPDATE_OK;
      }
    }

    // Only thing left to do is report a semantic failure
    return errorCode;
  }

  /**
   * Determine if the symbol table is considered empty. A symbol table is
   * considered empty if both the internal table is empty and there are no
   * usages of the scope
   *
   * @return A boolean value indicating if the above criteria are met
   */
  private boolean isEmpty()
  {
    return table.isEmpty() && lines.isEmpty();
  }

  /**
   * Recursively remove all of the empty tables from the current table. Note that
   * records will not be affected.
   */
  public void removeAllEmpty()
  {
    // If the current table is empty, do nothing
    if (isEmpty())
    {
      return;
    }

    // Create a new set of Strings that constitute the table's current key set. This
    // is performed to be able to perform deletions on the table while processing
    // each key.
    Set<String> keys = new HashSet<>(table.keySet());
    // Iterate over each key in the table's key set to look for symbol tables
    for (final String keyName : keys)
    {
      SymbolItem item = table.get(keyName);
      // Check to see if the current item is a symbol table
      if (item.getSymbolType() == SymbolItemType.SYMBOL_TABLE_SCOPE ||
          item.getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION)
      {
        // If a symbol table, perform the necessary casting to obtain a symbol table
        // (FunctionalSymbolTable is a child of SymbolTable)
        SymbolTable symbolTable = (SymbolTable)item;

        // Check to see if the symbol table is empty
        if (symbolTable.isEmpty())
        {
          // If the symbol table is empty, delete the key from the table
          table.remove(keyName);
          continue;
        }
        // If the table is not empty, remove all of the empty entries in the symbol
        // table (recursive call)
        symbolTable.removeAllEmpty();
      }
    }
  }
}
