package syntaxtree;

/**
 * Enumeration that identifies the various types of nodes that
 * can be present within the
 */
public enum ASTNodeType
{
  // Expression Node Types
  EXPRESSION_IDENTIFIER,
  EXPRESSION_NUMBER,
  EXPRESSION_OPERATION,
  // Meta/Structural Node Types
  META_FUNCTION,
  META_PARAMETER,
  // Statement Node Types
  STATEMENT_ASSIGN,
  STATEMENT_IF,
  STATEMENT_RETURN,
  STATEMENT_WHILE,
  STATEMENT_VAR_DECLARATION
}
