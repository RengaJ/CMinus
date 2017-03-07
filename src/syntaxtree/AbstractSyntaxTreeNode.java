package syntaxtree;

import tokens.TokenType;

import java.util.ArrayList;

/**
 * The abstract class that represents an Abstract Syntax Tree node.
 * This class contains the child and sibling information used to
 * construct the abstract syntax tree in the parser.
 */
public abstract class AbstractSyntaxTreeNode
{
  /**
   * The list of child nodes attached to this abstract syntax
   * tree node. Each node can have a varying number of children
   * associated with the current node.
   */
  private ArrayList<AbstractSyntaxTreeNode> children;

  /**
   * The sibling node connected to this abstract syntax tree node.
   * The sibling node is used to simulate statement flow.
   */
  private AbstractSyntaxTreeNode sibling;

  /**
   * The contained attribute of the Abstract Syntax Tree Node. This
   * is used to store additional meta-information without cluttering
   * up the definition of the Abstract Syntax Tree Node object.
   */
  private SyntaxAttribute attribute;

  /**
   * Full constructor for the Abstract Syntax Tree Node object
   */
  public AbstractSyntaxTreeNode()
  {
    children = new ArrayList<>();

    sibling = null;

    attribute = new SyntaxAttribute();
  }

  /**
   * Add a child to the AbstractTreeNode's list of children
   *
   * @param child The child to add to the current AbstractSyntaxTreeNode
   */
  public void addChild(AbstractSyntaxTreeNode child)
  {
    children.add(child);
  }

  /**
   * Retrieve the child at the supplied index from the current
   * AbstractSyntaxTreeNode.
   *
   * @param index The index at which the child should be retrieved
   *
   * @return The child at the provided index, or null if the
   */
  public AbstractSyntaxTreeNode getChild(int index)
  {
    if (index >= 0 && index < children.size())
    {
      return children.get(index);
    }

    return null;
  }

  /**
   * Associate the current Abstract Syntax Tree node's sibling
   * with the provided sibling value. Note that providing a null
   * value will erase the current sibling.
   *
   * @param sibling The sibling to associate with this node
   */
  public void setSibling(AbstractSyntaxTreeNode sibling)
  {
    this.sibling = sibling;
  }

  /**
   * Obtain the sibling node associated with the current node
   *
   * @return The current node's sibling node, or null if none exists
   */
  public AbstractSyntaxTreeNode getSibling()
  {
    return sibling;
  }

  /**
   * Return an indicator if there is a sibling node present
   * within this abstract syntax tree node.
   *
   * @return A boolean value indicating the presence of a sibling node
   */
  public boolean hasSibling()
  {
    return sibling != null;
  }

  /**
   * Get the number of children associated with this Abstract
   * Syntax Tree node.
   *
   * @return The number of children associated with node
   */
  public int getChildCount()
  {
    return children.size();
  }

  /**
   * Get the enumerated type of the current Abstract Syntax Tree node
   *
   * @return The ASTNodeType that represents the current node type
   */
  public abstract ASTNodeType getNodeType();

  /////////////////////////////////
  // Attribute Related Accessors //
  /////////////////////////////////

  /**
   * Set the value of the attribute's token type
   *
   * @param tokenType The new token type of the attribute
   */
  public void setAttributeTokenType(final TokenType tokenType)
  {
    attribute.setTokenType(tokenType);
  }

  /**
   * Set the value of the attribute.
   *
   * @param value The new value of the attribute
   */
  public void setAttributeValue(final int value)
  {
    attribute.setValue(value);
  }

  /**
   * Set the name of the attribute.
   *
   * @param name The new name of the attribute
   */
  public void setAttributeName(final String name)
  {
    attribute.setName(name);
  }

  /**
   * Set the contained attribute's type to the provided {@link Class} object
   *
   * @param type The {@link Class} object used to be contained within the
   *             attribute
   */
  public void setAttributeType(final Class<?> type)
  {
    attribute.setType(type);
  }
  /**
   * Get the token type of the contained attribute
   *
   * @return The {@link TokenType} of the contained attribute
   *         (default is TokenType.BOOKKEEPING_ERROR)
   */
  public TokenType getAttributeTokenType()
  {
    return attribute.getTokenType();
  }

  /**
   * Get the value of the contained attribute
   *
   * @return The integer value of the contained attribute
   *         (default is Integer.MIN_VALUE)
   */
  public int getAttributeValue()
  {
    return attribute.getValue();
  }

  /**
   * Get the name of the contained attribute
   *
   * @return The string value of the contained attribute
   *         (default is the empty string)
   */
  public String getAttributeName()
  {
    return attribute.getName();
  }

  /**
   * Get the class type of the contained attribute
   *
   * @return The {@link Class} object that represents the
   *         object type of the attribute (default is
   *         the Void class)
   */
  public Class<?> getAttributeType()
  {
    return attribute.getType();
  }
}
