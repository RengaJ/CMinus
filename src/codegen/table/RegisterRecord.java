package codegen.table;

public final class RegisterRecord
{
  private String       label;
  private int          offset;
  private int          size;

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
