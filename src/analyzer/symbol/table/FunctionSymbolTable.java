package analyzer.symbol.table;

import analyzer.symbol.SymbolItemType;

import java.util.ArrayList;

/**
 * Concrete representation of a function-based SymbolTable. This SymbolTable
 * contains fields for managing parameters and their counts. These SymbolTables
 * should be used when encountering a function definition node from the parser.
 */
public final class FunctionSymbolTable extends SymbolTable
{
  /**
   * An integer representation of the number of parameters supported by this
   * function. Can increase only when a ParameterNode has been properly examined
   */
  private int parameterCount;

  /**
   * A list-representation of the parameters. Since parameters can only currently
   * be an integer, this list is used to identify if the current parameter is
   * expecting an array-type.
   */
  private ArrayList<Boolean> arrayParameter;

  /**
   * Full constructor for the FunctionSymbolTable
   *
   * @param declared  The line on which the represented function was declared
   * @param classType The return type of the function being declared (needed to
   *                  ensure the function is being used properly)
   */
  public FunctionSymbolTable(final int declared, final Class<?> classType)
  {
    super(declared, classType);

    parameterCount = 0;
    arrayParameter = new ArrayList<>();
  }

  /**
   * Add a parameter to the set of known parameters for this function. Parameters
   * are always known to be of the integer type.
   *
   * @param isArray Boolean flag indicating if the parameter is an array of integers
   */
  public void addParameter(final boolean isArray)
  {
    ++parameterCount;

    arrayParameter.add(isArray);
  }

  /**
   * Obtain the number of parameters expected by this function
   *
   * @return The number of parameters expected by a function call
   */
  public final int getParameterCount()
  {
    return parameterCount;
  }

  /**
   * Get the symbol type associated with this particular realization of SymbolItem
   *
   * @return SYMBOL_TABLE_FUNCTION
   */
  @Override
  public SymbolItemType getSymbolType()
  {
    return SymbolItemType.SYMBOL_TABLE_FUNCTION;
  }
}
