package codegen.emitter;

import globals.pair.IdentifierPair;

import java.io.*;
import java.util.ArrayList;

/**
 * Concrete class for emitting MIPS assembly code
 */
public final class MIPSCodeEmitter
{
  private PrintStream writer;

  public MIPSCodeEmitter(final String filename) throws IOException
  {
    writer = new PrintStream(new FileOutputStream(filename + ".asm"));
  }

  public void close() throws IOException
  {
    writer.close();
  }

  public void emitHeader(final ArrayList<IdentifierPair> ids) throws IOException
  {
    writer.println(".data");
    if (ids != null)
    {
      for (final IdentifierPair dataPair : ids)
      {
        writer.println(
            String.format("%s: .space %d", dataPair.name, 4 * dataPair.size));
      }
    }
    writer.println("inputStr_ : .asciiz \"Enter int: \"");
    writer.println("outputStr_: .asciiz \"Result: \"");
    writer.println(".text");
  }

  public void emitSystemExit()
  {
    writer.println("li      $v0, 10  # Load System Code For Exit (10) ");
    writer.println("syscall          # Call Operating System (EXIT)   ");
  }

  public void emitJump()
  {
  }

  public void emitReturn()
  {
  }
}
