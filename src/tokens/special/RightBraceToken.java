package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol }
 */
public final class RightBraceToken extends SpecialToken
{
  /**
   * Full constructor for the RightBraceToken
   */
  public RightBraceToken()
  {
    super("}");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_RIGHT_BRACE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_RIGHT_BRACE;
  }
}
