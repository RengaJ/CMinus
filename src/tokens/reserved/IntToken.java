package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the reserved word "int"
 */
public final class IntToken extends ReservedToken
{
  /**
   * Full constructor for the IntToken
   */
  public IntToken()
  {
    super ("int");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_INT
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_INT;
  }
}
