package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the reserved word "if"
 */
public final class IfToken extends ReservedToken
{
  /**
   * Full constructor for the IfToken
   */
  public IfToken()
  {
    super ("if");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_IF
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_IF;
  }
}
