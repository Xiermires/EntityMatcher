package org.matcher.name;

import org.matcher.builder.GroupByBuilder;
import org.matcher.expression.FunctionExpression;
import org.matcher.expression.TypedExpression;

public class NameBasedGroupByBuilder<T> extends GroupByBuilder<T> {

    public NameBasedGroupByBuilder(Class<T> leadingReferent, String leadingProperty) {
	super(leadingReferent, leadingProperty);
    }

    /**
     * A having expression, where each property belongs to the leading query referent.
     * <p>
     * i.e. {@code having(count("foo"), gt(2)} translates as {@code HAVING COUNT(?.foo) > 2}.
     */
    public NameBasedHavingBuilder<T> having(FunctionExpression<T> function, NameBasedWhereBuilder qualifier) {
	final TypedExpression<T> expression = NameBasedHavingBuilder.createHavingExpression(function, qualifier);
	final NameBasedHavingBuilder<T> builder = new NameBasedHavingBuilder<>(expression.getType(), null);
	builder.getExpressions().add(expression);
	builder.setPreviousClause(this);
	return builder;
    }
}
