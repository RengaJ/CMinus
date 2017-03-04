package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the reserved word "while"
 */
public final class WhileToken extends ReservedToken
{
  /**
   * Full constructor for the WhileToken
   */
  public WhileToken()
  {
    super ("while");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_WHILE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_WHILE;
  }
}
