package analyzer.symbol;

/**
 * An enumeration used to easily identify what sub-class of SymbolItem is currently
 * being examined. Useful when traversing SymbolTables and SymbolRecords
 */
public enum SymbolItemType
{
  SYMBOL_RECORD_ARRAY,    // Used to identify an array-based symbol record
  SYMBOL_RECORD_SIMPLE,   // Used to identify a non-array symbol record
  SYMBOL_TABLE_FUNCTION,  // Used to identify the symbol table for a function
  SYMBOL_TABLE_SCOPE      // Used to identify the symbol table for a control scope
}
