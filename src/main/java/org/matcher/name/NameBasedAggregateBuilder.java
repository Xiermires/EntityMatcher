package org.matcher.name;

import org.matcher.builder.AggregateBuilder;
import org.matcher.expression.FunctionExpression;

public class NameBasedAggregateBuilder<T> extends AggregateBuilder<T, NameBasedAggregateBuilder<T>> {

    public NameBasedAggregateBuilder(FunctionExpression<T> expression) {
	super(expression);
	setData(this);
    }
    
    @Override
    protected NameBasedAggregateBuilder<T> getThis() {
	return this;
    }
}
