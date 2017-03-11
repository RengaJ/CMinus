package syntaxtree.statement;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing the return statement node
 * in the abstract syntax tree.
 */
public final class ReturnStatementNode extends StatementNode
{
  /**
   * Full constructor for the ReturnStatementNode
   */
  public ReturnStatementNode()
  {
    super(TokenType.RESERVED_RETURN);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return STATEMENT_RETURN
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.STATEMENT_RETURN;
  }
}
