package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol /
 */
public final class DivideToken extends SpecialToken
{
  /**
   * Full constructor for the DivideToken
   */
  public DivideToken()
  {
    super("/");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_DIVIDE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_DIVIDE;
  }
}
