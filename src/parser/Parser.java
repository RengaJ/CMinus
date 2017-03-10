package parser;

import syntaxtree.ASTNodeType;
import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.expression.AssignExpressionNode;
import syntaxtree.expression.ConstantExpressionNode;
import syntaxtree.expression.IDExpressionNode;
import syntaxtree.expression.OperationExpressionNode;
import syntaxtree.meta.FunctionNode;
import syntaxtree.meta.ParameterNode;
import syntaxtree.statement.IfStatementNode;
import syntaxtree.statement.ReturnStatementNode;
import syntaxtree.statement.VarDeclarationStatementNode;
import syntaxtree.statement.WhileStatementNode;
import tokens.Token;
import tokens.TokenType;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * The C- Parser. This Parser will read in a list of scanned
 * tokens from the Scanner and produce an abstract syntax
 * tree used for determining the structure of the file being
 * compiled.
 */
public final class Parser
{
  /**
   * A private copy of the token list provided in the parse operation
   */
  private Deque<Token> tokenList;

  /**
   * The current token being examined
   */
  private Token currentToken;

  /**
   * Full constructor for the Parser
   */
  public Parser()
  {
  }

  /**
   * Method stub for main parsing operation.
   *
   * @param tokenList The list of tokens to parse (obtained from the
   *                  Scanner)
   * @return The root of the Abstract Syntax Tree that is constructed
   *         during the parsing operation
   */
  public AbstractSyntaxTreeNode parse(Deque<Token> tokenList)
  {
    // Store the provided list of tokens
    this.tokenList = new ArrayDeque<>(tokenList);

    // Keep track of the first token in the list,
    // and remove it from the list
    currentToken = this.tokenList.pop();

    return createSyntaxTree();
  }

  private AbstractSyntaxTreeNode createSyntaxTree()
  {
    // Check to see if the end-of-file indicator has been reached.
    // This should preempt any statement processing
    if (matchCurrent(TokenType.BOOKKEEPING_END_OF_FILE))
    {
      // If the end-of-file indicator has been detected,
      // terminate processing.
      return null;
    }

    // Initialize the syntax tree to a processed statement
    AbstractSyntaxTreeNode tree = processStatement();

    // Check to see if the end of a statement has been reached
    // (indicated by a semi-colon)
    if (matchCurrent(TokenType.SPECIAL_SEMICOLON))
    {
      // Consume the semi-colon and advance the current token
      matchAndPop(TokenType.SPECIAL_SEMICOLON);
    }

    // Check to see if the current token is now a body terminator ( } )
    if (!matchCurrent(TokenType.SPECIAL_RIGHT_BRACE))
    {
      // If not, process the next statement and set its value to
      // the current statement's sibling
      tree.setSibling(createSyntaxTree());
    }
    // If the current token is in fact a body terminator, consume the
    // token and terminate processing
    else
    {
      matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
    }

    // Return the current tree
    return tree;
  }

  /**
   * Process statements by examining the current token for
   * the necessary path of execution
   *
   * @return An AbstractSyntaxTreeNode that contains the ne
   */
  private AbstractSyntaxTreeNode processStatement()
  {
    AbstractSyntaxTreeNode statement = null;
    switch (currentToken.getType())
    {
      // If the current token is a type
      // (int or void), process the type
      // (no statement should be created)
      case RESERVED_INT:
      case RESERVED_VOID:
      {
        // Obtain the current identifier type
        Class<?> identifierType = processTypeSpecifier(currentToken.getType());

        if (identifierType == null)
        {
          logSyntaxError();

          break;
        }
        // If the current token is an identifier, process the ID here
        // as opposed to waiting, since it's simpler to trace
        if (matchCurrent(TokenType.VARIABLE_IDENTIFIER))
        {
          statement = processID(identifierType);
        }
        // If not, check to see if the special case of
        //       ... ID( void )
        // was detected
        else if (identifierType == Void.class &&
            matchCurrent(TokenType.SPECIAL_RIGHT_PAREN))
        {
          statement = processSimpleParameter(identifierType);
        }
        // If not, then a syntax error has occurred and should be
        // reported
        else
        {
          logSyntaxError(currentToken, TokenType.VARIABLE_IDENTIFIER);
        }

        break;
      }

      // If the current token is an identifier, process the identifier
      // with no identifier type (there is no known identifier context here)
      case VARIABLE_IDENTIFIER:
      {
        statement = processID(null);
        break;
      }

      // If the current token is the right brace, do nothing (we don't
      // want to log a syntax error here, since it might actually be
      // the expected token)
      case SPECIAL_RIGHT_BRACE:
      // If the current token is the semi-colon, do nothing (we don't
      // want to log a syntax error here, since it might actually be
      // the expected token)
      case SPECIAL_SEMICOLON:
      {
        break;
      }

      case RESERVED_IF:
      {
        break;
      }

      case RESERVED_WHILE:
      {
        break;
      }

      case RESERVED_RETURN:
      {
        break;
      }

      // If none of the above token types were matched, there is an
      // issue with the current token. Log a syntax error and do not
      // provide a statement
      default:
      {
        logSyntaxError();
        break;
      }
    }
    return statement;
  }

