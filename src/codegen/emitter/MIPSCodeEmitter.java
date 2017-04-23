package codegen.emitter;

import globals.pair.IdentifierPair;

import java.io.*;
import java.util.ArrayList;

/**
 * Concrete class for emitting MIPS assembly code
 */
public final class MIPSCodeEmitter
{
  /**
   * The print stream used to write to the file
   */
  private PrintStream writer;

  /**
   * The full constructor for the MIPSCodeEmitter
   *
   * @param filename The extension-less file name
   * @throws IOException Thrown if there is trouble with the file access
   */
  public MIPSCodeEmitter(final String filename) throws IOException
  {
    // Create and open the assembly file
    writer = new PrintStream(new FileOutputStream(filename + ".asm"));
  }

  /**
   * Close the file managed by the code emitter
   */
  public void close()
  {
    writer.close();
  }

  /**
   * Emit the assembly code header, along with any global identifiers
   * @param ids The list of global identifiers
   */
  public void emitHeader(final ArrayList<IdentifierPair> ids)
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
    // Save some strings for the I/O functions
    writer.println("inputStr_ : .asciiz \"Enter int: \"");
    writer.println("outputStr_: .asciiz \"Result: \"");
    writer.println("newlineStr_: .asciiz \"\\n\"");
    writer.println(".text");
  }

  /**
   * Emit the code necessary to perform an array allocation
   *
   * @param size     The size of the array to allocate
   * @param register The register to store the address of the allocated array
   */
  public void emitArrayAlloc(final int size, final String register)
  {
    writer.printf("li $v0, 9  # Load System call\n");
    writer.printf("addi $a0, %d # Load size of the array to allocate\n", size * 4);
    writer.println("syscall # allocate an array");
    writer.printf("mov %s, $a0 # Save address\n", register);
  }

  /**
   * Emit a system call, given an ID, a register and a flag indicating if it's
   * an address.
   *
   * @param ID       The system call number
   * @param register The register containing data to output (if necessary)
   * @param isAddr   Is the register an address?
   */
  public void emitSyscall(final int ID, final String register, final boolean isAddr)
  {
    writer.printf("li $v0, %d  # Load System call\n", ID);
    if (register != null)
    {
      if (isAddr)
      {
        writer.printf("la $a0, %s\n", register);
      }
      else
      {
        writer.printf("move $a0, %s\n", register);
      }
    }
    writer.println("syscall");
  }

  /**
   * Print a new line
   */
  public void emitSeparator()
  {
    writer.println("");
  }

  /**
   * Print a block label
   * @param labelName The label to emit (will be suffixed with __)
   */
  public void emitLabel(final String labelName)
  {
    writer.printf("%1$s__:            # Begin the %1$s__ block\n", labelName);
  }

  /**
   * Emit a function call
   * @param name The name of the function to be called (will be suffixed with __)
   */
  public void emitFunctionCall(final String name)
  {
    writer.printf("jal %1$s__        # Jump and link to %1$s\n", name);
  }

  /**
   * Emit a jump
   *
   * @param label The label to jump to (to be suffixed with __)
   */
  public void emitJump(final String label)
  {
    writer.printf("j %1$s__            # Unconditional Jump To %1$s__\n", label);
  }

  /**
   * Push a stack frame
   * @param size The number of bytes for the stack frame
   */
  public void emitStackPush(final int size)
  {
    writer.printf("addi $sp, $sp, -%d     # Push a stack frame\n", size);
  }

  /**
   * Save a register to the stack
   * @param register The register to save on the stack
   * @param offset   The offset from the stack base to push onto
   */
  public void emitStackSave(final String register, final int offset)
  {
    writer.printf("sw %s, %d($sp)\n", register, offset);
  }

  /**
   * Retrieve a register from the stack
   * @param register The register to restore from the stack
   * @param offset   The offset from the stack base to restore from
   */
  public void emitStackRetrieve(final String register, final int offset)
  {
    writer.printf("lw %s, %d($sp)\n", register, offset);
  }

  /**
   * Pop a stack frame
   * @param size The number of bytes for the stack frame
   */
  public void emitStackPop(final int size)
  {
    writer.printf("addi $sp, $sp, %d     # Pop a stack frame\n", size);
  }

  /**
   * Multiply a register by 4 (left shift twice)
   * @param offset      The offset register (where the number comes from)
   * @param destination The register to save the new offset
   */
  public void emitShift(final String offset, final String destination)
  {
    writer.printf("sll %s, %s, 2\n", destination, offset);
  }

  /**
   * Store a word to an address
   * @param address The address for storage
   * @param offset  The register offset for storage
   * @param from    The originating register
   */
  public void emitStoreWord(final String address,
                            final String offset,
                            final String from)
  {
    writer.printf("sw %s, %s(%s)\n", from, offset, address);
  }

  /**
   * Emit an R-Type (register) operation
   * @param opcode The operation code
   * @param r1     RS (register source)
   * @param r2     RT (register target)
   * @param dest   RD (register destination)
   */
  public void emitRType(final String opcode,
                        final String r1,
                        final String r2,
                        final String dest)
  {
    writer.printf("%s %s, %s, %s\n", opcode, dest, r1, r2);
  }

  /**
   * Perform a data load (from a String offset)
   * @param to     The register that will contain the loaded data
   * @param from   The register that contains the address to perform the data load
   * @param offset The register that contains the offset (bytes)
   */
  public void emitLoadWord(final String to, final String from, final String offset)
  {
    writer.printf("lw %s, %s(%s)\n", to, offset, from);
  }

  /**
   * Perform a data load (from an integer offset)
   *
   * @param to     The register that will contain the loaded data
   * @param from   The register that contains the address to perform the data load
   * @param offset The address offset (bytes)
   */
  private void emitLoadWord(final String to, final String from, int offset)
  {
    writer.printf("lw %s, %d(%s)\n", to, offset, from);
  }

  /**
   * Perform a data save (data move)
   * @param to   The register to which the data should be saved
   * @param from The register that contains the data to be saved
   */
  public void emitDataSave(final String to, final String from)
  {
    writer.printf(
        "add %1$s, %2$s, $0 # Assign contents of %1$s to %2$s\n", to, from);
  }

  /**
   * Emit a branch statement, given a operation code (opcode), registers
   * for comparison, and the true/false branches.
   *
   * @param opcode      The operation code (comparison type) to use
   * @param r1          The lhs of the comparison
   * @param r2          The rhs of the comparison
   * @param trueBranch  The true branch (used if the comparison passes)
   * @param falseBranch The false branch (used if the comparison fails)
   */
  public void emitBranch(final String opcode,
                         final String r1,
                         final String r2,
                         final String trueBranch,
                         final String falseBranch)
  {
    // Emit the branch comparison statement
    writer.printf("%s %s, %s, %s__\n", opcode, r1, r2, trueBranch);

    // Insert a no-op into the branch-delay slot
    writer.printf("nop\n");
    writer.printf("j %s__\n", falseBranch);

    emitLabel(trueBranch);
  }

  /**
   * Emit a No-Operation statement
   */
  public void emitNoop()
  {
    writer.println("nop");
  }

  /**
   * Emit the code necessary to produce the input function
   */
  public void emitInputFunction()
  {
    emitLabel("input");
    emitSyscall(4, "inputStr_", true);
    emitSyscall(5, null, false);
    emitFunctionExit();
  }

  /**
   * Emit the code necessary to produce the output function
   */
  public void emitOutputFunction()
  {
    emitLabel("output");
    emitSyscall(4, "outputStr_", true);
    emitLoadWord("$a0", "$sp", 0);
    emitSyscall(1, null, false);
    emitSyscall(4, "newlineStr_", true);
    emitFunctionExit();
  }

  /**
   * Emit a function exit statement
   */
  public void emitFunctionExit()
  {
    writer.println("jr $ra");
  }
}