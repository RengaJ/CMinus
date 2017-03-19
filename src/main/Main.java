package main;

import analyzer.SemanticAnalyzer;
import analyzer.symbol.SymbolItem;
import analyzer.symbol.table.SymbolTable;
import globals.CompilerFlags;
import parser.Parser;
import scanner.Scanner;
import syntaxtree.AbstractSyntaxTreeNode;
import tokens.Token;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;

/**
 * The main class that drives the compiler execution
 */
public final class Main
{
  // Flag name constants (used for parsing through optional arguments)
  private static final String  NO_PARSE_FLAG       = "-NO_PARSE";
  private static final String  NO_ANALYZER_FLAG    = "-NO_ANALYZE";
  private static final String  NO_CODE_GEN_FLAG    = "-NO_CODE";
  private static final String  ECHO_SOURCE_FLAG    = "-EchoSource";
  private static final String  TRACE_SCANNER_FLAG  = "-TraceScan";
  private static final String  TRACE_PARSER_FLAG   = "-TraceParse";
  private static final String  TRACE_ANALYZER_FLAG = "-TraceAnalysis";
  private static final String  TRACE_CODE_GEN_FLAG = "-TraceCode";

  /**
   * The main method for the compiler's execution
   * @param args The program arguments
   */
  public static void main(String[] args)
  {
    // Check the program arguments to ensure that the compiler will
    // operate correctly
    if (args.length == 0)
    {
      System.err.println("Incorrect program usage. Valid usage is as follows:");
      showProgramUsage();
      System.exit(-1);
    }
    // Iterate over the arguments to identify what flags to turn on
    // prior to execution
    for (int i = 0; i < args.length - 1; i++)
    {
      switch (args[i])
      {
        // If the -NO_PARSE flag is found, set the
        // NO_PARSE flag. Note that this will fall
        // through to the other cases, turning off
        // the subsequent portions of the compiler
        case NO_PARSE_FLAG:
        {
          CompilerFlags.NoParser = true;
        }
        // If the -NO_ANALYZER flag is found, set
        // the NO_ANALYZE flag. Note that this will
        // fall through to the other cases, turning
        // off the subsequent portions of the compiler
        case NO_ANALYZER_FLAG:
        {
          CompilerFlags.NoAnalyzer = true;
        }
        // If the -NO_CODE flag is found, set the
        // the NO_CODE flag.
        case NO_CODE_GEN_FLAG:
        {
          CompilerFlags.NoGenerator = true;
          break;
        }
        // If source echoing is desired, set the flag
        case ECHO_SOURCE_FLAG:
        {
          CompilerFlags.EchoSource = true;
          break;
        }
        // If trace notifications from the scanner are
        // desired, set the flag
        case TRACE_SCANNER_FLAG:
        {
          CompilerFlags.TraceScanner = true;
          break;
        }
        // If trace notifications from the parser are
        // desired, set the flag
        case TRACE_PARSER_FLAG:
        {
          CompilerFlags.TraceParser = true;
          break;
        }
        // If trace notifications from the analyzer are
        // desired, set the flag
        case TRACE_ANALYZER_FLAG:
        {
          CompilerFlags.TraceAnalyzer = true;
          break;
        }
        // If trace notifications from the code generator
        // are desired, set the flag
        case TRACE_CODE_GEN_FLAG:
        {
          CompilerFlags.TraceGenerator = true;
          break;
        }
        // If an unknown flag was detected, let the user
        // know that there was was a problem with the flag,
        // show the user what the program accepts, and
        // terminate the program
        default:
        {
          System.err.println(
              String.format(
                  "Unknown flag %s detected. Valid usage is as follows:",
                  args[i]));
          showProgramUsage();
          System.exit(-1);
          break;
        }
      }
    }

    // Establish the file to be used as the final argument
    File sourceFile = null;
    String filename = args[args.length - 1];
    String cMinusFilename = String.format("%s.cm", filename);
    try
    {
      // Attempt to create a File object
      sourceFile = new File(cMinusFilename);

      // If the file does not exist, throw an
      // exception
      if (!sourceFile.exists())
      {
        throw new FileNotFoundException("");
      }
    }
    catch (FileNotFoundException fnfe)
    {
      System.err.println(
          String.format(
              "File %s does not exist. Please verify file " +
                  "location and try again.",
              filename));
      System.exit(-1);
    }

    // Print out the current flags that will be used for the
    // compilation execution (for the user's benefit)
    showCurrentFlags();

    try
    {
      Scanner scanner = new Scanner();

      ArrayDeque<Token> tokens = scanner.scanForTokens(sourceFile);

      if (!CompilerFlags.NoParser)
      {
        Parser parser = new Parser();

        AbstractSyntaxTreeNode tree = parser.parse(tokens);
        if (parser.syntaxErrorOccurred())
        {
          System.err.println("Errors occurred during parsing.");
          System.err.println("Terminating compilation.");

          System.exit(-1);
        }

        if (!CompilerFlags.NoAnalyzer)
        {
          SemanticAnalyzer analyzer = new SemanticAnalyzer();

          SymbolTable table = analyzer.analyze(tree);
          
          System.out.println("Analyzer Complete");

          if (analyzer.didErrorOccur())
          {
            System.err.println("Errors occurred during semantic analysis.");
            System.err.println("Terminating compilation.");

            System.exit(-1);
          }
        }
      }

      System.out.println("Compilation Completed.");
    }
    catch (IOException ioe)
    {
      System.err.println("A fatal I/O error occurred. Please see" +
          " stack trace for more details:");
      ioe.printStackTrace(System.err);
    }
  }