  /**
   * Processes the current identifier type based on the provided
   * token type.
   *
   * @param tokenType The token type used to identify what the
   *                  current identifier type should be.
   * @return The value of the processed type sepecifier
   */
  private Class<?> processTypeSpecifier(final TokenType tokenType)
  {
    // Initialize the identifier type
    Class<?> identifierType = null;

    // If the provided token type is RESERVED_INT,
    // set the identifier type to Integer.class
    if (tokenType == TokenType.RESERVED_INT)
    {
      // Assign the identifier type to Integer.class
      identifierType = Integer.class;

      // Advance to the next token
      matchAndPop(TokenType.RESERVED_INT);
    }
    // If the provided token type is RESERVED_VOID,
    // set the identifier type to Void.class
    else if (tokenType == TokenType.RESERVED_VOID)
    {
      // Assign the identifier type to Void.class
      identifierType = Void.class;

      // Advance to the next token
      matchAndPop(TokenType.RESERVED_VOID);
    }
    // Return the value of the (hopefully) newly assigned
    // identifier type
    return identifierType;
  }

  /**
   * Process an identifier token, which requires context
   * to determine how to properly process the token
   *
   * @return The resulting AST node, or {@code null} if
   *         the processing of the token failed.
   */
  private AbstractSyntaxTreeNode processID(Class<?> identifierType)
  {
    // Context is needed in order to properly process an identifier.
    // Look at the next token for the necessary context
    TokenType nextTokenType = tokenList.peek().getType();

    AbstractSyntaxTreeNode node = null;
    // The valid tokens that can follow the VARIABLE_IDENTIFIER token are:
    // --> Any expression operator (+, -, /, *, <, <=, >, >=, ==, !=)
    // --> Comma (found in parameter and argument lists)
    // --> Right Parenthesis (found at the end of parameter and argument lists)
    // --> Left bracket (found in ID[] and ID[<expression>] statements)
    // --> Right bracket (found at the end of ID[<expression>] statements)
    // --> Left Parenthesis (found in function signatures and function calls)
    // --> Semi-colon (found at the end of declaration or statements)
    // --> Assignment operator (ID = <expression>;)
    switch (nextTokenType)
    {
      // Expression Operators
      case SPECIAL_PLUS:
      case SPECIAL_MINUS:
      case SPECIAL_DIVIDE:
      case SPECIAL_TIMES:
      case SPECIAL_LESS_THAN:
      case SPECIAL_LTE:
      case SPECIAL_GREATER_THAN:
      case SPECIAL_GTE:
      case SPECIAL_EQUAL:
      case SPECIAL_NOT_EQUAL:
      {
        // The start of an expression has been found, but it's not necessary
        // to process the expression here. It is only necessary to process the
        // identifier here.

        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier type is not null, a syntax error
          // has occurred. At statement such as:
          //     int x + ...
          // has been detected. This is not valid within the language,
          // so an error must be reported.
          logSyntaxError();
        }
        else
        {
          // If the identifier type is null, we're currently looking at a
          // statement such as:
          //        x + ...
          // At this point, just return the identifier node, as the full
          // expression will be processed in another function.
          node = processSimpleIdentifier();
        }
        break;
      }

      // Comma (Parameter and argument lists)
      case SPECIAL_COMMA:
      {
        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier type is not null, we're currently looking
          // in a parameter list:
          //
          // ... ... (int x, int y)
          node = processSimpleParameter(identifierType);
        }
        else
        {
          // If the identifier type is null, we're currently looking in
          // an argument list:
          //
          // ...(x, y)
          node = processSimpleIdentifier();
        }
        break;
      }

