package parser;

import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.expression.ConstantExpressionNode;
import syntaxtree.expression.ExpressionNode;
import syntaxtree.expression.IDExpressionNode;
import syntaxtree.expression.OperationExpressionNode;
import syntaxtree.meta.FunctionNode;
import syntaxtree.meta.ParameterNode;
import syntaxtree.expression.AssignExpressionNode;
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
    this.tokenList = new ArrayDeque<>(tokenList);

    return createSyntaxTree();
  }

  private AbstractSyntaxTreeNode createSyntaxTree()
  {
    AbstractSyntaxTreeNode statement = null;

    currentToken = tokenList.pop();

    while (statement == null &&
           !matchCurrent(TokenType.BOOKKEEPING_END_OF_FILE) &&
           !matchNext(TokenType.SPECIAL_RIGHT_BRACE))
    {
      statement = processStatement();
    }

    if (statement != null &&
        !matchCurrent(TokenType.BOOKKEEPING_END_OF_FILE) &&
        !matchCurrent(TokenType.SPECIAL_RIGHT_BRACE))
    {
      statement.setSibling(createSyntaxTree());
    }
    return statement;
  }

  private AbstractSyntaxTreeNode processStatement()
  {
    AbstractSyntaxTreeNode statement = null;

    TokenType tokenType = currentToken.getType();
    switch (tokenType)
    {
      case RESERVED_INT:
      {
        identifierType = Integer.class;
        currentToken = tokenList.pop();
        break;
      }

      case RESERVED_VOID:
      {
        identifierType = Void.class;
        currentToken = tokenList.pop();
        break;
      }
      case VARIABLE_IDENTIFIER:
      {
        statement = processExpression();
        break;
      }

      case RESERVED_RETURN:
      {
        statement = processReturnStatement();
        break;
      }

      case RESERVED_IF:
      {
        statement = processIfStatement();
        break;
      }

      case RESERVED_WHILE:
      {

      }

      case RESERVED_INPUT:
      case RESERVED_OUTPUT:
      {
        break;
      }
      default:
      {
        break;
      }
    }

    return statement;
  }

  /**
   * Resolves the ambiguity inherent with Identifier Tokens. This
   * function will pick the correct node based on the next token
   * in the token list
   *
   * @return The appropriate AbstractSyntaxTreeNode based on the current
   *         context.
   */
  private AbstractSyntaxTreeNode processExpression()
  {
    AbstractSyntaxTreeNode node = null;

    // Take a peek at the next token in the list to get some context
    TokenType nextType = tokenList.peek().getType();

    switch (nextType)
    {

      // If the next token is a semi-colon ( ; ), the current operation is
      // a variable declaration ( var-declaration --> type-specifier ID; )
      case SPECIAL_SEMICOLON:
      {
        // If there is no known type, create an IDExpressionNode
        if (identifierType == null)
        {
          IDExpressionNode idNode = new IDExpressionNode();
          idNode.setName(currentToken.getLexeme());
          idNode.setLineNumber(currentToken.getLineNumber());

          node = idNode;

          // Pop off the identifier
          matchAndPop(TokenType.VARIABLE_IDENTIFIER);
        }
        // If there is an known type, create a VarDeclarationStatementNode
        else
        {
          node = createVarDeclaration(currentToken);
          tokenList.pop();
        }
        break;
      }

      // If the next token
      case SPECIAL_LEFT_PAREN:
      {
        tokenList.pop();
        if (identifierType == null)
        {
          // Create a function call
        }
        else
        {
          // Create a function structure
          node = createFunction();
        }
        break;
      }

      case SPECIAL_LEFT_BRACKET:
      {
        break;
      }

      case SPECIAL_COMMA:
      {
        // This shouldn't happen
        if (identifierType != null)
        {
          node = processParameter();
        }
        // This should happen
        else
        {
          node = new IDExpressionNode();
          node.setName(currentToken.getLexeme());
          node.setLineNumber(currentToken.getLineNumber());
          node.setTokenType(TokenType.VARIABLE_IDENTIFIER);
        }
        break;
      }

      case SPECIAL_PLUS:
      case SPECIAL_MINUS:
      case SPECIAL_DIVIDE:
      case SPECIAL_TIMES:
      case SPECIAL_GREATER_THAN:
      case SPECIAL_GTE:
      case SPECIAL_LESS_THAN:
      case SPECIAL_LTE:
      case SPECIAL_EQUAL:
      case SPECIAL_NOT_EQUAL:
      {
        node = processSimpleExpression();
        break;
      }

      case SPECIAL_ASSIGN:
      {
        node = processAssign();
        break;
      }

      default:
      {
        logSyntaxError();
      }
    }

    return node;
  }

  /**
   * Create a VarDeclarationStatementNode, given a token
   * @param token The token from which a {@link VarDeclarationStatementNode} is
   *              to be created
   *
   * @return A {@link VarDeclarationStatementNode} that shares some attributes
   *         from the provided Token object
   */
  private VarDeclarationStatementNode createVarDeclaration(final Token token)
  {
    // Create a new instance of the VarDeclarationStatementNode object
    VarDeclarationStatementNode declaration = new VarDeclarationStatementNode();

    // Set various attributes on the VarDeclarationNode for
    // usage in the semantic analyzer
    declaration.setName(token.getLexeme());
    declaration.setType(identifierType);
    declaration.setLineNumber(token.getLineNumber());

    // Reset the identifier type for next time
    identifierType = null;

    // Return the newly created object
    return declaration;
  }

  private FunctionNode createFunction()
  {
    FunctionNode function = new FunctionNode();
    function.setName(currentToken.getLexeme());
    function.setLineNumber(currentToken.getLineNumber());
    function.setType(identifierType);
    identifierType = null;

    currentToken = tokenList.pop();

    // Add parameter list
    function.addChild(processParameterList());

    if (!matchCurrent(TokenType.SPECIAL_RIGHT_PAREN))
    {
      logSyntaxError(currentToken, TokenType.SPECIAL_RIGHT_PAREN);
    }
    // Pop off the right parenthesis (hopefully)
    currentToken = tokenList.pop();

    // matchNext the left brace ( { )
    if (!matchCurrent(TokenType.SPECIAL_LEFT_BRACE))
    {
      logSyntaxError(currentToken, TokenType.SPECIAL_LEFT_BRACE);
    }

    // Function body
    function.addChild(processStatementList());

    matchCurrent(TokenType.SPECIAL_RIGHT_BRACE);

    currentToken = tokenList.peek();

    return function;
  }

  private ParameterNode processParameterList()
  {
    ParameterNode parameter = processParameter();

    if (matchCurrent(TokenType.SPECIAL_COMMA))
    {
      currentToken = tokenList.pop();
      parameter.setSibling(processParameterList());
    }

    return parameter;
  }

  private ParameterNode processParameter()
  {
    ParameterNode parameter = null;

    if (!matchCurrent(TokenType.RESERVED_INT) &&
        !matchCurrent(TokenType.RESERVED_VOID))
    {
      tokenList.pop();

      logSyntaxError(currentToken, "RESERVED_INT or RESERVED_VOID");
    }
    else
    {
      if (matchCurrent(TokenType.RESERVED_VOID))
      {
        parameter = new ParameterNode();
        parameter.setLineNumber(currentToken.getLineNumber());
        if (matchNext(TokenType.VARIABLE_IDENTIFIER))
        {
          matchAndPop(currentToken.getType());
          parameter.setName(currentToken.getLexeme());
        }
      }
      else
      {
        matchAndPop(currentToken.getType());
        if (!matchCurrent(TokenType.VARIABLE_IDENTIFIER))
        {
          logSyntaxError(currentToken, TokenType.VARIABLE_IDENTIFIER);
        }
        else
        {
          parameter = new ParameterNode();
          parameter.setName(currentToken.getLexeme());
          parameter.setLineNumber(currentToken.getLineNumber());
          parameter.setType(Integer.class);
        }
      }

      identifierType = null;
      currentToken = tokenList.pop();
    }

    if (parameter != null)
    {
      parameter.setTokenType(TokenType.VARIABLE_IDENTIFIER);
    }
    return parameter;
  }

  private ReturnStatementNode processReturnStatement()
  {
    ReturnStatementNode returnStatement = new ReturnStatementNode();
    returnStatement.setLineNumber(currentToken.getLineNumber());

    // Pop off the RESERVED_RETURN token
    matchAndPop(currentToken.getType());
    if (!matchCurrent(TokenType.SPECIAL_SEMICOLON))
    {
      returnStatement.addChild(processExpression());

      matchAndPop(TokenType.SPECIAL_SEMICOLON);
    }

    return returnStatement;
  }

  /**
   * This function processes the IfToken detected from
   * the queue. The current token is the detected IfToken.
   *
   * @return The complete if-statement node
   */
  private IfStatementNode processIfStatement()
  {
    IfStatementNode ifStatement = new IfStatementNode();
    ifStatement.setLineNumber(currentToken.getLineNumber());

    matchAndPop(TokenType.RESERVED_IF);

    // Make sure the next token is the left parenthesis
    ifStatement.addChild(processParenthesis());

    // Check to see if multiple statements will be added, or if
    // only a single statement should be processed:
    // Multiple statements:
    //     if (<condition>) {
    //        <statement 1>
    //        <statement 2>
    //     }
    // Single statement
    //     if (<condition>) <statement>
    ifStatement.addChild(processStatementList());

    if (matchCurrent(TokenType.SPECIAL_RIGHT_BRACE))
    {
      matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
    }

    // Check to see if there is an else keyword here
    if (matchCurrent(TokenType.RESERVED_ELSE))
    {
      // Pop off the ELSE token, and check for a function body start ( { )
      matchAndPop(TokenType.RESERVED_ELSE);

      ifStatement.addChild(processStatementList());

      if (matchCurrent(TokenType.SPECIAL_RIGHT_BRACE))
      {
        matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
      }
    }

    // The if-statement is now complete
    return ifStatement;
  }

  private AbstractSyntaxTreeNode processStatementList()
  {
    AbstractSyntaxTreeNode node;
    if (matchCurrent(TokenType.SPECIAL_LEFT_BRACE))
    {
      // Perform multiple statement processing
      node = createSyntaxTree();
    }
    else
    {
      // Perform single statement processing
      node = processStatement();
    }

    return node;
  }

  private ExpressionNode processSimpleExpression()
  {
    ExpressionNode simpleExp = processAdditiveExpression();

    // Is the current token a relational operator?
    if (matchCurrent(TokenType.SPECIAL_EQUAL) ||
        matchCurrent(TokenType.SPECIAL_NOT_EQUAL) ||
        matchCurrent(TokenType.SPECIAL_GREATER_THAN) ||
        matchCurrent(TokenType.SPECIAL_GTE) ||
        matchCurrent(TokenType.SPECIAL_LESS_THAN) ||
        matchCurrent(TokenType.SPECIAL_LTE))
    {
      OperationExpressionNode opNode =
          new OperationExpressionNode(currentToken.getType());
      opNode.setLineNumber(currentToken.getLineNumber());
      opNode.setType(Boolean.class);
      opNode.addChild(simpleExp);

      // Change the orientation of the hierarchy
      simpleExp = opNode;

      matchAndPop(currentToken.getType());

      simpleExp.addChild(processAdditiveExpression());
    }

    return simpleExp;
  }

  private ExpressionNode processAdditiveExpression()
  {
    ExpressionNode term = processTerm();

    return term;
  }

  private ExpressionNode processTerm()
  {
    ExpressionNode factor = processFactor();

    if (matchCurrent(TokenType.SPECIAL_DIVIDE) ||
        matchCurrent(TokenType.SPECIAL_TIMES))
    {
      //
    }

    return factor;
  }

  private ExpressionNode processFactor()
  {
    ExpressionNode node;

    if (matchCurrent(TokenType.VARIABLE_NUMBER))
    {
      node = new ConstantExpressionNode();
      node.setValue(Integer.parseInt(currentToken.getLexeme()));
      node.setLineNumber(currentToken.getLineNumber());

      currentToken = tokenList.pop();
    }
    else if (matchCurrent(TokenType.SPECIAL_LEFT_PAREN))
    {
      node = processParenthesis();
    }
    else
    {
      node = new IDExpressionNode();
      node.setName(currentToken.getLexeme());
      node.setLineNumber(currentToken.getLineNumber());
      node.setTokenType(TokenType.VARIABLE_IDENTIFIER);

      matchAndPop(currentToken.getType());
    }
    return node;
  }

  private AssignExpressionNode processAssign()
  {
    AssignExpressionNode assign = new AssignExpressionNode();
    assign.setName(currentToken.getLexeme());
    assign.setLineNumber(currentToken.getLineNumber());

    // Pop off the = symbol from the queue
    tokenList.pop();

    // Assign the new top as the current token
    matchAndPop(currentToken.getType());
    assign.addChild(processExpression());

    return assign;
  }

  private ExpressionNode processParenthesis()
  {
    matchAndPop(TokenType.SPECIAL_LEFT_PAREN);
    ExpressionNode node = (ExpressionNode)processExpression();
    matchAndPop(TokenType.SPECIAL_RIGHT_PAREN);

    return node;
  }

  /**
   *
   * @param expected The expected token type
   *
   * @return True if the current and expected token types matchNext, otherwise False
   */
  private boolean matchNext(final TokenType expected)
  {
    return tokenList.peek().getType() == expected;
  }

  /**
   * Function used to matchNext the current token with an expected type.
   *
   * @param expected The expected token type
   * @return True if the true current type and the expected types matchNext, otherwise
   *         False
   */
  private boolean matchCurrent(final TokenType expected)
  {
    return currentToken.getType() == expected;
  }

  /**
   * Attempt to matchNext the current token type with the expected token
   * type. If a matchNext occurs, the token list will be moved forward by
   * one token. If a matchNext fails, a syntax error will be reported.
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

  private void logSyntaxError(final Token token, final TokenType expected)
  {
    logSyntaxError(token, expected.toString());
  }

  private void logSyntaxError(final Token token, final String expected)
  {
    System.err.printf("SYNTAX ERROR (Line %d) - Unexpected Token %s, Expected %s\n",
        token.getLineNumber(), token.getType().toString(), expected);
  }

  private void logSyntaxError()
  {
    System.err.printf("SYNTAX ERROR (Line %d) - Unexepected Token %s\n",
        currentToken.getLineNumber(), currentToken.getType().toString());
  }
}