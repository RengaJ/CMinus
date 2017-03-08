package syntaxtree.statement;

import syntaxtree.AbstractSyntaxTreeNode;
import tokens.TokenType;

/**
 * Abstract decorator class used to identify statement nodes
 */
public abstract class StatementNode extends AbstractSyntaxTreeNode
{
    /**
     * Full constructor for the abstract StatementNode
     *
     * @param tokenType The TokenType used to be contained within
     *                  the node's attribute
     */
    public StatementNode(final TokenType tokenType)
    {
        super();

        setTokenType(tokenType);
    }
}
