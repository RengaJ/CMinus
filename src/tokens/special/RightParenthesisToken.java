package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol )
 */
public final class RightParenthesisToken extends SpecialToken
{
  /**
   * Full constructor for the RightParenthesisToken
   */
  public RightParenthesisToken()
  {
    super(")");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_RIGHT_PAREN
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_RIGHT_PAREN;
  }
}
