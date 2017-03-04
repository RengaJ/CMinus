package tokens.bookkeeping;

import tokens.TokenType;

/**
 * Class that represents the meta-symbol EOF
 */
public final class ErrorToken extends BookKeepingToken
{
  /**
   * Full constructor for the ErrorToken
   */
  public ErrorToken()
  {
    super ("ERROR");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return BOOKKEEPING_ERROR
   */
  @Override
  public TokenType getType()
  {
    return TokenType.BOOKKEEPING_ERROR;
  }
}
