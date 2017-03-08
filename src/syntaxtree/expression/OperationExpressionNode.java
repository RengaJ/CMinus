package syntaxtree.expression;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class that represents an operation in the abstract syntax tree
 */
public class OperationExpressionNode extends ExpressionNode
{
  /**
   * Full constructor of the OperationExpressionNode
   */
  public OperationExpressionNode(final TokenType tokenType)
  {
    super(tokenType);
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return EXPRESSION_OPERATION
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.EXPRESSION_OPERATION;
  }
}
