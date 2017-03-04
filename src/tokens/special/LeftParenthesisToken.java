package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol (
 */
public final class LeftParenthesisToken extends SpecialToken
{
  /**
   * Full constructor for the LeftParenthesisToken
   */
  public LeftParenthesisToken()
  {
    super("(");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_LEFT_PAREN
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_LEFT_PAREN;
  }
}
