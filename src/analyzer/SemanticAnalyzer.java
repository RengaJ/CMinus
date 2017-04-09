package analyzer;

import analyzer.symbol.*;
import analyzer.symbol.table.*;
import globals.CompilerFlags;
import globals.ConsoleColor;
import syntaxtree.ASTNodeType;
import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.statement.IfStatementNode;
import syntaxtree.statement.WhileStatementNode;

import java.util.Stack;

/**
 * Class that represents the Semantic Analysis portion of the compilation process.
 * This class will take an AbstractSyntaxTreeNode obtained from the Parser and
 * create a SymbolTable that will be used for the Code Generation portion of the
 * compilation process.
 */
public final class SemanticAnalyzer
{
  /** The global scope string */
  private final String GLOBAL_SCOPE = "";

  /** Internally tracked symbol table (used during analysis procedure) */
  private SymbolTable symbolTable;

  /**
   * The current count of anonymous scopes (if, else and while) for unique scope
   * identification
   */
  private int anonymousScopeCount;

  /**
   * A boolean flag used to indicate if an error occurred during the semantic
   * analysis.
   */
  private boolean errorOccurred;

  /**
   * The current memory location for storing identifiers in the symbol table.
   * Reset for each function scope to properly prepare the stack in the code
   * generation step of the compiler.
   */
  private int memoryLocation;

  /**
   * Stack of memory locations such that functions and variables
   * can be declared in an interleaved manner.
   */
  private Stack<Integer> memoryStack;

  /**
   * The current parameter list count
   */
  private int parameterCount;

  /**
   * Full constructor for the SemanticAnalyzer
   */
  public SemanticAnalyzer()
  {
    symbolTable         = null;
    errorOccurred       = false;
    anonymousScopeCount = 0;
    memoryLocation      = 0;
    parameterCount      = 0;
    memoryStack         = new Stack<>();
  }

  /**
   * Given an abstract syntax tree, perform semantic analysis to produce a symbol
   * table.
   *
   * @param tree The abstract syntax tree on which to perform the semantic analysis
   * @return The newly created symbol table
   */
  public SymbolTable analyze(final AbstractSyntaxTreeNode tree)
  {
    // Reset the anonymous scope count
    anonymousScopeCount = 0;

    // Reset the memory location
    memoryLocation = 0;

    // Reset the memory stack
    memoryStack.clear();

    // Create the global symbol table that will be used to keep
    // track of everything in the semantic analysis
    symbolTable = new SymbolTable(-1, Void.class);

    // Add the input function call (assumes it's already defined)
    symbolTable.addScope("input",
        new FunctionSymbolTable(-1, Integer.class));

    // Add the output function call (assumes it's already defined)
    FunctionSymbolTable outputTable =
        new FunctionSymbolTable(-1, Void.class);
    // The output function takes 1 argument, so we'll add it here. This is a special
    // case of the addParameter function. This function should not be explicitly
    // used.
    outputTable.addParameter(false);
    symbolTable.addScope("output", outputTable);

    // Reset the errorOccurred flag from its previous value (hopefully false) to
    // false. This prevents accidental false failures
    errorOccurred = false;

    // Perform the actual processing
    processTree(tree, GLOBAL_SCOPE);

    // Remove all unused scopes from the symbol table (empty scopes usually appear
    // from the processing of if, else and while statements).
    symbolTable.removeAllEmpty();

    // Perform a final check to make sure there is a main method
    if (symbolTable.getSymbolItem(GLOBAL_SCOPE, "main", true) == null)
    {
      reportSemanticError(SymbolTableCode.MAIN_NOT_FOUND, 0);
    }

    // Return the completed symbol table
    return symbolTable;
  }

  /**
   * Check if an error occurred as a result of the semantic analysis
   *
   * @return The state of the SemanticAnalyzer's error indicator
   */
  public boolean errorOccurred()
  {
    return errorOccurred;
  }

