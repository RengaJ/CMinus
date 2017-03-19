package syntaxtree.expression;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class that represents a function call in the abstract syntax
 * tree
 */
public final class FunctionCallExpressionNode extends ExpressionNode
{
  /**
   * Full constructor for the FunctionCallExpressionNode
   */
  public FunctionCallExpressionNode()
  {
    super (TokenType.VARIABLE_IDENTIFIER);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return EXPRESSION_CALL
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.EXPRESSION_CALL;
  }
}
