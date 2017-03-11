package syntaxtree.meta;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing an array parameter node in the
 * abstract syntax tree
 */
public final class ArrayParameterNode extends ParameterNode
{
  /**
   * Full constructor for the ArrayParameterNode
   */
  public ArrayParameterNode()
  {
    super(TokenType.VARIABLE_IDENTIFIER);
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   * @return META_ARRAY_PARAMETER
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.META_ARRAY_PARAMETER;
  }
}
