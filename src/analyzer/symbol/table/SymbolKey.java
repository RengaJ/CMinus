package analyzer.symbol.table;

import java.util.HashMap;

/**
 * Immutable key that is to be used in the symbol table for storing records
 */
public class SymbolKey extends HashMap.SimpleEntry<String, SymbolKey.KeyType>
{
  /** The name of the identifier in the symbol table */
  private String name;

  /** The type of the identifier in the symbol table */
  private KeyType type;

  /**
   * The full constructor of the SymbolKey
   *
   * @param name The name of the identifier
   * @param type The type of the identifier (ID or SCOPE)
   */
  private SymbolKey(String name, KeyType type)
  {
    super(name, type);
    this.name = name;
    this.type = type;
  }

  /**
   * Create a SymbolKey for an IDENTIFIER entry
   *
   * @param name The name of the identifier
   *
   * @return A SymbolKey that represents an IDENTIFIER entry
   */
  static SymbolKey CreateIDKey(final String name)
  {
    return new SymbolKey(name, KeyType.IDENTIFIER);
  }

  /**
   * Create a SymbolKey for a SCOPE entry
   *
   * @param name The name of the scope
   *
   * @return A SymbolKey that represents a SCOPE entry
   */
  static SymbolKey CreateScopeKey(final String name)
  {
    return new SymbolKey(name, KeyType.SCOPE);
  }

  /**
   * Get the name of the SymbolKey
   *
   * @return The name of the SymbolKey
   */
  public String getName()
  {
    return name;
  }

  /**
   * Get the type of the SymbolKey
   *
   * @return The type of the SymbolKey
   */
  public KeyType getType()
  {
    return type;
  }

  /**
   * Obtain the String representation of the record
   *
   * @return The String representation of the record
   */
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
