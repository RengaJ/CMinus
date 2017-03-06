package syntaxtree;

import tokens.TokenType;

/**
 * Class that contains attribute-related information for
 * the Abstract Syntax Tree Node
 */
public final class SyntaxAttribute
{
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

  /** The class object representation of the Type for the AST Node */
  private Class<?> type;

  public SyntaxAttribute()
  {
    tokenType = TokenType.BOOKKEEPING_ERROR;
    value     = Integer.MIN_VALUE;
    name      = "";
    type      = Void.class;
  }

  public void setTokenType(final TokenType tokenType)
  {
    this.tokenType = tokenType;
  }

  public void setValue(final int value)
  {
    this.value = value;
  }

  public void setName(final String name)
  {
    this.name = String.copyValueOf(name.toCharArray());
  }

  public void setType(Class<?> type)
  {
    this.type = type;
  }

  public TokenType getTokenType()
  {
    return tokenType;
  }

  public int getValue()
  {
    return value;
  }

  public String getName()
  {
    return name;
  }

  public Class<?> getType()
  {
    return type;
  }
}
