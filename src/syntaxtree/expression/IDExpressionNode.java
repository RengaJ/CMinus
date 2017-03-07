package syntaxtree.expression;

import syntaxtree.ASTNodeType;
import tokens.TokenType;

/**
 * Concrete class representing an identifier expression node
 * in the abstract syntax tree
 */
public final class IDExpressionNode extends ExpressionNode
{
    /**
     * Full constructor for the IDExpressionNode
     */
    public IDExpressionNode()
    {
        super(TokenType.VARIABLE_IDENTIFIER);
    }

    /**
     * Get the enumerated type of the current Abstract Syntax Tree node
     *
     * @return EXPRESSION_IDENTIFIER
     */
    @Override
    public ASTNodeType getNodeType()
    {
        return ASTNodeType.EXPRESSION_IDENTIFIER;
    }
}
