package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol ,
 */
public final class CommaToken extends SpecialToken
{
  /**
   * Full constructor for the CommaToken
   */
  public CommaToken()
  {
    super(",");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_COMMA
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_COMMA;
  }
}
