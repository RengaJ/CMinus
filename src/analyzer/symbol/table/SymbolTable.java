package analyzer.symbol.table;

import analyzer.symbol.*;
import analyzer.symbol.record.*;
import syntaxtree.ASTNodeType;
import syntaxtree.AbstractSyntaxTreeNode;

import java.util.*;

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
  private HashMap<SymbolKey, SymbolItem> table;

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
    table.put(SymbolKey.CreateScopeKey(scopeName), scopeTable);
  }

  /**
   * Attempt to add a record the symbol table, given a scope level and the node
   * containing the information for addition.
   *
   * @param scope The scope level for the addition of the record. Scopes have a
   *              format of "scope1.scope2. ...". The root scope is defined as "".
   * @param node  The node that contains the information to use for creating a new
   *              record.
   * @param memoryLocation The location in memory where the identifier should
   *                       reside.
   *
   * @return Returns SymbolTableCode.OK if the addition of the record was
   *         successful, and anything else if something fails.
   */
  public SymbolTableCode addRecord(final String scope,
                                   final AbstractSyntaxTreeNode node,
                                   final int memoryLocation)
  {
    // Base Condition:
    // Scope is empty
    if (scope.isEmpty())
    {
      // At the base condition, try to find the identifier in the current scope
      // (the identifier should not be able to be located)
      final String identifier = node.getName();

      // Create the key that will be used for existence and insertion
      final SymbolKey idKey = SymbolKey.CreateIDKey(identifier);

      // If the identifier is able to be found, this is a semantic error.
      if (table.containsKey(idKey))
      {
        // Return DUPLICATE_RECORD
        return SymbolTableCode.DUPLICATE_RECORD;
      }

      // Create a new symbol record based on the type of node provided:
      ASTNodeType nodeType = node.getNodeType();
      // If the node type is either a simple variable declaration or a function
      // parameter, create a simple symbol record
      if ((nodeType == ASTNodeType.STATEMENT_VAR_DECLARATION) ||
          (nodeType == ASTNodeType.META_PARAMETER))
      {
        table.put(idKey,
            new SimpleSymbolRecord(
                node.getLineNumber(),
                node.getType(),
                memoryLocation));
        // Terminate processing (return OK)
        return SymbolTableCode.OK;
      }
      // If the node type is an array parameter, create an array symbol record
      // with no size
      else if (nodeType == ASTNodeType.META_ARRAY_PARAMETER)
      {
        table.put(idKey,
            new ArraySymbolRecord(
                node.getLineNumber(),
                node.getType(),
                memoryLocation,
                0));
        // Terminate processing (return OK)
        return SymbolTableCode.OK;
      }
      // If the node type is an array declaration, create an array symbol record
      // with a known size
      else if (nodeType == ASTNodeType.STATEMENT_ARRAY_DECLARATION)
      {
        // Obtain the size from the child of the array declaration
        final int size = node.getChild(0).getValue();
        table.put(idKey,
            new ArraySymbolRecord(
                node.getLineNumber(),
                node.getType(),
                memoryLocation,
                size));

        // Terminate processing (return OK)
        return SymbolTableCode.OK;
      }
      // If a node type not captured above is passed in, return a semantic error
      // (invalid type)
      return SymbolTableCode.INVALID_TYPE;
    }
    // Recursive Case:
    // Scope is not empty

    // Extract the current and remaining scopes from the provided scope
    final String currentScope   = SymbolTableUtilities.GetCurrentScope(scope);
    final String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

    // Create the scope key to use for extracting the current scope
    final SymbolKey scopeKey = SymbolKey.CreateScopeKey(currentScope);

    // Check to see if the current scope exists in the current table
    SymbolItem currentScopeItem = table.get(scopeKey);
    // If the current scope does not exist, return a semantic error (invalid scope)
    if (currentScopeItem == null)
    {
      return SymbolTableCode.INVALID_SCOPE;
    }
    // Check to see if the scope is actually a scope (success is that the scope
    // is a symbol table of some type)
    final SymbolItemType scopeType = currentScopeItem.getSymbolType();
    if ((scopeType != SymbolItemType.SYMBOL_TABLE_SCOPE) &&
        (scopeType != SymbolItemType.SYMBOL_TABLE_FUNCTION))
    {
      // If the scope type is not a symbol table, return a semantic error
      // (invalid scope)
      return SymbolTableCode.INVALID_SCOPE;
    }

    // The scope is actually a scope at this point. Cast the current scope to
    // a symbol table and return the result of a recursive call.
    return ((SymbolTable)currentScopeItem).addRecord(
        remainingScope,
        node,
        memoryLocation);
  }

  /**
   *
   * @param scope
   * @param node
   * @return
   */
  public SymbolTableCode addScope(final String scope,
                                  final AbstractSyntaxTreeNode node)
  {
    // Base Condition:
    // Scope is empty
    if (scope.isEmpty())
    {
      // At the base condition, check to see if the scope to be added exists already
      // (the best result is that the new scope CANNOT be found)
      final String scopeName = node.getName();

      // Create the scope key to be used for checking for a duplicate scope
      final SymbolKey scopeKey = SymbolKey.CreateScopeKey(scopeName);

      // If the scope name is able to be found, this is a semantic error.
      if (table.containsKey(scopeKey))
      {
        // Return DUPLICATE_SCOPE
        return SymbolTableCode.DUPLICATE_SCOPE;
      }

      // Create a new symbol table based on the type of node provided:
      ASTNodeType nodeType = node.getNodeType();

      // If the node type is a function declaration, create a function symbol table
      if (nodeType == ASTNodeType.META_FUNCTION)
      {
        table.put(scopeKey,
            new FunctionSymbolTable(
                node.getLineNumber(),
                node.getType()));
        // Terminate processing (return OK)
        return SymbolTableCode.OK;
      }
      // If the node type is either an If-Statement or a While-Loop, create a simple
      // symbol table
      else if ((nodeType == ASTNodeType.STATEMENT_IF) ||
               (nodeType == ASTNodeType.STATEMENT_WHILE) ||
               (nodeType == ASTNodeType.META_ANONYMOUS_BLOCK))
      {
        table.put(scopeKey,
            new SymbolTable(
                node.getLineNumber(),
                Void.class));
        // Terminate processing (return OK)
        return SymbolTableCode.OK;
      }
      // If a node type not captured above is passed in, return a semantic error
      // (invalid type)
      return SymbolTableCode.INVALID_TYPE;
    }
    // Recursive Case:
    // Scope is not empty

    // Extract the current and remaining scopes from the provided scope
    final String currentScope   = SymbolTableUtilities.GetCurrentScope(scope);
    final String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

    // Create the scope key to be used for checking for the current scope
    final SymbolKey scopeKey = SymbolKey.CreateScopeKey(currentScope);

    // Check to see if the current scope exists in the current table
    SymbolItem currentScopeItem = table.get(scopeKey);

    // If the current scope does not exist, return a semantic error (invalid scope)
    if (currentScopeItem == null)
    {
      return SymbolTableCode.INVALID_SCOPE;
    }
    // Check to see if the scope is actually a scope (success is that the scope
    // is a symbol table of some type)
    final SymbolItemType scopeType = currentScopeItem.getSymbolType();
    if ((scopeType != SymbolItemType.SYMBOL_TABLE_SCOPE) &&
        (scopeType != SymbolItemType.SYMBOL_TABLE_FUNCTION))
    {
      // If the scope type is not a symbol table, return a semantic error
      // (invalid scope)
      return SymbolTableCode.INVALID_SCOPE;
    }

    // The scope is actually a scope at this point. Cast the current scope to
    // a symbol table and return the result of a recursive call.
    return ((SymbolTable)currentScopeItem).addScope(
        remainingScope,
        node);
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
   * @param isScope     Boolean flag indicating if the desired item is a scope
   *
   * @return The most scoped SymbolItem that was able to be located, or null if no
   *         such identifier exists in the symbol table
   */
  public SymbolItem getSymbolItem(final String scope,
                                  final String identifier,
                                  final boolean isScope)
  {
    // Create the key that will be used to look for the desired entry
    final SymbolKey key = isScope ? SymbolKey.CreateScopeKey(identifier) :
                                    SymbolKey.CreateIDKey(identifier);
    // Base case:
    // If no scope is provided, simply attempt to retrieve the
    // identifier from the current symbol table.
    if (scope.isEmpty())
    {
      return table.get(key);
    }

    // Recursive case:
    // Look for remaining scopes and continue the descent from there
    SymbolItem item = null;

    final String currentScope = SymbolTableUtilities.GetCurrentScope(scope);
    final String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

    // Create the key that will be used to extract the current scope
    final SymbolKey scopeKey = SymbolKey.CreateScopeKey(currentScope);

    // Get the current scope from the symbol table (hopefully it exists!)
    SymbolItem scopeItem = table.get(scopeKey);

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
        item = symbolTable.getSymbolItem(remainingScope, identifier, isScope);
      }
    }

    // If, after the recursive descent, nothing has been found, attempt to retrieve
    // the identifier at the current scope level
    if (item == null)
    {
      item = table.get(key);
    }

    // At this point, the item may actually still be null if the identifier was
    // unable to be located.
    return item;
  }

  public SymbolTableCode update(final String scope,
                                final String identifier,
                                final int lineNumber,
                                final boolean isScope)
  {
    // Create the key that will be used to look for the desired entry
    final SymbolKey key = isScope ? SymbolKey.CreateScopeKey(identifier) :
                                    SymbolKey.CreateIDKey(identifier);

    // Base Condition:
    // Scope is empty
    if (scope.isEmpty())
    {
      // Check to see if the identifier exists in the current scope (the identifier
      // should be located). If the identifier cannot be located, report a semantic
      // error (note that this might not actually be true error)
      if (!table.containsKey(key))
      {
        // Return RECORD_NOT_FOUND
        return SymbolTableCode.RECORD_NOT_FOUND;
      }
      // It has been determined that the identifier exists. Extract the record
      // and add the line number to the record's list
      SymbolItem record = table.get(key);
      record.addLine(lineNumber);

      // Re-insert the record back into the symbol table
      table.put(key, record);

      // The record was successfully updated. Return OK.
      return SymbolTableCode.OK;
    }
    // At this point, the scope is not empty. Extract the current and remaining
    // scopes from the provided scope
    final String currentScope   = SymbolTableUtilities.GetCurrentScope(scope);
    final String remainingScope = SymbolTableUtilities.GetRemainingScope(scope);

    // Create the scope key to be used for checking for the current scope
    final SymbolKey scopeKey = SymbolKey.CreateScopeKey(currentScope);

    // Check to see if the current scope exists in the table. If the current scope
    // does not exist in the table, report a semantic error (this is a TRUE semantic
    // error)
    if (!table.containsKey(scopeKey))
    {
      // Return INVALID_SCOPE
      return SymbolTableCode.INVALID_SCOPE;
    }

    // Extract the current scope from the table.
    final SymbolItem scopeItem = table.get(scopeKey);

    // Make sure that scope is a symbol table. If not, report a semantic error
    // (this is a TRUE semantic error)
    final SymbolItemType scopeItemType = scopeItem.getSymbolType();
    if (scopeItemType != SymbolItemType.SYMBOL_TABLE_SCOPE &&
        scopeItemType != SymbolItemType.SYMBOL_TABLE_FUNCTION)
    {
      // Return INVALID_SCOPE
      return SymbolTableCode.INVALID_SCOPE;
    }

    // Perform the recursive call on the update function with the remaining scope
    final SymbolTableCode returnCode =
        ((SymbolTable)scopeItem).update(remainingScope,
                                        identifier,
                                        lineNumber,
                                        isScope);

    // If the return code is either OK or INVALID_SCOPE, simply return the code
    if (returnCode == SymbolTableCode.OK ||
        returnCode == SymbolTableCode.INVALID_SCOPE)
    {
      return returnCode;
    }

    // The last check to perform is to see if the identifier exists in the current
    // scope.
    if (!table.containsKey(key))
    {
      // If the identifier cannot be found, return RECORD_NOT_FOUND
      return SymbolTableCode.RECORD_NOT_FOUND;
    }
    // It has been determined that the identifier exists. Extract the record
    // and add the line number to the record's list
    SymbolItem record = table.get(key);
    record.addLine(lineNumber);

    // Re-insert the record back into the symbol table
    table.put(key, record);

    // The record was successfully updated. Return OK.
    return SymbolTableCode.OK;
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
    Set<SymbolKey> keys = new HashSet<>(table.keySet());
    // Iterate over each key in the table's key set to look for symbol tables
    for (final SymbolKey key : keys)
    {
      SymbolItem item = table.get(key);
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
          table.remove(key);
          continue;
        }
        // If the table is not empty, remove all of the empty entries in the symbol
        // table (recursive call)
        symbolTable.removeAllEmpty();

        // Check to see if the symbol table is now empty
        if (symbolTable.isEmpty())
        {
          // If the symbol table is now empty, delete the key from the table
          table.remove(key);
        }
      }
    }
  }

  public void printTable(final String scope)
  {
    Queue<SymbolTable> symbolTableQueue = new ArrayDeque<>();
    Queue<String> scopeQueue = new ArrayDeque<>();

    String label;
    if (getSymbolType() == SymbolItemType.SYMBOL_TABLE_FUNCTION)
    {
      label = "Function";
    }
    else
    {
      label = "Scope";
    }
    System.out.printf("%s: \"%s\" %s\n", label, scope, toString());
    System.out.println("----------------");

    for (final SymbolKey key : table.keySet())
    {
      SymbolItem record = table.get(key);
      System.out.printf("%s: %s %s\n", key.getValue().toString(),
                                       key.getKey(),
                                       record.toString());

      if (key.getValue() == SymbolKey.KeyType.SCOPE)
      {
        symbolTableQueue.add((SymbolTable)record);
        scopeQueue.add(key.getKey());
      }
    }

    if (table.size() == 0)
    {
      System.out.println("< empty >");
    }

    System.out.println("");

    while (!symbolTableQueue.isEmpty())
    {
      if (scope.isEmpty())
      {
        symbolTableQueue.poll().printTable(scopeQueue.poll());
      }
      else
      {
        symbolTableQueue.poll().printTable(String.format("%s.%s", scope, scopeQueue.poll()));
      }
    }
  }
}
