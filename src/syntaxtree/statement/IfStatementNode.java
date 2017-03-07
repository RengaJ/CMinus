package syntaxtree.statement;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing an if-statement node in the
 * abstract syntax tree
 */
public final class IfStatementNode extends StatementNode
{
    /**
     * Full constructor for the IfStatementNode
     */
    public IfStatementNode()
    {
        super(TokenType.RESERVED_IF);
    }

    /**
     * Get the enumerated type of the current Abstract Syntax Tree node
     *
     * @return STATEMENT_IF
     */
    @Override
    public ASTNodeType getNodeType()
    {
        return ASTNodeType.STATEMENT_IF;
    }
}
