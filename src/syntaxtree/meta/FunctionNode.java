package syntaxtree.meta;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing a function block in the abstract syntax tree
 * (necessary for scoping/method labels)
 */
public final class FunctionNode extends MetaNode
{
  /**
   * Full constructor of the FunctionNode
   */
  public FunctionNode()
  {
    super(TokenType.VARIABLE_IDENTIFIER);
  }
  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return META_FUNCTION
   */
  @Override
  public ASTNodeType getNodeType()
  {
    return ASTNodeType.META_FUNCTION;
  }
}
