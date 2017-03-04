package tokens.bookkeeping;

import tokens.Token;

/**
 * Decorator class used to isolate bookkeeping/meta tokens from
 * other token types.
 */
public abstract class BookKeepingToken extends Token
{
  /**
   * Full constructor for the BookKeepingToken
   * (needed to construct child objects)
   *
   * @param lexeme The lexeme that will be associated with the Token)
   */
  public BookKeepingToken(String lexeme)
  {
    super(lexeme);
  }

  /**
   * Set the lexeme of the BookKeepingToken. Does nothing.
   *
   * @param lexeme The new lexeme of the token
   */
  @Override
  public void setLexeme(String lexeme)
  {
    // Perform no operation here (the lexeme
    // should not be modified for special tokens)
  }
}
