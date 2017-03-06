package parser;

import syntaxtree.AbstractSyntaxTreeNode;
import tokens.Token;

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

    return null;
  }
}