  /**
   * Perform processing on a provided AbstractSyntaxNode tree (could be a single
   * node).
   *
   * @param tree  The AbstractSyntaxTreeNode on which processing should occur
   * @param scope The scope at which processing should be occurring
   * @return The number of siblings processed from this call
   */
  private int processTree(final AbstractSyntaxTreeNode tree, final String scope)
  {
    // Create a counter for the number of siblings processing in this manner
    int processed = 0;

    // Assign a new pointer to retain our position in the sibling list
    AbstractSyntaxTreeNode currentNode = tree;

    // While the node that we're looking at is actually something to examine...
    while (currentNode != null)
    {
      // Perform processing on the node
      processNode(currentNode, scope);

      // Set the current node to our sibling
      currentNode = currentNode.getSibling();

      // Increment our processed counter
      ++processed;
    }

    // When we're all done, return our processed count
    return processed;
  }

  /**
   * Perform processing on a single node, performing different actions based on
   * the type of node supplied to the function.
   *
   * @param node  The AbstractSyntaxTreeNode to be processed by this function
   * @param scope The scope at which the processing should occur
   */
  private void processNode(final AbstractSyntaxTreeNode node, final String scope)
  {
    // If the provided node is actually null, do nothing (it's okay if it is)
    if (node == null)
    {
      return;
    }

    // Write out a trace indicating what's currently being processed, and at what
    // scope
    if (CompilerFlags.TraceAnalyzer)
    {
      System.out.println("Analyzing " + node.getName() +
                         "\tScope = \"" + scope + "\"" +
                         "\tNode Type = \"" + node.getNodeType().toString() + "\"");
    }

    // Perform processing on the node based on its type...
    switch (node.getNodeType())
    {
      // If the node is a function call ( ID (...) )...
      case EXPRESSION_CALL:
      {
        processFunctionCall(node, scope);
        break;
      }
      // If the node is an array identifier ( ID[...] )...
      case EXPRESSION_ARRAY_IDENTIFIER:
      {
        // Attempt to update the current identifier's reference, and report the
        // semantic error if one occurs
        reportSemanticError(
            symbolTable.update(scope, node.getName(), node.getLineNumber(), false),
            node.getLineNumber());

        // Get the child of the identifier (the indexer)
        final AbstractSyntaxTreeNode child = node.getChild(0);

        // If the indexer is anything but a number...
        if (child.getNodeType() != ASTNodeType.EXPRESSION_NUMBER)
        {
          // Attempt to update the indexer's reference, and report the semantic
          // error if one occurs
          reportSemanticError(
              symbolTable.update(scope, child.getName(), child.getLineNumber(), false),
              child.getLineNumber());
        }
        break;
      }
      // If the node is a simple identifier ( ID )...
      case EXPRESSION_IDENTIFIER:
      {
        // Attempt to update the current identifier's reference, and report the
        // semantic error if one occurs
        reportSemanticError(
            symbolTable.update(scope, node.getName(), node.getLineNumber(), false),
            node.getLineNumber());
        break;
      }
      // If the node is an assignment ( ID = ... )...
      case STATEMENT_ASSIGN:
      // If the node is an operation ( ID + ID )...
      case EXPRESSION_OPERATION:
      {
        // Perform operator processing (assignment is still an operator)
        processOperator(node, scope);
        break;
      }
      // If the node is an array parameter ( int ID[] )...
      case META_ARRAY_PARAMETER:
      // If the node is a simple parameter ( int ID )...
      case META_PARAMETER:
      {
        processParameter(node, scope);
        break;
      }
      // If the node is a local array declaration ( int x[NUM] )...
      case STATEMENT_ARRAY_DECLARATION:
      {
        // Attempt to add the current identifier to the symbol table
        SymbolTableCode result = symbolTable.addRecord(scope, node, memoryLocation);

        // Check to see if the record was added to the symbol table
        if (result == SymbolTableCode.OK)
        {
          // If so, add the memory location by the size of the declared array
          // (array declarations are always declared with numbers)
          memoryLocation += node.getChild(0).getValue();
        }
        else
        {
          // Report the semantic error that occurred
          reportSemanticError(result, node.getLineNumber());
        }
        break;
      }
      // If the node is a local declaration ( int x )...
      case STATEMENT_VAR_DECLARATION:
      {
        // Attempt to add the current identifier to the symbol table
        SymbolTableCode result = symbolTable.addRecord(scope, node, memoryLocation);

        // Check to see if the record was added to the symbol table
        if (result == SymbolTableCode.OK)
        {
          // If so, increment the memory location by one
          ++memoryLocation;
        }
        else
        {
          // Report the semantic error that occurred
          reportSemanticError(result, node.getLineNumber());
        }
        break;
      }
      // If the node is a function declaration ( <type> ID( ... ) { ... } )...
      case META_FUNCTION:
      {
        processFunctionDeclaration(node, scope);
        break;
      }
      // If the node is an if-statement...
      case STATEMENT_IF:
      {
        processIfStatement(node, scope);
        break;
      }
      // If the node is a return statement...
      case STATEMENT_RETURN:
      {
        // Process the return's child
        processNode(node.getChild(0), scope);
        break;
      }
      // If the node is a while-statement...
      case STATEMENT_WHILE:
      {
        processWhileStatement(node, scope);
        break;
      }
      // If the node is an anonymous block...
      case META_ANONYMOUS_BLOCK:
      {
        processAnonymousBlock(node, scope);
        break;
      }
      // If the node is a constant value ( NUM ) or is unrecognized...
      default:
      {
        // Do nothing
        break;
      }
    }
  }

