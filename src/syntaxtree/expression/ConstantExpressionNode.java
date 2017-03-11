package syntaxtree.expression;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * The concrete class representing a number in the abstract syntax
 * tree.
 */
public final class ConstantExpressionNode extends ExpressionNode
{
  /**
   * Full constructor for the ConstantExpressionNode
   */
  public ConstantExpressionNode()
  {
    super(TokenType.VARIABLE_NUMBER);
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return EXPRESSION_NUMBER
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.EXPRESSION_NUMBER;
  }
}
