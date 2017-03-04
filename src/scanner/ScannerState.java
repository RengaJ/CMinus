package scanner;

/**
 * Package-Private enumeration that identifies the
 * various states that the Scanner can reside.
 */
enum ScannerState
{
  /**
   * Starting State
   */
  START_STATE,
  /**
   * State when receiving a < from START
   */
  IN_LESS_THAN,
  /**
   * State when receiving a > from START
   */
  IN_GREATER_THAN,
  /**
   * State when receiving a = from START
   */
  IN_EQUAL,
  /**
   * State when receiving a ! from START
   */
  IN_NOT_EQUAL,
  /**
   * State when receiving a letter from START
   */
  IN_IDENTIFIER,
  /**
   * State when receiving a digit from START
   */
  IN_NUMBER,
  /**
   * State when receiving a / from START
   */
  IN_DIVIDE,
  /**
   * State when residing in a comment
   */
  IN_COMMENT,
  /**
   * State when attempting to exit a comment
   */
  EXIT_COMMENT,
  /**
   * The final state for token processing
   */
  TOKEN_DONE,
  /**
   * The final state for the scanner
   */
  SCANNER_DONE
}
