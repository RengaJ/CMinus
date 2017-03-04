package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol {
 */
public final class LeftBraceToken extends SpecialToken
{
  /**
   * Full constructor for the LeftBraceToken
   */
  public LeftBraceToken()
  {
    super("{");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_LEFT_BRACE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_LEFT_BRACE;
  }
}
