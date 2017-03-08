package scanner;

import globals.CompilerFlags;
import tokens.Token;
import tokens.TokenTree;
import tokens.TokenType;
import tokens.bookkeeping.*;
import tokens.reserved.*;
import tokens.special.*;
import tokens.variable.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

/**
 * The C- Scanner. This Scanner will read in C- files
 * (files with the .cm extension) and will produce a list
 * of the tokens that are discovered within the file. A
 * single instance of the C- Scanner can be used to parse
 * multiple files (serially) by calling the main function
 * scanForTokens with different files. The Scanner WILL NOT
 * remember previous files scanned, so each file scan will
 * be a full scan.
 */
public final class Scanner
{
  /**
   * The current index of the current line of the file
   * content buffer.
   */
  private int index;

  /** The current line of the file content buffer */
  private int lineNumber;

  /**
   * A pre-defined character used to indicate that
   * the end of the file has been reached.
   */
  private final char EOF_CHAR = '\0';

  /**
   * The file content buffer that contains all lines
   * in the file provided to the Scanner
   */
  private ArrayList<String> fileContents;

  /**
   * A token tree that contains the reserved words
   * defined for the C- language.
   */
  private TokenTree<ReservedToken> tokenTree;

  /**
   * The full constructor of the Scanner object. This
   * initializes the internal values of the Scanner,
   * but does not allow for any file-associations to
   * occur.
   */
  public Scanner()
  {
    index = 0;
    lineNumber = 0;
    fileContents = new ArrayList<>();
    tokenTree = createTokenTree();
  }

