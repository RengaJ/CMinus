package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol *
 */
public final class TimesToken extends SpecialToken
{
  /**
   * Full constructor for the TimesToken
   */
  public TimesToken()
  {
    super("*");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_TIMES
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_TIMES;
  }
}
