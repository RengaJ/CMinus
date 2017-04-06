package codegen;

import analyzer.symbol.table.SymbolTable;
import codegen.emitter.MIPSCodeEmitter;
import syntaxtree.AbstractSyntaxTreeNode;

import java.io.IOException;

/**
 * Main code generation class
 */
public final class CodeGenerator
{
  private MIPSCodeEmitter emitter;

  public CodeGenerator()
  {
    emitter = null;
  }

  public void generate(final AbstractSyntaxTreeNode root,
                       final SymbolTable symbolTable,
                       final String filename) throws IOException
  {
    if (emitter != null)
    {
      emitter.close();
    }

    emitter = new MIPSCodeEmitter(filename);



    emitter.emitHeader(null);
    emitter.emitSystemExit();
  }
}