  /**
   * The main function of the Scanner. This will perform
   * scanning on a single File object and will return a
   * deque containing the tokens discovered (in order)
   * within the file.
   *
   * @param file The File object for which the Scanning will
   *             be performed.
   *
   * @return An {@link Deque} of {@link Token} objects that
   * were found in the provided File object.
   *
   * @throws IOException Thrown if there are any file operation
   *                     issues (opening, reading or closing).
   */
  public ArrayDeque<Token> scanForTokens(File file) throws IOException
  {
    if (file == null)
    {
      throw new IllegalArgumentException(
          "Invalid file provided - null File object");
    }
    readFile(file);

    // Create the list that will contain the tokens generated
    // by the Scanner
    ArrayDeque<Token> tokenList = new ArrayDeque<>();

    // Keep track of the current scanner state
    ScannerState scannerState = ScannerState.START_STATE;

    if (CompilerFlags.TraceScanner)
    {
      System.out.println("Tokens:");
    }

    while (scannerState == ScannerState.START_STATE ||
           scannerState == ScannerState.TOKEN_DONE)
    {
      // The token to be added to the tokenList
      Token currentToken = null;

      // An indicator as to consume the current
      // character from the file contents buffer
      boolean shouldConsumeCharacter = true;

      // Initialize the scanner state to START_STATE
      scannerState = ScannerState.START_STATE;

      // A buffer contains the current state of the
      // lexeme to assign to the token
      StringBuilder lexemeBuilder = new StringBuilder("");

      // An indicator to determine if the current character
      // should be added to the lexeme buffer
      boolean appendCharacter = false;

      while (scannerState != ScannerState.TOKEN_DONE &&
          scannerState != ScannerState.SCANNER_DONE)
      {
        char currentCharacter = getCharacter();

        switch (scannerState)
        {
          // If the current scanner state is START_STATE...
          case START_STATE:
          {
            // If the end-of-file
            if (currentCharacter == EOF_CHAR)
            {
              scannerState = ScannerState.SCANNER_DONE;
              continue;
            }
            // Check to see if the extracted character is
            // a letter. If so, advance the state to
            // IN_IDENTIFIER
            if (Character.isLetter(currentCharacter))
            {
              scannerState = ScannerState.IN_IDENTIFIER;
              appendCharacter = true;
              break;
            }
            // Check to see if the extracted character is
            // a digit. If so, advance the state to
            // IN_NUMBER
            if (Character.isDigit(currentCharacter))
            {
              scannerState = ScannerState.IN_NUMBER;
              appendCharacter = true;
              break;
            }
            // Check to see if the character is considered
            // whitespace. If so, do nothing (remain in
            // START_STATE)
            if (Character.isWhitespace(currentCharacter))
            {
              scannerState = ScannerState.START_STATE;
              break;
            }
            // Check to see if the character is the <
            // symbol. If so, advance the state to
            // IN_LESS_THAN
            if (currentCharacter == '<')
            {
              scannerState = ScannerState.IN_LESS_THAN;
              break;
            }
            // Check to see if the character is the >
            // symbol. If so, advance the state to
            // IN_GREATER_THAN
            if (currentCharacter == '>')
            {
              scannerState = ScannerState.IN_GREATER_THAN;
              break;
            }
            // Check to see if the character is the !
            // symbol. If so, advance the state to
            // IN_NOT_EQUAL
            if (currentCharacter == '!')
            {
              scannerState = ScannerState.IN_NOT_EQUAL;
              break;
            }
            // Check to see if the character is the =
            // symbol. If so, advance the state to
            // IN_EQUAL
            if (currentCharacter == '=')
            {
              scannerState = ScannerState.IN_EQUAL;
              break;
            }
            // Check to see if the character is the /
            // symbol. If so, advance the state to
            // IN_DIVIDE
            if (currentCharacter == '/')
            {
              scannerState = ScannerState.IN_DIVIDE;
              break;
            }
            // Regardless of the character, advance
            // the state to TOKEN_DONE and obtain
            // the appropriate SymbolToken
            scannerState = ScannerState.TOKEN_DONE;
            currentToken = processSimpleToken(currentCharacter);

            break;
          }
          case IN_DIVIDE:
          {
            if (currentCharacter == '*')
            {
              scannerState = ScannerState.IN_COMMENT;
            }
            else
            {
              currentToken = new DivideToken();
              scannerState = ScannerState.TOKEN_DONE;
              shouldConsumeCharacter = false;
            }
            break;
          }
          // If the current scanner state is IN_COMMENT...
          case IN_COMMENT:
          {
            // Check to see if the current character is the *
            // symbol. If so, advance the state to EXIT_COMMENT
            if (currentCharacter == '*')
            {
              scannerState = ScannerState.EXIT_COMMENT;
            }
            // Otherwise, do nothing (currently still in the comment)
            break;
          }
          // If the current scanner state is EXIT_COMMENT...
          case EXIT_COMMENT:
          {
            // Check to see if the current character is the *
            // symbol. If so, remain in the EXIT_COMMENT state.
            if (currentCharacter == '*')
            {
              scannerState = ScannerState.EXIT_COMMENT;
            }
            // Check to see if the
            else if (currentCharacter == '/')
            {
              scannerState = ScannerState.START_STATE;
            }
            else
            {
              scannerState = ScannerState.IN_COMMENT;
            }
            break;
          }
          // If the current scanner state is IN_LESS_THAN...
          case IN_LESS_THAN:
          {
            if (currentCharacter == '=')
            {
              currentToken = new LTEToken();
            }
            else
            {
              currentToken = new LessThanToken();
              shouldConsumeCharacter = false;
            }

            scannerState = ScannerState.TOKEN_DONE;
            break;
          }
          // If the current scanner state is in IN_GREATER_THAN...
          case IN_GREATER_THAN:
          {
            if (currentCharacter == '=')
            {
              currentToken = new GTEToken();
            }
            else
            {
              currentToken = new GreaterThanToken();
              shouldConsumeCharacter = false;
            }

            scannerState = ScannerState.TOKEN_DONE;
            break;
          }
          case IN_NOT_EQUAL:
          {
            if (currentCharacter == '=')
            {
              currentToken = new NotEqualToken();
            }
            else
            {
              currentToken = new ErrorToken();
              shouldConsumeCharacter = false;
            }

            scannerState = ScannerState.TOKEN_DONE;
            break;
          }
          case IN_EQUAL:
          {
            if (currentCharacter == '=')
            {
              currentToken = new EqualToken();
            }
            else
            {
              currentToken           = new AssignToken();
              shouldConsumeCharacter = false;
            }

            scannerState = ScannerState.TOKEN_DONE;
            break;
          }
          case IN_IDENTIFIER:
          {
            if (!Character.isAlphabetic(currentCharacter))
            {
              currentToken           = new IdentifierToken();
              scannerState           = ScannerState.TOKEN_DONE;
              shouldConsumeCharacter = false;
              appendCharacter        = false;
            }
            break;
          }
          case IN_NUMBER:
          {
            if (!Character.isDigit(currentCharacter))
            {
              currentToken           = new NumberToken();
              scannerState           = ScannerState.TOKEN_DONE;
              shouldConsumeCharacter = false;
              appendCharacter        = false;
            }
            break;
          }
          default:
          {
            break;
          }
        } // End-Switch Statement

        if (appendCharacter)
        {
          lexemeBuilder.append(currentCharacter);
        }

        if (scannerState == ScannerState.TOKEN_DONE)
        {
          currentToken.setLineNumber(lineNumber + 1);
          currentToken.setLexeme(lexemeBuilder.toString());
          if (currentToken.getType() == TokenType.VARIABLE_IDENTIFIER)
          {
            // Perform Reserved Lookup
            Token reservedToken = tokenTree.find(currentToken);

            if (reservedToken != null)
            {
              currentToken = reservedToken;
              // The line number needs to be transferred to the new token,
              // as it was lost during the transfer.
              currentToken.setLineNumber(lineNumber + 1);
            }
          }
          // Add the current token to the list
          tokenList.add(currentToken);
          if (CompilerFlags.TraceScanner)
          {
            System.out.printf("%s\t( %s ) - %03d\n",
                currentToken.getType().toString(),
                currentToken.toString(),
                lineNumber + 1);
          }
        }

        if (shouldConsumeCharacter)
        {
          consumeCharacter();
        }
      } // End of inner-while loop
    } /// End of outer-while loop

    if (scannerState != ScannerState.SCANNER_DONE)
    {
      tokenList.add(new ErrorToken());
    }
    else
    {
      tokenList.add(new EndOfFileToken());
    }

    return tokenList;
  }