  /**
   * Perform semantic analysis on a function declaration. This usually means that
   * the function will be added as a scope to the symbol table and then the
   * parameters and body will be processed.
   *
   * @param node  The AbstractSyntaxTreeNode on which the function declaration is
   *              defined
   * @param scope The current scope on which the function should be declared.
   */
  private void processFunctionDeclaration(final AbstractSyntaxTreeNode node,
                                          final String scope)
  {
    // Check to make sure the current scope is "" (GLOBAL_SCOPE). This is because
    // function declarations cannot be defined within other scopes.
    if (!scope.equals(GLOBAL_SCOPE))
    {
      // Report a semantic error and do not process further
      reportSemanticError(SymbolTableCode.NESTED_DEFINITION, node.getLineNumber());
      return;
    }

    // Attempt to add the function declaration
    reportSemanticError(symbolTable.addScope(scope, node), node.getLineNumber());

    // Reset the parameter counter
    parameterCount = 0;

    // Process parameters
    processTree(node.getChild(0), node.getName());

    // Reset the parameter counter
    parameterCount = 0;

    // Add the current memory count to the memory stack
    memoryStack.push(memoryLocation);

    // Reset the memory location
    memoryLocation = 0;

    // Process the function body
    processTree(node.getChild(1), node.getName());

    // Restore the memory location
    memoryLocation = memoryStack.pop();
  }

  /**
   * Process a parameter and add it to the scope's parameter list (if possible)
   *
   * @param node  The AbstractSyntaxTreeNode that contains the parameter's
   *              definition
   * @param scope The scope in which the parameter should be added.
   */
  private void processParameter(final AbstractSyntaxTreeNode node,
                                final String scope)
  {
    // Check to see if the parameter is a void type is in the middle or beginning
    // of an argument list, report a semantic error
    if (node.getType() == Void.class)
    {
      if (node.hasSibling())
      {
        reportSemanticError(SymbolTableCode.VOID_ARGUMENT, node.getLineNumber());
      }
    }
    else
    {
      // Add a record to the symbol table, using a memory location of 0 (parameters
      // won't know the memory location, so it's okay to do this)
      reportSemanticError(
          symbolTable.addRecord(scope, node, parameterCount++),
          node.getLineNumber());

      // If there is another argument in the list and the next type is void,
      // report a semantic error
      if (node.hasSibling() && node.getSibling().getType() == Void.class)
      {
        reportSemanticError(SymbolTableCode.VOID_ARGUMENT,
                            node.getSibling().getLineNumber());
      }
    }
  }