  /**
   * Shows the program's usage to the user in case of incorrect usage
   */
  private static void showProgramUsage()
  {
    System.err.println("Main [COMPILER_OPTIONS] [TRACE_OPTION] <filename>");
    System.err.println("-------------------------");
    System.err.println("Valid COMPILER_OPTIONS (choose one) (optional):");
    System.err.println("-NO_PARSE     : Turn off parser operations " +
        "(run compiler in scan-only mode)");
    System.err.println("-NO_ANALYZE   : Turn off semantic analysis operations" +
        " (run compiler in parser-only mode)");
    System.err.println("-NO_CODE      : Turn off code generation operations" +
        " (run compiler in analysis-only mode)");
    System.err.println("[Note that -NO_PARSE will take precedence over " +
        "-NO_ANALYZE mode, so only one flag is needed]");
    System.err.println("");
    System.err.println("Trace Flags (choose multiple) (optional):");
    System.err.println("-EchoSource   : Display the source as it's being scanned");
    System.err.println("-TraceScan    : Turn on scanner trace output");
    System.err.println("-TraceParse   : Turn on parser trace output");
    System.err.println("-TraceAnalyze : Turn on semantic analyzer trace output");
    System.err.println("-TraceCode    : Turn on code generator trace output");
    System.err.println("");
    System.err.println("              : The name of the file to compile. " +
        "This file name should not have the");
    System.err.println("<filename>    : .cm extension when provided, as " +
        "the compiler will append it");
    System.err.println("              : automatically. THIS FIELD IS REQUIRED");
  }

  /**
   * Show the current flags used by the compiler upon the
   * start of execution
   */
  private static void showCurrentFlags()
  {
    System.out.println("Compiler will be run with the following flags set:");
    System.out.println("--------------------------------------------------");
    System.out.println(String.format("NO_PARSE        : %s",
        (CompilerFlags.NoParser ? "true" : "false")));
    System.out.println(String.format("NO_ANALYZE      : %s",
        (CompilerFlags.NoAnalyzer ? "true" : "false")));
    System.out.println(String.format("NO_CODE         : %s",
        (CompilerFlags.NoGenerator ? "true" : "false")));
    System.out.println(String.format("EchoSource      : %s",
        (CompilerFlags.EchoSource ? "true" : "false")));
    System.out.println(String.format("TraceScan       : %s",
        (CompilerFlags.TraceScanner ? "true" : "false")));
    System.out.println(String.format("TraceParse      : %s",
        (CompilerFlags.TraceParser ? "true" : "false")));
    System.out.println(String.format("TraceAnalysis   : %s",
        (CompilerFlags.TraceAnalyzer ? "true" : "false")));
    System.out.println(String.format("TraceCode       : %s",
        (CompilerFlags.TraceGenerator ? "true" : "false")));
    System.out.println("");
  }
}