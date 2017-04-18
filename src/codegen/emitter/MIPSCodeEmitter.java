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
            String.format("%s: .word %d", dataPair.name, 4 * dataPair.size));
      }
    }
    writer.println("inputStr_ : .asciiz \"Enter int: \"");
    writer.println("outputStr_: .asciiz \"Result: \"");
    writer.println(".text");
  }

  public void emitSyscall(final int ID, final String register, final boolean isAddr)
  {
    writer.printf("li       $v0, %d  # Load System call\n", ID);
    if (register != null)
    {
      if (isAddr)
      {
        writer.printf("la    $a0, %s\n", register);
      }
      else
      {
        writer.printf("mov $a0, %s\n", register);
      }
    }
    writer.println("syscall");
  }

  public void emitSeparator()
  {
    writer.println("");
  }

  public void emitLabel(final String labelName)
  {
    writer.printf("%1$s__:            # Begin the %1$s__ block\n", labelName);
  }

  public void emitFunctionCall(final String name)
  {
    writer.printf("jal %1$s__        # Jump and link to %1$s\n", name);
  }

  public void emitJump(final String label)
  {
    writer.printf("j %1$s__            # Unconditional Jump To %1$s__\n", label);
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

  public void emitRType(final String opcode,
                        final String r1,
                        final String r2,
                        final String dest)
  {
    writer.printf("%s %s, %s, %s\n", opcode, dest, r1, r2);
  }

  public void emitLoadWord(final String to, final String from, int offset)
  {
    writer.printf("lw %s, %d(%s)\n", to, offset, from);
  }

  public void emitDataSave(final String to, final String from)
  {
    writer.printf("add  %1$s, %2$s, $0    # Assign contents of %1$s to %2$s\n", to, from);
  }

  public void emitBranch(final String opcode,
                         final String r1,
                         final String r2,
                         final String trueBranch,
                         final String falseBranch)
  {
    writer.printf("%s %s, %s, %s__\n", opcode, r1, r2, trueBranch);
    // Insert a no-op into the branch-delay slot
    writer.printf("nop\n");
    writer.printf("j %s__\n", falseBranch);
    emitLabel(trueBranch);
  }

  public void emitNoop()
  {
    writer.println("nop");
  }

  public void emitInputFunction()
  {
    emitLabel("input");
    emitSyscall(4, "inputStr_", true);
    emitSyscall(5, null, false);
    emitFunctionExit();
  }

  public void emitOutputFunction()
  {
    emitLabel("output");
    emitSyscall(4, "outputStr_", true);
    emitLoadWord("$a0", "$sp", 0);
    emitSyscall(1, null, false);
    emitFunctionExit();
  }

  public void emitFunctionExit()
  {
    writer.println("jr $ra");
  }
}