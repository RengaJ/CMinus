package tokens;

/**
 * A binary search tree that manages Token objects
 * @param <E> The type of Token that will be managed by the binary search tree
 */
public final class TokenTree<E extends Token>
{
  // The current token value
  private E value;
  // The left child
  private TokenTree<E> left;
  // The right child
  private TokenTree<E> right;

  /**
   * The full constructor of the TokenTree
   * @param value The value of the TokenTree
   */
  public TokenTree(E value)
  {
    this.value = value;
    left = null;
    right = null;
  }

  /**
   * Add the provided value to the TokenTree from the current node. May defer to
   * other nodes if necessary
   *
   * @param val The value to add to the TokenTree
   *            from the current node
   */
  public void add(E val)
  {
    int compareVal = value.compareTo(val);
    if (compareVal == 0)
    {
      return;
    }
    if (compareVal < 0)
    {
      if (right != null)
      {
        right.add(val);
      }
      else
      {
        right = new TokenTree<>(val);
      }
    }
    else
    {
      if (left != null)
      {
        left.add(val);
      }
      else
      {
        left = new TokenTree<>(val);
      }
    }
  }

  /**
   * Find the token with the provided name
   * @param val The Token to find in the Binary Search Tree
   * @return The node found in the TokenTree, or null if the node was not found
   */
  public Token find(Token val)
  {
    int compareValue = value.compareTo(val);
    if (compareValue == 0)
    {
      try
      {
        return value.getClass().newInstance();
      }
      catch (IllegalAccessException | InstantiationException e)
      {
        System.err.println("Illegal Instantiation of Token class");
        return null;
      }
    }
    if (compareValue < 0 && right != null)
    {
      return right.find(val);
    }
    if (compareValue > 0 && left != null)
    {
      return left.find(val);
    }
    return null;
  }
}
