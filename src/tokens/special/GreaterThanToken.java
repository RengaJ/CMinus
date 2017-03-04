package tokens.special;

import tokens.TokenType;

/**
 * Class that represents the special symbol >
 */
public final class GreaterThanToken extends SpecialToken
{
  /**
   * Full constructor for the GreaterThanToken
   */
  public GreaterThanToken()
  {
    super(">");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return SPECIAL_GREATER_THAN
   */
  @Override
  public TokenType getType()
  {
    return TokenType.SPECIAL_GREATER_THAN;
  }
}