  /**
   * Process an if-statement and it's corresponding else-statement (if it exists)
   *
   * @param node  The AbstractSyntaxTreeNode that contain's the if-statement's
   *              definition
   * @param scope The scope in which the if-statement should be processed
   */
  private void processIfStatement(final AbstractSyntaxTreeNode node,
                                  final String scope)
  {
    // Extract the current node's line number
    int lineNumber = node.getLineNumber();

    // Check to see if the condition contains a boolean expression (determined
    // at parse-completion).
    if (node.getChild(0).getType() != Boolean.class)
    {
      // If not, report a semantic error and continue processing (we want to
      // obtain as many semantic errors as possible)
      reportSemanticError(SymbolTableCode.SEMANTIC_FAILURE, lineNumber);
    }

    // Process the condition regardless of whether it's a boolean expression
    processNode(node.getChild(0), scope);

    // Create a scope for the if-statement to process within
    String ifScope = String.format("if_%d", ++anonymousScopeCount);

    // Create a new if-statement node that will be used to prime the creation
    // of a new scope in the symbol table (at the current scope)

    // Attempt to create the new scope at the current scope (reporting an error
    // if one occurs)
    reportSemanticError(symbolTable.addScope(scope, node), lineNumber);

    // Create a new scope that will be used for additional processing
    // ([currentScope].[newScope])
    String newScope = String.format("%s.%s", scope, ifScope);

    // Process the if-statement's "then" body with the newly created scope
    processTree(node.getChild(1), newScope);

    // Check to see if there is an else-statement associated with this if-statement
    if (node.getChild(2) == null)
    {
      return;
    }

    lineNumber = node.getChild(2).getLineNumber();

    // If there is an else-statement associated with this if-statement, a new
    // IfStatementNode needs to be created to add to the symbol table for the
    // else-statement's body
    String elseScope = String.format("else_%d", anonymousScopeCount);

    // Create a new scope that will be used for additional processing
    newScope = String.format("%s.%s", scope, elseScope);

    // Process the if-statement's "else" body with the newly created scope
    processTree(node.getChild(2), newScope);
  }

  /**
   * Perform semantic analysis on an while-statement. This processing will process
   * the condition being tested to ensure it's a boolean statement, and then will
   * initiate processing on the while-statement's body.
   *
   * @param node  The AbstractSyntaxTreeNode that contains the while-statement's
   *              definition
   * @param scope The scope at which the while-statement currently resides. This is
   *              important for scoping the identifiers properly.
   */
  private void processWhileStatement(final AbstractSyntaxTreeNode node,
                                     final String scope)
  {
    // Extract the current node's line number
    final int lineNumber = node.getLineNumber();

    // Check to see if the condition contains a boolean expression (determined
    // at parse-completion).
    if (node.getChild(0).getType() != Boolean.class)
    {
      // If not, report a semantic error and continue processing (we want to
      // obtain as many semantic errors as possible)
      reportSemanticError(SymbolTableCode.SEMANTIC_FAILURE, lineNumber);
    }

    // Process the condition regardless of whether it's a boolean expression
    processNode(node.getChild(0), scope);

    // Create a scope for the while-loop to process within
    String whileScope = String.format("while_%d", ++anonymousScopeCount);

    // Attempt to create the new scope at the current scope (reporting an error
    // if one occurs)
    reportSemanticError(symbolTable.addScope(scope, node), lineNumber);

    // Create a new scope that will be used for additional processing
    // ([currentScope].[newScope])
    String newScope = String.format("%s.%s", scope, whileScope);

    // Process the while-statement's body with the newly created scope
    processTree(node.getChild(1), newScope);
  }