      // Right Parenthesis (Indicates the end of a parameter or argument list)
      case SPECIAL_RIGHT_PAREN:
      {
        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier type is not null, we're currently looking at the
          // end of a parameter list
          node = processSimpleParameter(identifierType);
        }
        else
        {
          // If the identifier is null, we're currently looking at the end of an
          // argument list
          node = processSimpleIdentifier();
        }
        break;
      }

      // Left bracket (Array-based identifiers / expression)
      case SPECIAL_LEFT_BRACKET:
      {
        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier type is not null, we're currently looking
          // at a declaration (could be in a parameter list, but it's
          // currently unknown).
        }
        else
        {
          // If the identifier type is null, we're currently looking at
          // a usage of the variable. Special processing should take place
          // here to ensure proper parsing:
          //
          // We could have ID[<number>], ID[ID], ID[ID[...]] or even ID[x + y]
        }
        break;
      }

      case SPECIAL_RIGHT_BRACKET:
      {
        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier type is not null, a syntax error has occured. This
          // condition means we have discovered a statement such as:
          //    ID[int ID]
          // This is not syntactically valid, and must be reported.
          logSyntaxError();
        }
        else
        {
          // If the identifier type is null, we're probably looking at the end of
          // an expression
          node = processSimpleIdentifier();
        }
        break;
      }

      // Left Parenthesis (Function Signature or Call)
      case SPECIAL_LEFT_PAREN:
      {
        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier is not null, we're currently looking
          // at a function signature.
          node = processFunction(identifierType);
        }
        else
        {
          // If the identifier type is null, we're currently looking
          // at a function call
        }
        break;
      }

      // Semi-colon (Variable declaration or statement termination)
      case SPECIAL_SEMICOLON:
      {
        // Test to see if the identifier type has bene set
        if (identifierType != null)
        {
          // If the identifier is not null, we're currently looking
          // at a variable declaration
          node = processSimpleDeclaration(identifierType);
        }
        else
        {
          // If the identifier is null, we're currently looking at
          // the end of a statement
          node = processSimpleIdentifier();
        }
        break;
      }

      // Assignment operator (used to begin assigment operations)
      case SPECIAL_ASSIGN:
      {
        // Test to see if the identifier type has been set
        if (identifierType != null)
        {
          // If the identifier type is not null, a syntax error has occurred.
          // Variables cannot be defined and assigned to in the same
          // statement. Report that an error occurred.
          logSyntaxError();
        }
        else
        {
          // If the identifier is null, we're currently looking at an
          // assignment statement.
          node = processAssignment();
        }
        break;
      }

      // If none of the above token types were found, log a syntax error,
      // as it's possible that the identifier was improperly provided
      default:
      {
        logSyntaxError();
        break;
      }
    }
    return node;
  }

  /**
   * Create a simple (non-array) parameter that corresponds to
   * the current identifier.
   *
   * @param identifierType The type of non-array parameter being declared
   *
   * @return The created simple parameter node
   */
  private ParameterNode processSimpleParameter(Class<?> identifierType)
  {
    // Create the parameter node
    ParameterNode parameterNode = new ParameterNode();

    // Fill out the node with as much information as possible:
    // > The name of the node will be the name of the identifier being assigned
    // > The line number of the node
    // > The token type will be VARIABLE_IDENTIFIER
    // > The node type will be the current identifier type

    parameterNode.setName      (currentToken.getLexeme());
    parameterNode.setLineNumber(currentToken.getLineNumber());
    parameterNode.setTokenType (TokenType.VARIABLE_IDENTIFIER);
    parameterNode.setType      (identifierType);

    // Some special processing needs to take place to identify if this is
    // a void parameter or a non-void parameter. Void parameters will not
    // have a name associated with them.
    if (identifierType == Void.class)
    {
      parameterNode.setName("");
    }
    // If this is a non-void parameter, the current token should be an identifier.
    // If not, log a syntax error
    else
    {
      // Advance the current token to ensure that processing continues smoothly
      matchAndPop(TokenType.VARIABLE_IDENTIFIER);
    }
    // There's nothing left to assign to this node, so return it now
    return parameterNode;
  }

  /**
   * Create a simple (non-array) identifier declaration that
   * corresponds to the current identifier.
   *
   * @param identifierType The type of declaration being made
   *
   * @return The created identifier declaration node
   */
  private VarDeclarationStatementNode processSimpleDeclaration(Class<?> identifierType)
  {
    // Create the variable declaration statement node
    VarDeclarationStatementNode varDeclaration = new VarDeclarationStatementNode();

    // Fill out the node with as much information as possible:
    // > The name of the node will be the name of the identifier being assigned
    // > The line number of the node
    // > The token type will be VARIABLE_IDENTIFIER
    // > The node type will be the current identifier type
    varDeclaration.setName      (currentToken.getLexeme());
    varDeclaration.setLineNumber(currentToken.getLineNumber());
    varDeclaration.setTokenType (TokenType.VARIABLE_IDENTIFIER);
    varDeclaration.setType      (identifierType);

    // Reset the identifier type, since it's no longer valid
    identifierType = null;

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.VARIABLE_IDENTIFIER);

    // There's nothing left to assign to this node, so return it now
    return varDeclaration;
  }

  /**
   * Create a simple (non-array) identifier that corresponds
   * to the current identifier.
   *
   * @return The created identifier node
   */
  private IDExpressionNode processSimpleIdentifier()
  {
    // Create the identifier expression node
    IDExpressionNode idExpression = new IDExpressionNode();

    // Fill out the node with as much information as possible:
    // > The name of the node will be the name of the identifier being assigned
    // > The line number of the node
    // > The token type will be VARIABLE_IDENTIFIER
    idExpression.setName      (currentToken.getLexeme());
    idExpression.setLineNumber(currentToken.getLineNumber());
    idExpression.setTokenType (TokenType.VARIABLE_IDENTIFIER);

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.VARIABLE_IDENTIFIER);

    // There's nothing left to assign to this node, so return it now
    return idExpression;
  }

  /**
   * Create a FunctionNode that contains the signature and the function body.
   *
   * @param identifierType The return type of the function being processed
   *
   * @return The processed function definition
   */
  private FunctionNode processFunction(Class<?> identifierType)
  {
    // Create the function node
    FunctionNode functionNode = new FunctionNode();

    // Fill out the node with as much information as possible:
    // > The name of the node will be the name of the identifier being assigned
    // > The line number of the node
    // > The token type will be VARIABLE_IDENTIFIER
    // > The node type will be the current identifier type
    functionNode.setName      (currentToken.getLexeme());
    functionNode.setLineNumber(currentToken.getLineNumber());
    functionNode.setTokenType (TokenType.VARIABLE_IDENTIFIER);
    functionNode.setType      (identifierType);

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.VARIABLE_IDENTIFIER);

    // The completion of the function requires two more steps:
    // 1. Parsing of the parameter list
    // 2. Parsing of the function body

    // To complete step 1, the left parenthesis must be matched, followed by
    // the assembly of the parameter list as a series of sibling nodes, followed
    // by the matching of the right parenthesis.
    matchAndPop(TokenType.SPECIAL_LEFT_PAREN);
    functionNode.addChild(processParameterList());
    matchAndPop(TokenType.SPECIAL_RIGHT_PAREN);

    // To complete step 2, the left brace must be matched. Following this, all of the
    // subsequent statements must be processed until the right brace has been detected.
    matchAndPop(TokenType.SPECIAL_LEFT_BRACE);
    functionNode.addChild(createSyntaxTree());

    // The function node is now complete, and it should be returned
    return functionNode;
  }

  /**
   * Create an AssignExpressionNode that corresponds to the
   * current context
   *
   * @return The processed assignment statement
   */
  private AssignExpressionNode processAssignment()
  {
    // Create the assignment node
    AssignExpressionNode assignNode = new AssignExpressionNode();

    // Fill out the node with as much information as possible:
    // > The name of the node will be the name of the identifier being assigned
    // > The line number of the node
    // > The token type will be SPECIAL_ASSIGN
    assignNode.setName      (currentToken.getLexeme());
    assignNode.setLineNumber(currentToken.getLineNumber());
    assignNode.setTokenType (TokenType.SPECIAL_ASSIGN);

    // Currently the top of the token list is the assignment operator (=). This
    // needs to be advanced and assigned to the current token so that the next
    // operation is known:
    //
    //  1. ID  <-- current token        x ID
    //  2.  =  <-- current head    ==>  x  =
    //  3. ID                             ID <-- new current token
    //  4.  +                              + <-- new head
    // (the x prefix indicates that the token was removed)

    // Match the current ID
    matchAndPop(TokenType.VARIABLE_IDENTIFIER);

    // Match the assign context
    matchAndPop(TokenType.SPECIAL_ASSIGN);

    // Create an expression and assign it as the assignment node's child
    assignNode.addChild(processExpression());

    return assignNode;
  }

  /**
   * Process the list of parameters for a function signature.
   *
   * @return The ParameterNode that marks the beginning of a
   *         list of ParameterNodes contained in a function
   *         signature.
   */
  private ParameterNode processParameterList()
  {
    // Initialize the parameter node to be null, in case
    // processing starts to go bad
    ParameterNode parameterNode = null;

    // Process until either a comma or a right parenthesis is found.
    // These tokens indicate that (hopefully) a ParameterNode has been
    // properly parsed
    while (!matchCurrent(TokenType.SPECIAL_COMMA) &&
        !matchCurrent(TokenType.SPECIAL_RIGHT_PAREN))
    {
      // Get the statement returned from the processStatement function.
      // It is expected that the first call will return null, as it will
      // perform the type processing
      AbstractSyntaxTreeNode statement = processStatement();

      // If the returned (non-null) statement is actually a ParameterNode,
      // keep track of the newly obtained parameter
      if ((statement != null) &&
          (statement.getNodeType() == ASTNodeType.META_PARAMETER))
      {
        parameterNode = (ParameterNode) statement;
      }
    }

    // If a parameter was not found in the list, a syntax error should be
    // reported and processing should be terminated.
    if (parameterNode == null)
    {
      logSyntaxError();

      return null;
    }

    // If the current token is a comma, the list is not finished,
    // so continue the list as a sibling for the current parameter
    if (matchCurrent(TokenType.SPECIAL_COMMA))
    {
      // Assign the current tokne to the next token in the list
      matchAndPop(TokenType.SPECIAL_COMMA);
      parameterNode.setSibling(processParameterList());
    }

    return parameterNode;
  }

  /**
   * Process the simple-expression portion of the grammar. This
   * portion handles the relational operations ==, !=, >, <,
   * >= and <=.
   *
   * @return The AbstractSyntaxTreeNode that represents the
   *         current simple-expression
   */
  private AbstractSyntaxTreeNode processExpression()
  {
    // Initialize the first additive expression to the result
    // of processAdditiveExpression
    AbstractSyntaxTreeNode reference = processAdditiveExpression();

    // Check to see if the next token is a relational operator
    if (matchCurrent(TokenType.SPECIAL_EQUAL)        ||
        matchCurrent(TokenType.SPECIAL_NOT_EQUAL)    ||
        matchCurrent(TokenType.SPECIAL_GREATER_THAN) ||
        matchCurrent(TokenType.SPECIAL_LESS_THAN)    ||
        matchCurrent(TokenType.SPECIAL_GTE)          ||
        matchCurrent(TokenType.SPECIAL_LTE))
    {
      // If a relational operator has been found, create an operator
      // node that contains the current operation
      OperationExpressionNode opNode = processOperator(Boolean.class);

      // Add the current reference to the operator node (left-hand side)
      opNode.addChild(reference);

      // Create a new additive-expression and add it to the operator node
      // (right-hand side)
      opNode.addChild(processAdditiveExpression());

      // Change the reference to point to the operator node
      reference = opNode;
    }

    return reference;
  }

  /**
   * Process the additive-expression portion of the grammar. This
   * portion handles the additive operations + and -.
   *
   * @return The AbstractSyntaxTreeNode that represents the current
   *         additive-expression
   */
  private AbstractSyntaxTreeNode processAdditiveExpression()
  {
    // Initialize the first term to the result of processTerm
    AbstractSyntaxTreeNode reference = processTerm();

    // Check to see if the next token is an additive operator
    if (matchCurrent(TokenType.SPECIAL_PLUS) ||
        matchCurrent(TokenType.SPECIAL_MINUS))
    {
      // If an additive operator has been found, create an operator
      // node that contains the current operation
      OperationExpressionNode opNode = processOperator(Integer.class);

      // Add the current reference to the operator node (left-hand side)
      opNode.addChild(reference);

      // Create a new additive-expression and add it to the operator node
      // (right-hand side)
      opNode.addChild(processAdditiveExpression());

      // Change the reference to point to the operator node
      reference = opNode;
    }

    return reference;
  }

  /**
   * Process the term portion of the grammar. This portion handles
   * the multiplicative operations * and /, though great care needs
   * to be taken when ordering the resulting operations.
   *
   * @return The AbstractSyntaxNode that represents the current term
   */
  private AbstractSyntaxTreeNode processTerm()
  {
    // Initialize the first factor to the result of processFactor
    AbstractSyntaxTreeNode reference = processFactor();

    // Check to see if the next token is a multiplicative operator
    if (matchCurrent(TokenType.SPECIAL_TIMES) ||
        matchCurrent(TokenType.SPECIAL_DIVIDE))
    {
      // If a multiplicative operator has been found, create an operator
      // node that contains the current operation
      OperationExpressionNode opNode = processOperator(Integer.class);

      // TODO: COMPLETE IMPLEMENTATION
    }

    return reference;
  }

  /**
   * Process the factor portion of the grammar. This portion handles
   * the interpretation of (exp), var, NUM, or call.
   *
   * @return The AbstractSyntaxTreeNode that represents the current factor
   */
  private AbstractSyntaxTreeNode processFactor()
  {
    // Initialize the returned factor to null
    AbstractSyntaxTreeNode factor = null;

    // Let's look at the current node to identify what should be performed:
    switch (currentToken.getType())
    {
      // The left parenthesis has been discovered. The current processing will
      // satisfy the (exp) production rule
      case SPECIAL_LEFT_PAREN:
      {
        factor = processParenthesis();
        break;
      }

      // The current token is a number. The current processing will satisfy the NUM
      // production rule
      case VARIABLE_NUMBER:
      {
        factor = processConstant();
        break;
      }

      // The current token is an identifier. The current processing will need to satisfy
      // the var and call production rules. Thankfully the processID function will handle
      // this for us.
      case VARIABLE_IDENTIFIER:
      {
        factor = processID(null);
        break;
      }

      // The current token is none of the above options, so a syntax error has occurred.
      // Log the syntax error and return null.
      default:
      {
        logSyntaxError();
        break;
      }
    }

    return factor;
  }

  /**
   * Process the current token into a ConstantExpressionNode to represent
   * a constant number.
   *
   * @return The ConstantExpressionNode that represents the current number
   */
  private ConstantExpressionNode processConstant()
  {
    // Create the number node
    ConstantExpressionNode number = new ConstantExpressionNode();

    // Fill out the node with as much information as possible:
    // > The value of the node will be the name of the value being assigned
    // > The line number of the node
    // > The token type will be VARIABLE_NUMBER
    // > The node type will be Integer
    number.setValue     (Integer.parseInt(currentToken.getLexeme()));
    number.setLineNumber(currentToken.getLineNumber());
    number.setTokenType (TokenType.VARIABLE_NUMBER);
    number.setType      (Integer.class);

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.VARIABLE_NUMBER);

    return number;
  }

  /**
   * Process the current operation token into a usable node
   *
   * @param identifierType The return-type of the operation being performed
   *
   * @return The OperationExpressionNode that will be used for creating expressions
   */
  private OperationExpressionNode processOperator(Class<?> identifierType)
  {
    // Initialize the operation node
    OperationExpressionNode operation = new OperationExpressionNode();

    // Fill out the node with as much information as possible:
    // > The name of the node will be the name of the operator being assigned
    // > The line number of the node
    // > The token type will be operator being assigned
    // > The node type will be provided by the caller
    operation.setName      (currentToken.getLexeme());
    operation.setLineNumber(currentToken.getLineNumber());
    operation.setTokenType (currentToken.getType());
    operation.setType      (identifierType);

    // Advance the current token to ensure that processing continues smoothly
    // (since it's unknown what the current token type is, simply pass in the
    // current token type to ensure that it gets advanced correctly)
    matchAndPop(currentToken.getType());

    return operation;
  }

  /**
   * Process the expression type (exp) and obtain the resulting node
   * @return The AbstractSyntaxTreeNode contained within the (exp)
   *         statement
   */
  private AbstractSyntaxTreeNode processParenthesis()
  {
    // To process this type of expression, first match and pop the left
    // parenthesis, process the expression contained within, followed by
    // matching and popping the right parenthesis
    matchAndPop(TokenType.SPECIAL_LEFT_PAREN);
    AbstractSyntaxTreeNode expression = processExpression();
    matchAndPop(TokenType.SPECIAL_RIGHT_PAREN);

    return expression;
  }

  private IfStatementNode processIf()
  {
    // Create the if statement
    IfStatementNode ifStatement = new IfStatementNode();

    // Fill out the node with as much information as possible:
    // > The line number of the node
    // > The token type will be RESERVED_WHILE
    ifStatement.setLineNumber(currentToken.getLineNumber());
    ifStatement.setTokenType (TokenType.RESERVED_WHILE);

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.RESERVED_WHILE);

    // Assign the condition contained after the while statement as the first child
    ifStatement.addChild(processParenthesis());

    // Check to see if there is a left brace as the next token
    if (matchCurrent(TokenType.SPECIAL_LEFT_BRACE))
    {
      // If the next token is the left brace, advance the
      // token and assign the contents of the { } as the
      // if-statement's second child
      matchAndPop(TokenType.SPECIAL_LEFT_BRACE);
      ifStatement.addChild(createSyntaxTree());
    }
    else
    {
      // If the next token is not the left brace, assign
      // the next statement as the if-statement's
      // second child
      ifStatement.addChild(processStatement());
    }

    // TODO: COMPLETE IMPLEMENTATION FOR ELSE STATEMENT

    // Return the newly created if-statement node
    return ifStatement;
  }

  private WhileStatementNode processWhile()
  {
    // Create the while statement
    WhileStatementNode whileStatement = new WhileStatementNode();

    // Fill out the node with as much information as possible:
    // > The line number of the node
    // > The token type will be RESERVED_WHILE
    whileStatement.setLineNumber(currentToken.getLineNumber());
    whileStatement.setTokenType (TokenType.RESERVED_WHILE);

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.RESERVED_WHILE);

    // Assign the condition contained after the while statement as the first child
    whileStatement.addChild(processParenthesis());

    // Check to see if there is a left brace as the next token
    if (matchCurrent(TokenType.SPECIAL_LEFT_BRACE))
    {
      // If the next token is the left brace, advance the
      // token and assign the contents of the { } as the
      // while-statement's second child
      matchAndPop(TokenType.SPECIAL_LEFT_BRACE);
      whileStatement.addChild(createSyntaxTree());
    }
    else
    {
      // If the next token is not the left brace, assign
      // the next statement as the while-statement's
      // second child
      whileStatement.addChild(processStatement());
    }

    // Return the newly created while-statement node
    return whileStatement;
  }

  private ReturnStatementNode processReturn()
  {
    // Create the return statement
    ReturnStatementNode returnStatement = new ReturnStatementNode();

    // Fill out the node with as much information as possible:
    // > The line number of the node
    // > The token type will be RESERVED_RETURN
    returnStatement.setLineNumber(currentToken.getLineNumber());
    returnStatement.setTokenType (TokenType.RESERVED_RETURN);

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.RESERVED_RETURN);

    // Check to see if the current token is a semi-colon
    if (!matchCurrent(TokenType.SPECIAL_SEMICOLON))
    {
      // If the current token is not a semi-colon, process the
      // expression after the return statement
      returnStatement.addChild(processExpression());
    }

    // Return the newly-created return-statement node
    return returnStatement;
  }

  /* **************************************************************************** *
   *                       PARSER UTILITY FUNCTIONS                               *
   * **************************************************************************** */

  /**
   * Attempt to match the current token type with the expected token
   * type. If a match occurs, the token list will be moved forward by
   * one token. If a match fails, a syntax error will be reported.
   *
   * @param expected The expected token type
   */
  private void matchAndPop(final TokenType expected)
  {
    if (!matchCurrent(expected))
    {
      logSyntaxError(currentToken, expected);
    }
    else
    {
      currentToken = tokenList.pop();
    }
  }

  /**
   * Match the current token's type with an expected type
   * @param expected The expected token type
   * @return True if the current token's type matches the expected type
   */
  private boolean matchCurrent(final TokenType expected)
  {
    return currentToken.getType() == expected;
  }

  private void logSyntaxError(final Token token, final TokenType expected)
  {
    logSyntaxError(token, String.format("Expected %s", expected.toString()));
  }

  private void logSyntaxError(final Token token, final String expected)
  {
    System.err.printf("SYNTAX ERROR (Line %d) - Unexpected Token %s | %s\n",
        token.getLineNumber(), token.getType().toString(), expected);
  }

  private void logSyntaxError()
  {
    System.err.printf("SYNTAX ERROR (Line %d) - Unexpected Token %s\n",
        currentToken.getLineNumber(), currentToken.getType().toString());
  }
}