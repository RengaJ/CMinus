package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol +
 */
public final class PlusToken extends SpecialToken
{
  /**
   * Full constructor for the PlusToken
   */
  public PlusToken()
  {
    super("+");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_PLUS
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_PLUS;
  }
}
