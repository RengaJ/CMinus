package syntaxtree;

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
   * Full constructor for the Abstract Syntax Tree Node object
   */
  public AbstractSyntaxTreeNode()
  {
    children = new ArrayList<>();

    sibling = null;
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
}