  /**
   * Private method used to reset the Scanner when prompted
   * to read in a new File object. This will reset the
   * file content buffer and the various counters and
   * indicators, as well as actually read in all of the
   * File's contents into an {@link Deque} of
   * {@link String}s.
   *
   * @param file The File object whose contents will be
   *             read.
   *
   * @throws IOException Thrown if there are any issues with
   *                     file operations (opening, reading
   *                     or closing).
   */
  private void readFile(File file) throws IOException
  {
    // Open up the reader to extract the file's contents
    BufferedReader reader =
        new BufferedReader(new FileReader(file));

    // Erase the contents of the file contents buffer
    fileContents.clear();

    // Read all of the lines in the file and place them
    // in the file contents buffer
    String line;
    while ((line = reader.readLine()) != null)
    {
      // Add a space to the end of each line read in order to
      // catch the rare identifier-split case (syntactically
      // incorrect):
      //
      // Two example lines with a split identifier:
      //
      // Line 001: int longIdentif
      // Line 002: ierName;
      //
      // The identifier longIdentifierName is split across
      // two lines, and should properly cause a syntax error
      // by producing four tokens:
      // 1. IntToken
      // 2. IdentifierToken - longIdentif
      // 3. IdentifierToken - ierName
      // 4. SemicolonToken
      //
      // Token 3 is unexpected and will properly cause a syntax error.
      fileContents.add(String.format("%s ", line));

      if (CompilerFlags.EchoSource)
      {
        System.out.printf("Source Line %03d: %s\n",
            fileContents.size(), fileContents.get(fileContents.size() - 1));
      }
    }

    // Ensure that the file is properly closed
    reader.close();

    // Add a final null line in the file contents buffer
    // in order to signal that the Scanner should emit an
    // EndOfFileToken.
    fileContents.add(null);

    // Reset the line number and index positions
    lineNumber = 0;
    index = 0;
  }

