package parser;

import syntaxtree.AbstractSyntaxTreeNode;
import syntaxtree.expression.IDExpressionNode;
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

    Token firstToken = tokenList.pop();

    while (firstToken.getType() != TokenType.BOOKKEEPING_END_OF_FILE)
    {
      TokenType tokenType = firstToken.getType();
      switch (tokenType)
      {
        case RESERVED_INT:
        {
          identifierType = Integer.class;
          firstToken = tokenList.pop();
          break;
        }

        case RESERVED_VOID:
        {
          identifierType = Void.class;
          firstToken = tokenList.pop();
        }
        case VARIABLE_IDENTIFIER:
        {
          resolveIDAmbiguity(firstToken);
        }
        default:
        {
          break;
        }
      }
    }
    return statement;
  }

  private AbstractSyntaxTreeNode resolveIDAmbiguity(final Token token)
  {
    // Take a peek at the next token in the list to get some context
    Token nextToken = tokenList.peek();

    // Extract the token type so that
    TokenType type = nextToken.getType();

    // If the next token is a semi-colon ( ; ), the current operation is
    // a variable declaration ( var-declaration --> type-specifier ID; )
    if (match(type, TokenType.SPECIAL_SEMICOLON))
    {
      // If there is no known type, create an IDExpressionNode
      if (identifierType == null)
      {
        IDExpressionNode idNode = new IDExpressionNode();
        idNode.setAttributeName(token.getLexeme());

        return idNode;
      }
      // If there is an known type, create a VarDeclarationStatementNode
      else
      {
        return createVarDeclaration(token);
      }
    }

    return null;
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
    declaration.setAttributeName(token.getLexeme());
    declaration.setAttributeType(identifierType);

    // Reset the identifier type for next time
    identifierType = null;

    // Return the newly created object
    return declaration;
  }

  /**
   * Attempt to match the current token type with the expected token
   * type. If a match occurs, the token list will be moved forward by
   * one token.
   *
   * @param current The current token type
   * @param expected The expected token type
   *
   * @return True if the current and expected token types match, otherwise False
   */
  private boolean match(final TokenType current, final TokenType expected)
  {
    if (current == expected)
    {
      tokenList.pop();
      return true;
    }

    return false;
  }

  private void logSyntaxError(final Token token, final TokenType expected)
  {
    System.err.printf("SYNTAX ERROR (Line %d)- Unexpected Token %s, Expected %s\n",
        token.getLineNumber(), token.getType().toString(), expected.toString());
  }
}