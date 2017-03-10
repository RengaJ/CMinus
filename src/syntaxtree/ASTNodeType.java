package syntaxtree;

/**
 * Enumeration that identifies the various types of nodes that
 * can be present within the
 */
public enum ASTNodeType
{
  // Expression Node Types
  EXPRESSION_ARRAY_IDENTIFIER, // Variable Name ( ID[] )
  EXPRESSION_CALL,             // Function Call
  EXPRESSION_IDENTIFIER,       // Variable Name
  EXPRESSION_NUMBER,           // Constant Number
  EXPRESSION_OPERATION,        // Operation Expression ( x + y )
  // Meta/Structural Node Types
  META_ARRAY_PARAMETER,        // Function Array Parameter ( ...(int x[]) )
  META_FUNCTION,               // Function Block ( void main() { ... } )
  META_PARAMETER,              // Function Parameter ( ...(int x) )
  // Statement Node Types
  STATEMENT_ASSIGN,            // Assignment Statement ( x = ... )
  STATEMENT_IF,                // If-Statement ( if (...) { ... } [else { ... }] )
  STATEMENT_RETURN,            // Return Statement ( return [...]; )
  STATEMENT_WHILE,             // While-Statement ( while (...) { ... } )
  STATEMENT_VAR_DECLARATION    // Variable Declaration ( int x; )
}
