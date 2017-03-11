package syntaxtree.statement;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class that represents the declaration of an array-type in the
 * abstract syntax tree.
 */
public final class ArrayDeclarationStatementNode extends StatementNode
{
  /**
   * Full constructor for the ArrayDeclarationStatementNode
   */
  public ArrayDeclarationStatementNode()
  {
    super(TokenType.VARIABLE_IDENTIFIER);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return STATEMENT_ARRAY_DECLARATION
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.STATEMENT_ARRAY_DECLARATION;
  }
}
