package analyzer.symbol.table;

/**
 * Enumeration that contains error codes for more accurate error reporting
 */
public enum SymbolTableCode
{
  OK                (""),
  DUPLICATE_RECORD  ("Identifier already defined."),
  DUPLICATE_SCOPE   ("Scope already defined."),
  INVALID_SCOPE     ("Invalid scope provided."),
  INVALID_TYPE      ("Invalid expression being added."),
  RECORD_NOT_FOUND  (""),
  NO_SCOPE          ("Outside known scopes."),
  SEMANTIC_FAILURE  ("Type mismatch.");

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