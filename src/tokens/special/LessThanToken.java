package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol <
 */
public final class LessThanToken extends SpecialToken
{
  /**
   * Full constructor for the LessThanToken
   */
  public LessThanToken()
  {
    super("<");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_LESS_THAN
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_LESS_THAN;
  }
}
