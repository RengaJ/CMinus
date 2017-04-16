package codegen.table;

import codegen.emitter.MIPSRegister;

public final class RegisterRecord
{
  private MIPSRegister register;
  private String       label;
  private int          offset;
  private int          size;

  public RegisterRecord(final MIPSRegister register,
                        final int          offset  ,
                        final int          size    )
  {
    this.register = register;
    this.offset   = offset;
    this.size     = size;

    label         = (register != null) ? register.getRegister() : "";
  }

  public void setLabel(final String label)
  {
    this.label = label;
  }

  public MIPSRegister getRegister() { return register; }
  public String       getLabel()    { return label;    }
  public int          getOffset()   { return offset;   }
  public int          getSize()     { return size;     }
}
