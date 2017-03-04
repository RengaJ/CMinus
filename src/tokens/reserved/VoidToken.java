package tokens.reserved;

import tokens.TokenType;

/**
 * Class that represents the reserved word "void"
 */
public final class VoidToken extends ReservedToken
{
  /**
   * Full constructor for the VoidToken
   */
  public VoidToken()
  {
    super ("void");
  }

  /**
   * Obtain the enumeration value of the Token object
   *
   * @return RESERVED_VOID
   */
  @Override
  public TokenType getType()
  {
    return TokenType.RESERVED_VOID;
  }
}
