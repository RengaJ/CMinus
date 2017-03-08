package parser;

import syntaxtree.AbstractSyntaxTreeNode;
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
    // TODO: Complete Implementation
    this.tokenList = new ArrayDeque<>(tokenList);

    return createSyntaxTree();
  }

  private AbstractSyntaxTreeNode createSyntaxTree()
  {
    AbstractSyntaxTreeNode statement = null;

    currentToken = tokenList.pop();

    while (statement == null &&
           !match(currentToken.getType(), TokenType.BOOKKEEPING_END_OF_FILE) &&
           !match(currentToken.getType(), TokenType.SPECIAL_RIGHT_BRACE))
    {
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
          statement = resolveIDAmbiguity();
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
        default:
        {
          break;
        }
      }
    }

    if (statement != null &&
        !match(currentToken.getType(), TokenType.BOOKKEEPING_END_OF_FILE))
    {
      statement.setSibling(createSyntaxTree());
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
  private AbstractSyntaxTreeNode resolveIDAmbiguity()
  {
    // Take a peek at the next token in the list to get some context
    Token nextToken = tokenList.peek();

    // Extract the token type so that
    TokenType type = nextToken.getType();

    AbstractSyntaxTreeNode node = null;

    // If the next token is a semi-colon ( ; ), the current operation is
    // a variable declaration ( var-declaration --> type-specifier ID; )
    if (match(type, TokenType.SPECIAL_SEMICOLON))
    {
      // If there is no known type, create an IDExpressionNode
      if (identifierType == null)
      {
        IDExpressionNode idNode = new IDExpressionNode();
        idNode.setName(currentToken.getLexeme());
        idNode.setLineNumber(currentToken.getLineNumber());

        node = idNode;
      }
      // If there is an known type, create a VarDeclarationStatementNode
      else
      {
        node = createVarDeclaration(currentToken);
        tokenList.pop();
      }
    }
    else if (match(type, TokenType.SPECIAL_LEFT_PAREN))
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

    if (!match(currentToken.getType(), TokenType.SPECIAL_RIGHT_PAREN))
    {
      logSyntaxError(currentToken, TokenType.SPECIAL_RIGHT_PAREN);
    }
    // Pop off the right parenthesis (hopefully)
    currentToken = tokenList.pop();

    // match the left brace ( { )
    if (!match(currentToken.getType(), TokenType.SPECIAL_LEFT_BRACE))
    {
      logSyntaxError(currentToken, TokenType.SPECIAL_LEFT_BRACE);
    }

    // Function body
    function.addChild(createSyntaxTree());

    return function;
  }

  private ParameterNode processParameterList()
  {
    ParameterNode parameter = processParameter();

    if (match(currentToken.getType(), TokenType.SPECIAL_COMMA))
    {
      currentToken = tokenList.pop();
      parameter.setSibling(processParameterList());
    }

    return parameter;
  }

  private ParameterNode processParameter()
  {
    ParameterNode parameter = null;

    TokenType currentType = currentToken.getType();

    if (!match(currentType, TokenType.RESERVED_INT) &&
        !match(currentType, TokenType.RESERVED_VOID))
    {
      tokenList.pop();

      logSyntaxError(currentToken, "RESERVED_INT or RESERVED_VOID");
    }
    else
    {
      if (match(currentType, TokenType.RESERVED_VOID))
      {
        parameter = new ParameterNode();
        parameter.setLineNumber(currentToken.getLineNumber());
      }
      else
      {
        currentToken = tokenList.pop();
        if (!match(currentToken.getType(), TokenType.VARIABLE_IDENTIFIER))
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
      currentToken = tokenList.pop();
    }

    return parameter;
  }

  private ReturnStatementNode processReturnStatement()
  {
    ReturnStatementNode returnStatement = new ReturnStatementNode();
    returnStatement.setLineNumber(currentToken.getLineNumber());

    currentToken = tokenList.pop();
    if (!match(currentToken.getType(), TokenType.SPECIAL_SEMICOLON))
    {
      returnStatement.addChild(processExpression());
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

    // Make sure the next token is the left parenthesis
    matchAndPop(TokenType.SPECIAL_LEFT_PAREN);

    ifStatement.addChild(processExpression());

    // Make sure the next token is the right parenthesis
    matchAndPop(TokenType.SPECIAL_RIGHT_PAREN);

    // Check to see if multiple statements will be added, or if
    // only a single statment should be processed:
    // Multiple statements:
    //     if (<condition>) {
    //        <statement 1>
    //        <statement 2>
    //     }
    // Single statement
    //     if (<condition>) <statement>
    if (match(currentToken.getType(), TokenType.SPECIAL_LEFT_BRACE))
    {
      // Perform multiple statement processing
      ifStatement.addChild(createSyntaxTree());

      // Ensure that the next token is a closing brace
      matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
    }
    else
    {
      // Perform single statement processing
    }

    // Check to see if there is an else keyword here
    if (match(currentToken.getType(), TokenType.RESERVED_ELSE))
    {
      // Pop off the ELSE token, and check for a function body start ( { )
      currentToken = tokenList.pop();
      if (match(currentToken.getType(), TokenType.SPECIAL_LEFT_BRACE))
      {
        // Perform multiple statement processing
        ifStatement.addChild(createSyntaxTree());

        // Ensure that the next token is a closing brace
        matchAndPop(TokenType.SPECIAL_RIGHT_BRACE);
      }
      else
      {
        // Perform single statement processing
      }
    }

    // The if-statement is now complete
    return ifStatement;
  }

  private ExpressionNode processExpression()
  {
    return null;
  }

  /**
   *
   * @param current The current token type
   * @param expected The expected token type
   *
   * @return True if the current and expected token types match, otherwise False
   */
  private boolean match(final TokenType current, final TokenType expected)
  {
    return current == expected;
  }

  /**
   * Attempt to match the current token type with the expected token
   * type. If a match occurs, the token list will be moved forward by
   * one token. If a match fails, a syntax error will be reported.
   *
   * @param expected The expected token type
   */
  private void matchAndPop(final TokenType expected)
  {
    if (!match(currentToken.getType(), expected))
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
    System.err.printf("SYNTAX ERROR (Line %d)- Unexpected Token %s, Expected %s\n",
        token.getLineNumber(), token.getType().toString(), expected);
  }
}