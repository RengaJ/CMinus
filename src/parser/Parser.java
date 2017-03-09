package parser;

import syntaxtree.ASTNodeType;
import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.expression.AssignExpressionNode;
import syntaxtree.expression.ExpressionNode;
import syntaxtree.expression.IDExpressionNode;
import syntaxtree.meta.FunctionNode;
import syntaxtree.meta.ParameterNode;
import syntaxtree.statement.IfStatementNode;
import syntaxtree.statement.ReturnStatementNode;
import syntaxtree.statement.VarDeclarationStatementNode;
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
   * The currently known identifier's type (when provided)
   */
  private Class<?> identifierType;

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

    return null;
    // return createSyntaxTree();
  }

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
        // Set the current identifier type (if returns
        // true, the current token will advance)
        if (!setCurrentType(currentToken.getType()))
        {
          logSyntaxError();
        }
        break;
      }

      case VARIABLE_IDENTIFIER:
      {
        statement = processID();
        break;
      }
    }
    return statement;
  }

  /**
   * Sets the current identifier type based on the provided
   * token type.
   *
   * @param tokenType The token type used to identify what the
   *                  current identifier type should be.
   * @return An indicator if the identifier type was set
   */
  private boolean setCurrentType(final TokenType tokenType)
  {
    // If the provided token type is RESERVED_INT,
    // set the identifier type to Integer.class
    if (tokenType == TokenType.RESERVED_INT)
    {
      // Assign the identifier type to Integer.class
      identifierType = Integer.class;

      // Advance to the next token
      matchAndPop(TokenType.RESERVED_INT);

      // Indicate that the identifier was properly assigned
      return true;
    }
    // If the provided token type is RESERVED_VOID,
    // set the identifier type to Void.class
    else if (tokenType == TokenType.RESERVED_VOID)
    {
      // Assign the identifier type to Void.class
      identifierType = Void.class;

      // Advance to the next token
      matchAndPop(TokenType.RESERVED_VOID);

      // Indicate that the identifier was properly assigned
      return true;
    }
    // If the provided token type is neither RESERVED_INT
    // nor RESERVED_VOID, do nothing and return false.
    return false;
  }

  /**
   * Process an identifier token, which requires context
   * to determine how to properly process the token
   *
   * @return The resulting AST node, or {@code null} if
   *         the processing of the token failed.
   */
  private AbstractSyntaxTreeNode processID()
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
          node = processSimpleParameter();
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
          node = processSimpleParameter();
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
          node = processFunction();
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
          node = processSimpleDeclaration();
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
   * @return The created simple parameter node
   */
  private ParameterNode processSimpleParameter()
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

    // Reset the identifier type, since it's no longer valid
    identifierType = null;

    // Advance the current token to ensure that processing continues smoothly
    matchAndPop(TokenType.VARIABLE_IDENTIFIER);

    // There's nothing left to assign to this node, so return it now
    return parameterNode;
  }

  /**
   * Create a simple (non-array) identifier declaration that
   * corresponds to the current identifier.
   *
   * @return The created identifier declaration node
   */
  private VarDeclarationStatementNode processSimpleDeclaration()
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
   * @return The processed function definition
   */
  private FunctionNode processFunction()
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

    // Reset the identifier type, since it's no longer valid
    identifierType = null;

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
    // TODO: IMPLEMENT STATEMENT LIST PROCESSING

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
    // TODO: IMPLEMENT EXPRESSION FUNCTIONALITY

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
    while (!matchCurrent(TokenType.SPECIAL_COMMA) ||
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
        parameterNode = (ParameterNode)statement;
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
      parameterNode.setSibling(processParameterList());
    }

    return parameterNode;
  }

