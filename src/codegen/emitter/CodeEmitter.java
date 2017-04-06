package codegen.emitter;

/**
 * Abstract class used for emitting target source code
 */
public abstract class CodeEmitter
{
  public CodeEmitter()
  {
  }

  public abstract void emitJump();
  public abstract void emitReturn();
}
