package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol !=
 */
public final class NotEqualToken extends SpecialToken
{
  /**
   * Full constructor for the NotEqualToken
   */
  public NotEqualToken()
  {
    super("!=");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_NOT_EQUAL
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_NOT_EQUAL;
  }
}
