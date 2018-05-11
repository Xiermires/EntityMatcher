package org.matcher.builder;

import java.util.Set;

import org.matcher.expression.SelectExpression;
import org.matcher.parameter.ParameterBinding;

public abstract class SelectBuilder<T, E extends SelectBuilder<T, E>> extends ExpressionBuilder<E> {

    private final Class<T> referent;
    private final String property;

    public SelectBuilder(SelectExpression<T> expression) {
	super();
	expressions.add(expression);
	referent = expression.getReferent();
	property = expression.getProperty();
    }

    public Class<T> getReferent() {
	return referent;
    }

    public String getProperty() {
	return property;
    }

    @Override
    public String build(Set<Class<?>> seenReferents, ParameterBinding bindings) {
	initializeBindings();

	final StringBuilder selectClause = new StringBuilder();
	parseExpressions(selectClause, null, seenReferents, bindings);
	return new StringBuilder("SELECT ").append(selectClause).toString();
    }

    @Override
    protected String getResolveFromSeparator() {
	return "";
    }

    @Override
    protected String getResolveSeparator() {
	return ", ";
    }
}
