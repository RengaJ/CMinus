package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol ;
 */
public final class SemicolonToken extends SpecialToken
{
  /**
   * Full constructor for the SemicolonToken
   */
  public SemicolonToken()
  {
    super(";");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_SEMICOLON
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_SEMICOLON;
  }
}