  /**
   * Retrieves the current character from the file content
   * buffer.
   *
   * @return The current character extracted from the file
   * content buffer, or EOF_CHAR if the end of file
   * has been reached.
   */
  private char getCharacter()
  {
    // Check to see if the current line is null.
    // If so, return the end of file character.
    if (fileContents.get(lineNumber) == null)
    {
      return EOF_CHAR;
    }
    // If the line is not null, return the character
    // at the current line and position.
    return fileContents.get(lineNumber).charAt(index);
  }

  /**
   * Consumes a character from the current line, and advances
   * the line if necessary. Note that this does not return a
   * character, it simply advances the line pointer, thus
   * "consuming" a character.
   */
  private void consumeCharacter()
  {
    // Check to see if the current line is null
    if (fileContents.get(lineNumber) == null)
    {
      // Do nothing (any checks against the line size
      // of a null String will cause a NullPointerException
      // to be thrown)
    }
    // If, after the index has been incremented, the index is
    // equal to the current line's length, the last character was
    // just read, so the index should reset and the line counter
    // should advance.
    else if ((++index) == fileContents.get(lineNumber).length())
    {
      index = 0;
      ++lineNumber;
    }
  }

  /**
   * Perform processing on a character to determine the SymbolToken
   * that best represents provided character. If an invalid character
   * is provided, an ErrorToken will be returned.
   *
   * @param character The character on which to perform processing
   *
   * @return A SymbolToken representing the provided character, or
   *         an ErrorToken is the character is invalid
   */
  private Token processSimpleToken(char character)
  {
    switch (character)
    {
      // If the character is +, create a new PlusToken instance
      case '+':
      {
        return new PlusToken();
      }
      // If the character is -, create a new MinusToken instance
      case '-':
      {
        return new MinusToken();
      }
      // If the character is *, create a new PlusToken instance
      case '*':
      {
        return new TimesToken();
      }
      // If the character is (, create a new LeftParenthesisToken
      case '(':
      {
        return new LeftParenthesisToken();
      }
      // If the character is ), create a new RightParenthesisToken
      case ')':
      {
        return new RightParenthesisToken();
      }
      // If the character is [, create a new LeftBracketToken
      case '[':
      {
        return new LeftBracketToken();
      }
      // If the character is ], create a new RightBracketToken
      case ']':
      {
        return new RightBracketToken();
      }
      // If the character is {, create a new LeftBraceToken
      case '{':
      {
        return new LeftBraceToken();
      }
      // If the character is }, create a new RightBraceToken
      case '}':
      {
        return new RightBraceToken();
      }
      // If the character is ',', create a new CommaToken
      case ',':
      {
        return new CommaToken();
      }
      // If the character is ;,  create a new SemicolonToken
      case ';':
      {
        return new SemicolonToken();
      }
      // If an unexpected character is visited, create a new ErrorToken
      default:
      {
        return new ErrorToken();
      }
    }
  }


  /**
   * Create the reserved token tree that has the following structure:
   *
   *                       o u t p u t
   *                     /            \
   *                input              void
   *               /     \            /    \
   *             if       int   return      while
   *            /
   *        else
   *
   * @return A {@link TokenTree} object that contains {@link ReservedToken}
   *         objects
   */
  private TokenTree<ReservedToken> createTokenTree()
  {
    TokenTree<ReservedToken> tokenTree = new TokenTree<>(new OutputToken());

    tokenTree.add(new InputToken());
    tokenTree.add(new IfToken());
    tokenTree.add(new ElseToken());
    tokenTree.add(new IntToken());
    tokenTree.add(new VoidToken());
    tokenTree.add(new ReturnToken());
    tokenTree.add(new WhileToken());

    return tokenTree;
  }
}
