package codegen.emitter;

import globals.pair.IdentifierPair;
import syntaxtree.ASTNodeType;
import tokens.TokenType;

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

  public void emitLabel(final String labelName)
  {
    writer.printf("%1$s:            # Begin the %1$s block\n", labelName);
  }

  public void emitFunctionCall(final String name)
  {
    writer.printf("jal %1$s        # Jump and link to %1$s\n", name);
  }

  public void emitJump(final String label)
  {
    writer.printf("j %1$s            # Unconditional Jump To %1$s\n", label);
  }

  public void emitStackPush(final int size)
  {
    writer.printf("addi $sp, $sp, -%d     # Push a stack frame\n", size);
  }

  public void emitStackSave(final String register, final int offset)
  {
    writer.printf("sw %s, %d($sp)\n", register, offset);
  }

  public void emitStackRetrieve(final String register, final int offset)
  {
    writer.printf("lw %s, %d($sp)\n", register, offset);
  }

  public void emitStackPop(final int size)
  {
    writer.printf("addi $sp, $sp, %d     # Pop a stack frame\n", size);
  }

  public void emitReturn()
  {
  }

  public void emitRType(final String opcode,
                        final String r1,
                        final String r2,
                        final String dest)
  {
    writer.printf("%s %s, %s, %s\n", opcode, dest, r1, r2);
  }

  public void emitIType()
  {

  }

  public void emitJType()
  {

  }

  public void emitBranch(final String opcode,
                         final String r1,
                         final String r2,
                         final String trueBranch,
                         final String falseBranch)
  {
    writer.printf("%s %s, %s, %s\n", opcode, r1, r2, trueBranch);
    // Insert a no-op into the branch-delay slot
    writer.printf("nop\n");
    writer.printf("j %s\n", falseBranch);
    emitLabel(trueBranch);
  }
}
