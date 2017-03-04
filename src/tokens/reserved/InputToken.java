package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the non-keyword "input".
 * The value "input" is being treated as a reserved
 * word as it's defined to have a specific functionality
 * within the C- language specification.
 */
public final class InputToken extends ReservedToken
{
  /**
   * Full constructor for the InputToken
   */
  public InputToken()
  {
    super ("input");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_INPUT
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_INPUT;
  }
}
