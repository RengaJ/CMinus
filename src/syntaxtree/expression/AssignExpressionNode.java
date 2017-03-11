package syntaxtree.expression;

import syntaxtree.ASTNodeType;
import syntaxtree.statement.StatementNode;
import tokens.TokenType;

/**
 * The concrete class that represents an assignment statement in the
 * abstract syntax tree
 */
public final class AssignExpressionNode extends ExpressionNode
{
  /**
   * Full constructor for the AssignExpressionNode
   */
  public AssignExpressionNode()
  {
    super(TokenType.SPECIAL_ASSIGN);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return STATEMENT_ASSIGN
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.STATEMENT_ASSIGN;
  }
}
