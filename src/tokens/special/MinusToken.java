package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol -
 */
public final class MinusToken extends SpecialToken
{
  /**
   * Full constructor for the MinusToken
   */
  public MinusToken()
  {
    super("-");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_MINUS
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_MINUS;
  }
}
