package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol [
 */
public final class LeftBracketToken extends SpecialToken
{
  /**
   * Full constructor for the LeftBracketToken
   */
  public LeftBracketToken()
  {
    super("[");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_LEFT_BRACKET
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_LEFT_BRACKET;
  }
}
