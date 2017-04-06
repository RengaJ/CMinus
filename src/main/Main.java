package main;

import analyzer.SemanticAnalyzer;
import analyzer.symbol.table.SymbolTable;
import codegen.CodeGenerator;
import globals.CompilerFlags;
import globals.ConsoleColor;
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
      ConsoleColor.PrintRed("Incorrect program usage. Valid usage is as follows:");
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
          ConsoleColor.PrintRed(
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
      ConsoleColor.PrintRed(
          String.format(
              "File %s does not exist. Please verify file " +
                  "location and try again.",
              filename));
      System.exit(-1);
    }

    // Print out the current flags that will be used for the
    // compilation execution (for the user's benefit)
    showCurrentFlags();

    ArrayDeque<Token>      tokens;
    AbstractSyntaxTreeNode tree        = null;
    SymbolTable            symbolTable = null;
    try
    {
      Scanner scanner = new Scanner();

      tokens = scanner.scanForTokens(sourceFile);

      if (!CompilerFlags.NoParser)
      {
        Parser parser = new Parser();

        tree = parser.parse(tokens);
        if (parser.syntaxErrorOccurred())
        {
          ConsoleColor.PrintRed("Errors occurred during parsing.");
          ConsoleColor.PrintRed("Terminating compilation.");

          System.exit(-1);
        }
      }
      if (!CompilerFlags.NoAnalyzer)
      {
        SemanticAnalyzer analyzer = new SemanticAnalyzer();

        symbolTable = analyzer.analyze(tree);

        if (analyzer.errorOccurred())
        {
          ConsoleColor.PrintRed("Errors occurred during semantic analysis.");
          ConsoleColor.PrintRed("Terminating compilation.");

          System.exit(-1);
        }

        if (CompilerFlags.TraceAnalyzer)
        {
          System.out.println("\nProduced Symbol Tables:\n");
          symbolTable.printTable("");
        }

        System.out.println("Analyzer Complete");
      }

      if (!CompilerFlags.NoGenerator)
      {
        CodeGenerator codeGenerator = new CodeGenerator();

        codeGenerator.generate(tree, symbolTable, filename);
      }
      System.out.println("Compilation Completed.");
    }
    catch (IOException ioe)
    {
      ConsoleColor.PrintRed("A fatal I/O error occurred. Please see" +
          " stack trace for more details:");
      ioe.printStackTrace(System.err);
    }
  }

  /**
   * Shows the program's usage to the user in case of incorrect usage
   */
  private static void showProgramUsage()
  {
    ConsoleColor.PrintRed("Main [COMPILER_OPTIONS] [TRACE_OPTION] <filename>");
    ConsoleColor.PrintRed("-------------------------");
    ConsoleColor.PrintRed("Valid COMPILER_OPTIONS (choose one) (optional):");
    ConsoleColor.PrintRed("-NO_PARSE     : Turn off parser operations " +
        "(run compiler in scan-only mode)");
    ConsoleColor.PrintRed("-NO_ANALYZE   : Turn off semantic analysis operations" +
        " (run compiler in parser-only mode)");
    ConsoleColor.PrintRed("-NO_CODE      : Turn off code generation operations" +
        " (run compiler in analysis-only mode)");
    ConsoleColor.PrintRed("[Note that -NO_PARSE will take precedence over " +
        "-NO_ANALYZE mode, so only one flag is needed]");
    ConsoleColor.PrintRed("");
    ConsoleColor.PrintRed("Trace Flags (choose multiple) (optional):");
    ConsoleColor.PrintRed("-EchoSource   : Display the source as it's being scanned");
    ConsoleColor.PrintRed("-TraceScan    : Turn on scanner trace output");
    ConsoleColor.PrintRed("-TraceParse   : Turn on parser trace output");
    ConsoleColor.PrintRed("-TraceAnalyze : Turn on semantic analyzer trace output");
    ConsoleColor.PrintRed("-TraceCode    : Turn on code generator trace output");
    ConsoleColor.PrintRed("");
    ConsoleColor.PrintRed("              : The name of the file to compile. " +
        "This file name should not have the");
    ConsoleColor.PrintRed("<filename>    : .cm extension when provided, as " +
        "the compiler will append it");
    ConsoleColor.PrintRed("              : automatically. THIS FIELD IS REQUIRED");
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