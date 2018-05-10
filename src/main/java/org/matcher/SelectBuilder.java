package org.matcher;

import java.util.Set;

import org.matcher.expression.SelectExpression;

public class SelectBuilder<T> extends ExpressionBuilder<SelectBuilder<T>> {

    private final Class<T> referent;
    private final String property;

    public SelectBuilder(SelectExpression<T> expression) {
	super();
	setData(this);
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
    protected SelectBuilder<T> getThis() {
	return this;
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