  /**
   * Perform semantic analysis on an operator. This processing will process each
   * side of the operand and then ensure that the proper type is being used for
   * each side of the operation (integer)
   *
   * @param node  The AbstractSyntaxTreeNode that contains the operator's
   *              definition
   * @param scope The scope at which the operator currently resides. This is
   *              important for scoping the identifiers properly.
   */
  private void processOperator(final AbstractSyntaxTreeNode node,
                               final String scope)
  {
    // Process the left hand side of the operation
    processNode(node.getChild(0), scope);

    // Check to see if the left hand side of the operation is an integer
    // (operators can only operate on integers)
    if (node.getChild(0).getType() != Integer.class)
    {
      // If the left hand side is not an integer, report a semantic error and
      // continue processing (we want to get as many errors as possible)
      reportSemanticError(SymbolTableCode.INVALID_LHS, node.getLineNumber());
    }

    // Process the right hand side of the operation
    processNode(node.getChild(1), scope);

    // Check to see if the right hand side of the operation is an integer
    // (operators can only operate on integers)
    if (node.getChild(1).getType() != Integer.class)
    {
      // If the right hand side is not an integer, report a semantic error.
      reportSemanticError(SymbolTableCode.INVALID_RHS, node.getLineNumber());
    }
  }

  /**
   * Perform semantic analysis on a function call. This processing will look at:
   *  1) The function being called's existence
   *  2) The number of arguments being supplied
   *  3) The types of arguments being supplied
   *
   *  If any of those three areas contain invalid information, the processing
   *  of the function call will terminate. It WILL NOT terminate the remaining
   *  semantic analysis procedure.
   *
   * @param node The AbstractSyntaxTreeNode that contains the function call
   *             definition.
   * @param scope The scope at which the function call resides. This is important
   *              for scoping the identifiers properly.
   */
  private void processFunctionCall(final AbstractSyntaxTreeNode node,
                                   final String scope)
  {
    // Retrieve the values that will be used multiple times throughout this
    // processing function
    final int lineNumber = node.getLineNumber();
    final String name    = node.getName();

    // Attempt to update the function call's usage in the symbol table
    SymbolTableCode result = symbolTable.update(scope, name, lineNumber, true);

    // Check to see if the processing succeeded
    if (result != SymbolTableCode.OK)
    {
      // If the processing failed (a semantic error occurred), report the error
      // and terminate further processing on this function call (not enough
      // information can be extracted without a proper function existing in the
      // symbol table)
      reportSemanticError(result, lineNumber);
      return;
    }

    // Retrieve the function from the symbol table
    SymbolItem function = symbolTable.getSymbolItem(scope, name, true);

    // Make sure the function retrieved is actually a function symbol table
    if (function.getSymbolType() != SymbolItemType.SYMBOL_TABLE_FUNCTION)
    {
      // If not, report a semantic failure and terminate further processing
      // (the remaining portion of the processing requires the use of
      // function-table specific parameters that are unobtainable in non-function
      // symbol tables)
      reportSemanticError(SymbolTableCode.SEMANTIC_FAILURE, lineNumber);
      return;
    }

    // Cast the function symbol item into a function symbol table
    FunctionSymbolTable functionSymbolTable = (FunctionSymbolTable)function;

    // make sure the node type is changed to the class type (it's more than
    // likely that the node type is Void.class, which may be incorrect for other
    // processing, such as operators or function arguments).
    node.setType(functionSymbolTable.getClassType());

    // Process each of the arguments of the function, extracting the number of
    // arguments processed as a result
    int processed = processTree(node.getChild(0), scope);

    // Make sure the number of arguments processed is the same as the number of
    // arguments in the definition of the function.
    if (processed != functionSymbolTable.getParameterCount())
    {
      // If there are not exactly the same number of parameters, throw an error
      // indicating that the provided parameter count is incorrect. Terminate
      // further processing, as it's possible that there may be more given arguments
      // than the function supports.
      reportSemanticError(SymbolTableCode.BAD_PARAM_COUNT, lineNumber);
      return;
    }

    // Prepare to iterate through all of the function's argument types to perform
    // type checking
    int index = 0;
    AbstractSyntaxTreeNode arg = node.getChild(index);
    // While there is an argument to process:
    while (arg != null)
    {
      // Check the argument's type. If it's an operator (+, -), a number (0, 1),
      // or an array identifier ( x[k] ):
      if (arg.getNodeType() == ASTNodeType.EXPRESSION_OPERATION ||
          arg.getNodeType() == ASTNodeType.EXPRESSION_NUMBER    ||
          arg.getNodeType() == ASTNodeType.EXPRESSION_ARRAY_IDENTIFIER)
      {
        // Expect an integer type in the function at the current index
        if (functionSymbolTable.isParameterArray(index))
        {
          // If the current index is actually an array-type, the provided argument
          // is invalid. Report an error and continue processing (we want to
          // collect as many semantic errors as possible now)
          reportSemanticError(SymbolTableCode.INVALID_PTYPE, arg.getLineNumber());
        }
      }
      // If the node isn't any of those mentioned above, they probably exist in
      // the symbol table (although they might not...)
      else
      {
        // Determine if the argument is a scope
        boolean isScope = arg.getNodeType() == ASTNodeType.EXPRESSION_CALL;

        // Retrieve the current argument from the symbol table
        final SymbolItem argument =
            symbolTable.getSymbolItem(scope, arg.getName(), isScope);

        // Check to make sure the argument exists (not null)
        if (argument == null)
        {
          // If the argument is null (for some reason), report a semantic
          // error and continue processing
          reportSemanticError(SymbolTableCode.RECORD_NOT_FOUND, lineNumber);
        }
        // The argument exists...
        else
        {
          // Get the type of argument
          final SymbolItemType type = argument.getSymbolType();

          // If the argument is an array and we're expecting an integer OR
          //    the argument is an integer and we're expecting an array...
          if ((type == SymbolItemType.SYMBOL_RECORD_ARRAY &&
              !functionSymbolTable.isParameterArray(index)) ||
              (type != SymbolItemType.SYMBOL_RECORD_ARRAY &&
                  functionSymbolTable.isParameterArray(index)))
          {
            // Report a semantic error of invalid parameter type. Continue
            // processing...
            reportSemanticError(SymbolTableCode.INVALID_PTYPE, lineNumber);
          }
          // If the argument is a function call...
          else if (type == SymbolItemType.SYMBOL_TABLE_FUNCTION)
          {
            // If either the function has a void return type or we're expecting
            // an array argument...
            if (argument.getClassType() == Void.class ||
                functionSymbolTable.isParameterArray(index))
            {
              // Report a semantic error of invalid parameter type. Continue
              // processing...
              reportSemanticError(SymbolTableCode.INVALID_PTYPE, lineNumber);
            }
          }// end if SYMBOL_TABLE_FUNCTION
        }// end ELSE
      } // end ELSE

      // Increment the parameter index by one
      ++index;

      // Retrieve the next argument in the list
      arg = arg.getSibling();
    } // end WHILE

    // Function complete
  }