//  private AbstractSyntaxTreeNode createSyntaxTree()
//  {
//    AbstractSyntaxTreeNode statement = null;
//
//    currentToken = tokenList.pop();
//
//    while (statement == null &&
//        !match(currentToken.getType(), TokenType.BOOKKEEPING_END_OF_FILE) &&
//        !match(currentToken.getType(), TokenType.SPECIAL_RIGHT_BRACE))
//    {
//      TokenType tokenType = currentToken.getType();
//      switch (tokenType)
//      {
//        case RESERVED_INT:
//        {
//          identifierType = Integer.class;
//          currentToken = tokenList.pop();
//          break;
//        }
//
//        case RESERVED_VOID:
//        {
//          identifierType = Void.class;
//          currentToken = tokenList.pop();
//          break;
//        }
//        case VARIABLE_IDENTIFIER:
//        {
//          statement = resolveIDAmbiguity();
//          break;
//        }
//
//        case RESERVED_RETURN:
//        {
//          statement = processReturnStatement();
//          break;
//        }
//
//        case RESERVED_IF:
//        {
//          statement = processIfStatement();
//          break;
//        }
//
//        case RESERVED_WHILE:
//        {
//
//        }
//
//        case RESERVED_INPUT:
//        case RESERVED_OUTPUT:
//        {
//          break;
//        }
//        default:
//        {
//          break;
//        }
//      }
//    }
//
//    if (statement != null &&
//        !match(currentToken.getType(), TokenType.BOOKKEEPING_END_OF_FILE))
//    {
//      statement.setSibling(createSyntaxTree());
//    }
//    return statement;
//  }
//
//  /**
//   * Resolves the ambiguity inherent with Identifier Tokens. This
//   * function will pick the correct node based on the next token
//   * in the token list
//   *
//   * @return The appropriate AbstractSyntaxTreeNode based on the current
//   *         context.
//   */
//  private AbstractSyntaxTreeNode resolveIDAmbiguity()
//  {
//    // Take a peek at the next token in the list to get some context
//    Token nextToken = tokenList.peek();
//
//    // Extract the token type so that
//    TokenType type = nextToken.getType();
//
//    AbstractSyntaxTreeNode node = null;
//
//    switch (type)
//    {
//
//      // If the next token is a semi-colon ( ; ), the current operation is
//      // a variable declaration ( var-declaration --> type-specifier ID; )
//      case SPECIAL_SEMICOLON:
//      {
//        // If there is no known type, create an IDExpressionNode
//        if (identifierType == null)
//        {
//          IDExpressionNode idNode = new IDExpressionNode();
//          idNode.setName(currentToken.getLexeme());
//          idNode.setLineNumber(currentToken.getLineNumber());
//
//          node = idNode;
//        }
//        // If there is an known type, create a VarDeclarationStatementNode
//        else
//        {
//          node = createVarDeclaration(currentToken);
//          tokenList.pop();
//        }
//        break;
//      }
//
//      // If the next token
//      case SPECIAL_LEFT_PAREN:
//      {
//        tokenList.pop();
//        if (identifierType == null)
//        {
//          // Create a function call
//        }
//        else
//        {
//          // Create a function structure
//          node = createFunction();
//        }
//        break;
//      }
//
//      case SPECIAL_LEFT_BRACKET:
//      {
//        break;
//      }
//
//      case SPECIAL_COMMA:
//      {
//        break;
//      }
//
//      case SPECIAL_PLUS:
//      case SPECIAL_MINUS:
//      case SPECIAL_DIVIDE:
//      case SPECIAL_TIMES:
//      case SPECIAL_GREATER_THAN:
//      case SPECIAL_GTE:
//      case SPECIAL_LESS_THAN:
//      case SPECIAL_LTE:
//      {
//        break;
//      }
//
//      default:
//      {
//        logSyntaxError();
//      }
//    }
//    return node;
//  }
//
//  /**
//   * Create a VarDeclarationStatementNode, given a token
//   * @param token The token from which a {@link VarDeclarationStatementNode} is
//   *              to be created
//   *
//   * @return A {@link VarDeclarationStatementNode} that shares some attributes
//   *         from the provided Token object
//   */
//  private VarDeclarationStatementNode createVarDeclaration(final Token token)
//  {
//    // Create a new instance of the VarDeclarationStatementNode object
//    VarDeclarationStatementNode declaration = new VarDeclarationStatementNode();
//
//    // Set various attributes on the VarDeclarationNode for
//    // usage in the semantic analyzer
//    declaration.setName(token.getLexeme());
//    declaration.setType(identifierType);
//    declaration.setLineNumber(token.getLineNumber());
//
//    // Reset the identifier type for next time
//    identifierType = null;
//
//    // Return the newly created object
//    return declaration;
//  }
//
//  private FunctionNode createFunction()
//  {
//    FunctionNode function = new FunctionNode();
//    function.setName(currentToken.getLexeme());
//    function.setLineNumber(currentToken.getLineNumber());
//    function.setType(identifierType);
//    identifierType = null;
//
//    currentToken = tokenList.pop();
//
//    // Add parameter list
//    function.addChild(processParameterList());
//
//    if (!match(currentToken.getType(), TokenType.SPECIAL_RIGHT_PAREN))
//    {
//      logSyntaxError(currentToken, TokenType.SPECIAL_RIGHT_PAREN);
//    }
//    // Pop off the right parenthesis (hopefully)
//    currentToken = tokenList.pop();
//
//    // match the left brace ( { )
//    if (!match(currentToken.getType(), TokenType.SPECIAL_LEFT_BRACE))
//    {
//      logSyntaxError(currentToken, TokenType.SPECIAL_LEFT_BRACE);
//    }
//
//    // Function body
//    function.addChild(createSyntaxTree());
//
//    return function;
//  }
//
//  private ParameterNode processParameterList()
//  {
//    ParameterNode parameter = processParameter();
//
//    if (match(currentToken.getType(), TokenType.SPECIAL_COMMA))
//    {
//      currentToken = tokenList.pop();
//      parameter.setSibling(processParameterList());
//    }
//
//    return parameter;
//  }
//
//  private ParameterNode processParameter()
//  {
//    ParameterNode parameter = null;
//
//    TokenType currentType = currentToken.getType();
//
//    if (!match(currentType, TokenType.RESERVED_INT) &&
//        !match(currentType, TokenType.RESERVED_VOID))
//    {
//      tokenList.pop();
//
//      logSyntaxError(currentToken, "RESERVED_INT or RESERVED_VOID");
//    }
//    else
//    {
//      if (match(currentType, TokenType.RESERVED_VOID))
//      {
//        parameter = new ParameterNode();
//        parameter.setLineNumber(currentToken.getLineNumber());
//      }
//      else
//      {
//        currentToken = tokenList.pop();
//        if (!match(currentToken.getType(), TokenType.VARIABLE_IDENTIFIER))
//        {
//          logSyntaxError(currentToken, TokenType.VARIABLE_IDENTIFIER);
//        }
//        else
//        {
//          parameter = new ParameterNode();
//          parameter.setName(currentToken.getLexeme());
//          parameter.setLineNumber(currentToken.getLineNumber());
//          parameter.setType(Integer.class);
//        }
//      }
//      currentToken = tokenList.pop();
//    }
//
//    return parameter;
//  }
//
//  private ReturnStatementNode processReturnStatement()
//  {
//    ReturnStatementNode returnStatement = new ReturnStatementNode();
//    returnStatement.setLineNumber(currentToken.getLineNumber());
//
//    currentToken = tokenList.pop();
//    if (!match(currentToken.getType(), TokenType.SPECIAL_SEMICOLON))
//    {
//      returnStatement.addChild(processExpression());
//    }
//
//    return returnStatement;
//  }
//
//  /**
//   * This function processes the IfToken detected from
//   * the queue. The current token is the detected IfToken.
//   *
//   * @return The complete if-statement node
//   */
//  private IfStatementNode processIfStatement()
//  {
//    IfStatementNode ifStatement = new IfStatementNode();
//    ifStatement.setLineNumber(currentToken.getLineNumber());
//
//    // Make sure the next token is the left parenthesis
//    matchAndPop(TokenType.SPECIAL_LEFT_PAREN);
//
//    ifStatement.addChild(processExpression());
//
//    // Make sure the next token is the right parenthesis
//    matchAndPop(TokenType.SPECIAL_RIGHT_PAREN);
//
//    // Check to see if multiple statements will be added, or if
//    // only a single statment should be processed:
//    // Multiple statements:
//    //     if (<condition>) {
//    //        <statement 1>
//    //        <statement 2>
//    //     }
//    // Single statement
//    //     if (<condition>) <statement>
//    if (match(currentToken.getType(), TokenType.SPECIAL_LEFT_BRACE))
//    {
//      // Perform multiple statement processing
//      ifStatement.addChild(createSyntaxTree());
//
//      // Ensure that the next token is a closing brace
//      matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
//    }
//    else
//    {
//      // Perform single statement processing
//    }
//
//    // Check to see if there is an else keyword here
//    if (match(currentToken.getType(), TokenType.RESERVED_ELSE))
//    {
//      // Pop off the ELSE token, and check for a function body start ( { )
//      currentToken = tokenList.pop();
//      if (match(currentToken.getType(), TokenType.SPECIAL_LEFT_BRACE))
//      {
//        // Perform multiple statement processing
//        ifStatement.addChild(createSyntaxTree());
//
//        // Ensure that the next token is a closing brace
//        matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
//      }
//      else
//      {
//        // Perform single statement processing
//      }
//    }
//
//    // The if-statement is now complete
//    return ifStatement;
//  }
//
//  private ExpressionNode processExpression()
//  {
//    return null;
//  }
//
//  private ExpressionNode processTerm()
//  {
//    return null;
//  }
//
//  private ExpressionNode processFactor()
//  {
//    return null;
//  }
//
//  /**
//   *
//   * @param current The current token type
//   * @param expected The expected token type
//   *
//   * @return True if the current and expected token types match, otherwise False
//   */
//  private boolean match(final TokenType current, final TokenType expected)
//  {
//    return current == expected;
//  }
//

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
    System.err.printf("SYNTAX ERROR (Line %d) - Unexpected Token %s; %s\n",
        token.getLineNumber(), token.getType().toString(), expected);
  }

  private void logSyntaxError()
  {
    System.err.printf("SYNTAX ERROR (Line %d) - Unexepected Token %s\n",
        currentToken.getLineNumber(), currentToken.getType().toString());
  }
}