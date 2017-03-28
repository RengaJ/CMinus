package syntaxtree.meta;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing an anonymous scope block
 * in the abstract syntax tree
 */
public final class AnonymousBlockNode extends MetaNode
{
  /**
   * Full constructor for the AnonymousBlockNode
   */
  public AnonymousBlockNode()
  {
    super(TokenType.SPECIAL_LEFT_BRACE);
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   * @return META_ANONYMOUS_BLOCK
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.META_ANONYMOUS_BLOCK;
  }
}
