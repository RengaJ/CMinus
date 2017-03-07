package syntaxtree.meta;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class that represents a function parameter in the
 * abstract syntax tree.
 */
public final class ParameterNode extends MetaNode
{
  /**
   * Full constructor of the ParameterNode
   */
  public ParameterNode()
  {
    super(TokenType.VARIABLE_IDENTIFIER);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return The ASTNodeType that represents the current node type
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.META_PARAMETER;
  }
}
