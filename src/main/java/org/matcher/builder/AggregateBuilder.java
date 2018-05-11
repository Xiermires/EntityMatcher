package org.matcher.builder;

import java.util.Set;

import org.matcher.expression.FunctionExpression;
import org.matcher.parameter.ParameterBinding;

public abstract class AggregateBuilder<T, E extends AggregateBuilder<T, E>> extends SelectBuilder<T, E> {

    public AggregateBuilder(FunctionExpression<T> expression) {
	super(expression);
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder aggregateClause = new StringBuilder();
	parseExpressions(aggregateClause, null, seenReferents, bindings);
	return new StringBuilder().append(aggregateClause).toString();
    }

    @Override
    protected String getResolveSeparator() {
	return " ";
    }
}
