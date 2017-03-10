package syntaxtree.expression;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing an array identifier expression node
 * in the abstract syntax tree
 */
public class ArrayIDExpressionNode extends ExpressionNode
{
  /**
   * Full constructor for the ArrayIDExpressionNode
   */
  public ArrayIDExpressionNode()
  {
    super(TokenType.VARIABLE_IDENTIFIER);
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   * @return EXPRESSION_ARRAY_IDENTIFIER
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.EXPRESSION_ARRAY_IDENTIFIER;
  }
}
