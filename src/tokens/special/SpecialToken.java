package tokens.special;

import tokens.Token;

/**
 * Decorator class used to isolate special symbol tokens from
 * other token types.
 */
public abstract class SpecialToken extends Token
{
  /**
   * Full constructor for the SpecialToken
   * (needed to construct child objects)
   *
   * @param lexeme The lexeme that will be associated with the Token)
   */
  public SpecialToken(String lexeme)
  {
    super(lexeme);
  }

  /**
   * Set the lexeme of the SpecialToken. Does nothing.
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
