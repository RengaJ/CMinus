package tokens.bookkeeping;

import tokens.TokenType;

/**
 * Class that represents the meta-symbol EOF
 */
public final class EndOfFileToken extends BookKeepingToken
{
  /**
   * Full constructor for the EndOfFileToken
   */
  public EndOfFileToken()
  {
    super ("EOF");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return BOOKKEEPING_END_OF_FILE
   */
  @Override
  public TokenType getType()
  {
    return TokenType.BOOKKEEPING_END_OF_FILE;
  }
}
