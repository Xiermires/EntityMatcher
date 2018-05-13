package org.matcher.builder;

import static org.matcher.expression.Expressions.AND;
import static org.matcher.expression.Expressions.OR;

import java.util.Set;

import org.matcher.operator.Operator;
import org.matcher.parameter.ParameterBinding;

public class FromBuilder extends ExpressionBuilder<FromBuilder> {

    @Override
    protected FromBuilder getThis() {
	return this;
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder fromClause = new StringBuilder();
	parseExpressions(fromClause, bindings);

	return new StringBuilder("FROM ").append(fromClause).toString();
    }

    @Override
    protected String getResolveFromSeparator() {
	return "";
    }

    @Override
    protected Operator getOrOperator() {
	return OR;
    }

    @Override
    protected Operator getAndOperator() {
	return AND;
    }
}
