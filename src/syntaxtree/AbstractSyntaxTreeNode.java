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
   * The type of token represented by the AST Node.
   * Used primarily for operation codes
   */
  private TokenType tokenType;

  /**
   * The contained value of the  AST Node
   * (Used for constant numbers)
   */
  private int value;

  /**
   * The name of the AST Node (Used to provide necessary context/
   * identifier information)
   */
  private String name;

  /**
   * The class object representation of the Type for the AST Node
   */
  private Class<?> type;

  /**
   * The line number that this Abstract Syntax Tree Node would reside on
   */
  private int lineNumber;

  /**
   * Full constructor for the Abstract Syntax Tree Node object
   */
  public AbstractSyntaxTreeNode()
  {
    children   = new ArrayList<>();
    sibling    = null;
    tokenType  = TokenType.BOOKKEEPING_ERROR;
    value      = Integer.MIN_VALUE;
    name       = "";
    type       = Void.class;
    lineNumber = 0;

  }

  /**
   * Add a child to the AbstractTreeNode's list of children
   *
   * @param child The child to add to the current AbstractSyntaxTreeNode
   */
  public void addChild(AbstractSyntaxTreeNode child)
  {
    if (child != null)
    {
      children.add(child);
    }
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
   * Set the contained token type
   *
   * @param tokenType The new token type of the node
   */
  public void setTokenType(final TokenType tokenType)
  {
    this.tokenType = tokenType;
  }

  /**
   * Set the value of the abstract syntax tree node
   *
   * @param value The new value of the abstract syntax tree node
   */
  public void setValue(final int value)
  {
    this.value = value;
  }

  /**
   * Set the name of the abstract syntax tree node
   *
   * @param name The new name of the abstract syntax tree node
   */
  public void setName(final String name)
  {
    this.name = name;
  }

  /**
   * Set the contained type to the provided {@link Class} object
   *
   * @param type The {@link Class} object used to be contained within the
   *             abstract syntax tree node
   */
  public void setType(final Class<?> type)
  {
    this.type = type;
  }

  /**
   * Set the line number on which the abstract syntax tree node would reside
   *
   * @param lineNumber The line number of the of node
   */
  public void setLineNumber(final int lineNumber)
  {
    this.lineNumber = lineNumber;
  }
  /**
   * Get the token type of the contained attribute
   *
   * @return The {@link TokenType} of the contained attribute
   *         (default is TokenType.BOOKKEEPING_ERROR)
   */
  public final TokenType getTokenType()
  {
    return tokenType;
  }

  /**
   * Get the value of the node
   *
   * @return The integer value of the node (default is Integer.MIN_VALUE)000000
   */
  public final int getValue()
  {
    return value;
  }

  /**
   * Get the name of the node
   *
   * @return The string value of the contained (default is the empty string)
   */
  public final String getName()
  {
    return name;
  }

  /**
   * Get the class type of the node
   *
   * @return The {@link Class} object that represents the
   *         object type of the node (default is
   *         the Void class)
   */
  public final Class<?> getType()
  {
    return type;
  }

  /**
   * Get the line number on which the abstract syntax tree node would
   * reside.
   *
   * @return The line number of the abstract syntax tree node
   */
  public final int getLineNumber()
  {
    return lineNumber;
  }
}
