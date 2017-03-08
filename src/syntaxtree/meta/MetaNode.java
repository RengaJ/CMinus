package syntaxtree.meta;

import syntaxtree.AbstractSyntaxTreeNode;
import tokens.TokenType;

/**
 * Abstract decorator used to identify meta-information nodes
 */
public abstract class MetaNode extends AbstractSyntaxTreeNode
{
  /**
   * Full constructor for the abstract MetaNode
   *
   * @param tokenType The TokenType used to be contained within
   *                  the node's attribute
   */
  public MetaNode(final TokenType tokenType)
  {
    super();

    setTokenType(tokenType);
  }
}
