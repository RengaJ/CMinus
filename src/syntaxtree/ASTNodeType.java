package syntaxtree;

/**
 * Enumeration that identifies the various types of nodes that
 * can be present within the
 */
public enum ASTNodeType
{
    EXPRESSION_IDENTIFIER,
    EXPRESSION_NUMBER,
    EXPRESSION_OPERATION,
    // Statement Node Types
    STATEMENT_ASSIGN,
    STATEMENT_IF,
    STATEMENT_RETURN,
    STATEMENT_WHILE,
    STATEMENT_VAR_DECLARATION
}
