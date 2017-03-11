package syntaxtree.meta;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Decorator for the ArrayParameterNode and SimpleParameterNode meta nodes
 */
public abstract class ParameterNode extends MetaNode
{
  /**
   * Full constructor for the ParameterNode
   *
   * @param tokenType The token type to be assigned to the node
   */
  public ParameterNode(TokenType tokenType)
  {
    super(tokenType);
  }
}
