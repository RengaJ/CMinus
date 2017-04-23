package globals.pair;

/**
 * Simple POJO to contain ID and size information
 */
public class IdentifierPair
{
  /**
   * The name of the id
   */
  public String name;

  /**
   * The size of the ID
   */
  public int size;

  /**
   * The full constructor fo the IdentifierPair
   * @param name The name of the id
   * @param size The size of the id
   */
  public IdentifierPair(final String name, final int size)
  {
    this.name = name;
    this.size = size;
  }
}
