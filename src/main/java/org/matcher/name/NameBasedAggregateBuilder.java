package org.matcher.name;

import org.matcher.builder.AggregateBuilder;
import org.matcher.expression.FunctionExpression;

public class NameBasedAggregateBuilder<T> extends AggregateBuilder<T, NameBasedAggregateBuilder<T>> {

    public NameBasedAggregateBuilder(FunctionExpression<T> expression) {
	super(expression);
    }
    
    @Override
    protected NameBasedAggregateBuilder<T> getThis() {
	return this;
    }

//    public NameBasedAggregateBuilder<?> and(String first, String... others) {
//	addChild(selection(first, others));
//	return this;
//    }
//
//    public NameBasedAggregateBuilder<T> orderBy(FunctionExpression<?> first, FunctionExpression<?>... others) {
//	getExpressions().add(new OperatorExpression(ORDERBY));
//	getExpressions().addAll(NameBasedExpressions.orderBy(first, others).getExpressions());
//	return this;
//    }
}
