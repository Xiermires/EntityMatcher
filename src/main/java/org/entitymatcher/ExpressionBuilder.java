package org.entitymatcher;

import java.util.Deque;

import org.entitymatcher.NameBasedExpressionBuilder.Expression;

public interface ExpressionBuilder extends Node<ExpressionBuilder>
{
    
    Deque<Expression> getExpressions();
    
    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    ExpressionBuilder or(ExpressionBuilder other);

    /**
     * Composes an OR expression between this and the other.
     * <p>
     * The other builder is typified with {@code property} and also inherits any other types defined
     * in the this builder.
     */
    ExpressionBuilder or(String property, ExpressionBuilder other);
    
    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder inherits any types defined in the this builder.
     */
    ExpressionBuilder and(ExpressionBuilder other);

    /**
     * Composes an AND expression between this and the other.
     * <p>
     * The other builder is typified with {@code property} and also inherits any other types defined
     * in the this builder.
     */
    ExpressionBuilder and(String property, ExpressionBuilder other);

    /**
     * Builds the query and updates the parameter bindings.
     */
    String build(Class<?> clazz, ParameterBinding params);
}
