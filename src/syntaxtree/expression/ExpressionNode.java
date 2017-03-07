package syntaxtree.expression;

import syntaxtree.AbstractSyntaxTreeNode;
import tokens.TokenType;

/**
 * Abstract decorator class used to identify expression nodes
 */
public abstract class ExpressionNode extends AbstractSyntaxTreeNode
{
    /**
     * Full constructor for the abstract ExpressionNode
     *
     * @param tokenType The TokenType used to be contained within
     *                  the node's attribute
     */
    public ExpressionNode(final TokenType tokenType)
    {
        super();

        setAttributeTokenType(tokenType);
    }
}
