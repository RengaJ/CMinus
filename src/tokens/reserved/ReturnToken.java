package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the reserved word "return"
 */
public final class ReturnToken extends ReservedToken
{
  /**
   * Full constructor for the ReturnToken
   */
  public ReturnToken()
  {
    super ("return");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_RETURN
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_RETURN;
  }
}
