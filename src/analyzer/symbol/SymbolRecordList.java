package analyzer.symbol;

import java.util.ArrayList;

/**
 * Class that wraps an ArrayList of SymbolRecord objects.
 */
public class SymbolRecordList implements SymbolItem
{
  private ArrayList<SymbolRecord> recordList;

  public SymbolRecordList()
  {
    recordList = new ArrayList<>();
  }
  /**
   * Get the symbol type associated with this particular realization of SymbolItem
   *
   * @return The enumeration that identifies the symbol item
   */
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_RECORD;
  }


  public void add(final SymbolRecord record)
  {
    recordList.add(record);
  }
}
