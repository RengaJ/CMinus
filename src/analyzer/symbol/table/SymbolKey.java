package analyzer.symbol.table;

import java.util.HashMap;

/**
 * Immutable key that is to be used in the symbol table for storing records
 */
public class SymbolKey extends HashMap.SimpleEntry<String, SymbolKey.KeyType>
{
  public String name;

  public KeyType type;

  public SymbolKey(String name, KeyType type)
  {
    super(name, type);
    this.name = name;
    this.type = type;
  }

  public static SymbolKey CreateIDKey(final String name)
  {
    return new SymbolKey(name, KeyType.IDENTIFIER);
  }

  public static SymbolKey CreateScopeKey(final String name)
  {
    return new SymbolKey(name, KeyType.SCOPE);
  }

  @Override
  public String toString()
  {
    return String.format("(%s, %s)", name, type.toString());
  }

  /**
   * Enumeration that is used to identify the type of record being stored
   * in the symbol table
   */
  public enum KeyType
  {
    IDENTIFIER,      // The record being stored is an identifier
    SCOPE            // The record being stored is a scope (function or body)
  }
}
