package syntaxtree.meta;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing an empty statement in the abstract syntax tree
 */
public class NullNode extends MetaNode
{
  /**
   * Full constructor for the NullNode
   */
  public NullNode()
  {
    super(TokenType.BOOKKEEPING_ERROR);
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return The ASTNodeType that represents the current node type
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.META_NULL;
  }
}
