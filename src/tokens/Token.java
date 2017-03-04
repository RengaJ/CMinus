package tokens;

/**
 * Class used to represent the abstract concept of a Token.
 * The token is obtained from the Scanner as part of its
 * standard operation.
 *
 * The Token object is abstract, as only the concrete
 * implementations should be allowed to be instantiated.
 */
public abstract class Token implements Comparable<Token>
{
  /** The value contained within the Token object */
  protected String lexeme;

  /** The line number on which the Token was created */
  private int lineNumber;

  /**
   * The full constructor of the Token object
   * @param lexeme The value associated with the Token object
   */
  public Token(final String lexeme)
  {
    lineNumber  = 0;
    this.lexeme = lexeme;
  }

  /**
   * Obtain the lexeme contained within the Token object
   *
   * @return The lexeme contained within the Token
   */
  public String getLexeme()
  {
    return lexeme;
  }

  /**
   * Obtain the String-representation of the Token object.
   *
   * @return The String-representation of the Token
   */
  @Override
  public String toString()
  {
    return lexeme;
  }

  /**
   * Set the line number on which the token was generated
   * @param lineNumber The line number on which the token was generated
   */
  public void setLineNumber(final int lineNumber)
  {
    this.lineNumber = lineNumber;
  }

  /**
   * Retrieve the line number of the token
   * @return The line number of the token
   */
  public int getLineNumber()
  {
    return lineNumber;
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return The enumeration value of the Token
   */
  public abstract TokenType getType();

  /**
   * Set the lexeme of the token post-construction.
   * @param lexeme The new lexeme of the token
   */
  public abstract void setLexeme(String lexeme);

  /**
   * Performs comparisons against another Token object,
   * based on the lexeme values
   * @param token The Token object being compared
   * @return a value < 0 if current lexeme is less than compared,
   *         a value > 0 if current lexeme is greater than compared,
   *         a value of = if current lexeme and compared the same
   */
  @Override
  public int compareTo(Token token)
  {
    return lexeme.compareTo(token.lexeme);
  }
}
