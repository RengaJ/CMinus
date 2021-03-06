package analyzer.symbol.table;

/**
 * A static collection of utility functions useful for the
 * symbol table's operations
 */
final class SymbolTableUtilities
{
  /** Private constructor. Should not be used */
  private SymbolTableUtilities() {}

  /**
   * Retrieve the current scope from the period delimited scope string.
   *
   * @param scope The scope string from which the current scope shall be
   *              retrieved.
   * @return The current scope, or "" if no scope exists.
   */
  static String GetCurrentScope(final String scope)
  {
    if (scope == null || scope.isEmpty())
    {
      return "";
    }

    final int scopeEnd = scope.indexOf('.');

    if (scopeEnd == -1)
    {
      return scope;
    }

    return scope.substring(0, scopeEnd);
  }

  /**
   * Retrieve the remaining scope from the period delimited scope string.
   *
   * @param scope The scope string from which the remaining scope shall be
   *              retrieved.
   * @return The current scope, or "" if no scope exists.
   */
  static String GetRemainingScope(final String scope)
  {
    if (scope == null || scope.isEmpty())
    {
      return "";
    }

    final int scopeEnd = scope.indexOf('.');

    if (scopeEnd == -1)
    {
      return "";
    }

    return scope.substring(scopeEnd + 1);
  }
}
