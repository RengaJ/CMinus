package analyzer.symbol.table;

import analyzer.symbol.SymbolItemType;
import syntaxtree.ASTNodeType;
import syntaxtree.AbstractSyntaxTreeNode;

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
   * Full constructor for the FunctionSymbolTable
   *
   * @param declared  The line on which the represented function was declared
   * @param classType The return type of the function being declared (needed to
   *                  ensure the function is being used properly)
   */
  public FunctionSymbolTable(final int declared,
                             final Class<?> classType,
                             final AbstractSyntaxTreeNode node)
  {
    super(declared, classType, node);

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
   * Determine if the provided parameter index is an array.
   * Returns false if either the parameter is not an array or
   * if the parameter index is out of bounds.
   *
   * @param index The parameter number to examine
   * @return True if the parameter index is in bounds and is an array
   */
  public boolean isParameterArray(final int index)
  {
    if (index < 0 || index >= parameterCount)
    {
      return false;
    }

    return arrayParameter.get(index);
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
   * Add a record to the FunctionSymbolTable
   *
   * @param scope The scope level for the addition of the record. Scopes have a
   *              format of "scope1.scope2. ...". The root scope is defined as "".
   * @param node           The AbstractSyntaxTreeNode that contains the identifier
   *                       that is to be added
   * @param memoryLocation The location in memory where the identifier should
   *                       reside.
   *
   * @return An error code indicating the success or failure of the addition
   */
  @Override
  public SymbolTableCode addRecord(final String scope,
                                   final AbstractSyntaxTreeNode node,
                                   final int memoryLocation)
  {
    // Let the SymbolTable class perform the actual addition logic
    SymbolTableCode returnCode = super.addRecord(scope, node, memoryLocation);

    // If the addition went well...
    if (returnCode == SymbolTableCode.OK)
    {
      // If the record was a simple parameter, add a non-array entry to the
      // arrayParameter list
      if (node.getNodeType() == ASTNodeType.META_PARAMETER)
      {
        addParameter(false);
      }
      // If the record was an array parameter, add an array entry to the
      // arrayParameter list
      else if (node.getNodeType() == ASTNodeType.META_ARRAY_PARAMETER)
      {
        addParameter(true);
      }
    }

    return returnCode;
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

  /**
   * Obtain the String representation of the function symbol table
   *
   * @return The String representation of the function symbol table
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder(super.toString());

    builder.append(" Parameters:");
    if (parameterCount == 0)
    {
      builder.append(" NONE");
    }
    else
    {
      for (int i = 0; i < parameterCount; ++i)
      {
        String paramType = (arrayParameter.get(i) ? "int[]" : "int");

        builder.append(String.format(" %s", paramType));
      }
    }

    return builder.toString();
  }
}
