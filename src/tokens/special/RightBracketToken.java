package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol ]
 */
public final class RightBracketToken extends SpecialToken
{
  /**
   * Full constructor for the RightBracketToken
   */
  public RightBracketToken()
  {
    super("]");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_RIGHT_BRACKET
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_RIGHT_BRACKET;
  }
}
