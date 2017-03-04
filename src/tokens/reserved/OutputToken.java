package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the non-keyword "output".
 * The value "output" is being treated as a reserved
 * word as it's defined to have a specific functionality
 * within the C- language specification.
 */
public final class OutputToken extends ReservedToken
{
  /**
   * Full constructor for the OutputToken
   */
  public OutputToken()
  {
    super ("output");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_OUTPUT
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_OUTPUT;
  }
}
