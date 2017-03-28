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
  RECORD_NOT_FOUND  ("Record not found."),
  INVALID_LHS       ("Left-hand side of operator invalid."),
  INVALID_RHS       ("Right-hand side of operator invalid."),
  INVALID_PTYPE     ("Invalid parameter type."),
  BAD_PARAM_COUNT   ("Incorrect number of parameters."),
  SEMANTIC_FAILURE  ("Type mismatch."),
  NESTED_DEFINITION ("Function not defined at global scope."),
  VOID_ARGUMENT     ("Function cannot have void paramter in argument list."),
  MAIN_NOT_FOUND    ("main method not found.");

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
