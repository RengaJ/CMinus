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

  public void emitReturn()
  {
  }

  public void emitRType()
  {

  }

  public void emitIType()
  {

  }

  public void emitJType()
  {

  }

  public void emitBranch(final TokenType operatorType,
                         final String register1,
                         final String register2,
                         final String branchLabel)
  {
    String branchType;
    switch (operatorType)
    {
      case SPECIAL_EQUAL:
      {
        branchType = "bne";
        break;
      }
      case SPECIAL_NOT_EQUAL:
      {
        branchType = "beq";
        break;
      }
      case SPECIAL_GREATER_THAN:
      {
        branchType = "ble";
        break;
      }
      case SPECIAL_GTE:
      {
        branchType = "blt";
        break;
      }
      case SPECIAL_LESS_THAN:
      {
        branchType = "bge";
        break;
      }
      case SPECIAL_LTE:
      {
        branchType = "bgt";
        break;
      }
      default:
      {
        branchType = null;
        break;
      }
    }

    if (branchType == null)
    {
      return;
    }

    writer.printf("%s %s, %s, %s           # Branch\n",
        branchType, register1, register2, branchLabel);
  }
}
