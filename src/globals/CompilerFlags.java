package globals;

/**
 * Class that contains global compiler flags
 */
public final class CompilerFlags
{
  /** Private constructor. Should not be used */
  private CompilerFlags() {}

  /** Flag indicating if source should be printed during scanning */
  public static boolean EchoSource     = false;
  /** Flag indicating if trace lines should be printed during scanning */
  public static boolean TraceScanner   = false;
  /** Flag indicating if trace lines should be printed during parsing */
  public static boolean TraceParser    = false;
  /** Flag indicating if trace lines should be printed during analysis */
  public static boolean TraceAnalyzer  = false;
  /** Flag indicating if trace lines should be printed during code generation */
  public static boolean TraceGenerator = false;
  /** Flag indicating if only scanner should be run */
  public static boolean NoParser       = false;
  /** Flag indicating if only scanner and parser should be run */
  public static boolean NoAnalyzer     = false;
  /** Flag indicating if code generator should not be run */
  public static boolean NoGenerator    = false;
}