  /**
   * Perform semantic analysis on an anonymous scope block.
   *
   * @param node  The AbstractSyntaxTreeNode in which the anonymous block is
   *              defined
   * @param scope The scope at which the anonymous block should be processed
   */
  private void processAnonymousBlock(final AbstractSyntaxTreeNode node,
                                     final String scope)
  {
    // Attempt to add the scope to the symbol table and report any semantic errors
    // that may have occurred
    reportSemanticError(symbolTable.addScope(scope, node), node.getLineNumber());

    // Create a new scope that will be used for the duration of the anonymous
    // block's processing
    final String newScope = String.format("%s.%s", scope, node.getName());

    // Process the contents of the anonymous block
    processTree(node.getChild(0), newScope);
  }

  /**
   * Report a semantic error to the user, indicating that something went wrong. If
   * an error code of OK is reported, this function does nothing. Note that if an
   * error is reported to the user, the 'errorOccurred' flag will flip to true.
   *
   * @param errorCode  The error code to report to the user
   * @param lineNumber The line number on which the error occurred
   */
  private void reportSemanticError(final SymbolTableCode errorCode,
                                   final int lineNumber)
  {
    // If the provided error is not OK, present the error to the user
    if (errorCode != SymbolTableCode.OK)
    {
        ConsoleColor.PrintRed(String.format(
            "***** SEMANTIC ERROR - %s - Line %d *****",
            errorCode.toString(),
            lineNumber));

        // Flip the 'errorOccurred' flag to true
        errorOccurred = true;
    }
  }
}
