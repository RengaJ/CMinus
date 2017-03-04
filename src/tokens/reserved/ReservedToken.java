package tokens.reserved;

import tokens.Token;

/**
 * Decorator class used to isolate reserved word tokens from
 * other token types.
 */
public abstract class ReservedToken extends Token
{
  /**
   * Full constructor for the ReservedToken
   * (needed to construct child objects)
   *
   * @param lexeme The lexeme that will be
   *               associated with the Token
   */
  public ReservedToken(final String lexeme)
  {
    super(lexeme);
  }

  /**
   * Set the lexeme of the ReservedToken. Does nothing.
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
