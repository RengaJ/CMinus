package codegen.table;

/**
 * Structure to contain information about a register/variable pairing
 */
public final class RegisterRecord
{
  /**
   * The label (or register)
   */
  private String       label;
  /**
   * The offset
   */
  private int          offset;
  /**
   * The size of the variable
   */
  private int          size;

  /**
   * The full constructor of the Register Record
   * @param label  The label of the register
   * @param offset The offset of the register
   * @param size   The size of the register
   */
  public RegisterRecord(final String label ,
                        final int    offset,
                        final int    size  )
  {
    this.offset   = offset * 4;
    this.size     = size;

    this.label = label;
  }

  public void setLabel(final String label)
  {
    this.label = label;
  }

  public String       getLabel()    { return label;    }
  public int          getOffset()   { return offset;   }
  public int          getSize()     { return size;     }
}
