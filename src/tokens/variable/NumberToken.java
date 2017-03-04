package tokens.variable;

import tokens.TokenType;

/**
 * Class that represents a non-reserved identifier
 */
public final class NumberToken extends VariableToken
{
  /**
   * Full constructor for the NumberToken
   */
  public NumberToken()
  {
    super();
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return VARIABLE_NUMBER
   */
  @Override
  public TokenType getType()
  {
    return TokenType.VARIABLE_NUMBER;
  }
}
