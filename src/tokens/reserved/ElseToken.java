package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the reserved word "else"
 */
public final class ElseToken extends ReservedToken
{
  /**
   * Full constructor for the ElseToken
   */
  public ElseToken()
  {
    super ("else");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_ELSE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_ELSE;
  }
}
