package tokens.variable;

import tokens.Token;

/**
 * Decorator class used to isolate variable tokens from
 * other token types.
 */
public abstract class VariableToken extends Token
{
  /**
   * Full constructor for the VariableToken
   * (needed to construct child objects).
   * Defaults the lexeme to the empty String, and
   * should be set post-construction.
   */
  public VariableToken()
  {
    super("");
  }

  /**
   * Set the lexeme of the VariableToken post-construction.
   *
   * @param lexeme The new lexeme of the token
   */
  @Override
  public void setLexeme(String lexeme)
  {
    this.lexeme = lexeme;
  }
}
