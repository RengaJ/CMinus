package tokens.variable;

import tokens.TokenType;

/**
 * Class that represents a non-reserved identifier
 */
public final class IdentifierToken extends VariableToken
{
  /**
   * Full constructor for the IdentifierToken
   */
  public IdentifierToken()
  {
    super();
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return VARIABLE_IDENTIFIER
   */
  @Override
  public TokenType getType()
  {
    return TokenType.VARIABLE_IDENTIFIER;
  }
}
