package parser;

import syntaxtree.AbstractSyntaxTreeNode;
import tokens.Token;

import java.util.ArrayList;

/**
 * The C- Parser. This Parser will read in a list of scanned
 * tokens from the Scanner and produce an abstract syntax
 * tree used for determining the structure of the file being
 * compiled.
 */
public final class Parser
{
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
  public AbstractSyntaxTreeNode parse(ArrayList<Token> tokenList)
  {
    // TODO: Complete Implementation
    return null;
  }
}
