package analyzer.symbol;

/**
 * Enumeration that contains error codes for more accurate error reporting
 */
public enum SymbolTableCode
{
  UPDATE_OK               (""),
  UPDATE_NOT_FOUND        (""),
  UPDATE_NO_SCOPE         ("Outside known scopes."),
  UPDATE_SEMANTIC_FAILURE ("Type mismatch.");

  private String errorString;

  SymbolTableCode(final String errorString)
  {
    this.errorString = errorString;
  }

  public String toString()
  {
    return errorString;
  }
}
