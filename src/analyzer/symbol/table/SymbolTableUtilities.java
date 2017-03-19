package analyzer.symbol.table;

/**
 * A static collection of utility functions useful for the
 * symbol table's operations
 */
public final class SymbolTableUtilities
{
  /** Private constructor. Should not be used */
  private SymbolTableUtilities() {}

  public static String GetCurrentScope(final String scope)
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

  public static String GetRemainingScope(final String scope)
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
