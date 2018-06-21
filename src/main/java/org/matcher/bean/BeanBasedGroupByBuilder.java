package org.matcher.bean;

import org.matcher.builder.GroupByBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.TypedExpression;

public class BeanBasedGroupByBuilder<T> extends GroupByBuilder<T> {

    public BeanBasedGroupByBuilder(Class<T> leadingReferent, String leadingProperty) {
	super(leadingReferent, leadingProperty);
    }

    /**
     * A having expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code having(count("foo"), gt(2)} translates as {@code HAVING COUNT(?.foo) > 2}.
     */
    public BeanBasedHavingBuilder<?> having(FunctionExpression<?> function, BeanBasedWhereBuilder qualifier) {
	final TypedExpression<?> expression = BeanBasedHavingBuilder.createHavingExpression(function, qualifier);
	final BeanBasedHavingBuilder<?> builder = new BeanBasedHavingBuilder<>(expression.getType(), null);
	builder.getExpressions().add(expression);
	builder.setPreviousClause(this);
	return builder;
    }
}
