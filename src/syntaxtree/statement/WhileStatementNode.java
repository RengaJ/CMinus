package syntaxtree.statement;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing a while-statement in the abstract
 * syntax tree
 */
public class WhileStatementNode extends StatementNode
{
  /**
   * Full constructor for the WhileStatementNode
   */
  public WhileStatementNode()
  {
    super(TokenType.RESERVED_WHILE);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return The ASTNodeType that represents the current node type
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.STATEMENT_WHILE;
  }
}
