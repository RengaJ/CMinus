package globals;

/**
 * Simple utility class designed to improve logging errors as red text without
 * the race conditions found in System.err.println.
 */
public class ConsoleColor
{
  /**
   * Print the text in red.
   *
   * @param message The text to be printed in red
   */
  public static void PrintRed(final String message)
  {
    System.out.printf("\u001B[31m%s\u001B[0m\n",message);
  }
}
