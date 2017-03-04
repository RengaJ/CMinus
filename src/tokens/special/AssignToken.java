package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol =
 */
public final class AssignToken extends SpecialToken
{
  /**
   * Full constructor for the AssignToken
   */
  public AssignToken()
  {
    super("=");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_ASSIGN
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_ASSIGN;
  }
}
