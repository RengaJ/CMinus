package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol >=
 */
public final class GTEToken extends SpecialToken
{
  /**
   * Full constructor for the GTEToken
   */
  public GTEToken()
  {
    super(">=");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_GTE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_GTE;
  }
}
