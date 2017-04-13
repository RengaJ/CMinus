package codegen.table;

import codegen.emitter.MIPSRegister;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class LocalTable
{
  private HashMap<String, RegisterRecord> table;

  private int maxIDSize;

  public LocalTable()
  {
    table = new LinkedHashMap<>();

    maxIDSize = 0;
  }

  public void addRecord(final String id, final RegisterRecord record)
  {
    maxIDSize = Math.max(maxIDSize, id.length());

    table.put(id, record);
  }

  public RegisterRecord getRecord(final String id)
  {
    return table.get(id);
  }

  public void printTable()
  {
    System.out.println(" ID     | Register | Label  | Offset | Size");
    System.out.println("--------|----------|--------|--------|-----");

    if (table.isEmpty())
    {
      System.out.println(" <NONE> | <NONE>   | <NONE> | <NONE> | <NONE>");
      return;
    }
    for (final String id : table.keySet())
    {
      final RegisterRecord record = table.get(id);
      final MIPSRegister register = record.getRegister();
      final String regStr = (register != null) ? register.getRegister() : "";

      System.out.println(
          String.format(
              " %-5s  | %-8s | %-5s  | %-6d | %-4d",
              id,
              regStr,
              record.getLabel(),
              record.getOffset(),
              record.getSize()));
    }
  }

}
