package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol <=
 */
public final class LTEToken extends SpecialToken
{
  /**
   * Full constructor for the LTEToken
   */
  public LTEToken()
  {
    super("<=");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_LTE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_LTE;
  }
}
