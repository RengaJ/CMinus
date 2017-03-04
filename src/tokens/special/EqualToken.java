package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol ==
 */
public final class EqualToken extends SpecialToken
{
  /**
   * Full constructor for the EqualToken
   */
  public EqualToken()
  {
    super("==");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_EQUAL
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_EQUAL;
  }
}
