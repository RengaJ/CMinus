package syntaxtree.statement;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing a variable declaration node in the
 * abstract syntax tree (used to prime the symbol table)
 */
public final class VarDeclarationStatementNode extends StatementNode
{
    /**
     * Full constructor for the VarDeclarationStatementNode
     */
    public VarDeclarationStatementNode()
    {
        super(TokenType.VARIABLE_IDENTIFIER);
    }

    /**
     * Get the enumerated type of the current Abstract Syntax Tree node
     *
     * @return STATEMENT_VAR_DECLARATION
     */
    @Override
    public ASTNodeType getNodeType()
    {
        return ASTNodeType.STATEMENT_VAR_DECLARATION;
    }
}
